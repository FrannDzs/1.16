package cofh.thermal.core.tileentity.device;

import cofh.core.fluid.FluidStorageCoFH;
import cofh.core.inventory.ItemStorageCoFH;
import cofh.thermal.core.inventory.container.device.DeviceHiveExtractorContainer;
import cofh.thermal.core.tileentity.DeviceTileBase;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.Items;
import net.minecraft.tileentity.BeehiveTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;

import static cofh.core.util.StorageGroup.OUTPUT;
import static cofh.core.util.constants.Constants.TANK_MEDIUM;
import static cofh.core.util.helpers.ItemHelper.cloneStack;
import static cofh.core.util.references.CoreReferences.FLUID_HONEY;
import static cofh.thermal.core.common.ThermalConfig.deviceAugments;
import static cofh.thermal.core.init.TCoreReferences.DEVICE_HIVE_EXTRACTOR_TILE;

public class DeviceHiveExtractorTile extends DeviceTileBase {

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

    @Override
    protected void updateActiveState() {

        super.updateActiveState();

        if (isActive) {
            extractProducts(pos.up());
        }
    }

    @Override
    protected boolean isValid() {

        return world != null && world.getBlockState(pos.up()).hasProperty(BeehiveBlock.HONEY_LEVEL);
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

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory inventory, PlayerEntity player) {

        return new DeviceHiveExtractorContainer(i, world, pos, inventory, player);
    }

}
