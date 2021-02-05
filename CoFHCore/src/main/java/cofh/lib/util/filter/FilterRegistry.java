package cofh.lib.util.filter;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundNBT;

import java.util.Map;

public class FilterRegistry {

    protected static final Map<String, IFilterFactory<? extends IFilter>> FILTER_MAP = new Object2ObjectOpenHashMap<>();

    static {
        registerFilter("item_filter", ItemFilter.ITEM_FILTER_FACTORY);
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
