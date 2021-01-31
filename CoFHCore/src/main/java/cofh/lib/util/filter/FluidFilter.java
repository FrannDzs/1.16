package cofh.lib.util.filter;

import cofh.core.util.helpers.FluidHelper;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.Container;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static cofh.lib.util.constants.NBTTags.*;
import static net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND;

public class FluidFilter implements IFilter {

    public static FluidFilter EMPTY_FILTER = new FluidFilter(0) {

        @Override
        public Predicate<FluidStack> getFluidRules() {

            return ALWAYS_ALLOW;
        }
    };
    public static Predicate<FluidStack> ALWAYS_ALLOW = (stack) -> true;

    protected List<FluidStack> fluids;
    protected Predicate<FluidStack> rules;

    protected boolean allowList = false;
    protected boolean checkNBT = false;

    public FluidFilter(int size) {

        fluids = new ArrayList<>(size);
        for (int i = 0; i < size; ++i) {
            fluids.add(FluidStack.EMPTY);
        }
    }

    @Override
    public Predicate<FluidStack> getFluidRules() {

        if (rules == null) {
            Set<Fluid> fluidSet = new ObjectOpenHashSet<>();
            for (FluidStack fluid : fluids) {
                fluidSet.add(fluid.getFluid());
            }
            rules = stack -> {
                if (stack.isEmpty()) {
                    return false;
                }
                if (allowList != fluidSet.contains(stack.getFluid())) {
                    return false;
                }
                if (checkNBT) {
                    for (FluidStack fluid : fluids) {
                        if (FluidHelper.fluidsEqual(stack, fluid)) {
                            return allowList;
                        }
                    }
                }
                return true;
            };
        }
        return rules;
    }

    public static IFilter readFromNBT(CompoundNBT nbt) {

        if (nbt == null || !nbt.contains(TAG_FILTER)) {
            return EMPTY_FILTER;
        }
        return new FluidFilter(12);
    }

    @Override
    public IFilter read(CompoundNBT nbt) {

        CompoundNBT subTag = nbt.getCompound(TAG_FILTER);
        if (this == EMPTY_FILTER || subTag.isEmpty()) {
            return EMPTY_FILTER;
        }
        ListNBT list = subTag.getList(TAG_TANK_INV, TAG_COMPOUND);
        for (int i = 0; i < list.size(); ++i) {
            CompoundNBT tankTag = list.getCompound(i);
            int tank = tankTag.getByte(TAG_TANK);
            if (tank >= 0 && tank < fluids.size()) {
                fluids.set(tank, FluidStack.loadFluidStackFromNBT(tankTag));
            }
        }
        return this;
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {

        if (this == EMPTY_FILTER || fluids.size() <= 0) {
            return nbt;
        }
        CompoundNBT subTag = new CompoundNBT();
        ListNBT list = new ListNBT();
        for (int i = 0; i < fluids.size(); ++i) {
            if (!fluids.get(i).isEmpty()) {
                CompoundNBT tankTag = new CompoundNBT();
                tankTag.putByte(TAG_TANK, (byte) i);
                fluids.get(i).writeToNBT(tankTag);
                list.add(tankTag);
            }
        }
        if (!list.isEmpty()) {
            subTag.put(TAG_TANK_INV, list);
            nbt.put(TAG_FILTER, subTag);
        }
        return nbt;
    }

    // region IFilterOptions
    @Override
    public boolean getAllowList() {

        return allowList;
    }

    @Override
    public boolean setAllowList(boolean allowList) {

        this.allowList = allowList;
        return true;
    }

    @Override
    public boolean getCheckNBT() {

        return checkNBT;
    }

    @Override
    public boolean setCheckNBT(boolean checkNBT) {

        this.checkNBT = checkNBT;
        return true;
    }
    // endregion

    // region INamedContainerProvider
    @Override
    public ITextComponent getDisplayName() {

        return new TranslationTextComponent("info.cofh.filter");
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory inventory, PlayerEntity player) {

        return null;
        // return new ItemFilterContainer(i, inventory, player);
    }
    // endregion
}
