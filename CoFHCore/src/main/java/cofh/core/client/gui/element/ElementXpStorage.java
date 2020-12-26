package cofh.core.client.gui.element;

import cofh.core.client.gui.IGuiAccess;
import cofh.core.util.helpers.RenderHelper;
import cofh.core.xp.XpStorage;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

import static cofh.core.util.constants.Constants.FALSE;

public class ElementXpStorage extends ElementResourceStorage {

    public ElementXpStorage(IGuiAccess gui, int posX, int posY, XpStorage storage) {

        super(gui, posX, posY, storage);
        drawStorage = FALSE;
        minDisplay = 0;
    }

    protected void drawResource() {

        RenderHelper.bindTexture(texture);
        int amount = storage.getStored() <= 0 ? 0 : Math.min(getScaled(4) + 1, 4);
        drawTexturedModalRect(posX(), posY(), 0, amount * height, width, height);
    }

    @Override
    public void addTooltip(List<ITextComponent> tooltipList, int mouseX, int mouseY) {

        super.addTooltip(tooltipList, mouseX, mouseY);

        if (storage.getStored() > 0) {
            tooltipList.add(new TranslationTextComponent("info.cofh.click_to_claim").mergeStyle(TextFormatting.GRAY));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {

        if (storage.getStored() > 0) {
            return claimStorage.getAsBoolean();
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

}
