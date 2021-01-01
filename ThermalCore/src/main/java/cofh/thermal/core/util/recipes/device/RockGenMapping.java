package cofh.thermal.core.util.recipes.device;

import cofh.core.util.recipes.SerializableRecipe;
import cofh.thermal.core.init.TCoreRecipeTypes;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;

import static cofh.thermal.core.ThermalCore.RECIPE_SERIALIZERS;
import static cofh.thermal.core.init.TCoreRecipeTypes.ID_MAPPING_ROCK_GEN;

public class RockGenMapping extends SerializableRecipe {

    protected final Block below;
    protected final Block adjacent;
    protected final ItemStack item;

    protected RockGenMapping(ResourceLocation recipeId, Block below, Block adjacent, ItemStack item) {

        super(recipeId);

        this.below = below;
        this.adjacent = adjacent;
        this.item = item;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {

        return RECIPE_SERIALIZERS.get(ID_MAPPING_ROCK_GEN);
    }

    @Override
    public IRecipeType<?> getType() {

        return TCoreRecipeTypes.MAPPING_ROCK_GEN;
    }

    // region GETTERS
    public Block getBelow() {

        return below;
    }

    public Block getAdjacent() {

        return adjacent;
    }

    public ItemStack getItem() {

        return item;
    }
    // endregion
}
