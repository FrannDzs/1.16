package cofh.thermal.core.tileentity.device;

import cofh.core.fluid.FluidStorageCoFH;
import cofh.core.inventory.ItemStorageCoFH;
import cofh.thermal.core.inventory.container.device.DeviceHiveExtractorContainer;
import cofh.thermal.core.tileentity.ThermalTileBase;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.BeehiveTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;

import static cofh.core.util.StorageGroup.OUTPUT;
import static cofh.core.util.constants.Constants.TANK_MEDIUM;
import static cofh.core.util.helpers.ItemHelper.cloneStack;
import static cofh.core.util.references.CoreReferences.FLUID_HONEY;
import static cofh.thermal.core.common.ThermalConfig.deviceAugments;
import static cofh.thermal.core.init.TCoreReferences.DEVICE_HIVE_EXTRACTOR_TILE;

public class DeviceHiveExtractorTile extends ThermalTileBase {

    private static final int COMB_AMOUNT = 2;
    private static final int HONEY_AMOUNT = 250;

    protected ItemStorageCoFH outputSlot = new ItemStorageCoFH();
    protected FluidStorageCoFH outputTank = new FluidStorageCoFH(TANK_MEDIUM);

    public DeviceHiveExtractorTile() {

        super(DEVICE_HIVE_EXTRACTOR_TILE);

        inventory.addSlot(outputSlot, OUTPUT);

        tankInv.addTank(outputTank, OUTPUT);

        addAugmentSlots(deviceAugments);
        initHandlers();
    }

    protected void extractProducts(BlockPos above) {

        if (world == null) {
            return;
        }
        BlockState hive = world.getBlockState(above);
        if (hive.hasProperty(BeehiveBlock.HONEY_LEVEL) && BeehiveTileEntity.getHoneyLevel(hive) >= 5) {
            world.setBlockState(above, hive.with(BeehiveBlock.HONEY_LEVEL, 0), 3);
            if (outputSlot.isEmpty()) {
                outputSlot.setItemStack(cloneStack(Items.HONEYCOMB, COMB_AMOUNT));
            } else {
                outputSlot.modify(Math.min(COMB_AMOUNT, outputSlot.getSpace()));
            }
            if (outputTank.isEmpty()) {
                outputTank.setFluidStack(new FluidStack(FLUID_HONEY, HONEY_AMOUNT));
            } else {
                outputTank.modify(Math.min(HONEY_AMOUNT, outputTank.getSpace()));
            }
        }
    }

    protected void updateActiveState() {

        boolean curActive = isActive;
        isActive = redstoneControl().getState() && world.getBlockState(pos.up()).hasProperty(BeehiveBlock.HONEY_LEVEL);
        updateActiveState(curActive);
    }

    @Override
    public void neighborChanged(Block blockIn, BlockPos fromPos) {

        super.neighborChanged(blockIn, fromPos);

        updateActiveState();

        if (fromPos.equals(pos.up())) {
            if (isActive) {
                extractProducts(pos.up());
            }
        }
    }

    @Override
    public void onPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {

        super.onPlacedBy(worldIn, pos, state, placer, stack);

        updateActiveState();

        if (isActive) {
            extractProducts(pos.up());
        }
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory inventory, PlayerEntity player) {

        return new DeviceHiveExtractorContainer(i, world, pos, inventory, player);
    }

    // region ITileCallback
    @Override
    public void onControlUpdate() {

        updateActiveState();
        super.onControlUpdate();
    }
    // endregion
}
