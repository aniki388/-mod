    package com.chengcode.sgsmod.gui;

    import com.chengcode.sgsmod.entity.GeneralEntity;
    import com.chengcode.sgsmod.entity.TacticCardEntity;
    import net.minecraft.client.gui.screen.Screen;
    import net.minecraft.client.gui.widget.ButtonWidget;
    import net.minecraft.entity.LivingEntity;
    import net.minecraft.entity.player.PlayerEntity;
    import net.minecraft.server.network.ServerPlayerEntity;
    import net.minecraft.text.Text;
    import net.minecraft.util.TypeFilter;
    import net.minecraft.util.math.Box;

    import java.util.ArrayList;
    import java.util.List;

    public class SelectTargetPlayerScreen extends Screen {

        private final PlayerEntity player;
        private ArrayList<LivingEntity> targets = new ArrayList<>();
        private final TargetFilter filter;
        public SelectTargetPlayerScreen(PlayerEntity player) {
            super(Text.of("选择目标玩家"));
            this.player = player;
            this.targets = TacticCardEntity.getTargets(player);
            filter = null;
        }
        /**
         * 构造函数
         * @param player 当前操作玩家
         * @param filter 目标筛选条件，可为 null 表示不过滤
         */
        public SelectTargetPlayerScreen(PlayerEntity player, TargetFilter filter) {
            super(Text.of("选择目标玩家"));
            this.player = player;
            this.filter = filter;

            for (LivingEntity entity : TacticCardEntity.getTargets(player)) {
                if (filter == null || filter.test(entity)) {
                    targets.add(entity);
                }
            }
        }


        @Override
        protected void init() {
            super.init(); // 重要：调用父类初始化方法
            int buttonY = this.height / 2 - 100;  // 起始Y坐标
            int buttonHeight = 20;  // 每个按钮的高度
            int buttonSpacing = 5;  // 按钮之间的间距

            if (targets.size() == 0) {
                // 如果没有目标，显示一条提示信息
                this.addDrawableChild(ButtonWidget.builder(Text.of("没有可选目标！"), button -> {
                            if (this.client != null) {
                                this.client.setScreen(null);
                            }
                        })
                        .dimensions(this.width / 2 - 100, buttonY, 200, buttonHeight)
                        .build());
            } else {
                // 创建目标玩家选择按钮
                for (int i = 0; i < targets.size(); i++) {
                    LivingEntity targetEntity = targets.get(i);
                    this.addDrawableChild(ButtonWidget.builder(
                                    Text.of(targetEntity.getName().getString()),
                                    button -> {
                                        // 选择目标后打开选择牌区界面
                                        if (this.client != null) {
                                            if(filter== null) this.client.setScreen(new SelectCardAreaScreen(player, targetEntity));
                                            else  onTargetSelected(targetEntity);
                                        }
                                    })
                            .dimensions(this.width / 2 - 100, buttonY + i * (buttonHeight + buttonSpacing), 200, buttonHeight)
                            .build());
                }
            }
        }

        /**
         * 回调方法，选择目标后触发
         * 子类可以重写
         */
        protected void onTargetSelected(LivingEntity target) {
            // 默认关闭界面
            if (this.client != null) {
                this.client.setScreen(null);
            }
        }

        public ArrayList<LivingEntity> getTargets() {
            return targets;
        }

        /**
         * 目标筛选器接口
         */
        public interface TargetFilter {
            boolean test(LivingEntity entity);
        }
    }
