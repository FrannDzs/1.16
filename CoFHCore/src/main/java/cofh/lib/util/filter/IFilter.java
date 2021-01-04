package cofh.lib.util.filter;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.function.Predicate;

public interface IFilter<T> extends INBTSerializable<CompoundNBT> {

    Predicate<T> getRules();

    IFilter<T> read(CompoundNBT nbt);

    CompoundNBT write(CompoundNBT nbt);

    @Override
    default CompoundNBT serializeNBT() {

        return write(new CompoundNBT());
    }

    @Override
    default void deserializeNBT(CompoundNBT nbt) {

        read(nbt);
    }

}
