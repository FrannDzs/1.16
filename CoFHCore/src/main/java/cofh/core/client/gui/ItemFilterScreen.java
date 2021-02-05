package cofh.core.client.gui;

import cofh.core.inventory.container.ItemFilterContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import static cofh.core.util.helpers.GuiHelper.createSlot;
import static cofh.lib.util.constants.Constants.ID_COFH_CORE;

public class ItemFilterScreen extends ContainerScreenCoFH<ItemFilterContainer> {

    public static final String TEX_PATH = ID_COFH_CORE + ":textures/gui/filter.png";
    public static final ResourceLocation TEXTURE = new ResourceLocation(TEX_PATH);

    public ItemFilterScreen(ItemFilterContainer container, PlayerInventory inv, ITextComponent titleIn) {

        super(container, inv, titleIn);

        texture = TEXTURE;
    }

    @Override
    public void init() {

        super.init();

        for (int i = 0; i < container.getFilterSize(); ++i) {
            Slot slot = container.inventorySlots.get(i);
            addElement(createSlot(this, slot.xPos, slot.yPos));
        }
    }

}
