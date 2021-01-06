package cofh.core.client.gui;

import cofh.core.inventory.container.ItemFilterContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class ItemFilterScreen extends ContainerScreenCoFH<ItemFilterContainer> {

    public ItemFilterScreen(ItemFilterContainer container, PlayerInventory inv, ITextComponent titleIn) {

        super(container, inv, titleIn);
    }

    @Override
    public void init() {

    }

}
