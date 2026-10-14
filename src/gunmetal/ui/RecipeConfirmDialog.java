package gunmetal.ui;

import arc.Core;
import arc.scene.ui.*;
import gunmetal.*;
import gunmetal.world.blocks.production.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;

// Should this be under RecipeDialog?
public class RecipeConfirmDialog extends BaseDialog {
    public RecipeConfirmDialog() {
        super("Confirm Recipe");
    }

    public Dialog show(RecipeCrafter.RecipeCrafterBuild build, Recipe recipe){
        // Clear
        cont.clearChildren();
        titleTable.clear();
        buttons.clear();

        // Build
        float divx = Core.graphics.getWidth() / 6f;
        float divy = Core.graphics.getHeight() / 6f;

        // Recipe change confirmation
        cont.table(t -> {
            t.background(Styles.grayPanel);
            t.margin(25f);

            t.label(() -> "Change recipes? This will discard all items inside the building.").left();

            t.row();

            t.table(c -> {
                c.background(Tex.button);

                RecipeDialog.buildRecipeInfo(build.activeRecipe, c);
            }).height(170f).growX().left().padTop(10f);

            t.row();

            t.image(Icon.down).size(40f).padTop(10f);

            t.row();

            t.table(c -> {
                c.background(Tex.button);

                RecipeDialog.buildRecipeInfo(recipe, c);
            }).height(170f).growX().left().padTop(10f);

            t.row();

            t.table(b -> {
                b.button("Switch Recipe", () -> {
                    build.switchRecipe(recipe, true);
                    hide();
                    Gunmetal.recipeDialog.show(build);
                }).grow().padRight(5f);
                b.button("Cancel", () -> {
                    hide();
                    Gunmetal.recipeDialog.show(build);
                }).grow().padLeft(5f);
            }).height(70f).growX().left().padTop(10f);

        }).width((divx * 2) + 50f).center().padRight(10f);

        return show();
    }
}
