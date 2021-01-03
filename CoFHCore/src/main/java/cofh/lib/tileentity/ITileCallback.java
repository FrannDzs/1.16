package cofh.lib.tileentity;

import cofh.lib.util.IInventoryCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Mostly a sneaky way to reduce some stupid but useful boilerplate. :)
 */
public interface ITileCallback extends IInventoryCallback {

    Block block();

    BlockState state();

    BlockPos pos();

    World world();

    default int invSize() {

        return 0;
    }

    default int augSize() {

        return 0;
    }

    default ItemStack createItemStackTag(ItemStack stack) {

        return stack;
    }

    default void callBlockUpdate() {

        if (world() == null) {
            return;
        }
        BlockState state = world().getBlockState(pos());
        world().notifyBlockUpdate(pos(), state, state, 3);
    }

    default void callNeighborStateChange() {

        if (world() == null) {
            return;
        }
        world().notifyNeighborsOfStateChange(pos(), block());
    }

    default void onControlUpdate() {

    }

    default void onPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {

    }

    default void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState) {

    }

    default boolean onWrench(PlayerEntity player, Direction side) {

        return false;
    }

}
