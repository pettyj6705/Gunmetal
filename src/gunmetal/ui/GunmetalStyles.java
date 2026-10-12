package gunmetal.ui;

import arc.func.*;
import arc.graphics.g2d.*;
import arc.scene.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import mindustry.core.*;
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

    public static Element itemImage(TextureRegion region, Prov<CharSequence> text){
        Stack stack = new Stack();

        Table t = new Table().left().bottom();
        t.label(text);

        stack.add(new Image(region));
        stack.add(t);
        return stack;
    }

    public static String round(float f){
        f = (int)f;
        if(f >= 1000000){
            return (int)(f / 1000000f) + "[gray]" + UI.millions;
        }else if(f >= 1000){
            return (int)(f / 1000) + UI.thousands;
        }else{
            return (int)f + "";
        }
    }

}
