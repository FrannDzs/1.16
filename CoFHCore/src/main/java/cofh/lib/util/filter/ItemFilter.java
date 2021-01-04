package cofh.lib.util.filter;

import cofh.lib.util.helpers.ItemHelper;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static cofh.lib.util.constants.NBTTags.*;
import static net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND;

public class ItemFilter implements IFilter<ItemStack> {

    public static ItemFilter EMPTY_FILTER = new ItemFilter(0) {

        @Override
        public Predicate<ItemStack> getRules() {

            return ALWAYS_ALLOW;
        }
    };
    public static Predicate<ItemStack> ALWAYS_ALLOW = (stack) -> true;

    protected List<ItemStack> items;
    protected Predicate<ItemStack> rules;

    protected boolean allowlist = false;
    protected boolean checkNBT = false;

    public ItemFilter(int size) {

        items = new ArrayList<>(size);
        for (int i = 0; i < size; ++i) {
            items.add(ItemStack.EMPTY);
        }
    }

    @Override
    public Predicate<ItemStack> getRules() {

        if (rules == null) {
            Set<Item> itemSet = new ObjectOpenHashSet<>();
            for (ItemStack item : items) {
                itemSet.add(item.getItem());
            }
            rules = stack -> {
                if (stack.isEmpty()) {
                    return false;
                }
                if (allowlist != itemSet.contains(stack.getItem())) {
                    return false;
                }
                if (checkNBT) {
                    for (ItemStack item : items) {
                        if (ItemHelper.itemsEqualWithTags(stack, item)) {
                            return allowlist;
                        }
                    }
                }
                return true;
            };
        }
        return rules;
    }

    public static IFilter<ItemStack> readFromNBT(CompoundNBT nbt) {

        if (nbt == null || !nbt.contains(TAG_FILTER)) {
            return EMPTY_FILTER;
        }
        return new ItemFilter(0).read(nbt);
    }

    public IFilter<ItemStack> read(CompoundNBT nbt) {

        CompoundNBT subTag = nbt.getCompound(TAG_FILTER);
        if (this == EMPTY_FILTER || subTag.isEmpty()) {
            return EMPTY_FILTER;
        }
        ListNBT list = subTag.getList(TAG_ITEM_INV, TAG_COMPOUND);
        for (int i = 0; i < list.size(); ++i) {
            CompoundNBT slotTag = list.getCompound(i);
            int slot = slotTag.getByte(TAG_SLOT);
            if (slot >= 0 && slot < items.size()) {
                items.set(slot, ItemStack.read(slotTag));
            }
        }
        return this;
    }

    public CompoundNBT write(CompoundNBT nbt) {

        if (this == EMPTY_FILTER || items.size() <= 0) {
            return nbt;
        }
        CompoundNBT subTag = new CompoundNBT();
        ListNBT list = new ListNBT();
        for (int i = 0; i < items.size(); ++i) {
            if (!items.get(i).isEmpty()) {
                CompoundNBT slotTag = new CompoundNBT();
                slotTag.putByte(TAG_SLOT, (byte) i);
                items.get(i).write(slotTag);
                list.add(slotTag);
            }
        }
        if (!list.isEmpty()) {
            subTag.put(TAG_ITEM_INV, list);
            nbt.put(TAG_FILTER, subTag);
        }
        return nbt;
    }

}
