package cofh.thermal.cultivation;

import cofh.thermal.cultivation.client.gui.device.DeviceSoilInfuserScreen;
import cofh.thermal.cultivation.init.TCulBlocks;
import cofh.thermal.cultivation.init.TCulItems;
import net.minecraft.block.ComposterBlock;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.item.HoeItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static cofh.lib.util.constants.Constants.ID_THERMAL_CULTIVATION;
import static cofh.thermal.core.ThermalCore.BLOCKS;
import static cofh.thermal.core.ThermalCore.ITEMS;
import static cofh.thermal.core.common.ThermalFlags.*;
import static cofh.thermal.core.init.TCoreIDs.ID_DEVICE_HIVE_EXTRACTOR;
import static cofh.thermal.core.init.TCoreIDs.ID_DEVICE_TREE_EXTRACTOR;
import static cofh.thermal.core.util.RegistrationHelper.seeds;
import static cofh.thermal.cultivation.init.TCulIDs.*;
import static cofh.thermal.cultivation.init.TCulReferences.DEVICE_SOIL_INFUSER_CONTAINER;

@Mod(ID_THERMAL_CULTIVATION)
public class ThermalCultivation {

    public ThermalCultivation() {

        setFeatureFlags();

        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        TCulBlocks.register();
        TCulItems.register();
    }

    private void setFeatureFlags() {

        setFlag(FLAG_RESOURCE_APATITE, true);

        setFlag(FLAG_BEEKEEPER_ARMOR, true);

        setFlag(FLAG_PHYTOGRO_EXPLOSIVES, true);

        setFlag(ID_DEVICE_HIVE_EXTRACTOR, true);
        setFlag(ID_DEVICE_TREE_EXTRACTOR, true);
    }

    // region INITIALIZATION
    private void commonSetup(final FMLCommonSetupEvent event) {

        event.enqueueWork(() -> {
            // CROPS
            {
                float chance = 0.65F;

                ComposterBlock.CHANCES.put(ITEMS.get(ID_BARLEY), chance);
                ComposterBlock.CHANCES.put(ITEMS.get(ID_BARLEY), chance);
                ComposterBlock.CHANCES.put(ITEMS.get(ID_ONION), chance);
                ComposterBlock.CHANCES.put(ITEMS.get(ID_RADISH), chance);
                ComposterBlock.CHANCES.put(ITEMS.get(ID_RICE), chance);
                ComposterBlock.CHANCES.put(ITEMS.get(ID_SADIROOT), chance);
                ComposterBlock.CHANCES.put(ITEMS.get(ID_SPINACH), chance);

                ComposterBlock.CHANCES.put(ITEMS.get(ID_BELL_PEPPER), chance);
                ComposterBlock.CHANCES.put(ITEMS.get(ID_EGGPLANT), chance);
                ComposterBlock.CHANCES.put(ITEMS.get(ID_GREEN_BEAN), chance);
                ComposterBlock.CHANCES.put(ITEMS.get(ID_PEANUT), chance);
                ComposterBlock.CHANCES.put(ITEMS.get(ID_STRAWBERRY), chance);
                ComposterBlock.CHANCES.put(ITEMS.get(ID_TOMATO), chance);

                ComposterBlock.CHANCES.put(ITEMS.get(ID_COFFEE), chance);
                ComposterBlock.CHANCES.put(ITEMS.get(ID_TEA), chance);

                ComposterBlock.CHANCES.put(ITEMS.get(ID_FROST_MELON), chance);
            }
            {
                float chance = 0.5F;

                ComposterBlock.CHANCES.put(ITEMS.get(ID_FROST_MELON_SLICE), chance);
            }
            // SEEDS
            {
                float chance = 0.3F;

                ComposterBlock.CHANCES.put(ITEMS.get(seeds(ID_BARLEY)), chance);
                ComposterBlock.CHANCES.put(ITEMS.get(seeds(ID_CORN)), chance);
                ComposterBlock.CHANCES.put(ITEMS.get(seeds(ID_ONION)), chance);
                ComposterBlock.CHANCES.put(ITEMS.get(seeds(ID_RADISH)), chance);
                ComposterBlock.CHANCES.put(ITEMS.get(seeds(ID_RICE)), chance);
                ComposterBlock.CHANCES.put(ITEMS.get(seeds(ID_SADIROOT)), chance);
                ComposterBlock.CHANCES.put(ITEMS.get(seeds(ID_SPINACH)), chance);

                ComposterBlock.CHANCES.put(ITEMS.get(seeds(ID_BELL_PEPPER)), chance);
                ComposterBlock.CHANCES.put(ITEMS.get(seeds(ID_EGGPLANT)), chance);
                ComposterBlock.CHANCES.put(ITEMS.get(seeds(ID_GREEN_BEAN)), chance);
                ComposterBlock.CHANCES.put(ITEMS.get(seeds(ID_PEANUT)), chance);
                ComposterBlock.CHANCES.put(ITEMS.get(seeds(ID_STRAWBERRY)), chance);
                ComposterBlock.CHANCES.put(ITEMS.get(seeds(ID_TOMATO)), chance);

                ComposterBlock.CHANCES.put(ITEMS.get(seeds(ID_COFFEE)), chance);
                ComposterBlock.CHANCES.put(ITEMS.get(seeds(ID_TEA)), chance);

                ComposterBlock.CHANCES.put(ITEMS.get(seeds(ID_FROST_MELON)), chance);
            }
            HoeItem.HOE_LOOKUP.put(BLOCKS.get(ID_PHYTOSOIL), BLOCKS.get(ID_PHYTOSOIL_TILLED).getDefaultState());
        });
        event.enqueueWork(TCulBlocks::setup);
    }

