package cofh.lib.util.filter;

import net.minecraft.nbt.CompoundNBT;

public interface IFilterFactory<T extends IFilter> {

    T createFilter(CompoundNBT nbt);

}
