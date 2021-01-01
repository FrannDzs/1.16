package cofh.thermal.core.util.recipes.device;

import cofh.core.util.recipes.SerializableRecipe;
import cofh.thermal.core.init.TCoreRecipeTypes;
import net.minecraft.block.Block;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;

import static cofh.thermal.core.ThermalCore.RECIPE_SERIALIZERS;
import static cofh.thermal.core.init.TCoreRecipeTypes.ID_MAPPING_ROCK_GEN;

public class RockGenMapping extends SerializableRecipe {

    protected RockGenMapping(ResourceLocation recipeId, Block base, Block cold) {

        super(recipeId);
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {

        return RECIPE_SERIALIZERS.get(ID_MAPPING_ROCK_GEN);
    }

    @Override
    public IRecipeType<?> getType() {

        return TCoreRecipeTypes.MAPPING_ROCK_GEN;
    }

}
