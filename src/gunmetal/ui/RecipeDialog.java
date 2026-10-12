package gunmetal.ui;

import arc.Core;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import gunmetal.world.blocks.production.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;

public class RecipeDialog extends BaseDialog {
    public RecipeConfirmDialog confirmDialog;

    public RecipeDialog() {
        super("Recipe Dialog", GunmetalStyles.transDialog);

        confirmDialog = new RecipeConfirmDialog();
    }

    public Dialog show(RecipeCrafter.RecipeCrafterBuild build) {
        // Clear
        cont.clearChildren();
        titleTable.clear();
        buttons.clear();

        // Build
        float divx = Core.graphics.getWidth() / 6f;
        float divy = Core.graphics.getHeight() / 6f;

        cont.center();

        cont.table(bg -> {

            bg.background(Styles.black8);
            bg.margin(20f);

            bg.table(t -> {
                t.left();
                t.button("Exit", this::hide).left();
            }).colspan(2).height(50f).growX().padBottom(10f);

            bg.row();

            // Active recipe
            bg.table(t -> {
                t.background(Styles.grayPanel);
                t.margin(10f);

                t.table(h -> {
                    h.left();
                    h.image(Icon.box).size(20f).padRight(10f).left();
                    h.label(() -> "Active Recipe").left();
                }).height(25f).growX().padBottom(10f).left();

                t.row();

                // TODO: Make crafting progress & block inventory UI
                t.table(Styles.black).grow();

                t.row();

                t.table(c -> {
                    c.background(Tex.button);

                    buildRecipeInfo(build.activeRecipe, c);
                }).height(170f).growX().bottom().left().padTop(10f);
            }).size(divx * 2, divy * 5).center().padRight(10f);

            // Available recipes
            bg.table(t -> {
                t.background(Styles.grayPanel);
                t.margin(10f);

                t.table(h -> {
                    h.left();
                    h.image(Icon.box).size(20f).padRight(10f).left();
                    h.label(() -> "Available Recipes").left();
                }).height(25f).growX().padBottom(10f).left();

                t.row();

                t.pane(Styles.defaultPane, p -> {
                    for(Recipe recipe : ((RecipeCrafter) build.block).recipes) {
                        p.table(c -> {
                            c.background((recipe.name != build.activeRecipe.name) ? Tex.button : Tex.buttonOver);
                            c.touchable = (recipe.name == build.activeRecipe.name) ? Touchable.disabled : Touchable.enabled;

                            c.clicked(() -> {
                                confirmDialog.show(build, recipe);
                                hide();
                            });

                            buildRecipeInfo(recipe, c);
                        }).height(170f).growX().top().left();
                        p.row();
                    }

                }).grow();
            }).size(divx * 2, divy * 5).center().padLeft(10f);
        }).center();
        cont.pack();
        return show();
    }

    public static void buildRecipeInfo(Recipe recipe, Table t){
        float iconSize = 40f;

        t.margin(10f);

        t.top().left();

        // Recipe name header
        t.table(h -> {
            h.margin(5f);
            h.background(Styles.grayPanel);
            h.left();
            h.image(Icon.box).size(20f).padRight(10f).left();
            h.label(() -> recipe.name).left();
        }).height(30f).growX().padBottom(10f).left().top();

        t.row();

        // Recipe ingredients and products
        Table ingredients = new Table(i -> {
            i.left();
            i.top();

            for(ItemStack stack : recipe.consumeItems){
                i.table(c -> {
                    c.background(Styles.grayPanel);
                    c.image(stack.item.fullIcon).size(iconSize).padBottom(5f);
                    c.row();
                    c.label(() -> "" + stack.amount);
                }).width(80f).growY().padRight(10f);
            }

            for(LiquidStack stack : recipe.consumeLiquids){
                i.table(c -> {
                    c.background(Styles.grayPanel);
                    c.image(stack.liquid.fullIcon).size(iconSize).padBottom(5f);
                    c.row();
                    c.label(() -> "" + stack.amount);
                }).width(80f).growY().padRight(10f);
            }

            i.setFillParent(true);
        });

        Table products = new Table(i -> {
            i.right();
            i.top();

            for(ItemStack stack : recipe.outputItems){
                i.table(c -> {
                    c.background(Styles.grayPanel);
                    c.image(stack.item.fullIcon).size(iconSize).padBottom(5f);
                    c.row();
                    c.label(() -> "" + stack.amount);
                }).width(80f).growY().padLeft(10f);
            }

            for(LiquidStack stack : recipe.outputLiquids){
                i.table(c -> {
                    c.background(Styles.grayPanel);
                    c.image(stack.liquid.fullIcon).size(iconSize).padBottom(5f);
                    c.row();
                    c.label(() -> "" + stack.amount);
                }).width(80f).growY().padLeft(10f);
            }

            i.setFillParent(true);
        });

        // Craft time
        Table arrow = new Table(i -> {
            i.center();
            i.label(() -> recipe.craftTime + "s");
            i.row();
            i.image(Icon.right).size(iconSize).center();

            i.setFillParent(true);
        });

        // Final cards stack
        t.table(c -> c.stack(ingredients, products, arrow).grow()).grow();
    }
}
