package cofh.core.client.gui.element;

import cofh.core.client.gui.IGuiAccess;
import cofh.core.util.helpers.RenderHelper;
import cofh.core.xp.XpStorage;

import static cofh.core.util.constants.Constants.FALSE;

public class ElementXpStorage extends ElementResourceStorage {

    public ElementXpStorage(IGuiAccess gui, int posX, int posY, XpStorage storage) {

        super(gui, posX, posY, storage);
        drawStorage = FALSE;
        minDisplay = 0;

        this.claimable = () -> storage.getStored() > 0;
    }

    @Override
    protected void drawResource() {

        RenderHelper.bindTexture(texture);
        int amount = storage.getStored() <= 0 ? 0 : Math.min(getScaled(4) + 1, 4);
        drawTexturedModalRect(posX(), posY(), 0, amount * height, width, height);
    }

}
