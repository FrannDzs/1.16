package cofh.core.client.gui.element.panel;

import cofh.core.util.helpers.RenderHelper;
import cofh.lib.client.gui.IGuiAccess;
import cofh.lib.util.control.ISecurable;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;
import java.util.UUID;

import static cofh.core.client.gui.CoreTextures.*;
import static cofh.lib.util.control.ISecurable.AccessMode.*;
import static cofh.lib.util.helpers.SoundHelper.playClickSound;
import static cofh.lib.util.helpers.StringHelper.localize;

public class PanelSecurity extends PanelBase {

    public static int defaultSide = LEFT;
    public static int defaultHeaderColor = 0xe1c92f;
    public static int defaultSubHeaderColor = 0xaaafb8;
    public static int defaultTextColor = 0xf0f0f0;
    public static int defaultBackgroundColor = 0x50b050;

    private final ISecurable mySecurable;
    private final UUID myPlayer;

    public PanelSecurity(IGuiAccess gui, ISecurable securable, UUID playerID) {

        this(gui, defaultSide, securable, playerID);
    }

    protected PanelSecurity(IGuiAccess gui, int sideIn, ISecurable securable, UUID playerID) {

        super(gui, sideIn);

        headerColor = defaultHeaderColor;
        subheaderColor = defaultSubHeaderColor;
        textColor = defaultTextColor;
        backgroundColor = defaultBackgroundColor;

        maxHeight = 92;
        maxWidth = 112;
        mySecurable = securable;
        myPlayer = playerID;

        this.setVisible(mySecurable::hasSecurity);
    }

    @Override
    protected void drawBackground(MatrixStack matrixStack) {

        switch (mySecurable.getAccess()) {
            case PUBLIC:
                backgroundColor = 0x40a040;
                break;
            case PRIVATE:
                backgroundColor = 0xa04040;
                break;
            case FRIENDS:
                backgroundColor = 0xa0a040;
                break;
            case TEAM:
                backgroundColor = 0x90b040;
                break;
        }
        super.drawBackground(matrixStack);

        if (!fullyOpen) {
            return;
        }
        float colorR = (backgroundColor >> 16 & 255) / 255.0F * 0.6F;
        float colorG = (backgroundColor >> 8 & 255) / 255.0F * 0.6F;
        float colorB = (backgroundColor & 255) / 255.0F * 0.6F;
        RenderSystem.color4f(colorR, colorG, colorB, 1.0F);
        gui.drawTexturedModalRect(34, 18, 16, 20, 44, 44);
        RenderHelper.resetColor();
    }

    @Override
    protected void drawForeground(MatrixStack matrixStack) {

        switch (mySecurable.getAccess()) {
            case PUBLIC:
                drawPanelIcon(matrixStack, ICON_ACCESS_PUBLIC);
                break;
            case PRIVATE:
                drawPanelIcon(matrixStack, ICON_ACCESS_PRIVATE);
                break;
            case FRIENDS:
                drawPanelIcon(matrixStack, ICON_ACCESS_FRIENDS);
                break;
            case TEAM:
                drawPanelIcon(matrixStack, ICON_ACCESS_TEAM);
                break;
        }
        if (!fullyOpen) {
            return;
        }
        getFontRenderer().drawStringWithShadow(matrixStack, localize("info.cofh.security"), sideOffset() + 18, 6, headerColor);
        getFontRenderer().drawStringWithShadow(matrixStack, localize("info.cofh.access") + ":", sideOffset() + 6, 66, subheaderColor);

        gui.drawIcon(matrixStack, ICON_BUTTON, 38, 22);
        gui.drawIcon(matrixStack, ICON_BUTTON, 58, 22);
        gui.drawIcon(matrixStack, ICON_BUTTON, 38, 42);
        gui.drawIcon(matrixStack, ICON_BUTTON, 58, 42);

        switch (mySecurable.getAccess()) {
            case PUBLIC:
                gui.drawIcon(matrixStack, ICON_BUTTON_HIGHLIGHT, 38, 22);
                getFontRenderer().drawString(matrixStack, localize("info.cofh.access_public"), sideOffset() + 14, 78, textColor);
                break;
            case PRIVATE:
                gui.drawIcon(matrixStack, ICON_BUTTON_HIGHLIGHT, 58, 22);
                getFontRenderer().drawString(matrixStack, localize("info.cofh.access_private"), sideOffset() + 14, 78, textColor);
                break;
            case FRIENDS:
                gui.drawIcon(matrixStack, ICON_BUTTON_HIGHLIGHT, 38, 42);
                getFontRenderer().drawString(matrixStack, localize("info.cofh.access_friends"), sideOffset() + 14, 78, textColor);
                break;
            case TEAM:
                gui.drawIcon(matrixStack, ICON_BUTTON_HIGHLIGHT, 58, 42);
                getFontRenderer().drawString(matrixStack, localize("info.cofh.access_team"), sideOffset() + 14, 78, textColor);
                break;
        }
        gui.drawIcon(matrixStack, ICON_ACCESS_PUBLIC, 38, 22);
        gui.drawIcon(matrixStack, ICON_ACCESS_PRIVATE, 58, 22);
        gui.drawIcon(matrixStack, ICON_ACCESS_FRIENDS, 38, 42);
        gui.drawIcon(matrixStack, ICON_ACCESS_TEAM, 58, 42);

        RenderHelper.resetColor();
    }

