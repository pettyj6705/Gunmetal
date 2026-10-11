package gunmetal.world.blocks.production;

import mindustry.type.*;

import static mindustry.Vars.*;

public class Recipe {
    public boolean[] itemFilter;
    public boolean[] liquidFilter;

    public ItemStack[] consumeItems;
    public LiquidStack[] consumeLiquids;

    public ItemStack[] outputItems;
    public LiquidStack[] outputLiquids;

    // CraftTime in seconds
    public float craftTime;

    public Recipe(float craftTime, ItemStack[] consumeItems, LiquidStack[] consumeLiquids, ItemStack[] outputItems, LiquidStack[] outputLiquids){
        this.consumeItems = consumeItems;
        this.consumeLiquids = consumeLiquids;

        this.outputItems = outputItems;
        this.outputLiquids = outputLiquids;

        this.craftTime = craftTime;

        itemFilter = new boolean[content.items().size];
        liquidFilter = new boolean[content.liquids().size];
        for(ItemStack stack : consumeItems){
            itemFilter[stack.item.id] = true;
        }

        for(LiquidStack stack : consumeLiquids){
            liquidFilter[stack.liquid.id] = true;
        }
    }

    public Recipe(float craftTime, ItemStack[] consumeItems, ItemStack[] outputItems){
        this(craftTime, consumeItems, new LiquidStack[]{}, outputItems, new LiquidStack[]{});
    }
}
