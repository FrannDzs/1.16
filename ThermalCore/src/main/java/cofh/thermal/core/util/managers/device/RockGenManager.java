package cofh.thermal.core.util.managers.device;

import cofh.core.inventory.FalseIInventory;
import cofh.thermal.core.init.TCoreRecipeTypes;
import cofh.thermal.core.util.managers.AbstractManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

public class RockGenManager extends AbstractManager {

    private static final RockGenManager INSTANCE = new RockGenManager();

    protected RockGenManager() {

        super(0);
    }

    public static RockGenManager instance() {

        return INSTANCE;
    }

    protected void clear() {

    }

    // region MAPPINGS

    // endregion

    // region IManager
    @Override
    public void config() {

    }

    @Override
    public void refresh(RecipeManager recipeManager) {

        clear();
        Map<ResourceLocation, IRecipe<FalseIInventory>> mappings = recipeManager.getRecipes(TCoreRecipeTypes.MAPPING_ROCK_GEN);
        for (Map.Entry<ResourceLocation, IRecipe<FalseIInventory>> entry : mappings.entrySet()) {

        }
    }
    // endregion

}
