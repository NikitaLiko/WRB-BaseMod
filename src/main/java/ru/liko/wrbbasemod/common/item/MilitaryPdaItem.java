package ru.liko.wrbbasemod.common.item;

import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;

import ru.liko.wrbbasemod.common.item.render.MilitaryPdaRenderer;
import ru.liko.wrbbasemod.common.network.WrbNetworking;
import ru.liko.wrbbasemod.common.network.packet.SyncWrbDataPacket;
import ru.liko.wrbbasemod.common.player.WrbPlayerDataProvider;

public class MilitaryPdaItem extends Item implements GeoItem {

    private static final Component ENABLED_MSG = Component.translatable("item.wrbbasemod.military_pda.tooltip.enabled").withStyle(ChatFormatting.GREEN);
    private static final Component DISABLED_MSG = Component.translatable("item.wrbbasemod.military_pda.tooltip.disabled").withStyle(ChatFormatting.RED);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public MilitaryPdaItem(Properties properties) {
        super(properties);
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            player.getCapability(WrbPlayerDataProvider.WRB_PLAYER_DATA_CAPABILITY).ifPresent(data -> {
                boolean newState = !data.isCompassActive();
                data.setCompassActive(newState);
                if (player instanceof ServerPlayer serverPlayer) {
                    WrbNetworking.sendToClient(new SyncWrbDataPacket(data), serverPlayer);
                    player.displayClientMessage(newState ? ENABLED_MSG : DISABLED_MSG, true);
                }
            });
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private MilitaryPdaRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new MilitaryPdaRenderer();
                }
                return renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // No animation controller is required for a static item model.
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
