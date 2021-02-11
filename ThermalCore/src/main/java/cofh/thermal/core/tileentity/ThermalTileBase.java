package cofh.thermal.core.tileentity;

import cofh.core.network.packet.client.TileControlPacket;
import cofh.core.network.packet.client.TileRedstonePacket;
import cofh.core.network.packet.client.TileStatePacket;
import cofh.core.tileentity.TileCoFH;
import cofh.core.util.control.*;
import cofh.core.util.filter.EmptyFilter;
import cofh.lib.energy.EmptyEnergyStorage;
import cofh.lib.energy.EnergyStorageCoFH;
import cofh.lib.fluid.FluidStorageCoFH;
import cofh.lib.fluid.ManagedTankInv;
import cofh.lib.inventory.ItemStorageCoFH;
import cofh.lib.inventory.ManagedItemInv;
import cofh.lib.item.IAugmentableItem;
import cofh.lib.util.EmptyTimeTracker;
import cofh.lib.util.TimeTracker;
import cofh.lib.util.Utils;
import cofh.lib.util.filter.IFilter;
import cofh.lib.util.helpers.AugmentDataHelper;
import cofh.lib.xp.EmptyXpStorage;
import cofh.lib.xp.XpStorage;
import cofh.thermal.core.common.ThermalConfig;
import cofh.thermal.core.util.IThermalInventory;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cofh.core.util.helpers.GuiHelper.*;
import static cofh.lib.util.StorageGroup.ACCESSIBLE;
import static cofh.lib.util.StorageGroup.INTERNAL;
import static cofh.lib.util.constants.Constants.ACTIVE;
import static cofh.lib.util.constants.NBTTags.*;
import static cofh.lib.util.helpers.AugmentableHelper.*;
import static cofh.lib.util.references.CoreReferences.HOLDING;
import static net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND;

public abstract class ThermalTileBase extends TileCoFH implements ISecurableTile, IRedstoneControllableTile, INamedContainerProvider, IThermalInventory {

    protected static final int BASE_ENERGY = 50000;
    protected static final int BASE_PROCESS_TICK = 20;
    protected static final int BASE_XP_STORAGE = 2500;

    protected TimeTracker timeTracker = EmptyTimeTracker.INSTANCE;
    protected ManagedItemInv inventory = new ManagedItemInv(this, TAG_ITEM_INV);
    protected ManagedTankInv tankInv = new ManagedTankInv(this, TAG_TANK_INV);
    protected EnergyStorageCoFH energyStorage = EmptyEnergyStorage.INSTANCE;
    protected XpStorage xpStorage = EmptyXpStorage.INSTANCE;
    protected IFilter filter = EmptyFilter.INSTANCE;

    protected SecurityControlModule securityControl = new SecurityControlModule(this);
    protected RedstoneControlModule redstoneControl = new RedstoneControlModule(this);

    protected List<ItemStorageCoFH> augments = new ArrayList<>();
    protected Set<String> augmentTypes = new ObjectOpenHashSet<>();

    protected ListNBT enchantments = new ListNBT();

    public boolean isActive;
    public boolean wasActive;
    protected FluidStack renderFluid = FluidStack.EMPTY;

    public ThermalTileBase(TileEntityType<?> tileEntityTypeIn) {

        super(tileEntityTypeIn);
        redstoneControl.setEnabled(() -> redstoneControlFeature);
    }

    // region BASE PARAMETERS
    protected int getBaseEnergyStorage() {

        return BASE_ENERGY;
    }

    protected int getBaseEnergyXfer() {

        return BASE_PROCESS_TICK * 10;
    }

    protected int getBaseProcessTick() {

        return BASE_PROCESS_TICK;
    }

    protected int getBaseXpStorage() {

        return BASE_XP_STORAGE;
    }
    // endregion

    // TODO: Does this need to exist?
    @Override
    public void remove() {

        super.remove();

        energyCap.invalidate();
        itemCap.invalidate();
        fluidCap.invalidate();
    }

