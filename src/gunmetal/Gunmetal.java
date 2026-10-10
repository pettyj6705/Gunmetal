package gunmetal;

import mindustry.mod.*;
import gunmetal.gen.*;

public class Gunmetal extends Mod{
    @Override
    public void loadContent(){
        EntityRegistry.register();
        EntityRegistry.registerUnits();
    }
}
