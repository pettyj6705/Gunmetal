package gunmetal.world.blocks.production;

import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import gunmetal.Gunmetal;
import gunmetal.ui.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.blocks.production.*;

// TODO: Handle these later
//    - Improve liquid handling
//    - I/O Ports
//    - Animations!!
//    - Multiplayer sync
//    - Building data saving
public class RecipeCrafter extends GenericCrafter {
    public Seq<Recipe> recipes = new Seq<>();

    public RecipeCrafter(String name){
        super(name);

        hasItems = true;
        hasLiquids = true;
        hasPower = true;
        itemCapacity = 50;

        configurable = true;
        logicConfigurable = false;
        consumesTap = true;
    }

    public void setRecipes(Recipe... recipes){
        for(Recipe recipe : recipes){
            this.recipes.add(recipe);
        }
    }

    // Naive(?) implementation so far
    public class RecipeCrafterBuild extends GenericCrafterBuild {
        public boolean crafting = false;
        public boolean clogged = false;
        public Recipe activeRecipe = recipes.get(0);

        public float craftingTime = 0;

        @Override
        public void updateTile() {
            if(activeRecipe == null || efficiency <= 0) return;

            if(!crafting){
                // Check for recipe ingredients
                boolean enoughItems = true;
                for(ItemStack stack : activeRecipe.consumeItems){
                    enoughItems = enoughItems && items.get(stack.item) >= stack.amount;
                }

                boolean enoughLiquids = true;
                for(LiquidStack stack : activeRecipe.consumeLiquids){
                    enoughLiquids = enoughItems && liquids.get(stack.liquid) >= stack.amount;
                }

                // Enter crafting state if ingredients are complete
                if(enoughItems && enoughLiquids){
                    consume();
                    crafting = true;
                }
            }else{
                // progress crafting
                if(!clogged) craftingTime += Time.delta / 60;

                Log.info(craftingTime);

                // Output products and exit crafting state
                if(craftingTime >= activeRecipe.craftTime){
                    // Check for clogging
                    boolean itemsClogged = false;
                    for(ItemStack stack : activeRecipe.outputItems){
                        itemsClogged = itemsClogged || items.get(stack.item) + stack.amount > block.itemCapacity;
                    }

                    boolean liquidsClogged = false;
                    for(LiquidStack stack : activeRecipe.outputLiquids){
                        liquidsClogged = liquidsClogged || liquids.get(stack.liquid) + stack.amount > block.liquidCapacity;
                    }

                    clogged = itemsClogged || liquidsClogged;

                    // Output items if not clogged
                    if(!clogged) {
                        craftingTime = 0;
                        crafting = false;

                        for (LiquidStack stack : activeRecipe.outputLiquids) {
                            liquids.add(stack.liquid, stack.amount);
                        }

                        for (ItemStack stack : activeRecipe.outputItems) {
                            items.add(stack.item, stack.amount);
                        }
                    }
                }
            }

            // Continuously output products
            for (LiquidStack stack : activeRecipe.outputLiquids) {
                dumpLiquid(stack.liquid);
            }

            for (ItemStack stack : activeRecipe.outputItems) {
                dump(stack.item);
            }
        }

        @Override
        public void consume() {
            for(ItemStack stack : activeRecipe.consumeItems){
                items.remove(stack.item, stack.amount);
            }

            for(LiquidStack stack : activeRecipe.consumeLiquids){
                liquids.remove(stack.liquid, stack.amount);
            }
        }

        @Override
        public void buildConfiguration(Table table) {
            // TODO: Make proper configuration UI.
            // Show block inventory, current recipe, & recipe selection button
            super.buildConfiguration(table);
            table.button("Recipe", () -> Gunmetal.recipeDialog.show(this));
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            // TODO: I/O Port mechanics
            return activeRecipe.itemFilter[item.id] && items.get(item) < getMaximumAccepted(item);
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            // TODO: I/O Port mechanics
            return activeRecipe.liquidFilter[liquid.id];
        }
    }
}
