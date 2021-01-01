package cofh.thermal.core.util.managers.device;

import cofh.thermal.core.util.managers.AbstractManager;
import net.minecraft.item.crafting.RecipeManager;

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

    // region IManager
    @Override
    public void config() {

    }

    @Override
    public void refresh(RecipeManager recipeManager) {

        //        clear();
        //        Map<ResourceLocation, IRecipe<FalseIInventory>> boosts = recipeManager.getRecipes(TCoreRecipeTypes.ID_MAPPING_ROCK_GEN);
        //        for (Map.Entry<ResourceLocation, IRecipe<FalseIInventory>> entry : boosts.entrySet()) {
        //
        //        }
    }
    // endregion

}
