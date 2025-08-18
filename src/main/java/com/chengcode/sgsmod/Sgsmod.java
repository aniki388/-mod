package com.chengcode.sgsmod;

import com.chengcode.sgsmod.command.ModCommands;
import com.chengcode.sgsmod.effect.ModEffects;
import com.chengcode.sgsmod.entity.ModEntities;
import com.chengcode.sgsmod.entity.general.*;
import com.chengcode.sgsmod.manager.CardGameManager;
import com.chengcode.sgsmod.event.ModEventHandlers;
import com.chengcode.sgsmod.item.ModItemGroups;
import com.chengcode.sgsmod.item.ModItems;
import com.chengcode.sgsmod.logic.CardDrawScheduler;
import com.chengcode.sgsmod.network.NetWorking;
import com.chengcode.sgsmod.network.ServerReceiver;
import com.chengcode.sgsmod.sound.ModSoundEvents;
import com.chengcode.sgsmod.sound.SkillSoundManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.chengcode.sgsmod.skill.ModSkills.loadPlayerSkills;
import static com.chengcode.sgsmod.skill.ModSkills.savePlayerSkills;

public class Sgsmod implements ModInitializer {
	public static final String MOD_ID = "sgsmod";


	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// 实体名称常量（避免硬编码，提高可维护性）
	private static final String ENTITY_SUNCE = "sunce";
	private static final String ENTITY_LVBU = "lvbu";
	private static final String ENTITY_WEIYAN = "weiyan";
	private static final String ENTITY_DABAO = "dabao";
	private static final String ENTITY_CAOCHONG = "caochong";

	@Override
	public void onInitialize() {
		// 1. 原有基础初始化逻辑（物品、实体、音效等）
		ModItems.registerItems();
		ModItemGroups.registerItemGroups();
		ModEntities.registerEntities();
		ModSoundEvents.register();
		ServerReceiver.register();
		CardDrawScheduler.register();
		ModCommands.register();
		ModEventHandlers.registerEvents();
		SkillSoundManager.init();
		ModEffects.registerEffects();


		// 2. 注册服务器核心事件（Tick、玩家连接、服务器生命周期）
		registerServerCoreEvents();

		// 3. 注册服务器端网络接收器（处理客户端发来的数据包）
		registerServerNetworkHandlers();

		LOGGER.info("【三国杀Mod】初始化完成！");
	}

	/**
	 * 注册服务器核心事件（Tick、玩家连接、服务器停止等）
	 */
	private void registerServerCoreEvents() {
		// 服务器Tick事件：执行卡牌游戏核心逻辑
		ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);

