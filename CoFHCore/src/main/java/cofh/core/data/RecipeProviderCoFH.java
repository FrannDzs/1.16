package cofh.core.data;

import cofh.core.CoFHCore;
import cofh.core.init.CoreFlags;
import cofh.core.registries.DeferredRegisterCoFH;
import cofh.core.util.FlagManager;
import cofh.core.util.FlagRecipeCondition;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.advancements.criterion.*;
import net.minecraft.block.Block;
import net.minecraft.data.*;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.CompoundIngredient;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static cofh.core.util.constants.Constants.ID_FORGE;

public class RecipeProviderCoFH extends RecipeProvider implements IConditionBuilder {

    protected final String modid;
    protected FlagManager manager = CoreFlags.manager();

    protected Map<ResourceLocation, ICondition> recipeConditions = new Object2ObjectOpenHashMap<>();

    public RecipeProviderCoFH(DataGenerator generatorIn, String modid) {

        super(generatorIn);
        this.modid = modid;
    }

    @Override
    public void act(DirectoryCache cache) {

        Path path = this.generator.getOutputFolder();
        Set<ResourceLocation> set = Sets.newHashSet();
        registerRecipes((recipe) -> {
            if (!set.add(recipe.getID())) {
                CoFHCore.LOG.error("Duplicate recipe " + recipe.getID());
            } else {
                saveRecipe(cache, createRecipeJson(recipe), path.resolve("data/" + recipe.getID().getNamespace() + "/recipes/" + recipe.getID().getPath() + ".json"));
            }
            // We do not generate advancements - they add a LOT of time to server connection.

            // JsonObject jsonobject = recipe.getAdvancementJson();
            // if (jsonobject != null) {
            //     saveRecipeAdvancement(cache, jsonobject, path.resolve("data/" + recipe.getID().getNamespace() + "/advancements/" + recipe.getAdvancementID().getPath() + ".json"));
            // }
        });
    }

    protected JsonObject createRecipeJson(IFinishedRecipe recipe) {

        ResourceLocation recipeId = recipe.getID();
        if (recipeConditions.containsKey(recipeId)) {
            return new ConditionalRecipeWrapper(recipe).condition(recipeConditions.get(recipeId)).getRecipeJson();
        }
        return recipe.getRecipeJson();
    }

    @SafeVarargs
    protected final Ingredient fromTags(ITag.INamedTag<Item>... tagsIn) {

        List<Ingredient> ingredients = new ArrayList<>(tagsIn.length);
        for (ITag.INamedTag<Item> tag : tagsIn) {
            ingredients.add(Ingredient.fromTag(tag));
        }
        return new CompoundIngredientCoFH(ingredients);
    }

    // region HELPERS
    protected void generatePackingRecipe(Consumer<IFinishedRecipe> consumer, Item storage, Item individual, String suffix) {

        String storageName = name(storage);
        String individualName = name(individual);

        ShapedRecipeBuilder.shapedRecipe(storage)
                .key('#', individual)
                .patternLine("###")
                .patternLine("###")
                .patternLine("###")
                .addCriterion("has_at_least_9_" + individualName, hasItem(MinMaxBounds.IntBound.atLeast(9), individual))
                .build(consumer, this.modid + ":storage/" + storageName + suffix);
    }

    protected void generatePackingRecipe(Consumer<IFinishedRecipe> consumer, Item storage, Item individual, ITag.INamedTag<Item> tag, String suffix) {

        String storageName = name(storage);
        String individualName = name(individual);

        ShapedRecipeBuilder.shapedRecipe(storage)
                .key('I', individual)
                .key('#', tag)
                .patternLine("###")
                .patternLine("#I#")
                .patternLine("###")
                .addCriterion("has_at_least_9_" + individualName, hasItem(MinMaxBounds.IntBound.atLeast(9), individual))
                .build(consumer, this.modid + ":storage/" + storageName + suffix);
    }

    protected void generateUnpackingRecipe(Consumer<IFinishedRecipe> consumer, Item storage, Item individual, String suffix) {

        String storageName = name(storage);
        String individualName = name(individual);

        ShapelessRecipeBuilder.shapelessRecipe(individual, 9)
                .addIngredient(storage)
                .addCriterion("has_at_least_9_" + individualName, hasItem(MinMaxBounds.IntBound.atLeast(9), individual))
                .addCriterion("has_" + storageName, hasItem(storage))
                .build(consumer, this.modid + ":storage/" + individualName + suffix);
    }

    protected void generateStorageRecipes(Consumer<IFinishedRecipe> consumer, Item storage, Item individual, String packingSuffix, String unpackingSuffix) {

        generatePackingRecipe(consumer, storage, individual, packingSuffix);
        generateUnpackingRecipe(consumer, storage, individual, unpackingSuffix);
    }

