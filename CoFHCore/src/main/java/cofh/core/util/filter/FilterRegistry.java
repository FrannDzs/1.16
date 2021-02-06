package cofh.core.util.filter;

import cofh.lib.util.filter.IFilter;
import cofh.lib.util.filter.IFilterFactory;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundNBT;

import java.util.Map;

public class FilterRegistry {

    public static final String EMPTY_FILTER_TYPE = "";
    public static final String FLUID_FILTER_TYPE = "fluid";
    public static final String ITEM_FILTER_TYPE = "item";

    protected static final Map<String, IFilterFactory<? extends IFilter>> FILTER_MAP = new Object2ObjectOpenHashMap<>();

    static {
        registerFilter(ITEM_FILTER_TYPE, ItemFilter.FACTORY);
    }

    public static boolean registerFilter(String type, IFilterFactory<?> factory) {

        if (type == null || type.isEmpty() || factory == null) {
            return false;
        }
        FILTER_MAP.put(type, factory);
        return true;
    }

    public static IFilter getFilter(String type, CompoundNBT nbt) {

        if (FILTER_MAP.containsKey(type)) {
            return FILTER_MAP.get(type).createFilter(nbt);
        }
        return EmptyFilter.INSTANCE;
    }

}