		// 玩家加入服务器事件：加载技能、发送欢迎信息
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.player;
			// 加载玩家数据
			loadPlayerSkills(player);
			// 发送欢迎信息
			sendWelcomeMessage(player);
		});

		// 服务器停止事件：保存玩家技能、清理游戏数据
		ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);
	}

	/**
	 * 注册服务器端网络接收器（处理客户端请求，如实体生成）
	 */
	private void registerServerNetworkHandlers() {
		// 处理客户端"生成武将"请求（迁移自原客户端代码）
		ServerPlayNetworking.registerGlobalReceiver(NetWorking.SPAWN_PACKET_ID,
				(server, player, handler, buf, responseSender) -> {
					// 从数据包读取参数（网络线程安全读取）
					String entityName = buf.readString();
					double x = buf.readDouble();
					double y = buf.readDouble();
					double z = buf.readDouble();

					// 提交到服务器主线程执行（避免线程安全问题）
					server.execute(() -> {
						World world = player.getWorld();
						if (world.isClient) return; // 确保仅在服务器端执行

						ServerWorld serverWorld = (ServerWorld) world;
						// 根据实体名称生成对应武将
						switch (entityName.toLowerCase()) {
							case ENTITY_SUNCE:
								spawnSunCeEntity(serverWorld, player, x, y, z);
								break;
							case ENTITY_LVBU:
								spawnLvBuEntity(serverWorld, player, x, y, z);
								break;
							case ENTITY_WEIYAN:
								spawnWeiYanEntity(serverWorld, player, x, y, z);
								break;
							case ENTITY_DABAO:
								spawnDabaoEntity(serverWorld, player, x, y, z);
								break;
							case ENTITY_CAOCHONG:
								spawnCaochongEntity(serverWorld, player, x, y, z);
								break;
							default:
								LOGGER.warn("未知武将类型：{}，生成失败", entityName);
								player.sendMessage(Text.of("§c未知武将类型，生成失败！"), false);
								break;
						}
					});
				}
		);
	}

	/**
	 * 服务器Tick逻辑：执行卡牌游戏管理器的Tick（如濒死状态处理）
	 */
	private void onServerTick(MinecraftServer server) {
		if (server == null) return;
		// 获取主世界执行Tick逻辑（避免多世界重复执行）
		ServerWorld overworld = server.getOverworld();
		if (overworld != null) {
			CardGameManager.tick(overworld);
		}
	}

	/**
	 * 服务器停止逻辑：保存所有玩家技能数据、清理调度器
	 */
	private void onServerStopping(MinecraftServer server) {
		if (server == null) return;
		// 保存所有在线玩家的技能数据
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			savePlayerSkills(player);
		}
		// 清理卡牌调度器、游戏数据
		CardDrawScheduler.clear();
		CardGameManager.clearAll();
		LOGGER.info("【三国杀Mod】服务器停止，已保存所有玩家数据！");
	}

	/**
	 * 发送玩家加入欢迎信息（迁移自原客户端代码）
	 */
	private void sendWelcomeMessage(ServerPlayerEntity player) {
		if (player == null) return;
		// 分隔线
		player.sendMessage(Text.of("§6§m======================================"), false);
		// 欢迎标题
		player.sendMessage(Text.of("§c§l欢迎来到 §e§l三国杀 §a§l世界！"), false);
		// 副标题
		player.sendMessage(Text.of("§7在这里，你将体验智谋与武力的较量"), false);
		player.sendMessage(Text.of("§7与群雄逐鹿，成就一代枭雄！"), false);
		// 功能提示
		player.sendMessage(Text.of("§b提示：§f输入 §e/sgsmod§f 查看可用指令"), false);
		player.sendMessage(Text.of("§b提示：§f游戏模式开始后，你将自动获得手牌"), false);
		// 作者信息（带点击跳转）
		player.sendMessage(
				Text.literal("§8作者：§7Github链接  §8|  ")
						.append(
								Text.literal("§9§nhttps://github.com/aniki388")
										.styled(style -> style
												.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/aniki388"))
												.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("点击打开 GitHub 仓库")))
										)
						),
				false
		);
		// 分隔线
		player.sendMessage(Text.of("§6§m======================================"), false);
	}

	/**
	 * 生成孙策实体（服务器端）
	 */
	private void spawnSunCeEntity(ServerWorld serverWorld, ServerPlayerEntity player, double x, double y, double z) {
		SunCeEntity sunCe = ModEntities.SUNCE.create(serverWorld);
		if (sunCe == null) {
			player.sendMessage(Text.of("§c孙策生成失败！"), false);
			return;
		}
		// 设置实体位置（偏移10格避免与玩家重叠）
		sunCe.refreshPositionAndAngles(x + 10, y, z, player.getYaw(), player.getPitch());
		// 初始化实体（难度、生成原因等）
		sunCe.initialize(
				serverWorld,
				serverWorld.getLocalDifficulty(sunCe.getBlockPos()),
				SpawnReason.COMMAND,
				null,
				null
		);
		// 生成实体并发送提示
		serverWorld.spawnEntity(sunCe);
		sunCe.say("来者何人？");
		player.sendMessage(Text.of("§a孙策已生成！"), false);
	}

	/**
	 * 生成吕布实体（服务器端）
	 */
	private void spawnLvBuEntity(ServerWorld serverWorld, ServerPlayerEntity player, double x, double y, double z) {
		LvBuEntity lvBu = ModEntities.LUBU.create(serverWorld);
		if (lvBu == null) {
			player.sendMessage(Text.of("§c吕布生成失败！"), false);
			return;
		}
		lvBu.refreshPositionAndAngles(x + 10, y, z, player.getYaw(), player.getPitch());
		lvBu.initialize(
				serverWorld,
				serverWorld.getLocalDifficulty(lvBu.getBlockPos()),
				SpawnReason.COMMAND,
				null,
				null
		);
		// 吕布生成后默认攻击玩家
		lvBu.setTarget(player);
		lvBu.say("此处有吾镇守，尔辈有胆闯之？");
		serverWorld.spawnEntityAndPassengers(lvBu);
		player.sendMessage(Text.of("§a吕布已生成！"), false);
	}

	/**
	 * 生成魏延实体（服务器端）
	 */
	private void spawnWeiYanEntity(ServerWorld serverWorld, ServerPlayerEntity player, double x, double y, double z) {
		WeiYanEntity weiYan = ModEntities.WEIYAN.create(serverWorld);
		if (weiYan == null) {
			player.sendMessage(Text.of("§c魏延生成失败！"), false);
			return;
		}
		weiYan.refreshPositionAndAngles(x + 10, y, z, player.getYaw(), player.getPitch());
		weiYan.initialize(
				serverWorld,
				serverWorld.getLocalDifficulty(weiYan.getBlockPos()),
				SpawnReason.COMMAND,
				null,
				null
		);
		weiYan.setTarget(player);
		weiYan.say("若曹操举天下而来，请为大王拒之；偏将十万之众至，请为大王吞之。");
		serverWorld.spawnEntityAndPassengers(weiYan);
		player.sendMessage(Text.of("§a魏延已生成！"), false);
	}

	private void spawnDabaoEntity(ServerWorld serverWorld, ServerPlayerEntity player, double x, double y, double z) {
		JieXushengEntity jieXusheng = ModEntities.JIE_XUSHENG.create(serverWorld);
		if (jieXusheng == null) {
			player.sendMessage(Text.of("§c大宝生成失败！"), false);
			return;
		}
		jieXusheng.refreshPositionAndAngles(x + 10, y, z, player.getYaw(), player.getPitch());
		jieXusheng.initialize(
				serverWorld,
				serverWorld.getLocalDifficulty(jieXusheng.getBlockPos()),
				SpawnReason.COMMAND,
				null,
				null
		);
		jieXusheng.setTarget(player);
		jieXusheng.say("若敢来犯，必叫你大败而归！");
		serverWorld.spawnEntityAndPassengers(jieXusheng);
		player.sendMessage(Text.of("§a大宝已生成！"), false);
	}

	private void spawnCaochongEntity(ServerWorld serverWorld, ServerPlayerEntity player, double x, double y, double z) {
		CaochongEntity caochong = ModEntities.CAOCHONG.create(serverWorld);
		if (caochong == null) {
			player.sendMessage(Text.of("§c曹冲生成失败！"), false);
			return;
		}
		caochong.refreshPositionAndAngles(x + 10, y, z, player.getYaw(), player.getPitch());
		caochong.initialize(
				serverWorld,
				serverWorld.getLocalDifficulty(caochong.getBlockPos()),
				SpawnReason.COMMAND,
				null,
				null
		);

		caochong.say("大象，大象，你过来呀~");
		ElephantEntity elephant = ModEntities.ELEPHANT.create(serverWorld);
		if (elephant == null) {
			player.sendMessage(Text.of("§c大象生成失败！"), false);
			return;
		}
		elephant.refreshPositionAndAngles(x + 10, y, z, player.getYaw(), player.getPitch());
		elephant.initialize(
				serverWorld,
				serverWorld.getLocalDifficulty(elephant.getBlockPos()),
				SpawnReason.COMMAND,
				null,
				null
		);
		serverWorld.spawnEntityAndPassengers(elephant);
		serverWorld.spawnEntityAndPassengers(caochong);
		caochong.startRiding( elephant);
		caochong.setTarget(player);
		elephant.setTarget(player);
		player.sendMessage(Text.of("§a曹冲已生成！"), false);
	}
}