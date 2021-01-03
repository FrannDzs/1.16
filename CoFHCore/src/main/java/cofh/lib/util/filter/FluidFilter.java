package cofh.lib.util.filter;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static cofh.lib.util.constants.NBTTags.TAG_TANK;
import static cofh.lib.util.constants.NBTTags.TAG_TANK_INV;
import static net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND;

public class FluidFilter implements IFilter<FluidStack>, INBTSerializable<CompoundNBT> {

    protected List<FluidStack> fluids;
    protected Predicate<FluidStack> rules;

    protected boolean allowlist = true;

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
            rules = stack -> allowlist == fluidSet.contains(stack.getFluid());
        }
        return rules;
    }

    public FluidFilter read(CompoundNBT nbt) {

        ListNBT list = nbt.getList(TAG_TANK_INV, TAG_COMPOUND);
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

        if (fluids.size() <= 0) {
            return nbt;
        }
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
            nbt.put(TAG_TANK_INV, list);
        }
        return nbt;
    }

    @Override
    public CompoundNBT serializeNBT() {

        return write(new CompoundNBT());
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {

        read(nbt);
    }

}
