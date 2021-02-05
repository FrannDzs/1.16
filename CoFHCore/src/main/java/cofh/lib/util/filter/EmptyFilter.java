package cofh.lib.util.filter;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;

public class EmptyFilter implements IFilter {

    public static final EmptyFilter INSTANCE = new EmptyFilter();

    @Override
    public IFilter read(CompoundNBT nbt) {

        return INSTANCE;
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {

        return nbt;
    }

    // region IFilterOptions
    @Override
    public boolean getAllowList() {

        return false;
    }

    @Override
    public boolean setAllowList(boolean allowList) {

        return false;
    }

    @Override
    public boolean getCheckNBT() {

        return false;
    }

    @Override
    public boolean setCheckNBT(boolean checkNBT) {

        return false;
    }
    // endregion

    // region INamedContainerProvider
    @Override
    public ITextComponent getDisplayName() {

        return null;
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory inventory, PlayerEntity player) {

        return null;
    }
    // endregion
}
