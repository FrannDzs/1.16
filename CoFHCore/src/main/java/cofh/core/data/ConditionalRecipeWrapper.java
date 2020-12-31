package cofh.core.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ConditionalRecipeWrapper implements IFinishedRecipe {

    protected IFinishedRecipe recipe;
    protected List<ICondition> conditions = new ArrayList<>();

    public ConditionalRecipeWrapper(IFinishedRecipe recipe) {

        this.recipe = recipe;
    }

    public ConditionalRecipeWrapper condition(ICondition condition) {

        conditions.add(condition);
        return this;
    }

    @Override
    public void serialize(JsonObject json) {

        recipe.serialize(json);
    }

    @Override
    public JsonObject getRecipeJson() {

        JsonObject jsonobject = new JsonObject();
        jsonobject.addProperty("type", Registry.RECIPE_SERIALIZER.getKey(this.getSerializer()).toString());
        this.serialize(jsonobject);
        if (!conditions.isEmpty()) {
            JsonArray conditionArray = new JsonArray();
            for (ICondition condition : conditions) {
                conditionArray.add(CraftingHelper.serialize(condition));
            }
            jsonobject.add("conditions", conditionArray);

            //jsonobject.add("conditions", new JsonArray())
            //jsonobject.add("conditions", CraftingHelper.serialize(condition));
        }
        return jsonobject;
    }

    @Override
    public ResourceLocation getID() {

        return recipe.getID();
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {

        return recipe.getSerializer();
    }

    @Nullable
    @Override
    public JsonObject getAdvancementJson() {

        return recipe.getAdvancementJson();
    }

    @Nullable
    @Override
    public ResourceLocation getAdvancementID() {

        return recipe.getAdvancementID();
    }

}
