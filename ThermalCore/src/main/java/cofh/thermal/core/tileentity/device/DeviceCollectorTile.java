package cofh.thermal.core.tileentity.device;

import cofh.core.util.helpers.InventoryHelper;
import cofh.core.util.helpers.MathHelper;
import cofh.core.xp.XpStorage;
import cofh.thermal.core.inventory.container.device.DeviceCollectorContainer;
import cofh.thermal.core.tileentity.ThermalTileBase;
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
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static cofh.core.util.StorageGroup.ACCESSIBLE;
import static cofh.core.util.constants.NBTTags.*;
import static cofh.thermal.core.common.ThermalConfig.deviceAugments;
import static cofh.thermal.core.init.TCoreReferences.DEVICE_COLLECTOR_TILE;

public class DeviceCollectorTile extends ThermalTileBase implements ITickableTileEntity {

    protected static final Predicate<ItemEntity> VALID_ITEM_ENTITY = item -> {
        if (!item.isAlive() || item.cannotPickup()) {
            return false;
        }
        CompoundNBT data = item.getPersistentData();
        return data.getBoolean(TAG_CONVEYOR_COMPAT) && !data.getBoolean(TAG_DEMAGNETIZE_COMPAT);
    };

    protected XpStorage xpStorage;
    protected boolean pause;

    protected static final int RADIUS = 5;
    public int radius = RADIUS;

    protected final int timeOffset;

    public DeviceCollectorTile() {

        super(DEVICE_COLLECTOR_TILE);
        timeOffset = MathHelper.RANDOM.nextInt(TIME_CONSTANT_HALF);

        inventory.addSlots(ACCESSIBLE, 15);

        xpStorage = new XpStorage(2500);

        addAugmentSlots(deviceAugments);
        initHandlers();
    }

    protected void updateActiveState() {

        boolean curActive = isActive;
        isActive = redstoneControl.getState();
        updateActiveState(curActive);
    }

    @Override
    public void tick() {

        if (!timeCheckOffset()) {
            return;
        }
        updateActiveState();

        if (isActive) {
            collectItemsAndXp();
        }
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory inventory, PlayerEntity player) {

        return new DeviceCollectorContainer(i, world, pos, inventory, player);
    }

    @Override
    public boolean claimXP(Vector3d pos) {

        pause = true;
        return super.claimXP(pos);
    }

    // region HELPERS
    protected void collectItemsAndXp() {

        AxisAlignedBB area = new AxisAlignedBB(pos.add(-radius, -1, -radius), pos.add(1 + radius, 2, 1 + radius));

        if (true) { // TODO: Item Config
            collectItems(area);
        }
        if (xpStorageFeature && true) { // TODO: XP Config
            collectXpOrbs(area);
        }
    }

    protected void collectItems(AxisAlignedBB area) {

        IItemHandler handler = inventory.getHandler(ACCESSIBLE);
        List<ItemEntity> items = world.getEntitiesWithinAABB(ItemEntity.class, area, VALID_ITEM_ENTITY);

        for (ItemEntity item : items) {
            ItemStack entityStack = item.getItem();
            entityStack = InventoryHelper.insertStackIntoInventory(handler, entityStack, false);
            if (entityStack.isEmpty()) {
                item.remove();
            } else {
                item.setItem(entityStack);
            }
        }
    }

    protected void collectXpOrbs(AxisAlignedBB area) {

        if (pause) {
            pause = false;
            return;
        }
        List<ExperienceOrbEntity> orbs = world.getEntitiesWithinAABB(ExperienceOrbEntity.class, area, EntityPredicates.IS_ALIVE);

        for (ExperienceOrbEntity orb : orbs) {
            orb.xpValue -= xpStorage.receiveXp(orb.getXpValue(), false);
            if (orb.xpValue <= 0) {
                orb.remove();
            }
        }
    }

    protected boolean timeCheckOffset() {

        return (world.getGameTime() + timeOffset) % TIME_CONSTANT_HALF == 0;
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
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {

        super.write(nbt);

        xpStorage.write(nbt);

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

    // region ITileCallback
    @Override
    public void onControlUpdate() {

        updateActiveState();
        super.onControlUpdate();
    }
    // endregion
}
