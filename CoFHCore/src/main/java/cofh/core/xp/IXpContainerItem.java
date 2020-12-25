package cofh.core.xp;

import cofh.core.util.helpers.MathHelper;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.event.entity.player.PlayerXpEvent;

import static cofh.core.util.constants.NBTTags.TAG_XP;

public interface IXpContainerItem {

    int getCapacityXP(ItemStack stack);

    default int getStoredXp(ItemStack stack) {

        return stack.getOrCreateTag().getInt(TAG_XP);
    }

    default int getSpaceXP(ItemStack stack) {

        return getCapacityXP(stack) - getStoredXp(stack);
    }

    default int modifyXp(ItemStack stack, int xp) {

        int totalXP = getStoredXp(stack) + xp;

        if (totalXP > getCapacityXP(stack)) {
            totalXP = getCapacityXP(stack);
        } else if (totalXP < 0) {
            totalXP = 0;
        }
        stack.getOrCreateTag().putInt(TAG_XP, totalXP);
        return totalXP;
    }

    static boolean storeXpOrb(PlayerXpEvent.PickupXp event, ItemStack stack) {

        IXpContainerItem item = (IXpContainerItem) stack.getItem();
        ExperienceOrbEntity orb = event.getOrb();
        int toAdd = Math.min(item.getSpaceXP(stack), orb.xpValue);

        if (toAdd > 0) {
            stack.setAnimationsToGo(5);
            PlayerEntity player = event.getPlayer();
            player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.1F, (MathHelper.RANDOM.nextFloat() - MathHelper.RANDOM.nextFloat()) * 0.35F + 0.9F);

            item.modifyXp(stack, toAdd);
            orb.xpValue -= toAdd;
            return true;
        }
        return false;
    }

}
