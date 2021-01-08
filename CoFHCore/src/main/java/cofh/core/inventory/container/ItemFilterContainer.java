package cofh.core.inventory.container;

import cofh.lib.inventory.ItemInvWrapper;
import cofh.lib.inventory.container.ContainerCoFH;
import cofh.lib.inventory.container.slot.SlotFalseCopy;
import cofh.lib.util.filter.IFilterOptions;
import cofh.lib.util.filter.ItemFilter;
import cofh.lib.util.helpers.MathHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import static cofh.lib.util.references.CoreReferences.ITEM_FILTER_CONTAINER;

public class ItemFilterContainer extends ContainerCoFH implements IFilterOptions {

    protected ItemStack filterStack;
    protected ItemFilter filter;
    protected ItemInvWrapper filterInventory;

    public ItemFilterContainer(int windowId, PlayerInventory inventory, PlayerEntity player) {

        super(ITEM_FILTER_CONTAINER, windowId, inventory, player);

        filterStack = player.getHeldItemMainhand();
        filter = (ItemFilter) ItemFilter.readFromNBT(filterStack.getTag());

        int slots = filter.size();
        filterInventory = new ItemInvWrapper(this, slots);

        int rows = MathHelper.clamp(slots / 3, 1, 3);
        int rowSize = slots / rows;

        int xOffset = 62 - 9 * rowSize;
        int yOffset = 44 - 9 * rows;

        for (int i = 0; i < filter.size(); ++i) {
            addSlot(new SlotFalseCopy(filterInventory, i, xOffset + i % rowSize * 18, yOffset + i / rowSize * 18));
        }
        bindPlayerInventory(inventory);
    }

    @Override
    protected int getSizeInventory() {

        return filterInventory.getSizeInventory();
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {

        return true;
    }

    @Override
    public void onCraftMatrixChanged(IInventory inventoryIn) {

        this.detectAndSendChanges();
    }

    // region IFilterOptions
    @Override
    public boolean getAllowList() {

        return filter.getAllowList();
    }

    @Override
    public boolean setAllowList(boolean allowList) {

        return filter.setAllowList(allowList);
    }

    @Override
    public boolean getCheckNBT() {

        return filter.getCheckNBT();
    }

    @Override
    public boolean setCheckNBT(boolean checkNBT) {

        return filter.setCheckNBT(checkNBT);
    }
    // endregion
}
