package cofh.thermal.expansion.plugins.crt.base;

import cofh.thermal.core.util.recipes.ThermalRecipe;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.impl.item.MCWeightedItemStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CRTRecipe {

    private final ResourceLocation name;

    private List<Ingredient> inputItems;
    private List<FluidStack> inputFluids;

    private List<ItemStack> outputItems;
    private List<FluidStack> outputFluids;
    private List<Float> outputItemChances;

    private int energy = 0;
    private float experience = 0;
    private int minPower = -1;
    private int maxPower = -1;

    public CRTRecipe(ResourceLocation name) {

        this.name = name;
    }

    public CRTRecipe input(IIngredient... ingredient) {

        this.inputItems = Arrays.stream(ingredient).map(IIngredient::asVanillaIngredient).collect(Collectors.toList());
        return this;
    }

    public CRTRecipe input(FluidStack... ingredient) {

        this.inputFluids = Arrays.stream(ingredient).collect(Collectors.toList());
        return this;
    }

    public CRTRecipe input(IFluidStack... ingredient) {

        this.inputFluids = Arrays.stream(ingredient).map(IFluidStack::getInternal).collect(Collectors.toList());
        return this;
    }

    public CRTRecipe output(MCWeightedItemStack... stack) {

        this.outputItems = Arrays.stream(stack).filter(weightedStack -> !weightedStack.getItemStack().isEmpty()).map(weightedStack -> weightedStack.getItemStack().getInternal()).collect(Collectors.toList());
        this.outputItemChances = Arrays.stream(stack).filter(weightedStack -> !weightedStack.getItemStack().isEmpty()).map(weightedStack -> (float) weightedStack.getWeight()).collect(Collectors.toList());
        return this;
    }

    public CRTRecipe output(IItemStack... stack) {

        this.outputItems = Arrays.stream(stack).filter(iItemStack -> !iItemStack.isEmpty()).map(IItemStack::getInternal).collect(Collectors.toList());
        this.outputItemChances = Arrays.stream(stack).filter(iItemStack -> !iItemStack.isEmpty()).map(iItemStack -> {
            if (iItemStack instanceof MCWeightedItemStack) {
                return (float) ((MCWeightedItemStack) iItemStack).getWeight();
            }
            return 1.0f;
        }).collect(Collectors.toList());
        return this;
    }

    public CRTRecipe output(FluidStack... stack) {

        List<FluidStack> newList = Arrays.stream(stack).filter(fluidStack -> !fluidStack.isEmpty()).collect(Collectors.toList());
        // Not sure if an empty list vs a null list makes a difference, but if there are no entries I'm just not going to assign it
        if (!newList.isEmpty()) {
            this.outputFluids = newList;
        }
        return this;
    }

    public CRTRecipe output(IFluidStack... stack) {

        List<FluidStack> newList = Arrays.stream(stack).map(IFluidStack::getInternal).filter(fluidStack -> !fluidStack.isEmpty()).collect(Collectors.toList());
        // Not sure if an empty list vs a null list makes a difference, but if there are no entries I'm just not going to assign it
        if (!newList.isEmpty()) {
            this.outputFluids = newList;
        }
        return this;
    }

    public CRTRecipe energy(int energy) {

        this.energy = energy;
        return this;
    }

    public CRTRecipe experience(float experience) {

        this.experience = experience;
        return this;
    }

    public CRTRecipe minPower(int minPower) {

        this.minPower = minPower;
        return this;
    }

    public CRTRecipe maxPower(int maxPower) {

        this.maxPower = maxPower;
        return this;
    }

    public <T extends ThermalRecipe> T recipe(IRecipeBuilder<T> builder) {

        return builder.apply(name, energy, experience, maxPower, inputItems, inputFluids, outputItems, outputItemChances, outputFluids);
    }

    public interface IRecipeBuilder<T extends ThermalRecipe> {

        T apply(ResourceLocation recipeId, int energy, float experience, int maxPower, @Nullable List<Ingredient> inputItems, @Nullable List<FluidStack> inputFluids, @Nullable List<ItemStack> outputItems, @Nullable List<Float> outputItemChances, @Nullable List<FluidStack> outputFluids);

    }

}
