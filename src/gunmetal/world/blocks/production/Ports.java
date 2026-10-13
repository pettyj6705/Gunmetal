package gunmetal.world.blocks.production;

import arc.func.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.ctype.*;
import mindustry.gen.*;

public class Ports {
    public Seq<Port> ports = new Seq<>();

    public Ports(Seq<Port> ports){
        this.ports = ports;
    }

    public Port findPorts(PortType type, Building self, Building other){

        for(Port port : ports){
            if(port.type != type) continue;

            Tmp.v2.set(port.offset.x * Vars.tilesize, port.offset.y * Vars.tilesize).rotate(90 * self.rotation);
            Tmp.v2.add(self.x, self.y);

            Building b = Vars.world.buildWorld(Tmp.v2.x, Tmp.v2.y);

            if(b == null) continue;

            if(b == other) return port;
        }

        return null;
    }

    public static class Port {
        UnlockableContent filter;
        PortType type = PortType.Input;
        Vec2 offset;

        public Port(PortType type, Vec2 offset){
            this.type = type;
            this.offset = offset;
        }

        public Port(Vec2 offset){
            this.offset = offset;
        }
    }

    public enum PortType {
        Input,
        Output
    }
}
