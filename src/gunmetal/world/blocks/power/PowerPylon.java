package gunmetal.world.blocks.power;

import mindustry.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.blocks.power.*;

public class PowerPylon extends PowerTower {
    public PowerPylon(String name) {
        super(name);
    }

    @Override
    public boolean linkValid(Building tile, Building link, boolean checkMaxNodes) {
        // Cannot link to other pylons
        if(link instanceof PowerPylonBuild) return false;

        // Otherwise, pretty  normal connection behavior.
        if(link instanceof PowerTowerBuild) return super.linkValid(tile, link, checkMaxNodes);

        // Vanilla connection behavior.
        if(tile == link || link == null || !link.block.hasPower || !link.block.connectedPower || tile.team != link.team || (sameBlockConnection && tile.block != link.block)) return false;

        if(overlaps(tile, link, laserRange * Vars.tilesize) || (link.block instanceof PowerNode node && overlaps(link, tile, node.laserRange * Vars.tilesize))){
            if(checkMaxNodes && link.block instanceof PowerNode node){
                return link.power.links.size < node.maxNodes || link.power.links.contains(tile.pos());
            }
            return true;
        }
        return false;
    }

    public boolean validPotentialCondition(Tile tile, Team team, Building other){
        Tile t = other.tile;

        return !(other instanceof PowerTowerBuild) || (tile.x == t.x || tile.y == t.y);
    };

    public class PowerPylonBuild extends PowerTowerBuild{

    }
}
