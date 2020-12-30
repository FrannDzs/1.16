package cofh.core.datagen;

import cofh.core.data.LootTableProviderCoFH;
import net.minecraft.data.DataGenerator;

import static cofh.core.util.references.CoreReferences.GLOSSED_MAGMA;

public class CoreLootTableProvider extends LootTableProviderCoFH {

    public CoreLootTableProvider(DataGenerator gen) {

        super(gen);
    }

    @Override
    public String getName() {

        return "CoFH Core: Loot Tables";
    }

    @Override
    protected void addTables() {

        lootTables.put(GLOSSED_MAGMA, getEmptyTable());
    }

}
