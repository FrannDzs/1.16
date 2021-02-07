package cofh.core.inventory.container;

import cofh.core.util.filter.ItemFilter;
import cofh.lib.inventory.ItemInvWrapper;
import cofh.lib.inventory.container.ContainerCoFH;
import cofh.lib.util.filter.IFilterOptions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;

import static cofh.lib.util.references.CoreReferences.ITEM_FILTER_CONTAINER;

public class ItemFilterContainer extends ContainerCoFH implements IFilterOptions {

    protected ItemFilter filter;
    protected ItemInvWrapper filterInventory;

    public ItemFilterContainer(int windowId, ItemFilter filter, PlayerInventory inventory, PlayerEntity player) {

        super(ITEM_FILTER_CONTAINER, windowId, inventory, player);

        allowSwap = false;

        //        filterStack = player.getHeldItemMainhand();
        //        filter = ItemFilter.FACTORY.createFilter(filterStack.getTag());
        //
        //        int slots = filter.size();
        //        filterInventory = new ItemInvWrapper(this, slots);
        //        filterInventory.setInvContainer(filterStack, filter.getItems(), slots);
        //
        //        int rows = MathHelper.clamp(slots / 3, 1, 3);
        //        int rowSize = slots / rows;
        //
        //        int xOffset = 62 - 9 * rowSize;
        //        int yOffset = 44 - 9 * rows;
        //
        //        for (int i = 0; i < filter.size(); ++i) {
        //            addSlot(new SlotFalseCopy(filterInventory, i, xOffset + i % rowSize * 18, yOffset + i / rowSize * 18));
        //        }
        bindPlayerInventory(inventory);
    }

    //    @Override
    //    protected void bindPlayerInventory(PlayerInventory inventory) {
    //
    //        int xOffset = getPlayerInventoryHorizontalOffset();
    //        int yOffset = getPlayerInventoryVerticalOffset();
    //
    //        for (int i = 0; i < 3; ++i) {
    //            for (int j = 0; j < 9; ++j) {
    //                addSlot(new Slot(inventory, j + i * 9 + 9, xOffset + j * 18, yOffset + i * 18));
    //            }
    //        }
    //        for (int i = 0; i < 9; ++i) {
    //            addSlot(new Slot(inventory, i, xOffset + i * 18, yOffset + 58));
    //            //            if (i == inventory.currentItem) {
    //            //                addSlot(new SlotLocked(inventory, i, xOffset + i * 18, yOffset + 58));
    //            //            } else {
    //            //
    //            //            }
    //        }
    //    }

    public int getFilterSize() {

        return filter.size();
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
    public void onContainerClosed(PlayerEntity playerIn) {

        filter.setItems(filterInventory.getStacks());
        super.onContainerClosed(playerIn);
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
