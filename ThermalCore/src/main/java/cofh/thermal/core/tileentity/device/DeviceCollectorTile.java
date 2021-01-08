package cofh.thermal.core.tileentity.device;

import cofh.lib.util.filter.IFilter;
import cofh.lib.util.filter.ItemFilter;
import cofh.lib.util.helpers.InventoryHelper;
import cofh.lib.xp.XpStorage;
import cofh.thermal.core.client.gui.ThermalTextures;
import cofh.thermal.core.inventory.container.device.DeviceCollectorContainer;
import cofh.thermal.core.tileentity.DeviceTileBase;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static cofh.core.client.renderer.model.ModelUtils.UNDERLAY;
import static cofh.lib.util.StorageGroup.ACCESSIBLE;
import static cofh.lib.util.constants.NBTTags.*;
import static cofh.thermal.core.common.ThermalConfig.deviceAugments;
import static cofh.thermal.core.init.TCoreReferences.DEVICE_COLLECTOR_TILE;

public class DeviceCollectorTile extends DeviceTileBase implements ITickableTileEntity {

    protected static final int TICK_RATE = 20;

    protected static final IModelData MODEL_DATA = new ModelDataMap.Builder()
            .withInitial(UNDERLAY, ThermalTextures.DEVICE_COLLECTOR_UNDERLAY_LOC)
            .build();

    protected static final Predicate<ItemEntity> VALID_ITEM_ENTITY = item -> {
        if (!item.isAlive() || item.cannotPickup()) {
            return false;
        }
        CompoundNBT data = item.getPersistentData();
        return !data.getBoolean(TAG_CONVEYOR_COMPAT) || data.getBoolean(TAG_DEMAGNETIZE_COMPAT);
    };

    protected XpStorage xpStorage;
    protected IFilter filter = ItemFilter.EMPTY_FILTER;

    protected static final int RADIUS = 4;
    public int radius = RADIUS;

    protected int process = 1;

    public DeviceCollectorTile() {

        super(DEVICE_COLLECTOR_TILE);

        inventory.addSlots(ACCESSIBLE, 15);

        xpStorage = new XpStorage(getBaseXpStorage());

        addAugmentSlots(deviceAugments);
        initHandlers();
    }

    @Override
    protected void updateActiveState(boolean curActive) {

        if (!curActive && isActive) {
            process = 1;
        }
        super.updateActiveState(curActive);
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
        process = getTimeConstant();
        collectItemsAndXp();
    }

    @Nonnull
    @Override
    public IModelData getModelData() {

        return MODEL_DATA;
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory inventory, PlayerEntity player) {

        return new DeviceCollectorContainer(i, world, pos, inventory, player);
    }

    // region HELPERS
    public int getRadius() {

        return radius;
    }

    public int getTimeConstant() {

        return TICK_RATE;
    }

    protected void collectItemsAndXp() {

        AxisAlignedBB area = new AxisAlignedBB(pos.add(-radius, -1, -radius), pos.add(1 + radius, 1 + radius, 1 + radius));

        collectItems(area);

        if (xpStorageFeature) {
            collectXpOrbs(area);
        }
    }

    protected void collectItems(AxisAlignedBB area) {

        IItemHandler handler = inventory.getHandler(ACCESSIBLE);
        List<ItemEntity> items = world.getEntitiesWithinAABB(ItemEntity.class, area, VALID_ITEM_ENTITY);

        Predicate<ItemStack> filterRules = filter.getItemRules();

        for (ItemEntity item : items) {
            ItemStack entityStack = item.getItem();
            if (!filterRules.test(entityStack)) {
                continue;
            }
            entityStack = InventoryHelper.insertStackIntoInventory(handler, entityStack, false);
            if (entityStack.isEmpty()) {
                item.remove();
            } else {
                item.setItem(entityStack);
            }
        }
    }

    protected void collectXpOrbs(AxisAlignedBB area) {

        List<ExperienceOrbEntity> orbs = world.getEntitiesWithinAABB(ExperienceOrbEntity.class, area, EntityPredicates.IS_ALIVE);

        for (ExperienceOrbEntity orb : orbs) {
            orb.xpValue -= xpStorage.receiveXp(orb.getXpValue(), false);
            if (orb.xpValue <= 0) {
                orb.remove();
            }
        }
    }
    // endregion

    // region GUI
    @Override
    public XpStorage getXpStorage() {

        return xpStorage;
    }
    // endregion

    // region NETWORK
    @Override
    public PacketBuffer getGuiPacket(PacketBuffer buffer) {

        super.getGuiPacket(buffer);

        xpStorage.writeToBuffer(buffer);

        return buffer;
    }

    @Override
    public void handleGuiPacket(PacketBuffer buffer) {

        super.handleGuiPacket(buffer);

        xpStorage.readFromBuffer(buffer);
    }
    // endregion

    // region NBT
    @Override
    public void read(BlockState state, CompoundNBT nbt) {

        super.read(state, nbt);

        xpStorage.read(nbt);
        filter = filter.read(nbt);

        filter.read(nbt);
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {

        super.write(nbt);

        xpStorage.write(nbt);
        filter.write(nbt);

        return nbt;
    }
    // endregion

    // region AUGMENTS
    protected boolean xpStorageFeature = defaultXpStorageState();

    @Override
    protected void resetAttributes() {

        super.resetAttributes();

        xpStorageFeature = defaultXpStorageState();

        radius = RADIUS;
    }

    @Override
    protected void setAttributesFromAugment(CompoundNBT augmentData) {

        super.setAttributesFromAugment(augmentData);

        xpStorageFeature |= getAttributeMod(augmentData, TAG_AUGMENT_FEATURE_XP_STORAGE) > 0;

        radius += getAttributeMod(augmentData, TAG_AUGMENT_AREA_RADIUS);
    }

    @Override
    protected void finalizeAttributes(Map<Enchantment, Integer> enchantmentMap) {

        super.finalizeAttributes(enchantmentMap);

        float holdingMod = getHoldingMod(enchantmentMap);

        int storedXp = xpStorage.getStored();
        xpStorage.applyModifiers(holdingMod * baseMod * (xpStorageFeature ? 1 : 0));
        if (xpStorage.getStored() < storedXp) {
            spawnXpOrbs(storedXp - xpStorage.getStored(), Vector3d.copyCenteredHorizontally(pos));
        }
    }
    // endregion
}
