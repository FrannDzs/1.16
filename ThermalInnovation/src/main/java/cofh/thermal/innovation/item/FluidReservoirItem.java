package cofh.thermal.innovation.item;

import cofh.core.item.FluidContainerItem;
import cofh.core.item.IAugmentableItem;
import cofh.core.item.IMultiModeItem;
import cofh.core.util.ChatHelper;
import cofh.core.util.ProxyUtils;
import cofh.core.util.helpers.AugmentDataHelper;
import cofh.thermal.core.common.ThermalConfig;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Predicate;

import static cofh.core.util.constants.NBTTags.*;
import static cofh.core.util.helpers.AugmentableHelper.getPropertyWithDefault;
import static cofh.core.util.helpers.AugmentableHelper.setAttributeFromAugmentMax;
import static cofh.core.util.helpers.StringHelper.getTextComponent;
import static net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE;

public class FluidReservoirItem extends FluidContainerItem implements IAugmentableItem, IMultiModeItem {

    protected static final int FILL = 0;
    protected static final int EMPTY = 1;

    protected IntSupplier numSlots = () -> ThermalConfig.toolAugments;
    protected Predicate<ItemStack> augValidator = (e) -> true;

    public FluidReservoirItem(Properties builder, int fluidCapacity) {

        super(builder, fluidCapacity);

        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("filled"), (stack, world, entity) -> getFluidAmount(stack) > 0 ? 1F : 0F);
        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("active"), (stack, world, entity) -> getFluidAmount(stack) > 0 && getMode(stack) > 0 ? 1F : 0F);
    }

    public FluidReservoirItem setNumSlots(IntSupplier numSlots) {

        this.numSlots = numSlots;
        return this;
    }

    public FluidReservoirItem setAugValidator(Predicate<ItemStack> augValidator) {

        this.augValidator = augValidator;
        return this;
    }

    @Override
    protected void tooltipDelegate(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {

        tooltip.add(isActive(stack)
                ? new TranslationTextComponent("info.cofh_use_sneak_deactivate").mergeStyle(TextFormatting.DARK_GRAY)
                : new TranslationTextComponent("info.cofh.use_sneak_activate").mergeStyle(TextFormatting.DARK_GRAY));

        tooltip.add(getTextComponent("info.thermal.reservoir.mode." + getMode(stack)).mergeStyle(TextFormatting.ITALIC));
        addIncrementModeChangeTooltip(stack, worldIn, tooltip, flagIn);

        super.tooltipDelegate(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public int getItemEnchantability(ItemStack stack) {

        float base = getPropertyWithDefault(stack, TAG_AUGMENT_BASE_MOD, 1.0F);
        return Math.round(super.getItemEnchantability(stack) * base);
    }

    @Override
    public boolean isCreative(ItemStack stack) {

        return getPropertyWithDefault(stack, TAG_AUGMENT_FLUID_CREATIVE, 0.0F) > 0;
    }

    // region HELPERS
    protected boolean isActive(ItemStack stack) {

        return stack.getOrCreateTag().getBoolean(TAG_ACTIVE);
    }

    protected void setActive(ItemStack stack, boolean state) {

        stack.getOrCreateTag().putBoolean(TAG_ACTIVE, state);
    }

    protected void setAttributesFromAugment(ItemStack container, CompoundNBT augmentData) {

        CompoundNBT subTag = container.getChildTag(TAG_PROPERTIES);
        if (subTag == null) {
            return;
        }
        setAttributeFromAugmentMax(subTag, augmentData, TAG_AUGMENT_BASE_MOD);
        setAttributeFromAugmentMax(subTag, augmentData, TAG_AUGMENT_FLUID_STORAGE);
        setAttributeFromAugmentMax(subTag, augmentData, TAG_AUGMENT_FLUID_CREATIVE);
    }
    // endregion

    // region IFluidContainerItem
    @Override
    public int getCapacity(ItemStack container) {

        float base = getPropertyWithDefault(container, TAG_AUGMENT_BASE_MOD, 1.0F);
        float mod = getPropertyWithDefault(container, TAG_AUGMENT_FLUID_STORAGE, 1.0F);
        return getMaxStored(container, Math.round(fluidCapacity * mod * base));
    }

    // TODO: Determine if there should be limitations.
    //    @Override
    //    public int fill(ItemStack container, FluidStack resource, FluidAction action) {
    //
    //        if (getMode(container) != FILL) {
    //            return 0;
    //        }
    //        return super.fill(container, resource, action);
    //    }
    //
    //    @Override
    //    public FluidStack drain(ItemStack container, int maxDrain, FluidAction action) {
    //
    //        if (getMode(container) != EMPTY) {
    //            return FluidStack.EMPTY;
    //        }
    //        return super.drain(container, maxDrain, action);
    //    }
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
        int fluidExcess = getFluidAmount(container) - getCapacity(container);
        if (fluidExcess > 0) {
            drain(container, fluidExcess, EXECUTE);
        }
    }
    // endregion

    // region IMultiModeItem
    @Override
    public void onModeChange(PlayerEntity player, ItemStack stack) {

        player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.4F, 0.6F + 0.2F * getMode(stack));
        ChatHelper.sendIndexedChatMessageToPlayer(player, new TranslationTextComponent("info.thermal.reservoir.mode." + getMode(stack)));
    }
    // endregion
}
