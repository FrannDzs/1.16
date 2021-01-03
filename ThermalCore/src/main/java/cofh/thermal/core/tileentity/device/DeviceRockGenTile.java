package cofh.thermal.core.tileentity.device;

import cofh.lib.inventory.ItemStorageCoFH;
import cofh.thermal.core.inventory.container.device.DeviceRockGenContainer;
import cofh.thermal.core.tileentity.DeviceTileBase;
import cofh.thermal.core.util.managers.device.RockGenManager;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

import static cofh.core.client.renderer.model.ModelUtils.FLUID;
import static cofh.lib.util.StorageGroup.OUTPUT;
import static cofh.lib.util.constants.Constants.BUCKET_VOLUME;
import static cofh.thermal.core.common.ThermalConfig.deviceAugments;
import static cofh.thermal.core.init.TCoreReferences.DEVICE_ROCK_GEN_TILE;

public class DeviceRockGenTile extends DeviceTileBase implements ITickableTileEntity {

    protected static final Supplier<ItemStack> COBBLESTONE = () -> new ItemStack(Blocks.COBBLESTONE, 0);
    protected static final Supplier<ItemStack> BASALT = () -> new ItemStack(Blocks.BASALT, 0);

    protected ItemStorageCoFH slot = new ItemStorageCoFH(e -> false).setEmptyItem(COBBLESTONE).setEnabled(() -> isActive);

    protected boolean cached;
    protected boolean valid;

    protected int process;
    protected int processMax = RockGenManager.instance().getDefaultEnergy();
    protected int genAmount = 1;

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
        int adjLavaSource = 0;
        valid = false;

        Block[] adjBlocks = new Block[4];
        BlockPos[] cardinals = new BlockPos[]{
                pos.north(),
                pos.south(),
                pos.west(),
                pos.east(),
        };
        for (int i = 0; i < 4; ++i) {
            BlockPos adj = cardinals[i];
            FluidState fluidState = world.getFluidState(adj);
            if (fluidState.getFluid().equals(Fluids.LAVA)) {
                ++adjLavaSource;
            }
            adjBlocks[i] = fluidState.isEmpty() || fluidState.isSource() ? world.getBlockState(adj).getBlock() : Blocks.AIR;
        }
        if (adjLavaSource > 0) {
            Block below = world.getBlockState(pos.down()).getBlock();
            RockGenManager.RockGenRecipe recipe = RockGenManager.instance().getResult(below, adjBlocks);
            ItemStack result = recipe.getResult();
            if (!result.isEmpty()) {
                slot.setEmptyItem(() -> new ItemStack(result.getItem(), 0));
                processMax = recipe.getTime();
                genAmount = Math.max(1, result.getCount());
                if (world.getBiome(pos).getCategory() == Biome.Category.NETHER) {
                    processMax = Math.max(1, processMax / 2);
                }
                process = processMax;
                valid = true;
            }
        }
        cached = true;
    }

    @Override
    protected void updateActiveState() {

        if (!cached) {
            updateValidity();
        }
        super.updateActiveState();
    }

    @Override
    protected boolean isValid() {

        return valid;
    }

    @Override
    public void tick() {

        updateActiveState();

        if (!isActive) {
            return;
        }
        --process;
        if (process > 0) {
            return;
        }
        process = processMax;
        slot.modify((int) (genAmount * baseMod));
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

        return new DeviceRockGenContainer(i, world, pos, inventory, player);
    }

}
