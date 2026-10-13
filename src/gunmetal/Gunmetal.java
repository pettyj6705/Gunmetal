package gunmetal;

import arc.Events;
import gunmetal.ui.*;
import gunmetal.world.blocks.power.*;
import gunmetal.world.blocks.production.*;
import mindustry.content.*;
import mindustry.game.*;
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
        new PowerTower("test-power-tower"){{
            size = 1;
            laserRange = 16;
            category = Category.power;
            buildVisibility = BuildVisibility.shown;

            maxNodes = 4;
        }};

        new PowerPylon("test-power-pylon"){{
            size = 1;
            laserRange = 4;
            category = Category.power;
            buildVisibility = BuildVisibility.shown;

            maxNodes = 8;
        }};

        new RecipeCrafter("test-recipe-crafter"){{
            size = 3;
            buildVisibility = BuildVisibility.shown;
            category = Category.crafting;

            addInputPort(-2, 1);
            addInputPort(-2, 0);
            addInputPort(-2, -1);

            addOutputPort(2, 1);
            addOutputPort(2, 0);
            addOutputPort(2, -1);
        }}.setRecipes(
                new Recipe("silicon", 5,
                        new ItemStack[]{new ItemStack(Items.coal, 1), new ItemStack(Items.sand, 5)},
                        new ItemStack[]{new ItemStack(Items.silicon, 5)}
                ),
                new Recipe("metaglass", 5,
                        new ItemStack[]{new ItemStack(Items.lead, 5), new ItemStack(Items.sand, 5)},
                        new ItemStack[]{new ItemStack(Items.metaglass, 5)}
                )
        );

        // UI initialization
        Events.on(EventType.ClientLoadEvent.class, e -> {
            GunmetalStyles.load();

            recipeDialog = new RecipeDialog();
        });
    }
}