    protected void generateStorageRecipes(Consumer<IFinishedRecipe> consumer, Item storage, Item individual, ITag.INamedTag<Item> tag, String packingSuffix, String unpackingSuffix) {

        generatePackingRecipe(consumer, storage, individual, tag, packingSuffix);
        generateUnpackingRecipe(consumer, storage, individual, unpackingSuffix);
    }

    protected void generateStorageRecipes(Consumer<IFinishedRecipe> consumer, Item storage, Item individual, ITag.INamedTag<Item> tag) {

        generateStorageRecipes(consumer, storage, individual, tag, "", "_from_block");
    }

    protected void generateStorageRecipes(Consumer<IFinishedRecipe> consumer, Item storage, Item individual) {

        generateStorageRecipes(consumer, storage, individual, "", "_from_block");
    }

    protected void generateTypeRecipes(DeferredRegisterCoFH<Item> reg, Consumer<IFinishedRecipe> consumer, String type) {

        Item ingot = reg.get(type + "_ingot");
        Item gem = reg.get(type);
        Item block = reg.get(type + "_block");
        Item nugget = reg.get(type + "_nugget");

        ITag.INamedTag<Item> ingotTag = forgeTag("ingots/" + type);
        ITag.INamedTag<Item> gemTag = forgeTag("gems/" + type);
        ITag.INamedTag<Item> nuggetTag = forgeTag("nuggets/" + type);

        if (block != null) {
            if (ingot != null) {
                generateStorageRecipes(consumer, block, ingot, ingotTag, "", "_from_block");
            } else if (gem != null) {
                generateStorageRecipes(consumer, block, gem, gemTag, "", "_from_block");
            }
        }
        if (nugget != null) {
            if (ingot != null) {
                generateStorageRecipes(consumer, ingot, nugget, nuggetTag, "_from_nuggets", "_from_ingot");
            } else if (gem != null) {
                generateStorageRecipes(consumer, gem, nugget, nuggetTag, "_from_nuggets", "_from_gem");
            }
        }
        generateGearRecipe(reg, consumer, type);
    }

    protected void generateGearRecipe(DeferredRegisterCoFH<Item> reg, Consumer<IFinishedRecipe> consumer, String type) {

        Item gear = reg.get(type + "_gear");
        if (gear == null) {
            return;
        }
        Item ingot = reg.get(type + "_ingot");
        Item gem = reg.get(type);

        ITag.INamedTag<Item> ingotTag = forgeTag("ingots/" + type);
        ITag.INamedTag<Item> gemTag = forgeTag("gems/" + type);

        if (ingot != null) {
            ShapedRecipeBuilder.shapedRecipe(gear)
                    .key('#', ingotTag)
                    .key('i', Tags.Items.NUGGETS_IRON)
                    .patternLine(" # ")
                    .patternLine("#i#")
                    .patternLine(" # ")
                    .addCriterion("has_" + name(ingot), hasItem(ingotTag))
                    .build(consumer, this.modid + ":parts/" + name(gear));
        }
        if (gem != null) {
            ShapedRecipeBuilder.shapedRecipe(gear)
                    .key('#', gemTag)
                    .key('i', Tags.Items.NUGGETS_IRON)
                    .patternLine(" # ")
                    .patternLine("#i#")
                    .patternLine(" # ")
                    .addCriterion("has_" + name(gem), hasItem(gemTag))
                    .build(consumer, this.modid + ":parts/" + name(gear));
        }
    }

    protected void generateGearRecipe(Consumer<IFinishedRecipe> consumer, Item gear, Item material, ITag.INamedTag<Item> tag) {

        if (gear == null || material == null || tag == null) {
            return;
        }
        ShapedRecipeBuilder.shapedRecipe(gear)
                .key('#', tag)
                .key('i', Tags.Items.NUGGETS_IRON)
                .patternLine(" # ")
                .patternLine("#i#")
                .patternLine(" # ")
                .addCriterion("has_" + name(material), hasItem(tag))
                .build(consumer, this.modid + ":parts/" + name(gear));
    }

    protected void generateSmeltingRecipe(DeferredRegisterCoFH<Item> reg, Consumer<IFinishedRecipe> consumer, Item input, Item output, float xp) {

        generateSmeltingRecipe(reg, consumer, input, output, xp, "", "");
    }

    protected void generateSmeltingRecipe(DeferredRegisterCoFH<Item> reg, Consumer<IFinishedRecipe> consumer, Item input, Item output, float xp, String folder) {

        generateSmeltingRecipe(reg, consumer, input, output, xp, folder, "");
    }

