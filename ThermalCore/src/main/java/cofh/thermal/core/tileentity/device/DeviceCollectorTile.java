package cofh.thermal.core.tileentity.device;

import cofh.core.util.helpers.InventoryHelper;
import cofh.core.util.helpers.MathHelper;
import cofh.thermal.core.inventory.container.device.DeviceCollectorContainer;
import cofh.thermal.core.tileentity.ThermalTileBase;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;
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

    protected static final int TIME_CONSTANT = 16;
    protected static final int RADIUS = 5;

    public int radius = RADIUS;

    protected int timeConstant = TIME_CONSTANT;
    protected final int timeOffset;
    // protected FluidStorageCoFH xpTank = new FluidStorageCoFH(TANK_MEDIUM);

    public DeviceCollectorTile() {

        super(DEVICE_COLLECTOR_TILE);
        timeOffset = MathHelper.RANDOM.nextInt(TIME_CONSTANT);

        inventory.addSlots(ACCESSIBLE, 15);

        // tankInv.addTank(xpTank, ACCESSIBLE);

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
        if (isActive) {
            collectItemsAndXp();
        }
        updateActiveState();
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory inventory, PlayerEntity player) {

        return new DeviceCollectorContainer(i, world, pos, inventory, player);
    }

    // region HELPERS
    protected void collectItemsAndXp() {

        AxisAlignedBB area = new AxisAlignedBB(pos.add(-radius, -radius, -radius), pos.add(1 + radius, 1 + radius, 1 + radius));

        if (true) { // TODO: Item Config
            collectItems(area);
        }
        if (true) { // TODO: Xp Config
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

        List<ExperienceOrbEntity> orbs = world.getEntitiesWithinAABB(ExperienceOrbEntity.class, area, EntityPredicates.IS_ALIVE);

        for (ExperienceOrbEntity orb : orbs) {
            // xpBuffer += orb.getXpValue();
            orb.remove();
        }
    }

    protected boolean timeCheckOffset() {

        return (world.getGameTime() + timeOffset) % timeConstant == 0;
    }
    // endregion

    // region AUGMENTS
    @Override
    protected void resetAttributes() {

        super.resetAttributes();

        radius = RADIUS;
    }

    @Override
    protected void setAttributesFromAugment(CompoundNBT augmentData) {

        super.setAttributesFromAugment(augmentData);

        radius += getAttributeMod(augmentData, TAG_AUGMENT_AREA_RADIUS);
    }
    // endregion
}
