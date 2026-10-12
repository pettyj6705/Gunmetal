package gunmetal.world.blocks.power;

import arc.func.*;
import arc.util.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.blocks.power.*;

import static mindustry.Vars.*;

// TODO: Find a more elegant way to handle the custom connection conditions
public class PowerTower extends PowerNode {
    public PowerTower(String name) {
        super(name);

    }

    @Override
    public boolean linkValid(Building tile, Building link, boolean checkMaxNodes) {
        // Only consider linking to other towers or pylons.
        if(link instanceof PowerTowerBuild){
            PowerNode node = (PowerNode) link.block;

            if(tile == link || !link.block.hasPower || !link.block.connectedPower || tile.team != link.team || (sameBlockConnection && tile.block != link.block)) return false;

            // Only allow 90 degree connections; no diagonals.
            if(!(tile.x == link.x || tile.y == link.y)) return false;

            if(overlaps(tile, link, laserRange * tilesize) || overlaps(link, tile, node.laserRange * tilesize)){
                if(checkMaxNodes){
                    return link.power.links.size < node.maxNodes || link.power.links.contains(tile.pos());
                }
                return true;
            }
        }

        return false;
    }

    public boolean validPotentialCondition(Tile tile, Team team, Building other){
        Tile t = other.tile;

        return other instanceof PowerTowerBuild && (tile.x == t.x || tile.y == t.y);
    };

    // Straight from the source code, ew.
    // Needed potential links to comply with linkValid conditions.
    // TODO: Find a way to not do this
    @Override
    protected void getPotentialLinks(Tile tile, Team team, Cons<Building> others) {
        if(!autolink) return;

        Boolf<Building> valid = other -> other != null && other.tile != tile && other.block.connectedPower && other.power != null &&
                (other.block.outputsPower || other.block.consumesPower || other.block instanceof PowerNode) &&
                overlaps(tile.x * tilesize + offset, tile.y * tilesize + offset, other.tile, laserRange * tilesize) && other.team == team &&
                !graphs.contains(other.power.graph) &&
                !PowerNode.insulated(tile, other.tile) &&
                !(other instanceof PowerNodeBuild obuild && obuild.power.links.size >= ((PowerNode)obuild.block).maxNodes) &&
                !Structs.contains(Edges.getEdges(size), p -> { //do not link to adjacent buildings
                    var t = world.tile(tile.x + p.x, tile.y + p.y);
                    return t != null && t.build == other;
                }) && validPotentialCondition(tile, team, other);

        tempBuilds.clear();
        graphs.clear();

        //add conducting graphs to prevent double link
        for(var p : Edges.getEdges(size)){
            Tile other = tile.nearby(p);
            if(other != null && other.team() == team && other.build != null && other.build.power != null){
                graphs.add(other.build.power.graph);
            }
        }

        if(tile.build != null && tile.build.power != null){
            graphs.add(tile.build.power.graph);
        }

        var worldRange = laserRange * tilesize;
        var tree = team.data().buildingTree;
        if(tree != null){
            tree.intersect(tile.worldx() - worldRange, tile.worldy() - worldRange, worldRange * 2, worldRange * 2, build -> {
                if(valid.get(build) && !tempBuilds.contains(build)){
                    tempBuilds.add(build);
                }
            });
        }

        tempBuilds.sort((a, b) -> {
            int type = -Boolean.compare(a.block instanceof PowerNode, b.block instanceof PowerNode);
            if(type != 0) return type;
            return Float.compare(a.dst2(tile), b.dst2(tile));
        });

        returnInt = 0;

        tempBuilds.each(valid, t -> {
            if(returnInt ++ < maxNodes){
                graphs.add(t.power.graph);
                others.get(t);
            }
        });
    }

    public class PowerTowerBuild extends PowerNodeBuild{

    }
}