    protected void generateSmeltingRecipe(DeferredRegisterCoFH<Item> reg, Consumer<IFinishedRecipe> consumer, Item input, Item output, float xp, String folder, String suffix) {

        CookingRecipeBuilder.smeltingRecipe(Ingredient.fromItems(input), output, xp, 200)
                .addCriterion("has_" + name(input), hasItem(input))
                .build(consumer, this.modid + ":" + folder + "/" + name(output) + "_from" + suffix + "_smelting");
    }

    protected void generateSmeltingAndBlastingRecipes(DeferredRegisterCoFH<Item> reg, Consumer<IFinishedRecipe> consumer, String material, float xp) {

        generateSmeltingAndBlastingRecipes(reg, consumer, material, xp, "smelting");
    }

    protected void generateSmeltingAndBlastingRecipes(DeferredRegisterCoFH<Item> reg, Consumer<IFinishedRecipe> consumer, String material, float xp, String folder) {

        Item ore = reg.get(material + "_ore");
        Item ingot = reg.get(material + "_ingot");
        Item gem = reg.get(material);
        Item dust = reg.get(material + "_dust");

        if (ingot != null) {
            if (dust != null) {
                generateSmeltingAndBlastingRecipes(reg, consumer, dust, ingot, 0, folder, "_dust");
            }
            if (ore != null) {
                generateSmeltingAndBlastingRecipes(reg, consumer, ore, ingot, xp, folder, "_ore");
            }
        } else if (gem != null) {
            if (ore != null) {
                generateSmeltingAndBlastingRecipes(reg, consumer, ore, gem, xp, folder, "_ore");
            }
        }
    }

    protected void generateSmeltingAndBlastingRecipes(DeferredRegisterCoFH<Item> reg, Consumer<IFinishedRecipe> consumer, Item input, Item output, float xp) {

        generateSmeltingAndBlastingRecipes(reg, consumer, input, output, xp, "", "");
    }

    protected void generateSmeltingAndBlastingRecipes(DeferredRegisterCoFH<Item> reg, Consumer<IFinishedRecipe> consumer, Item input, Item output, float xp, String folder) {

        generateSmeltingAndBlastingRecipes(reg, consumer, input, output, xp, folder, "");
    }

    protected void generateSmeltingAndBlastingRecipes(DeferredRegisterCoFH<Item> reg, Consumer<IFinishedRecipe> consumer, Item input, Item output, float xp, String folder, String suffix) {

        CookingRecipeBuilder.smeltingRecipe(Ingredient.fromItems(input), output, xp, 200)
                .addCriterion("has_" + name(input), hasItem(input))
                .build(consumer, this.modid + ":" + folder + "/" + name(output) + "_from" + suffix + "_smelting");

        CookingRecipeBuilder.blastingRecipe(Ingredient.fromItems(input), output, xp, 100)
                .addCriterion("has_" + name(input), hasItem(input))
                .build(consumer, this.modid + ":" + folder + "/" + name(output) + "_from" + suffix + "_blasting");
    }

    // TODO: Change if Mojang implements some better defaults...
    public InventoryChangeTrigger.Instance hasItem(MinMaxBounds.IntBound amount, IItemProvider itemIn) {

        return hasItem(new ItemPredicate(null, itemIn.asItem(), amount, MinMaxBounds.IntBound.UNBOUNDED, EnchantmentPredicate.enchantments, EnchantmentPredicate.enchantments, null, NBTPredicate.ANY)); // ItemPredicate.Builder.create().item(itemIn).count(amount).build());
    }

    protected static Tags.IOptionalNamedTag<Item> forgeTag(String name) {

        return ItemTags.createOptional(new ResourceLocation(ID_FORGE, name));
    }

    protected static String name(Block block) {

        return block.getRegistryName() == null ? "" : block.getRegistryName().getPath();
    }

    protected static String name(Item item) {

        return item.getRegistryName() == null ? "" : item.getRegistryName().getPath();
    }
    // endregion

    // region CONDITIONS
    protected ICondition flag(String flag) {

        return new FlagRecipeCondition(manager, flag);
    }

    protected void setRecipeFlag(IItemProvider item, String flag) {

        recipeConditions.put(item.asItem().getRegistryName(), flag(flag));
    }

    protected void setRecipeFlag(ResourceLocation recipe, String flag) {

        recipeConditions.put(recipe, flag(flag));
    }
    // endregion

    protected static class CompoundIngredientCoFH extends CompoundIngredient {

        public CompoundIngredientCoFH(List<Ingredient> children) {

            super(children);
        }

    }

}
