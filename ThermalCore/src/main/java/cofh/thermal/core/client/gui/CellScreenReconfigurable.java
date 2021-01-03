package cofh.thermal.core.client.gui;

import cofh.core.client.gui.element.panel.PanelConfiguration;
import cofh.lib.inventory.container.ContainerCoFH;
import cofh.thermal.core.tileentity.CellTileBase;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class CellScreenReconfigurable<T extends ContainerCoFH> extends ThermalScreenBase<T> {

    protected CellTileBase tile;

    public CellScreenReconfigurable(T container, PlayerInventory inv, CellTileBase tile, ITextComponent titleIn) {

        super(container, inv, tile, titleIn);
        this.tile = tile;
    }

    @Override
    public void init() {

        super.init();

        addPanel(new PanelConfiguration(this, tile, () -> tile.getFacing())
                .allowFacingConfig(true)
                .addConditionals(ThermalGuiHelper.createDefaultCellConfigs(this, name, tile)));
    }

}
