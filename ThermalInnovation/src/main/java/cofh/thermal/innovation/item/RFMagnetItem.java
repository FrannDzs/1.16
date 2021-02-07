package cofh.thermal.innovation.item;

import cofh.core.item.EnergyContainerItem;
import cofh.core.util.ProxyUtils;
import cofh.core.util.helpers.ChatHelper;
import cofh.lib.item.IAugmentableItem;
import cofh.lib.item.IMultiModeItem;
import cofh.lib.util.RayTracer;
import cofh.lib.util.Utils;
import cofh.lib.util.helpers.AugmentDataHelper;
import cofh.thermal.core.common.ThermalConfig;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Predicate;

import static cofh.lib.util.constants.NBTTags.*;
import static cofh.lib.util.helpers.AugmentableHelper.*;
import static cofh.lib.util.helpers.StringHelper.getTextComponent;
import static cofh.thermal.core.init.TCoreSounds.SOUND_MAGNET;

public class RFMagnetItem extends EnergyContainerItem implements IAugmentableItem, IMultiModeItem {

    //    protected static final int MAP_CAPACITY = 128;
    //    protected static final WeakHashMap<ItemStack, IFilter> FILTERS = new WeakHashMap<>(MAP_CAPACITY);

    protected static final int RADIUS = 4;
    protected static final int REACH = 64;

    protected static final int TIME_CONSTANT = 8;
    protected static final int PICKUP_DELAY = 32;

    protected static final int ENERGY_PER_ITEM = 25;
    protected static final int ENERGY_PER_USE = 200;

    protected IntSupplier numSlots = () -> ThermalConfig.toolAugments;
    protected Predicate<ItemStack> augValidator = (e) -> true;

