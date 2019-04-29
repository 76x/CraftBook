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
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;

/**
 * MESSAGE ALL
 * Model ID: MC1511
 * @author chrishawthorne (76x)
 * @author Tom (tmhrtly) (Legacy)
 */


public class MessageAll extends IC implements RestrictedIC {
    public MessageAll(ICFactory<MessageAll> icFactory, Location<World> block){super(icFactory,block);}

    String message = "";
    @Override
    public void create(Player player, List<Text> lines) throws InvalidICException {
        String id = lines.get(2).toPlain();

        if (id.length()==0) {
            throw new InvalidICException ("Please put a message on the 3rd line to be broadcast to all players.");
        }

        if (!lines.get(3).toPlain().equals("")) {
            throw new InvalidICException ("Line 4 must be blank");
        }
    }

    @Override
    public void load(){
        message = getLine(2);
    }

    @Override
    public void trigger(){
        if (getPinSet().getInput(0, this)){
            for (Player p : Sponge.getServer().getOnlinePlayers())
                p.sendMessage(Text.of(message));

        }
    }

    public static class Factory implements ICFactory<MessageAll>{
        @Override
        public MessageAll createInstance(Location<World> location){ return new MessageAll(this,location);}

        @Override
        public String[][] getPinHelp(){
            return new String[][] {
                    new String[] {
                            "Input"
                    },
                    new String[] {
                            "Sends a message to all online players"
                    }
            };
        }
    }
}

