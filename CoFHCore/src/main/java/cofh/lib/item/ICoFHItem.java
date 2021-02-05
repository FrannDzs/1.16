package cofh.lib.item;

import cofh.lib.util.helpers.SecurityHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.extensions.IForgeItem;

import javax.annotation.Nullable;
import java.util.List;

import static cofh.lib.util.constants.NBTTags.TAG_DIRTY;
import static cofh.lib.util.helpers.StringHelper.*;

/**
 * Hacky default interface to reduce boilerplate. :)
 */
public interface ICoFHItem extends IForgeItem {

    default boolean isCreative(ItemStack stack) {

        return false;
    }

    default boolean isDirty(ItemStack stack) {

        return stack.getTag() != null && stack.getTag().getBoolean(TAG_DIRTY);
    }

    default void clearDirty(ItemStack stack) {

        if (stack.getTag() != null) {
            stack.getTag().remove(TAG_DIRTY);
        }
    }

    default void markDirty(ItemStack stack) {

        if (stack.getTag() != null) {
            stack.getOrCreateTag().putBoolean(TAG_DIRTY, true);
        }
    }

    @Override
    default boolean hasCustomEntity(ItemStack stack) {

        return SecurityHelper.hasSecurity(stack);
    }

    @Override
    @Nullable
    default Entity createEntity(World world, Entity location, ItemStack stack) {

        if (SecurityHelper.hasSecurity(stack)) {
            location.setInvulnerable(true);
            ((ItemEntity) location).lifespan = Integer.MAX_VALUE;
        }
        return null;
    }

    default void addEnergyTooltip(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn, int extract, int receive, boolean creative) {

        if (extract == receive && extract > 0 || creative) {
            tooltip.add(getTextComponent(localize("info.cofh.transfer") + ": " + getScaledNumber(extract) + " RF/t"));
        } else if (extract > 0) {
            if (receive > 0) {
                tooltip.add(getTextComponent(localize("info.cofh.send") + "|" + localize("info.cofh.receive") + ": " + getScaledNumber(extract) + "|" + getScaledNumber(receive) + " RF/t"));
            } else {
                tooltip.add(getTextComponent(localize("info.cofh.send") + ": " + getScaledNumber(extract) + " RF/t"));
            }
        } else if (receive > 0) {
            tooltip.add(getTextComponent(localize("info.cofh.receive") + ": " + getScaledNumber(receive) + " RF/t"));
        }
    }

}