    public RFMagnetItem(Properties builder, int maxEnergy, int maxTransfer) {

        super(builder, maxEnergy, maxTransfer);

        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("charged"), (stack, world, entity) -> getEnergyStored(stack) > 0 ? 1F : 0F);
        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("active"), (stack, world, entity) -> getEnergyStored(stack) > 0 && getMode(stack) > 0 ? 1F : 0F);
    }

    public RFMagnetItem setNumSlots(IntSupplier numSlots) {

        this.numSlots = numSlots;
        return this;
    }

    public RFMagnetItem setAugValidator(Predicate<ItemStack> augValidator) {

        this.augValidator = augValidator;
        return this;
    }

    @Override
    protected void tooltipDelegate(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {

        tooltip.add(getTextComponent("info.thermal.magnet.use").mergeStyle(TextFormatting.GRAY));
        //        tooltip.add(getTextComponent("info.thermal.magnet.use.sneak").mergeStyle(TextFormatting.DARK_GRAY));

        tooltip.add(getTextComponent("info.thermal.magnet.mode." + getMode(stack)).mergeStyle(TextFormatting.ITALIC));
        addIncrementModeChangeTooltip(stack, worldIn, tooltip, flagIn);

        super.tooltipDelegate(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public int getItemEnchantability(ItemStack stack) {

        float base = getPropertyWithDefault(stack, TAG_AUGMENT_BASE_MOD, 1.0F);
        return Math.round(super.getItemEnchantability(stack) * base);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {

        ItemStack stack = playerIn.getHeldItem(handIn);
        return useDelegate(stack, playerIn, handIn) ? ActionResult.resultSuccess(stack) : ActionResult.resultPass(stack);
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {

        return useDelegate(stack, context.getPlayer(), context.getHand()) ? ActionResultType.SUCCESS : ActionResultType.PASS;
    }

    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {

        if (worldIn.getGameTime() % TIME_CONSTANT != 0) {
            return;
        }
        if (Utils.isClientWorld(worldIn) || Utils.isFakePlayer(entityIn) || getMode(stack) <= 0) {
            return;
        }
        PlayerEntity player = (PlayerEntity) entityIn;

        if (getEnergyStored(stack) < ENERGY_PER_ITEM && !player.abilities.isCreativeMode) {
            return;
        }
        int radius = getRadius(stack);
        int radSq = radius * radius;

        AxisAlignedBB area = new AxisAlignedBB(player.getPosition().add(-radius, -radius, -radius), player.getPosition().add(1 + radius, 1 + radius, 1 + radius));
        List<ItemEntity> items = worldIn.getEntitiesWithinAABB(ItemEntity.class, area, EntityPredicates.IS_ALIVE);

        if (Utils.isClientWorld(worldIn)) {
            for (ItemEntity item : items) {
                if (item.cannotPickup() || item.getPersistentData().getBoolean(TAG_CONVEYOR_COMPAT)) {
                    continue;
                }
                if (item.getPositionVec().squareDistanceTo(player.getPositionVec()) <= radSq) {
                    worldIn.addParticle(RedstoneParticleData.REDSTONE_DUST, item.getPosX(), item.getPosY(), item.getPosZ(), 0, 0, 0);
                }
            }
        } else {
            // Predicate<ItemStack> filterRules = getFilter(stack).getItemRules();
            int itemCount = 0;
            for (ItemEntity item : items) {
                if (item.cannotPickup() || item.getPersistentData().getBoolean(TAG_CONVEYOR_COMPAT)) { // || !filterRules.test(item.getItem())) {
                    continue;
                }
                if (item.getThrowerId() == null || !item.getThrowerId().equals(player.getUniqueID()) || item.age >= PICKUP_DELAY) {
                    if (item.getPositionVec().squareDistanceTo(player.getPositionVec()) <= radSq) {
                        item.setPosition(player.getPosX(), player.getPosY(), player.getPosZ());
                        item.setPickupDelay(0);
                        ++itemCount;
                    }
                }
            }
            if (!player.abilities.isCreativeMode) {
                extractEnergy(stack, ENERGY_PER_ITEM * itemCount, false);
            }
        }
    }

    @Override
    public boolean isCreative(ItemStack stack) {

        return getPropertyWithDefault(stack, TAG_AUGMENT_RF_CREATIVE, 0.0F) > 0;
    }

    // region HELPERS
    //    protected IFilter getFilter(ItemStack stack) {
    //
    //        String filterType = getFilterType(stack);
    //        if (filterType.isEmpty()) {
    //            return EmptyFilter.INSTANCE;
    //        }
    //        IFilter ret = FILTERS.get(stack);
    //        if (ret != null) {
    //            return ret;
    //        }
    //        if (FILTERS.size() > MAP_CAPACITY) {
    //            FILTERS.clear();
    //        }
    //        FILTERS.put(stack, FilterRegistry.getFilter(filterType, stack.getTag()));
    //        return FILTERS.get(stack);
    //    }
    //
    //    protected boolean hasFilter(ItemStack stack) {
    //
    //        return !getFilterType(stack).isEmpty();
    //    }
    //
    //    protected String getFilterType(ItemStack stack) {
    //
    //        return getPropertyWithDefault(stack, TAG_FILTER_TYPE, "");
    //    }

    protected boolean useDelegate(ItemStack stack, PlayerEntity player, Hand hand) {

        if (Utils.isFakePlayer(player)) {
            return false;
        }
        //        if (player.isSecondaryUseActive()) {
        //            if (player instanceof ServerPlayerEntity && hasFilter(stack)) {
        //                NetworkHooks.openGui((ServerPlayerEntity) player, getFilter(stack));
        //                return true;
        //            }
        //            return false;
        //        } else

        if (getEnergyStored(stack) >= ENERGY_PER_USE || player.abilities.isCreativeMode) {
            BlockRayTraceResult traceResult = RayTracer.retrace(player, REACH);
            if (traceResult.getType() != RayTraceResult.Type.BLOCK) {
                return false;
            }
            int radius = getRadius(stack);
            int radSq = radius * radius;

            World world = player.getEntityWorld();
            BlockPos pos = traceResult.getPos();

            AxisAlignedBB area = new AxisAlignedBB(pos.add(-radius, -radius, -radius), pos.add(1 + radius, 1 + radius, 1 + radius));
            List<ItemEntity> items = world.getEntitiesWithinAABB(ItemEntity.class, area, EntityPredicates.IS_ALIVE);

            if (Utils.isClientWorld(world)) {
                for (ItemEntity item : items) {
                    if (item.getPositionVec().squareDistanceTo(traceResult.getHitVec()) <= radSq) {
                        world.addParticle(RedstoneParticleData.REDSTONE_DUST, item.getPosX(), item.getPosY(), item.getPosZ(), 0, 0, 0);
                    }
                }
            } else {
                // Predicate<ItemStack> filterRules = getFilter(stack).getItemRules();
                int itemCount = 0;
                for (ItemEntity item : items) {
                    if (item.cannotPickup() || item.getPersistentData().getBoolean(TAG_CONVEYOR_COMPAT)) { // || !filterRules.test(item.getItem())) {
                        continue;
                    }
                    if (item.getPositionVec().squareDistanceTo(traceResult.getHitVec()) <= radSq) {
                        item.setPosition(player.getPosX(), player.getPosY(), player.getPosZ());
                        item.setPickupDelay(0);
                        ++itemCount;
                    }
                }
                if (!player.abilities.isCreativeMode && itemCount > 0) {
                    extractEnergy(stack, ENERGY_PER_USE + ENERGY_PER_ITEM * itemCount, false);
                }
            }
            player.swingArm(hand);
            stack.setAnimationsToGo(5);
            player.world.playSound(null, player.getPosition(), SOUND_MAGNET, SoundCategory.PLAYERS, 0.4F, 1.0F);
        }
        return true;
    }

    protected int getRadius(ItemStack stack) {

        float base = getPropertyWithDefault(stack, TAG_AUGMENT_BASE_MOD, 1.0F);
        float mod = getPropertyWithDefault(stack, TAG_AUGMENT_AREA_RADIUS, 1.0F);
        return Math.round(RADIUS + mod * base);
    }

    protected void setAttributesFromAugment(ItemStack container, CompoundNBT augmentData) {

        CompoundNBT subTag = container.getChildTag(TAG_PROPERTIES);
        if (subTag == null) {
            return;
        }
        setAttributeFromAugmentAdd(subTag, augmentData, TAG_AUGMENT_AREA_RADIUS);

        setAttributeFromAugmentMax(subTag, augmentData, TAG_AUGMENT_BASE_MOD);
        setAttributeFromAugmentMax(subTag, augmentData, TAG_AUGMENT_RF_STORAGE);
        setAttributeFromAugmentMax(subTag, augmentData, TAG_AUGMENT_RF_XFER);
        setAttributeFromAugmentMax(subTag, augmentData, TAG_AUGMENT_RF_CREATIVE);

        // setAttributeFromAugmentString(subTag, augmentData, TAG_FILTER_TYPE);
    }
    // endregion

    // region IEnergyContainerItem
    @Override
    public int getExtract(ItemStack container) {

        float base = getPropertyWithDefault(container, TAG_AUGMENT_BASE_MOD, 1.0F);
        float mod = getPropertyWithDefault(container, TAG_AUGMENT_RF_XFER, 1.0F);
        return Math.round(extract * mod * base);
    }

    @Override
    public int getReceive(ItemStack container) {

        float base = getPropertyWithDefault(container, TAG_AUGMENT_BASE_MOD, 1.0F);
        float mod = getPropertyWithDefault(container, TAG_AUGMENT_RF_XFER, 1.0F);
        return Math.round(receive * mod * base);
    }

    @Override
    public int getMaxEnergyStored(ItemStack container) {

        float base = getPropertyWithDefault(container, TAG_AUGMENT_BASE_MOD, 1.0F);
        float mod = getPropertyWithDefault(container, TAG_AUGMENT_RF_STORAGE, 1.0F);
        return getMaxStored(container, Math.round(maxEnergy * mod * base));
    }
    // endregion

    // region IAugmentableItem
    @Override
    public int getAugmentSlots(ItemStack augmentable) {

        return numSlots.getAsInt();
    }

    @Override
    public boolean validAugment(ItemStack augmentable, ItemStack augment) {

        return augValidator.test(augment);
    }

    @Override
    public void updateAugmentState(ItemStack container, List<ItemStack> augments) {

        container.getOrCreateTag().put(TAG_PROPERTIES, new CompoundNBT());
        for (ItemStack augment : augments) {
            CompoundNBT augmentData = AugmentDataHelper.getAugmentData(augment);
            if (augmentData == null) {
                continue;
            }
            setAttributesFromAugment(container, augmentData);
        }
        // Energy Excess
        int energyExcess = getEnergyStored(container) - getMaxEnergyStored(container);
        if (energyExcess > 0) {
            setEnergyStored(container, getMaxEnergyStored(container));
        }
        // Filter Reset
        //        if (!hasFilter(container)) {
        //            container.getOrCreateTag().remove(TAG_FILTER);
        //        }
        //        FILTERS.remove(container);
    }
    // endregion

    // region IMultiModeItem
    @Override
    public void onModeChange(PlayerEntity player, ItemStack stack) {

        player.world.playSound(null, player.getPosition(), SOUND_MAGNET, SoundCategory.PLAYERS, 0.4F, 0.8F + 0.4F * getMode(stack));
        ChatHelper.sendIndexedChatMessageToPlayer(player, new TranslationTextComponent("info.thermal.magnet.mode." + getMode(stack)));
    }
    // endregion
}