    // region HELPERS
    @Override
    public int invSize() {

        return inventory.getSlots();
    }

    @Override
    public int augSize() {

        return augments.size();
    }

    public ManagedItemInv getItemInv() {

        return inventory;
    }

    public ManagedTankInv getTankInv() {

        return tankInv;
    }

    protected void initHandlers() {

        inventory.initHandlers();
        tankInv.initHandlers();
    }

    protected void updateActiveState(boolean curActive) {

        // TODO: Config time delay
        if (timeTracker.notSet() || !wasActive && curActive != isActive || wasActive && (timeTracker.hasDelayPassed(world, 40))) {
            wasActive = false;
            if (getBlockState().hasProperty(ACTIVE)) {
                world.setBlockState(pos, getBlockState().with(ACTIVE, isActive));
            }
            TileStatePacket.sendToClient(this);
        }
    }

    protected boolean cacheRenderFluid() {

        return false;
    }

    @Override
    public List<? extends ItemStorageCoFH> inputSlots() {

        return inventory.getInputSlots();
    }

    @Override
    public List<? extends FluidStorageCoFH> inputTanks() {

        return tankInv.getInputTanks();
    }

    protected List<? extends ItemStorageCoFH> outputSlots() {

        return inventory.getOutputSlots();
    }

    protected List<? extends FluidStorageCoFH> outputTanks() {

        return tankInv.getOutputTanks();
    }

    protected List<? extends ItemStorageCoFH> internalSlots() {

        return inventory.getInternalSlots();
    }

    protected List<? extends FluidStorageCoFH> internalTanks() {

        return tankInv.getInternalTanks();
    }

    @Override
    public void neighborChanged(Block blockIn, BlockPos fromPos) {

        if (world != null && redstoneControl.isControllable()) {
            redstoneControl.setPower(world.getRedstonePowerFromNeighbors(pos));
            TileRedstonePacket.sendToClient(this);
        }
    }