    private void clientSetup(final FMLClientSetupEvent event) {

        this.registerGuiFactories();
        this.registerRenderLayers();
    }
    // endregion

    // region HELPERS
    private void registerGuiFactories() {

        ScreenManager.registerFactory(DEVICE_SOIL_INFUSER_CONTAINER, DeviceSoilInfuserScreen::new);
    }

    private void registerRenderLayers() {

        RenderType cutout = RenderType.getCutout();

        RenderTypeLookup.setRenderLayer(BLOCKS.get(ID_BARLEY), cutout);
        RenderTypeLookup.setRenderLayer(BLOCKS.get(ID_CORN), cutout);
        RenderTypeLookup.setRenderLayer(BLOCKS.get(ID_ONION), cutout);
        RenderTypeLookup.setRenderLayer(BLOCKS.get(ID_RADISH), cutout);
        RenderTypeLookup.setRenderLayer(BLOCKS.get(ID_RICE), cutout);
        RenderTypeLookup.setRenderLayer(BLOCKS.get(ID_SADIROOT), cutout);
        RenderTypeLookup.setRenderLayer(BLOCKS.get(ID_SPINACH), cutout);

        RenderTypeLookup.setRenderLayer(BLOCKS.get(ID_BELL_PEPPER), cutout);
        RenderTypeLookup.setRenderLayer(BLOCKS.get(ID_EGGPLANT), cutout);
        RenderTypeLookup.setRenderLayer(BLOCKS.get(ID_GREEN_BEAN), cutout);
        RenderTypeLookup.setRenderLayer(BLOCKS.get(ID_PEANUT), cutout);
        RenderTypeLookup.setRenderLayer(BLOCKS.get(ID_STRAWBERRY), cutout);
        RenderTypeLookup.setRenderLayer(BLOCKS.get(ID_TOMATO), cutout);

        RenderTypeLookup.setRenderLayer(BLOCKS.get(ID_COFFEE), cutout);
        RenderTypeLookup.setRenderLayer(BLOCKS.get(ID_TEA), cutout);

        RenderTypeLookup.setRenderLayer(BLOCKS.get(ID_FROST_MELON_STEM), cutout);
        RenderTypeLookup.setRenderLayer(BLOCKS.get(ID_FROST_MELON_STEM_ATTACHED), cutout);
    }
    // endregion
}
