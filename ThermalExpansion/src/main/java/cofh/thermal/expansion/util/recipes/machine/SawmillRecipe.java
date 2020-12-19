package cofh.thermal.expansion.util.recipes.machine;

import cofh.thermal.core.util.recipes.ThermalRecipe;
import cofh.thermal.expansion.init.TExpRecipeTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static cofh.thermal.core.ThermalCore.RECIPE_SERIALIZERS;
import static cofh.thermal.expansion.init.TExpRecipeTypes.ID_RECIPE_SAWMILL;

public class SawmillRecipe extends ThermalRecipe {

    public SawmillRecipe(ResourceLocation recipeId, int energy, float experience, int minPower, int maxPower, @Nullable List<Ingredient> inputItems, @Nullable List<FluidStack> inputFluids, @Nullable List<ItemStack> outputItems, @Nullable List<Float> outputItemChances, @Nullable List<FluidStack> outputFluids) {

        super(recipeId, energy, experience, minPower, maxPower, inputItems, inputFluids, outputItems, outputItemChances, outputFluids);
    }

    @Nonnull
    @Override
    public IRecipeSerializer<?> getSerializer() {

        return RECIPE_SERIALIZERS.get(ID_RECIPE_SAWMILL);
    }

    @Nonnull
    @Override
    public IRecipeType<?> getType() {

        return TExpRecipeTypes.RECIPE_SAWMILL;
    }

}
