package cofh.thermal.core.tileentity.device;

import cofh.core.inventory.ItemStorageCoFH;
import cofh.core.inventory.ItemStorageInfinite;
import cofh.thermal.core.inventory.container.device.DeviceRockGenContainer;
import cofh.thermal.core.tileentity.DeviceTileBase;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.wrapper.EmptyHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

import static cofh.core.client.renderer.model.ModelUtils.FLUID;
import static cofh.core.util.StorageGroup.OUTPUT;
import static cofh.core.util.constants.Constants.BUCKET_VOLUME;
import static cofh.core.util.helpers.ItemHelper.cloneStack;
import static cofh.thermal.core.common.ThermalConfig.deviceAugments;
import static cofh.thermal.core.init.TCoreReferences.DEVICE_ROCK_GEN_TILE;

public class DeviceRockGenTile extends DeviceTileBase {

    private static final int AMOUNT = 16;

    protected static ItemStack COBBLESTONE = new ItemStack(Blocks.COBBLESTONE);
    protected static ItemStack BASALT = new ItemStack(Blocks.BASALT);

    protected byte adjLavaSource;
    protected byte adjWaterSource;

    protected byte adjBlueIce;
    protected byte adjSoulSand;

    protected boolean valid;

    protected ItemStorageCoFH slot = new ItemStorageInfinite(e -> false);

    public DeviceRockGenTile() {

        super(DEVICE_ROCK_GEN_TILE);

        inventory.addSlot(slot, OUTPUT);

        addAugmentSlots(deviceAugments);
        initHandlers();

        renderFluid = new FluidStack(Fluids.LAVA, BUCKET_VOLUME);
    }

    @Override
    protected void updateValidity() {

        if (world == null || !world.isAreaLoaded(pos, 1)) {
            return;
        }
        adjLavaSource = 0;
        adjWaterSource = 0;
        adjBlueIce = 0;
        adjSoulSand = 0;
        valid = false;

        BlockPos[] cardinals = new BlockPos[]{
                pos.north(),
                pos.south(),
                pos.west(),
                pos.east(),
        };
        for (BlockPos adj : cardinals) {
            BlockState blockState = world.getBlockState(adj);
            FluidState fluidState = world.getFluidState(adj);
            if (fluidState.getFluid().equals(Fluids.LAVA)) {
                ++adjLavaSource;
            }
            if (fluidState.getFluid().equals(Fluids.WATER)) {
                ++adjWaterSource;
            }
            if (blockState.getBlock().equals(Blocks.BLUE_ICE)) {
                ++adjBlueIce;
            }
        }
        BlockPos below = pos.down();
        BlockState blockState = world.getBlockState(below);
        if (blockState.getBlock().equals(Blocks.SOUL_SAND)) {
            ++adjSoulSand;
        }
        if (adjSoulSand > 0 && adjLavaSource > 0 && adjBlueIce > 0) {
            slot.setItemStack(cloneStack(BASALT, (int) (AMOUNT * baseMod)));
            valid = true;
        } else if (adjLavaSource > 0 && adjWaterSource > 0) {
            slot.setItemStack(cloneStack(COBBLESTONE, (int) (AMOUNT * baseMod)));
            valid = true;
        } else {
            slot.clear();
            itemCap.invalidate();
        }
    }

    //    @Override
    //    public boolean onActivatedDelegate(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
    //
    //        return player.inventory.addItemStackToInventory(cloneStack(slot.getItemStack()));
    //    }

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

        return new DeviceRockGenContainer(i, world, pos, inventory, player);
    }

    // TODO: Must be added if non-lava fluids are supported.
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

    // region AUGMENTS
    @Override
    protected void finalizeAttributes(Map<Enchantment, Integer> enchantmentMap) {

        super.finalizeAttributes(enchantmentMap);

        if (!slot.isEmpty()) {
            slot.setItemStack(cloneStack(slot.getItemStack(), (int) Math.min((AMOUNT * baseMod), slot.getCapacity())));
        }
    }
    // endregion

    // region CAPABILITIES
    @Override
    protected <T> LazyOptional<T> getItemHandlerCapability(@Nullable Direction side) {

        if (!itemCap.isPresent()) {
            itemCap = LazyOptional.of(() -> isActive ? inventory.getHandler(OUTPUT) : EmptyHandler.INSTANCE);
        }
        return itemCap.cast();
    }
    // endregion
}
