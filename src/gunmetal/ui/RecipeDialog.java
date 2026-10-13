package gunmetal.ui;

import arc.Core;
import arc.graphics.g2d.*;
import arc.input.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import gunmetal.world.blocks.production.*;
import mindustry.gen.*;
import mindustry.graphics.*;
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
        Recipe activeRecipe = build.activeRecipe;

        // Clear
        cont.clearChildren();
        titleTable.clear();
        buttons.clear();

        // Build
        float divx = Core.graphics.getWidth() / 6f;
        float divy = Core.graphics.getHeight() / 6f;

        cont.center();

        Table window = cont.table(bg -> {

            bg.background(Styles.black6);
            bg.margin(20f);

            bg.table(t -> {
                t.right();
                t.button(Icon.cancel, Styles.emptyi, this::hide).size(40f).right();
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

                // Cool crafting UI
                // Cache items so we don't have to run through the recipe everytime.
                // Store them as tables so we can just call its drawer in the main table
                Table[] ingredients = new Table[activeRecipe.consumeItems.length + activeRecipe.consumeLiquids.length];
                Table[] products = new Table[activeRecipe.outputItems.length + activeRecipe.outputLiquids.length];

                // Ingredients
                int i = 0;
                for(ItemStack stack : activeRecipe.consumeItems){
                    Table card = new Table(c -> {
                        c.background(Tex.button);
                        c.margin(10f);
                        c.center();

                        c.image(stack.item.fullIcon).size(40f).padBottom(10f);
                        c.row();
                        c.label(() -> build.items.get(stack.item) + " / " + stack.amount);
                    });

                    ingredients[i] = card;
                    i += 1;
                }

                i = 0;
                for(LiquidStack stack : activeRecipe.consumeLiquids){
                    Table card = new Table(c -> {
                        c.background(Tex.button);
                        c.margin(10f);
                        c.center();

                        c.image(stack.liquid.fullIcon).size(40f).padBottom(10f);
                        c.row();
                        c.label(() -> build.liquids.get(stack.liquid) + " / " + stack.amount);
                    });

                    ingredients[i] = card;
                    i += 1;
                }

                // Products
                i = 0;
                for(ItemStack stack : activeRecipe.outputItems){
                    Table card = new Table(c -> {
                        c.background(Tex.button);
                        c.margin(10f);
                        c.center();

                        c.image(stack.item.fullIcon).size(40f).padBottom(10f);
                        c.row();
                        c.label(() -> "" + build.items.get(stack.item));
                    });

                    products[i] = card;
                    i += 1;
                }

                i = 0;
                for(LiquidStack stack : activeRecipe.outputLiquids){
                    Table card = new Table(c -> {
                        c.background(Tex.button);
                        c.margin(10f);
                        c.center();

                        c.image(stack.liquid.fullIcon).size(40f).padBottom(10f);
                        c.row();
                        c.label(() -> "" + build.liquids.get(stack.liquid));
                    });

                    products[i] = card;
                    i += 1;
                }

                Table coolTable = new Table(c -> {
                    c.background(Styles.black);

                    c.update(() -> {
                        for(Table card : ingredients){
                            if(card != null) card.act(Time.delta);
                        }

                        for(Table card : products){
                            if(card != null) card.act(Time.delta);
                        }
                    });
                }){
                    @Override
                    public void draw() {
                        float w = this.width;
                        float h = this.height;

                        Tmp.m4.set(Draw.trans());
                        Draw.trans(this.computeTransform());

                        this.drawBackground(0, 0);

                        // TODO: Block crafting
                        build.drawCrafting(w / 2f, h / 2f);

                        // Block inventory
                        float margin = 75f;
                        float rowHeight = this.height - (margin * 2);
                        float ingredientRowSpacing = rowHeight / ingredients.length;

                        // Ingredient Cards
                        for(int i = 0; i < ingredients.length; i++){
                            Table ingredient = ingredients[i];
                            float x = margin;
                            float y = margin + (i * ingredientRowSpacing) + (ingredientRowSpacing / 2);

                            ingredient.setSize(75f);
                            ingredient.setPosition(x - ingredient.getWidth() / 2f, y - ingredient.getHeight() / 2f);
                            ingredient.draw();

                            Draw.rect(Icon.rightOpen.getRegion(), x - 55f, y, 13f, 20f);
                        }

                        float productRowSpacing = rowHeight / products.length;

                        // Product Cards
                        for(int i = 0; i < products.length; i++){
                            Table product = products[i];
                            float x = this.getWidth() - margin;
                            float y = margin + (i * ingredientRowSpacing) + (productRowSpacing / 2);

                            product.setSize(75f);
                            product.setPosition(x - product.getWidth() / 2f, y - product.getHeight() / 2f);
                            product.draw();

                            Draw.rect(Icon.rightOpen.getRegion(), x + 55f, y, 13f, 20f);
                        }

                        // Progress bar
                        float barMargin = 20f;
                        float barHeight = 15f;
                        float craftProgress = build.craftingTime / activeRecipe.craftTime;
                        float x2 = this.getWidth() - barMargin;

                        Draw.color(Pal.gray);
                        Fill.quad(barMargin, barMargin, x2, barMargin, x2, barMargin + barHeight, barMargin, barMargin + barHeight);

                        Draw.color(Pal.accent);
                        Fill.quad(barMargin, barMargin, (x2 - barMargin) * craftProgress + barMargin, barMargin, (x2 - barMargin) * craftProgress + barMargin, barMargin + barHeight , barMargin, barMargin + barHeight);

                        Draw.trans(Tmp.m4);
                    }
                };

                t.add(coolTable).grow();

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
        }).center().get();
        cont.pack();

        // Close when clicking outside of window
        cont.touchable(() -> Touchable.enabled);
        cont.addListener(new ClickListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
                if(!((x >= window.x && x <= window.x + window.getWidth()) && (y >= window.y && y <= window.y + window.getHeight()))){
                    hide();
                };
                return super.touchDown(event, x, y, pointer, button);
            }
        });

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
