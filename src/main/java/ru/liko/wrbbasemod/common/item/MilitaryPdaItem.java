package ru.liko.wrbbasemod.common.item;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
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

    // NBT Tags
    private static final String NBT_ENABLED = "Enabled";
    private static final String NBT_OWNER = "Owner";
    private static final String NBT_OWNER_UUID = "OwnerUUID";
    private static final String NBT_USAGE_COUNT = "UsageCount";
    private static final String NBT_LAST_USED = "LastUsed";
    private static final String NBT_CREATED_TIME = "CreatedTime";

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
        
        // Инициализация NBT тегов при первом использовании
        initializeNBT(stack, player);
        
        if (!level.isClientSide) {
            player.getCapability(WrbPlayerDataProvider.WRB_PLAYER_DATA_CAPABILITY).ifPresent(data -> {
                boolean newState = !data.isCompassActive();
                data.setCompassActive(newState);
                
                // Обновляем NBT теги
                updateNBT(stack, player, newState);
                
                if (player instanceof ServerPlayer serverPlayer) {
                    WrbNetworking.sendToClient(new SyncWrbDataPacket(data), serverPlayer);
                    player.displayClientMessage(newState ? ENABLED_MSG : DISABLED_MSG, true);
                }
            });
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
    
    /**
     * Инициализирует NBT теги при первом создании/использовании КПК
     */
    private void initializeNBT(ItemStack stack, Player player) {
        CompoundTag tag = stack.getOrCreateTag();
        
        // Устанавливаем время создания, если его нет
        if (!tag.contains(NBT_CREATED_TIME)) {
            tag.putLong(NBT_CREATED_TIME, System.currentTimeMillis());
        }
        
        // Устанавливаем владельца, если его нет
        if (!tag.contains(NBT_OWNER)) {
            tag.putString(NBT_OWNER, player.getName().getString());
            tag.putString(NBT_OWNER_UUID, player.getStringUUID());
        }
        
        // Инициализируем счетчик использований
        if (!tag.contains(NBT_USAGE_COUNT)) {
            tag.putInt(NBT_USAGE_COUNT, 0);
        }
        
        // Устанавливаем начальное состояние
        if (!tag.contains(NBT_ENABLED)) {
            tag.putBoolean(NBT_ENABLED, false);
        }
    }
    
    /**
     * Обновляет NBT теги при использовании КПК
     */
    private void updateNBT(ItemStack stack, Player player, boolean enabled) {
        CompoundTag tag = stack.getOrCreateTag();
        
        // Обновляем состояние
        tag.putBoolean(NBT_ENABLED, enabled);
        
        // Увеличиваем счетчик использований
        int usageCount = tag.getInt(NBT_USAGE_COUNT);
        tag.putInt(NBT_USAGE_COUNT, usageCount + 1);
        
        // Обновляем время последнего использования
        tag.putLong(NBT_LAST_USED, System.currentTimeMillis());
        
        // Обновляем владельца (на случай если КПК передали другому игроку)
        tag.putString(NBT_OWNER, player.getName().getString());
        tag.putString(NBT_OWNER_UUID, player.getStringUUID());
    }
    
    /**
     * Получает состояние КПК из NBT
     */
    public static boolean isEnabled(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(NBT_ENABLED);
    }
    
    /**
     * Получает имя владельца из NBT
     */
    public static String getOwner(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(NBT_OWNER) ? tag.getString(NBT_OWNER) : "Unknown";
    }
    
    /**
     * Получает количество использований из NBT
     */
    public static int getUsageCount(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null ? tag.getInt(NBT_USAGE_COUNT) : 0;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            // Показываем состояние
            boolean enabled = tag.getBoolean(NBT_ENABLED);
            tooltipComponents.add(Component.translatable("item.wrbbasemod.military_pda.tooltip.status")
                    .append(": ")
                    .append(enabled ? ENABLED_MSG : DISABLED_MSG));
            
            // Показываем владельца
            if (tag.contains(NBT_OWNER)) {
                tooltipComponents.add(Component.translatable("item.wrbbasemod.military_pda.tooltip.owner")
                        .append(": ")
                        .append(Component.literal(tag.getString(NBT_OWNER)).withStyle(ChatFormatting.AQUA)));
            }
            
            // Показываем количество использований в расширенном режиме
            if (isAdvanced.isAdvanced()) {
                int usageCount = tag.getInt(NBT_USAGE_COUNT);
                tooltipComponents.add(Component.translatable("item.wrbbasemod.military_pda.tooltip.usage_count")
                        .append(": ")
                        .append(Component.literal(String.valueOf(usageCount)).withStyle(ChatFormatting.GRAY)));
                
                // Показываем время создания
                if (tag.contains(NBT_CREATED_TIME)) {
                    long createdTime = tag.getLong(NBT_CREATED_TIME);
                    long currentTime = System.currentTimeMillis();
                    long ageInSeconds = (currentTime - createdTime) / 1000;
                    tooltipComponents.add(Component.translatable("item.wrbbasemod.military_pda.tooltip.age")
                            .append(": ")
                            .append(Component.literal(formatTime(ageInSeconds)).withStyle(ChatFormatting.GRAY)));
                }
                
                // Показываем UUID владельца для отладки
                if (tag.contains(NBT_OWNER_UUID)) {
                    tooltipComponents.add(Component.literal("UUID: " + tag.getString(NBT_OWNER_UUID))
                            .withStyle(ChatFormatting.DARK_GRAY));
                }
            }
        }
    }
    
    /**
     * Форматирует время в читаемый вид
     */
    private String formatTime(long seconds) {
        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            return (seconds / 60) + "m";
        } else if (seconds < 86400) {
            return (seconds / 3600) + "h";
        } else {
            return (seconds / 86400) + "d";
        }
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
