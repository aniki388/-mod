package com.chengcode.sgsmod;

import com.chengcode.sgsmod.command.ModCommands;
import com.chengcode.sgsmod.effect.ModEffects;
import com.chengcode.sgsmod.entity.ModEntities;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import com.chengcode.sgsmod.event.ModEventHandlers;
import com.chengcode.sgsmod.item.ModItemGroups;
import com.chengcode.sgsmod.item.ModItems;
import com.chengcode.sgsmod.logic.CardDrawScheduler;
import com.chengcode.sgsmod.network.ServerReceiver;
import com.chengcode.sgsmod.skill.ModSkills;
import com.chengcode.sgsmod.sound.ModSoundEvents;
import com.chengcode.sgsmod.sound.SkillSoundManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.fabricmc.fabric.api.event.server.ServerStartCallback;
import net.fabricmc.fabric.api.event.server.ServerStopCallback;
import net.fabricmc.fabric.api.event.world.WorldTickCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.chengcode.sgsmod.skill.ModSkills.loadPlayerSkills;
import static com.chengcode.sgsmod.skill.ModSkills.savePlayerSkills;

public class Sgsmod implements ModInitializer {
	public static final String MOD_ID = "sgsmod";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);




	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		ModItems.registerItems();
		ModItemGroups.registerItemGroups();
		ModEntities.register();
		ModSoundEvents.register();
		ServerReceiver.register();
		CardDrawScheduler.register();
		ModCommands.register();
		ModEventHandlers.registerEvents();
		SkillSoundManager.init();
		ModEffects.registerEffects();

		LOGGER.info("Hello Fabric world!");
	}
}