    @Override
    public void onPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {

        super.onPlacedBy(worldIn, pos, state, placer, stack);

        enchantments = stack.getEnchantmentTagList();

        updateAugmentState();
        onControlUpdate();
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState) {

        if (!keepItems()) {
            for (int i = 0; i < invSize() - augSize(); ++i) {
                InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), inventory.getStackInSlot(i));
            }
        }
        if (!ThermalConfig.keepAugments.get()) {
            for (int i = invSize() - augSize(); i < invSize(); ++i) {
                Utils.dropItemStackIntoWorldWithRandomness(inventory.getStackInSlot(i), worldIn, pos);
            }
        }
    }

    @Override
    public ItemStack createItemStackTag(ItemStack stack) {

        CompoundNBT nbt = stack.getOrCreateChildTag(TAG_BLOCK_ENTITY);
        if (keepEnergy()) {
            getEnergyStorage().writeWithParams(nbt);
        }
        if (keepItems()) {
            getItemInv().writeSlotsToNBT(nbt, 0, invSize() - augSize());
        }
        if (ThermalConfig.keepAugments.get() && augSize() > 0) {
            getItemInv().writeSlotsToNBTUnordered(nbt, TAG_AUGMENTS, invSize() - augSize());
            if (stack.getItem() instanceof IAugmentableItem) {
                List<ItemStack> items = augments.stream().map(ItemStorageCoFH::getItemStack).flatMap(Stream::of).collect(Collectors.toList());
                ((IAugmentableItem) stack.getItem()).updateAugmentState(stack, items);
            }
        }
        if (keepFluids()) {
            getTankInv().write(nbt);
        }
        // TODO: Keep XP?

        if (ThermalConfig.keepRSControl.get() && redstoneControlFeature) {
            redstoneControl().writeSettings(nbt);
        }
        if (ThermalConfig.keepSideConfig.get() && this instanceof IReconfigurableTile) {
            ((IReconfigurableTile) this).reconfigControl().writeSettings(nbt);
        }
        if (ThermalConfig.keepTransferControl.get() && this instanceof ITransferControllableTile) {
            ((ITransferControllableTile) this).transferControl().writeSettings(nbt);
        }
        if (hasSecurity()) {
            securityControl().write(nbt);
        }
        if (!nbt.isEmpty()) {
            stack.setTagInfo(TAG_BLOCK_ENTITY, nbt);
        }
        if (!enchantments.isEmpty()) {
            stack.getOrCreateTag().put(TAG_ENCHANTMENTS, enchantments);
        }
        return super.createItemStackTag(stack);
    }

    protected boolean keepEnergy() {

        return ThermalConfig.keepEnergy.get();
    }

    protected boolean keepFluids() {

        return ThermalConfig.keepFluids.get();
    }

    protected boolean keepItems() {

        return ThermalConfig.keepItems.get();
    }
    // endregion

    // region GUI
    public ItemStorageCoFH getSlot(int slot) {

        return inventory.getSlot(slot);
    }

    public FluidStorageCoFH getTank(int tank) {

        return tankInv.getTank(tank);
    }

    public EnergyStorageCoFH getEnergyStorage() {

        return energyStorage;
    }

    @Override
    public XpStorage getXpStorage() {

        return xpStorage;
    }

    public FluidStack getRenderFluid() {

        return renderFluid;
    }

    public int getScaledDuration() {

        return getScaledDuration(DURATION);
    }

    public int getScaledDuration(int scale) {

        return isActive ? scale : 0;
    }

    public int getScaledProgress() {

        return getScaledProgress(PROGRESS);
    }

    public int getScaledProgress(int scale) {

        return isActive ? scale : 0;
    }

    public int getScaledSpeed() {

        return getScaledSpeed(SPEED);
    }

    public int getScaledSpeed(int scale) {

        return isActive ? scale : 0;
    }

    public int getCurSpeed() {

        return -1;
    }

    public int getMaxSpeed() {

        return -1;
    }

    public double getEfficiency() {

        return -1.0D;
    }

    @Override
    public boolean clearEnergy(int coil) {

        return energyStorage.clear();
    }

    @Override
    public boolean clearSlot(int slot) {

        if (slot >= inventory.getSlots()) {
            return false;
        }
        if (inventory.getSlot(slot).clear()) {
            onInventoryChange(slot);
            return true;
        }
        return false;
    }

    @Override
    public boolean clearTank(int tank) {

        if (tank >= tankInv.getTanks()) {
            return false;
        }
        if (tankInv.getTank(tank).clear()) {
            onTankChange(tank);
            return true;
        }
        return false;
    }
    // endregion

    // region NETWORK

    // CONTROL
    @Override
    public PacketBuffer getControlPacket(PacketBuffer buffer) {

        super.getControlPacket(buffer);

        securityControl.writeToBuffer(buffer);
        redstoneControl.writeToBuffer(buffer);

        buffer.writeFluidStack(renderFluid);

        return buffer;
    }

    @Override
    public void handleControlPacket(PacketBuffer buffer) {

        super.handleControlPacket(buffer);

        securityControl.readFromBuffer(buffer);
        redstoneControl.readFromBuffer(buffer);

        renderFluid = buffer.readFluidStack();
    }

    // GUI
    @Override
    public PacketBuffer getGuiPacket(PacketBuffer buffer) {

        super.getGuiPacket(buffer);

        buffer.writeBoolean(isActive);
        buffer.writeFluidStack(renderFluid);

        energyStorage.writeToBuffer(buffer);
        xpStorage.writeToBuffer(buffer);

        for (int i = 0; i < tankInv.getTanks(); ++i) {
            buffer.writeFluidStack(tankInv.get(i));
        }
        return buffer;
    }

    @Override
    public void handleGuiPacket(PacketBuffer buffer) {

        super.handleGuiPacket(buffer);

        isActive = buffer.readBoolean();
        renderFluid = buffer.readFluidStack();

        energyStorage.readFromBuffer(buffer);
        xpStorage.readFromBuffer(buffer);

        for (int i = 0; i < tankInv.getTanks(); ++i) {
            tankInv.set(i, buffer.readFluidStack());
        }
    }

    // REDSTONE
    @Override
    public PacketBuffer getRedstonePacket(PacketBuffer buffer) {

        super.getRedstonePacket(buffer);

        buffer.writeInt(redstoneControl.getPower());

        return buffer;
    }

    @Override
    public void handleRedstonePacket(PacketBuffer buffer) {

        super.handleRedstonePacket(buffer);

        redstoneControl.setPower(buffer.readInt());
    }

    // STATE
    @Override
    public PacketBuffer getStatePacket(PacketBuffer buffer) {

        super.getStatePacket(buffer);

        buffer.writeBoolean(isActive);
        buffer.writeFluidStack(renderFluid);

        return buffer;
    }

    @Override
    public void handleStatePacket(PacketBuffer buffer) {

        super.handleStatePacket(buffer);

        isActive = buffer.readBoolean();
        renderFluid = buffer.readFluidStack();
    }
    // endregion

    // region NBT
    @Override
    public void read(BlockState state, CompoundNBT nbt) {

        super.read(state, nbt);

        isActive = nbt.getBoolean(TAG_ACTIVE);
        wasActive = nbt.getBoolean(TAG_ACTIVE_TRACK);

        enchantments = nbt.getList(TAG_ENCHANTMENTS, 10);

        inventory.read(nbt);

        if (nbt.contains(TAG_AUGMENTS)) {
            inventory.readSlotsUnordered(nbt.getList(TAG_AUGMENTS, TAG_COMPOUND), invSize() - augSize());
        }
        updateAugmentState();

        tankInv.read(nbt);
        energyStorage.read(nbt);
        xpStorage.read(nbt);
        filter.read(nbt);

        securityControl.read(nbt);
        redstoneControl.read(nbt);

        renderFluid = FluidStack.loadFluidStackFromNBT(nbt.getCompound(TAG_RENDER_FLUID));
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {

        super.write(nbt);

        nbt.putBoolean(TAG_ACTIVE, isActive);
        nbt.putBoolean(TAG_ACTIVE_TRACK, wasActive);

        nbt.put(TAG_ENCHANTMENTS, enchantments);

        inventory.write(nbt);
        tankInv.write(nbt);
        getEnergyStorage().write(nbt);
        getXpStorage().write(nbt);
        filter.write(nbt);

        securityControl.write(nbt);
        redstoneControl.write(nbt);

        if (!renderFluid.isEmpty()) {
            nbt.put(TAG_RENDER_FLUID, renderFluid.writeToNBT(new CompoundNBT()));
        }
        return nbt;
    }
    // endregion

    // region AUGMENTS
    protected boolean redstoneControlFeature = ThermalConfig.flagRSControl.get();
    protected boolean xpStorageFeature = ThermalConfig.flagXPStorage.get();

    protected boolean creativeEnergy = false;
    protected boolean creativeTanks = false;
    protected boolean creativeSlots = false;

    protected float baseMod = 1.0F;

    // This is CLEARED after augments are finalized.
    protected CompoundNBT augmentNBT = new CompoundNBT();

    /**
     * This should be called AFTER all other slots have been added.
     * Augment slots are added to the INTERNAL inventory category.
     *
     * @param numAugments Number of augment slots to add.
     */
    protected final void addAugmentSlots(int numAugments) {

        for (int i = 0; i < numAugments; ++i) {
            ItemStorageCoFH slot = new ItemStorageCoFH(1, AugmentDataHelper::hasAugmentData);
            augments.add(slot);
            inventory.addSlot(slot, INTERNAL);
        }
        ((ArrayList<ItemStorageCoFH>) augments).trimToSize();
    }

    protected final void updateAugmentState() {

        resetAttributes();
        for (ItemStorageCoFH slot : augments) {
            ItemStack augment = slot.getItemStack();
            CompoundNBT augmentData = AugmentDataHelper.getAugmentData(augment);
            if (augmentData == null) {
                continue;
            }
            augmentTypes.add(AugmentDataHelper.getAugmentType(augment));
            setAttributesFromAugment(augmentData);
        }
        finalizeAttributes(EnchantmentHelper.deserializeEnchantments(enchantments));
        augmentNBT = null;
    }

    protected void resetAttributes() {

        redstoneControlFeature = defaultRedstoneControlState();
        xpStorageFeature = defaultXpStorageState();

        creativeEnergy = false;
        creativeTanks = false;
        creativeSlots = false;

        baseMod = 1.0F;

        augmentNBT = new CompoundNBT();
    }

    protected void setAttributesFromAugment(CompoundNBT augmentData) {

        redstoneControlFeature |= getAttributeMod(augmentData, TAG_AUGMENT_FEATURE_RS_CONTROL) > 0;
        xpStorageFeature |= getAttributeMod(augmentData, TAG_AUGMENT_FEATURE_XP_STORAGE) > 0;

        setAttributeFromAugmentMax(augmentNBT, augmentData, TAG_AUGMENT_BASE_MOD);
        setAttributeFromAugmentMax(augmentNBT, augmentData, TAG_AUGMENT_RF_STORAGE);
        setAttributeFromAugmentMax(augmentNBT, augmentData, TAG_AUGMENT_RF_XFER);
        setAttributeFromAugmentMax(augmentNBT, augmentData, TAG_AUGMENT_FLUID_STORAGE);

        baseMod = Math.max(getAttributeMod(augmentData, TAG_AUGMENT_BASE_MOD), baseMod);

        creativeEnergy |= getAttributeMod(augmentData, TAG_AUGMENT_RF_CREATIVE) > 0;
        creativeTanks |= getAttributeMod(augmentData, TAG_AUGMENT_FLUID_CREATIVE) > 0;
    }

    protected void finalizeAttributes(Map<Enchantment, Integer> enchantmentMap) {

        float holdingMod = getHoldingMod(enchantmentMap);

        float energyStorageMod = holdingMod * baseMod * getAttributeModWithDefault(augmentNBT, TAG_AUGMENT_RF_STORAGE, 1.0F);
        float energyXferMod = holdingMod * baseMod * getAttributeModWithDefault(augmentNBT, TAG_AUGMENT_RF_STORAGE, 1.0F);
        float fluidStorageMod = holdingMod * baseMod * getAttributeModWithDefault(augmentNBT, TAG_AUGMENT_FLUID_STORAGE, 1.0F);
        float itemStorageMod = holdingMod * baseMod * getAttributeModWithDefault(augmentNBT, TAG_AUGMENT_ITEM_STORAGE, 1.0F);
        float xpStorageMod = holdingMod * baseMod;

        energyStorage.applyModifiers(energyStorageMod, energyXferMod).setCreative(() -> creativeEnergy);

        for (int i = 0; i < tankInv.getTanks(); ++i) {
            tankInv.getTank(i).applyModifiers(fluidStorageMod).setCreative(() -> creativeTanks);
        }
        for (int i = 0; i < inventory.getSlots(); ++i) {
            inventory.getSlot(i).applyModifiers(itemStorageMod).setCreative(() -> creativeSlots);
        }

        // TODO: XP Storage improvement
        int storedXp = xpStorage.getStored();
        xpStorage.applyModifiers(xpStorageMod * (xpStorageFeature ? 1 : 0));
        if (storedXp > 0 && xpStorage.getStored() < storedXp) {
            spawnXpOrbs(storedXp - xpStorage.getStored(), Vector3d.copyCenteredHorizontally(pos));
        }
    }

    protected boolean defaultReconfigState() {

        return ThermalConfig.flagReconfigSides.get();
    }

    protected boolean defaultRedstoneControlState() {

        return ThermalConfig.flagRSControl.get();
    }

    protected boolean defaultXpStorageState() {

        return ThermalConfig.flagXPStorage.get();
    }

    protected float getHoldingMod(Map<Enchantment, Integer> enchantmentMap) {

        int holding = enchantmentMap.getOrDefault(HOLDING, 0);
        return 1 + holding / 2F;
    }
    // endregion

    // region MODULES
    @Override
    public SecurityControlModule securityControl() {

        return securityControl;
    }

    @Override
    public RedstoneControlModule redstoneControl() {

        return redstoneControl;
    }
    // endregion

    // region CAPABILITIES
    protected LazyOptional<?> energyCap = LazyOptional.empty();
    protected LazyOptional<?> itemCap = LazyOptional.empty();
    protected LazyOptional<?> fluidCap = LazyOptional.empty();

    protected void updateHandlers() {

        LazyOptional<?> prevEnergyCap = energyCap;
        energyCap = energyStorage.getCapacity() > 0 ? LazyOptional.of(() -> energyStorage) : LazyOptional.empty();
        prevEnergyCap.invalidate();

        LazyOptional<?> prevItemCap = itemCap;
        IItemHandler invHandler = inventory.getHandler(ACCESSIBLE);
        itemCap = inventory.hasAccessibleSlots() ? LazyOptional.of(() -> invHandler) : LazyOptional.empty();
        prevItemCap.invalidate();

        LazyOptional<?> prevFluidCap = fluidCap;
        IFluidHandler fluidHandler = tankInv.getHandler(ACCESSIBLE);
        fluidCap = tankInv.hasAccessibleTanks() ? LazyOptional.of(() -> fluidHandler) : LazyOptional.empty();
        prevFluidCap.invalidate();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {

        if (cap == CapabilityEnergy.ENERGY && energyStorage.getMaxEnergyStored() > 0) {
            return getEnergyCapability(side);
        }
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && inventory.hasAccessibleSlots()) {
            return getItemHandlerCapability(side);
        }
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && tankInv.hasAccessibleTanks()) {
            return getFluidHandlerCapability(side);
        }
        return super.getCapability(cap, side);
    }

    protected <T> LazyOptional<T> getEnergyCapability(@Nullable Direction side) {

        if (!energyCap.isPresent() && energyStorage.getCapacity() > 0) {
            energyCap = LazyOptional.of(() -> energyStorage);
        }
        return energyCap.cast();
    }

    protected <T> LazyOptional<T> getItemHandlerCapability(@Nullable Direction side) {

        if (!itemCap.isPresent() && inventory.hasAccessibleSlots()) {
            IItemHandler handler = inventory.getHandler(ACCESSIBLE);
            itemCap = LazyOptional.of(() -> handler);
        }
        return itemCap.cast();
    }

    protected <T> LazyOptional<T> getFluidHandlerCapability(@Nullable Direction side) {

        if (!fluidCap.isPresent() && tankInv.hasAccessibleTanks()) {
            IFluidHandler handler = tankInv.getHandler(ACCESSIBLE);
            fluidCap = LazyOptional.of(() -> handler);
        }
        return fluidCap.cast();
    }
    // endregion

    // region INamedContainerProvider
    @Override
    public ITextComponent getDisplayName() {

        return new StringTextComponent(getType().getRegistryName().getPath());
    }
    // endregion

    // region ITileCallback
    @Override
    public void onInventoryChange(int slot) {

        /* Implicit requirement here that augments always come LAST in slot order.
        This isn't a bad assumption/rule though, as it's a solid way to handle it.*/
        if (slot >= invSize() - augSize()) {
            updateAugmentState();
        }
    }

    @Override
    public void onControlUpdate() {

        updateHandlers();
        callNeighborStateChange();
        TileControlPacket.sendToClient(this);
    }
    // endregion

    // region IConveyableData
    @Override
    public void readConveyableData(PlayerEntity player, CompoundNBT tag) {

        redstoneControl.readSettings(tag);

        onControlUpdate();
    }

    @Override
    public void writeConveyableData(PlayerEntity player, CompoundNBT tag) {

        redstoneControl.writeSettings(tag);
    }
    // endregion
}