    @Override
    public void addTooltip(List<ITextComponent> tooltipList, int mouseX, int mouseY) {

        if (!fullyOpen) {
            tooltipList.add(new TranslationTextComponent("info.cofh.owner").append(new StringTextComponent(": " + mySecurable.getOwnerName())));
            switch (mySecurable.getAccess()) {
                case PUBLIC:
                    tooltipList.add(new TranslationTextComponent("info.cofh.access_public").mergeStyle(TextFormatting.YELLOW));
                    break;
                case PRIVATE:
                    tooltipList.add(new TranslationTextComponent("info.cofh.access_private").mergeStyle(TextFormatting.YELLOW));
                    break;
                case FRIENDS:
                    tooltipList.add(new TranslationTextComponent("info.cofh.access_friends").mergeStyle(TextFormatting.YELLOW));
                    break;
                case TEAM:
                    tooltipList.add(new TranslationTextComponent("info.cofh.access_team").mergeStyle(TextFormatting.YELLOW));
                    break;
                default:
            }
            return;
        }
        int x = mouseX - this.posX();
        int y = mouseY - this.posY();

        if (38 <= x && x < 54 && 22 <= y && y < 38) {
            tooltipList.add(new TranslationTextComponent("info.cofh.access_public"));
        } else if (58 <= x && x < 74 && 22 <= y && y < 38) {
            tooltipList.add(new TranslationTextComponent("info.cofh.access_private"));
        } else if (38 <= x && x < 54 && 42 <= y && y < 58) {
            tooltipList.add(new TranslationTextComponent("info.cofh.access_friends"));
        } else if (58 <= x && x < 74 && 42 <= y && y < 58) {
            tooltipList.add(new TranslationTextComponent("info.cofh.access_team"));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {

        if (!myPlayer.equals(mySecurable.getOwner().getId())) {
            return true;
        }
        if (!fullyOpen) {
            return false;
        }
        double x = mouseX - this.posX();
        double y = mouseY - this.posY();

        if (x < 34 || x >= 78 || y < 18 || y >= 62) {
            return false;
        }
        if (38 <= x && x < 54 && 22 <= y && y < 38) {
            if (mySecurable.getAccess() != PUBLIC) {
                mySecurable.setAccess(PUBLIC);
                playClickSound(0.5F);
            }
        } else if (58 <= x && x < 74 && 22 <= y && y < 38) {
            if (mySecurable.getAccess() != PRIVATE) {
                mySecurable.setAccess(PRIVATE);
                playClickSound(0.8F);
            }
        } else if (38 <= x && x < 54 && 42 <= y && y < 58) {
            if (mySecurable.getAccess() != FRIENDS) {
                mySecurable.setAccess(FRIENDS);
                playClickSound(0.6F);
            }
        } else if (58 <= x && x < 74 && 42 <= y && y < 58) {
            if (mySecurable.getAccess() != TEAM) {
                mySecurable.setAccess(TEAM);
                playClickSound(0.7F);
            }
        }
        return true;
    }

    @Override
    public void setFullyOpen() {

        if (!myPlayer.equals(mySecurable.getOwner().getId())) {
            return;
        }
        super.setFullyOpen();
    }

}
