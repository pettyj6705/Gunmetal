package gunmetal.ui;

import arc.scene.ui.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;

public class GunmetalStyles {
    public static Dialog.DialogStyle transDialog;

    public static void load(){
        transDialog = new Dialog.DialogStyle(){{
            this.stageBackground = Tex.windowEmpty;
            this.titleFont = Fonts.def;
            this.background = Tex.windowEmpty;
            this.titleFontColor = Pal.accent;
        }};
    }
}
