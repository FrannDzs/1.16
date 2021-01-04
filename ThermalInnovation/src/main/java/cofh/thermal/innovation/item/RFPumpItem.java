package cofh.thermal.innovation.item;

import cofh.core.item.EnergyContainerItem;
import cofh.core.util.helpers.ChatHelper;
import cofh.core.util.ProxyUtils;
import cofh.lib.capability.CapabilityAreaEffect;
import cofh.lib.capability.IAreaEffect;
import cofh.lib.energy.EnergyContainerItemWrapper;
import cofh.lib.energy.IEnergyContainerItem;
import cofh.lib.item.IAugmentableItem;
import cofh.lib.item.IMultiModeItem;
import cofh.lib.util.helpers.AreaEffectHelper;
import cofh.lib.util.helpers.AugmentDataHelper;
import cofh.thermal.core.common.ThermalConfig;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Predicate;

import static cofh.lib.util.constants.NBTTags.*;
import static cofh.lib.util.helpers.AugmentableHelper.*;

public class RFPumpItem extends EnergyContainerItem implements IAugmentableItem, IMultiModeItem {

    public static final int ENERGY_PER_USE = 200;

    protected IntSupplier numSlots = () -> ThermalConfig.toolAugments;
    protected Predicate<ItemStack> augValidator = (e) -> true;

    public RFPumpItem(Properties builder, int maxEnergy, int maxTransfer) {

        super(builder, maxEnergy, maxTransfer);

        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("charged"), (stack, world, entity) -> getEnergyStored(stack) > 0 ? 1F : 0F);
        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("active"), (stack, world, entity) -> getEnergyStored(stack) > 0 && hasActiveTag(stack) ? 1F : 0F);
    }

    public RFPumpItem setNumSlots(IntSupplier numSlots) {

        this.numSlots = numSlots;
        return this;
    }

    public RFPumpItem setAugValidator(Predicate<ItemStack> augValidator) {

        this.augValidator = augValidator;
        return this;
    }

    @Override
    protected void tooltipDelegate(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {

        int radius = getMode(stack) * 2 + 1;
        if (radius <= 1) {
            tooltip.add(new TranslationTextComponent("info.cofh.single_block").mergeStyle(TextFormatting.ITALIC));
        } else {
            tooltip.add(new TranslationTextComponent("info.cofh.area").appendString(": " + radius + "x" + radius).mergeStyle(TextFormatting.ITALIC));
        }
        if (getNumModes(stack) > 1) {
            addIncrementModeChangeTooltip(stack, worldIn, tooltip, flagIn);
        }
        super.tooltipDelegate(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public int getItemEnchantability(ItemStack stack) {

        float base = getPropertyWithDefault(stack, TAG_AUGMENT_BASE_MOD, 1.0F);
        return Math.round(super.getItemEnchantability(stack) * base);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {

        // TODO: CHANGE
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {

        if (!hasActiveTag(stack)) {
            return;
        }
        long activeTime = stack.getOrCreateTag().getLong(TAG_ACTIVE);

        if (entityIn.world.getGameTime() > activeTime) {
            stack.getOrCreateTag().remove(TAG_ACTIVE);
        }
    }

    @Override
    public boolean isCreative(ItemStack stack) {

        return getPropertyWithDefault(stack, TAG_AUGMENT_RF_CREATIVE, 0.0F) > 0;
    }

    // region HELPERS
    protected void setActive(ItemStack stack, LivingEntity entity) {

        stack.getOrCreateTag().putLong(TAG_ACTIVE, entity.world.getGameTime() + 20);
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
    }

    protected boolean hasActiveTag(ItemStack stack) {

        return stack.getOrCreateTag().contains(TAG_ACTIVE);
    }

    protected float getBaseMod(ItemStack stack) {

        return getPropertyWithDefault(stack, TAG_AUGMENT_BASE_MOD, 1.0F);
    }

    protected int getEnergyPerUse(ItemStack stack) {

        return ENERGY_PER_USE;
    }

    protected int getRadius(ItemStack stack) {

        return (int) getPropertyWithDefault(stack, TAG_AUGMENT_AREA_RADIUS, 0.0F);
    }
    // endregion

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {

        return new RFPumpItemWrapper(stack, this);
    }

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
        int energyExcess = getEnergyStored(container) - getMaxEnergyStored(container);
        if (energyExcess > 0) {
            setEnergyStored(container, getMaxEnergyStored(container));
        }
        if (getMode(container) >= getNumModes(container)) {
            setMode(container, getNumModes(container) - 1);
        }
    }
    // endregion

    // region IMultiModeItem
    @Override
    public int getNumModes(ItemStack stack) {

        return 1 + getRadius(stack);
    }

    @Override
    public void onModeChange(PlayerEntity player, ItemStack stack) {

        player.world.playSound(null, player.getPosition(), SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.PLAYERS, 0.4F, 1.0F - 0.1F * getMode(stack));
        int radius = getMode(stack) * 2 + 1;
        if (radius <= 1) {
            ChatHelper.sendIndexedChatMessageToPlayer(player, new TranslationTextComponent("info.cofh.single_block"));
        } else {
            ChatHelper.sendIndexedChatMessageToPlayer(player, new TranslationTextComponent("info.cofh.area").appendString(": " + radius + "x" + radius));
        }
    }
    // endregion

    // region CAPABILITY WRAPPER
    protected class RFPumpItemWrapper extends EnergyContainerItemWrapper implements IAreaEffect {

        private final LazyOptional<IAreaEffect> holder = LazyOptional.of(() -> this);

        RFPumpItemWrapper(ItemStack containerIn, IEnergyContainerItem itemIn) {

            super(containerIn, itemIn);
        }

        @Override
        public ImmutableList<BlockPos> getAreaEffectBlocks(BlockPos pos, PlayerEntity player) {

            return AreaEffectHelper.getBucketableBlocksRadius(container, pos, player, getMode(container));
        }

        // region ICapabilityProvider
        @Override
        @Nonnull
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {

            if (cap == CapabilityAreaEffect.AREA_EFFECT_ITEM_CAPABILITY) {
                return CapabilityAreaEffect.AREA_EFFECT_ITEM_CAPABILITY.orEmpty(cap, holder);
            }
            return super.getCapability(cap, side);
        }
        // endregion
    }
    // endregion
}
