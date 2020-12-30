package cofh.thermal.locomotion.datagen;

import cofh.core.data.BlockStateProviderCoFH;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

import static cofh.core.util.constants.Constants.ID_THERMAL;

public class TLocBlockStateProvider extends BlockStateProviderCoFH {

    public TLocBlockStateProvider(DataGenerator gen, ExistingFileHelper existingFileHelper) {

        super(gen, ID_THERMAL, existingFileHelper);
    }

    @Override
    public String getName() {

        return "Thermal Locomotion: BlockStates";
    }

    @Override
    protected void registerStatesAndModels() {

    }

}
