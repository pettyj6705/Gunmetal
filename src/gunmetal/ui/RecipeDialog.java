package gunmetal.ui;

import arc.scene.ui.*;
import gunmetal.world.blocks.production.*;
import mindustry.ui.dialogs.*;

// TODO: Make the recipe selection UI.
public class RecipeDialog extends BaseDialog {
    public RecipeDialog() {
        super("Recipe Dialog");
    }

    public Dialog show(RecipeCrafter.RecipeCrafterBuild build) {
        return show();
    }
}
