package gunmetal;

import arc.Events;
import gunmetal.ui.RecipeDialog;
import gunmetal.world.blocks.production.*;
import mindustry.content.*;
import mindustry.game.EventType;
import mindustry.mod.*;
import gunmetal.gen.*;
import mindustry.type.*;
import mindustry.world.meta.*;

public class Gunmetal extends Mod{
    public static RecipeDialog recipeDialog;
    @Override
    public void loadContent(){
        EntityRegistry.register();
        EntityRegistry.registerUnits();

        // Testing
        new RecipeCrafter("test-recipe-crafter"){{
            size = 3;
            buildVisibility = BuildVisibility.shown;
            category = Category.crafting;

        }}.setRecipes(
                new Recipe(5,
                        new ItemStack[]{new ItemStack(Items.coal, 1), new ItemStack(Items.sand, 5)},
                        new ItemStack[]{new ItemStack(Items.silicon, 5)}
                )
        );

        // UI initialization
        Events.on(EventType.ClientLoadEvent.class, e -> {
            recipeDialog = new RecipeDialog();
        });
    }
}
