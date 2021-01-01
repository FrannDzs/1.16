package cofh.thermal.core.util.recipes.device;

import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;

import static cofh.core.util.RecipeJsonUtils.*;

public class RockGenMappingSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<RockGenMapping> {

    @Override
    public RockGenMapping read(ResourceLocation recipeId, JsonObject json) {

        Block below = Blocks.AIR;
        Block adjacent = Blocks.AIR;
        ItemStack item = ItemStack.EMPTY;

        if (json.has(BELOW)) {
            below = parseBlock(json.get(BELOW));
        } else if (json.has(BASE)) {
            below = parseBlock(json.get(BASE));
        }
        if (json.has(ADJACENT)) {
            adjacent = parseBlock(json.get(ADJACENT));
        }
        if (json.has(RESULT)) {
            item = parseItemStack(json.get(RESULT));
        } else if (json.has(ITEM)) {
            item = parseItemStack(json.get(ITEM));
        }
        return new RockGenMapping(recipeId, below, adjacent, item);
    }

    @Nullable
    @Override
    public RockGenMapping read(ResourceLocation recipeId, PacketBuffer buffer) {

        Block trunk = ForgeRegistries.BLOCKS.getValue(buffer.readResourceLocation());
        Block leaves = ForgeRegistries.BLOCKS.getValue(buffer.readResourceLocation());
        ItemStack item = buffer.readItemStack();

        return new RockGenMapping(recipeId, trunk, leaves, item);
    }

    @Override
    public void write(PacketBuffer buffer, RockGenMapping recipe) {

        buffer.writeResourceLocation(recipe.below.getRegistryName());
        buffer.writeResourceLocation(recipe.adjacent.getRegistryName());
        buffer.writeItemStack(recipe.item);
    }

}
