package cofh.lib.util.filter;

import cofh.core.util.helpers.FluidHelper;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static cofh.lib.util.constants.NBTTags.*;
import static net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND;

public class FluidFilter implements IFilter<FluidStack> {

    public static FluidFilter EMPTY_FILTER = new FluidFilter(0) {

        @Override
        public Predicate<FluidStack> getRules() {

            return ALWAYS_ALLOW;
        }
    };
    public static Predicate<FluidStack> ALWAYS_ALLOW = (stack) -> true;

    protected List<FluidStack> fluids;
    protected Predicate<FluidStack> rules;

    protected boolean allowlist = false;
    protected boolean checkNBT = false;

    public FluidFilter(int size) {

        fluids = new ArrayList<>(size);
        for (int i = 0; i < size; ++i) {
            fluids.add(FluidStack.EMPTY);
        }
    }

    @Override
    public Predicate<FluidStack> getRules() {

        if (rules == null) {
            Set<Fluid> fluidSet = new ObjectOpenHashSet<>();
            for (FluidStack fluid : fluids) {
                fluidSet.add(fluid.getFluid());
            }
            rules = stack -> {
                if (stack.isEmpty()) {
                    return false;
                }
                if (allowlist != fluidSet.contains(stack.getFluid())) {
                    return false;
                }
                if (checkNBT) {
                    for (FluidStack fluid : fluids) {
                        if (FluidHelper.fluidsEqual(stack, fluid)) {
                            return allowlist;
                        }
                    }
                }
                return true;
            };
        }
        return rules;
    }

    public static IFilter<FluidStack> readFromNBT(CompoundNBT nbt) {

        if (nbt == null || !nbt.contains(TAG_FILTER)) {
            return EMPTY_FILTER;
        }
        return new FluidFilter(0).read(nbt);
    }

    public IFilter<FluidStack> read(CompoundNBT nbt) {

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

}
