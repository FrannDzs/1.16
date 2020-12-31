package cofh.thermal.core.tileentity.device;

import cofh.core.fluid.FluidStorageCoFH;
import cofh.core.inventory.ItemStorageCoFH;
import cofh.core.util.helpers.FluidHelper;
import cofh.thermal.core.inventory.container.device.DeviceWaterGenContainer;
import cofh.thermal.core.tileentity.DeviceTileBase;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.container.Container;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

import static cofh.core.client.renderer.model.ModelUtils.FLUID;
import static cofh.core.util.StorageGroup.INTERNAL;
import static cofh.core.util.StorageGroup.OUTPUT;
import static cofh.core.util.constants.Constants.BUCKET_VOLUME;
import static cofh.core.util.constants.Constants.TANK_SMALL;
import static cofh.thermal.core.common.ThermalConfig.deviceAugments;
import static cofh.thermal.core.init.TCoreReferences.DEVICE_WATER_GEN_TILE;
import static net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE;

public class DeviceWaterGenTile extends DeviceTileBase implements ITickableTileEntity {

    protected static final int GENERATION_RATE = 250;
    protected static final Supplier<FluidStack> WATER = () -> new FluidStack(Fluids.WATER, 0);
    protected ItemStorageCoFH fillSlot = new ItemStorageCoFH(1, FluidHelper::hasFluidHandlerCap);

    protected byte adjWaterSource;
    protected boolean valid;

    protected FluidStorageCoFH tank = new FluidStorageCoFH(TANK_SMALL, e -> false).setEmptyFluid(WATER).setEnabled(() -> isActive);

    public DeviceWaterGenTile() {

        super(DEVICE_WATER_GEN_TILE);

        inventory.addSlot(fillSlot, INTERNAL);

        tankInv.addTank(tank, OUTPUT);

        addAugmentSlots(deviceAugments);
        initHandlers();

        renderFluid = new FluidStack(Fluids.WATER, BUCKET_VOLUME);
    }

    @Override
    protected void updateValidity() {

        if (world == null || !world.isAreaLoaded(pos, 1)) {
            return;
        }
        adjWaterSource = 0;
        valid = false;

        BlockPos[] cardinals = new BlockPos[]{
                pos.north(),
                pos.south(),
                pos.west(),
                pos.east(),
        };
        for (BlockPos adj : cardinals) {
            FluidState state = world.getFluidState(adj);
            if (state.getFluid().equals(Fluids.WATER)) {
                ++adjWaterSource;
            }
        }
        if (adjWaterSource > 1) {
            valid = true;
        } else {
            tank.clear();
        }
    }

    @Override
    protected void updateActiveState() {

        super.updateActiveState();

        if (!isActive) {
            tank.clear();
        }
    }

    @Override
    protected boolean isValid() {

        return valid;
    }

    protected void fillFluid() {

        if (!fillSlot.isEmpty()) {
            fillSlot.getItemStack()
                    .getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)
                    .ifPresent(c -> {
                        tank.drain(c.fill(new FluidStack(tank.getFluidStack(), (int) (BUCKET_VOLUME * baseMod)), EXECUTE), EXECUTE);
                        fillSlot.setItemStack(c.getContainer());
                    });
        }
    }

    @Override
    public void tick() {

        updateActiveState();

        if (isActive) {
            tank.modify((int) (GENERATION_RATE * baseMod));
            fillFluid();
        }
    }

    @Nonnull
    @Override
    public IModelData getModelData() {

        return new ModelDataMap.Builder()
                .withInitial(FLUID, renderFluid)
                .build();
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory inventory, PlayerEntity player) {

        return new DeviceWaterGenContainer(i, world, pos, inventory, player);
    }

    // TODO: Must be added if *other* water types are supported.
    //    // region NETWORK
    //    @Override
    //    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
    //
    //        super.onDataPacket(net, pkt);
    //
    //        ModelDataManager.requestModelDataRefresh(this);
    //    }
    //
    //    @Override
    //    public void handleControlPacket(PacketBuffer buffer) {
    //
    //        super.handleControlPacket(buffer);
    //
    //        ModelDataManager.requestModelDataRefresh(this);
    //    }
    //
    //    @Override
    //    public void handleStatePacket(PacketBuffer buffer) {
    //
    //        super.handleStatePacket(buffer);
    //
    //        ModelDataManager.requestModelDataRefresh(this);
    //    }
    //    // endregion
}
