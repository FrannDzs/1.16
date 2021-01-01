package cofh.thermal.core.util.recipes.device;

import com.google.gson.JsonObject;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;

public class RockGenMappingSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<RockGenMapping> {

    @Override
    public RockGenMapping read(ResourceLocation recipeId, JsonObject json) {

        //        Block trunk = Blocks.AIR;
        //        Block leaves = Blocks.AIR;
        //        FluidStack fluid = FluidStack.EMPTY;
        //
        //        if (json.has(TRUNK)) {
        //            trunk = parseBlock(json.get(TRUNK));
        //        }
        //
        //        if (json.has(LEAF)) {
        //            leaves = parseBlock(json.get(LEAF));
        //        } else if (json.has(LEAVES)) {
        //            leaves = parseBlock(json.get(LEAVES));
        //        }
        //
        //        if (json.has(RESULT)) {
        //            fluid = parseFluidStack(json.get(RESULT));
        //        } else if (json.has(FLUID)) {
        //            fluid = parseFluidStack(json.get(FLUID));
        //        }
        //        return new TreeExtractorMapping(recipeId, trunk, leaves, fluid);
        return null;
    }

    @Nullable
    @Override
    public RockGenMapping read(ResourceLocation recipeId, PacketBuffer buffer) {

        //        Block trunk = ForgeRegistries.BLOCKS.getValue(buffer.readResourceLocation());
        //        Block leaves = ForgeRegistries.BLOCKS.getValue(buffer.readResourceLocation());
        //        FluidStack fluid = buffer.readFluidStack();
        //
        //        return new RockGenMapping(recipeId, trunk, leaves, fluid);
        return null;
    }

    @Override
    public void write(PacketBuffer buffer, RockGenMapping recipe) {

        //        buffer.writeResourceLocation(recipe.trunk.getRegistryName());
        //        buffer.writeResourceLocation(recipe.leaves.getRegistryName());
        //        buffer.writeFluidStack(recipe.fluid);
    }

}
