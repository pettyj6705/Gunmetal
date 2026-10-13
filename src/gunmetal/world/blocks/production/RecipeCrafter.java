package gunmetal.world.blocks.production;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import gunmetal.*;
import gunmetal.ui.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.blocks.production.*;

// TODO: Handle these later
//    - Improve liquid handling
//    - Animations!!
//    - Multiplayer sync
//    - Building data saving
//    - Power mechanics
public class RecipeCrafter extends GenericCrafter {
    public Seq<Recipe> recipes = new Seq<>();
    public Seq<Ports.Port> ports = new Seq<>();

    public RecipeCrafter(String name){
        super(name);

        hasItems = true;
        hasLiquids = true;
        hasPower = true;
        itemCapacity = 50;

        configurable = true;
        logicConfigurable = false;
        consumesTap = true;

        rotate = true;
    }

    public void setRecipes(Recipe... recipes){
        for(Recipe recipe : recipes){
            this.recipes.add(recipe);
        }
    }

    public void drawCrafting(float x, float y, RecipeCrafterBuild b){

    }

    public void addOutputPort(float x, float y){
        ports.add(new Ports.Port(Ports.PortType.Output, new Vec2(x, y)));
    }

    public void addInputPort(float x, float y){
        ports.add(new Ports.Port(Ports.PortType.Input, new Vec2(x, y)));
    }

    // Naive(?) implementation so far
    public class RecipeCrafterBuild extends GenericCrafterBuild {
        public Ports ports;

        public boolean crafting = false;
        public boolean clogged = false;
        public Recipe activeRecipe = recipes.get(0);

        public float craftingTime = 0;

        @Override
        public Building init(Tile tile, Team team, boolean shouldAdd, int rotation) {
            Building b = super.init(tile, team, shouldAdd, rotation);
            this.ports = new Ports(((RecipeCrafter) this.block).ports);
            return b;
        }

        public void switchRecipe(Recipe recipe){
            items.clear();
            activeRecipe = recipe;
            clogged = false;
            crafting = false;
            craftingTime = 0;
        }

        // Used inside RecipeDialog's inventory display
        public void drawCrafting(float x, float y){
            ((RecipeCrafter) block).drawCrafting(x, y, this);
        }

        // TODO: proper drawers
        @Override
        public void draw() {
            Draw.color(Pal.darkishGray);
            Fill.square(x, y, (block.size / 2f) * Vars.tilesize);

            Draw.color(Pal.accent);
            Log.info(rotation);
            Tmp.v2.set(0.5f * Vars.tilesize, 0).rotate(90 * rotation);
            Drawf.arrow(x, y, x + Tmp.v2.x, y + Tmp.v2.y, 5, 2);
        }

        // TODO: Draw ports
        @Override
        public void drawConfigure() {
            super.drawConfigure();
        }

        // TODO: Split this up into different methods.
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

                // Log.info(craftingTime);

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
                Liquid liquid = stack.liquid;
                int dump = cdump;

                for(int i = 0; i < this.proximity.size; ++i) {
                    this.incrementDump(this.proximity.size);
                    Building other = this.proximity.get((i + dump) % this.proximity.size);

                    other = other.getLiquidDestination(this, liquid);

                    // Only output to pipes
                    if (other instanceof Conduit.ConduitBuild && liquids.get(liquid) > 0 && other.block.hasLiquids && this.canDumpLiquid(other, liquid) && other.liquids != null && ports.findPorts(Ports.PortType.Output, this, other) != null) {
                        float ofract = other.liquids.get(liquid) / other.block.liquidCapacity;
                        float fract = this.liquids.get(liquid) / this.block.liquidCapacity;
                        if (ofract < fract) {
                            this.transferLiquid(other, (fract - ofract) * this.block.liquidCapacity / 2.0F, liquid);
                        }
                    }
                }
            }

            for (ItemStack stack : activeRecipe.outputItems) {
                Item todump = stack.item;
                int dump = cdump;

                if(items.get(todump) <= 0) continue;

                for(int i = 0; i < this.proximity.size; ++i) {
                    Building other = this.proximity.get((i + dump) % this.proximity.size);

                    // Only output to conveyors
                    if (other instanceof Conveyor.ConveyorBuild && items.get(todump) > 0 && other.acceptItem(this, todump) && this.canDump(other, todump) && ports.findPorts(Ports.PortType.Output, this, other) != null) {
                        other.handleItem(this, todump);
                        this.items.remove(todump, 1);
                        this.incrementDump(this.proximity.size);
                    }

                    this.incrementDump(this.proximity.size);
                }
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
            super.buildConfiguration(table);
            table.table(t -> {
                t.background(Tex.inventory);


                t.table(c -> {
                    c.defaults().size(40f).top().left();
                    c.top().left();

                    int i = 0;
                    for(ItemStack stack : activeRecipe.consumeItems){
                        c.add(GunmetalStyles.itemImage(stack.item.uiIcon, () -> GunmetalStyles.round(items.get(stack.item))));
                        i += 1;

                        if(i > 3){
                            c.row();
                            i = 0;
                        }
                    }

                    for(LiquidStack stack : activeRecipe.consumeLiquids){
                        c.add(GunmetalStyles.itemImage(stack.liquid.uiIcon, () -> GunmetalStyles.round(liquids.get(stack.liquid))));
                        i += 1;

                        if(i > 3){
                            c.row();
                            i = 0;
                        }
                    }
                }).width(120f).top().left();

                t.row();

                t.table(c -> {
                    c.image(Icon.down).size(20f).center();
                }).height(40f).width(120f).top().left();

                t.row();

                t.table(c -> {
                    c.defaults().size(40f).top().left();
                    c.top().left();

                    int i = 0;
                    for(ItemStack stack : activeRecipe.outputItems){
                        c.add(GunmetalStyles.itemImage(stack.item.uiIcon, () -> GunmetalStyles.round(items.get(stack.item))));
                        i += 1;

                        if(i > 3){
                            c.row();
                            i = 0;
                        }
                    }

                    for(LiquidStack stack : activeRecipe.outputLiquids){
                        c.add(GunmetalStyles.itemImage(stack.liquid.uiIcon, () -> GunmetalStyles.round(liquids.get(stack.liquid))));
                        i += 1;

                        if(i > 3){
                            c.row();
                            i = 0;
                        }
                    }
                }).width(120f).top().left();
            });
        }

        @Override
        public boolean onConfigureBuildTapped(Building other) {
            if(other == this){
                Gunmetal.recipeDialog.show(this);
            }

            return other != this;
        }

        @Override
        public void updateTableAlign(Table table) {
            Vec2 pos = Core.input.mouseScreen(x + block.size * Vars.tilesize / 2f, y + block.size * Vars.tilesize / 2f);
            table.setPosition(pos.x - Core.scene.marginLeft, pos.y - Core.scene.marginBottom, Align.topLeft);
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            // Only accept items from conveyors
            return source instanceof Conveyor.ConveyorBuild && activeRecipe.itemFilter[item.id] && items.get(item) < getMaximumAccepted(item) && ports.findPorts(Ports.PortType.Input, this, source) != null;
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            // Only accept liquids from pipes
            return source instanceof Conduit.ConduitBuild && activeRecipe.liquidFilter[liquid.id] && ports.findPorts(Ports.PortType.Input, this, source) != null;
        }
    }
}
