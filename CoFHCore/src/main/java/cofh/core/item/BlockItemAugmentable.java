package cofh.core.item;

import cofh.lib.item.IAugmentableItem;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import java.util.function.IntSupplier;
import java.util.function.Predicate;

public class BlockItemAugmentable extends BlockItemCoFH implements IAugmentableItem {

    protected IntSupplier numSlots = () -> 0;
    protected Predicate<ItemStack> augValidator = (e) -> true;

    public BlockItemAugmentable(Block blockIn, Properties builder) {

        super(blockIn, builder);
    }

    public BlockItemAugmentable setNumSlots(IntSupplier numSlots) {

        this.numSlots = numSlots;
        return this;
    }

    public BlockItemAugmentable setAugValidator(Predicate<ItemStack> augValidator) {

        this.augValidator = augValidator;
        return this;
    }

    // region IAugmentableItem
    @Override
    public int getAugmentSlots(ItemStack augmentable) {

        return numSlots.getAsInt();
    }

    @Override
    public boolean validAugment(ItemStack augmentable, ItemStack augment) {

        return augValidator.test(augment);
    }
    // endregion
}
