package cofh.thermal.core.client.gui.device;

import cofh.core.util.helpers.StringHelper;
import cofh.thermal.core.client.gui.ThermalScreenBase;
import cofh.thermal.core.inventory.container.device.DeviceCollectorContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import static cofh.core.util.GuiHelper.generatePanelInfo;
import static cofh.core.util.constants.Constants.ID_THERMAL;

public class DeviceCollectorScreen extends ThermalScreenBase<DeviceCollectorContainer> {

    public static final String TEX_PATH = ID_THERMAL + ":textures/gui/devices/collector.png";
    public static final ResourceLocation TEXTURE = new ResourceLocation(TEX_PATH);

    public DeviceCollectorScreen(DeviceCollectorContainer container, PlayerInventory inv, ITextComponent titleIn) {

        super(container, inv, container.tile, StringHelper.getTextComponent("block.thermal.device_collector"));
        texture = TEXTURE;
        info = generatePanelInfo("info.thermal.device_collector");
    }

}
