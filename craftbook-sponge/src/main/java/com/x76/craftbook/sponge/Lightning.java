/*
 * CraftBook Copyright (C) 2010-2019 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2019 me4502 <http://www.me4502.com>
 * CraftBook Copyright (C) Contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */
package com.x76.craftbook.sponge;

import com.sk89q.craftbook.sponge.mechanics.ics.IC;
import com.sk89q.craftbook.sponge.mechanics.ics.InvalidICException;
import com.sk89q.craftbook.sponge.mechanics.ics.RestrictedIC;
import com.sk89q.craftbook.sponge.mechanics.ics.factory.ICFactory;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Entity;

import java.util.List;

/**
 * LIGHTNING
 * Model ID: MCX255
 *
 * @author chrishawthorne (76x)
 * @author sk89q (Legacy)
 */


public class Lightning extends IC implements RestrictedIC {

    int y = 0;
    @Override
    public void create(Player player, List<Text> lines) throws InvalidICException {
        if (lines.get(2).toPlain().length() != 0) {
            try
            {
                int y = Integer.parseInt(lines.get(2).toPlain());
                if(y < -256 + 2 || y > 256 - 1)
                    throw new InvalidICException("Third line needs to be a number from "+(-256 + 2)+" to "+(256 - 1)) ;
            }
            catch(NumberFormatException e)
            {
                throw new InvalidICException("Third line needs to be a number or blank.") ;
            }
        }

        if (lines.get(3).toPlain().length() != 0) {
            throw new InvalidICException("Fourth line needs to be blank");
        }
    }
    @Override
    public void load(){
                try
                {
                    y = Integer.parseInt(getLine(2));
                }
                catch(NumberFormatException nfe){

                }
        y+= getBackBlock().getBlockY();
    }

    public Lightning(ICFactory<Lightning> icFactory, Location<World> block){super(icFactory,block);}

    @Override
    public void trigger() {
        if (getPinSet().getInput(0,this)){
            Location<World> location = new Location<World>(getBlock().getExtent(), getBackBlock().getBlockX(), y, getBackBlock().getBlockZ());
            Entity lightning = location.getExtent().createEntity(
                    EntityTypes.LIGHTNING, location.getPosition()
            );
            org.spongepowered.api.entity.weather.Lightning entity = (org.spongepowered.api.entity.weather.Lightning) lightning;
            location.getExtent().spawnEntity(entity);
            getPinSet().setOutput(0,true,this);

        }
        getPinSet().setOutput(0,false,this );
    }

    public static class Factory implements ICFactory<Lightning>{
        @Override
        public Lightning createInstance(Location<World> location) {
            return new Lightning(this,location);
        }

        @Override
        public String[][] getPinHelp() {
            return new String[][] {
                    new String[]{
                            "Input"
                    },
                    new String[]{
                            "Strikes bolt upon location"
                    }
            };
    }


    }



}
