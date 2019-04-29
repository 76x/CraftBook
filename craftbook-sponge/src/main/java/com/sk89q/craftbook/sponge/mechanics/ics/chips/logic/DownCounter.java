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
package com.sk89q.craftbook.sponge.mechanics.ics.chips.logic;

import com.sk89q.craftbook.core.util.RegexUtil;
import com.sk89q.craftbook.sponge.mechanics.ics.IC;
import com.sk89q.craftbook.sponge.mechanics.ics.InvalidICException;
import com.sk89q.craftbook.sponge.mechanics.ics.factory.ICFactory;
import com.sk89q.craftbook.sponge.util.SignUtil;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;

public class DownCounter extends IC {

    private int resetVal;
    private boolean inf;

    public DownCounter(ICFactory<DownCounter> icFactory, Location<World> block) {
        super(icFactory, block);
    }

    @Override
    public void create(Player player, List<Text> lines) throws InvalidICException {
        super.create(player, lines);

        String[] config = RegexUtil.COLON_PATTERN.split(SignUtil.getTextRaw(lines.get(2)));

        resetVal = 0;
        inf = false;
        try {
            resetVal = Integer.parseInt(config[0]);
            inf = config[1].equals("INF");
        } catch (NumberFormatException e) {
            resetVal = 5;
        } catch (ArrayIndexOutOfBoundsException e) {
            inf = false;
        } catch (Exception ignored) {
        }

        lines.set(2, Text.of(resetVal + (inf ? ":INF" : "")));
    }

    @Override
    public void load() {
        super.load();

        String[] config = RegexUtil.COLON_PATTERN.split(getLine(2));

        resetVal = 0;
        inf = false;
        try {
            resetVal = Integer.parseInt(config[0]);
            inf = config[1].equals("INF");
        } catch (NumberFormatException e) {
            resetVal = 5;
        } catch (ArrayIndexOutOfBoundsException e) {
            inf = false;
        } catch (Exception ignored) {
        }
        setLine(2, Text.of(resetVal + (inf ? ":INF" : "")));
    }

    @Override
    public void trigger() {
        int curVal;

        try {
            curVal = Integer.parseInt(getLine(3));
        } catch (Exception e) {
            curVal = 0;
        }

        int oldVal = curVal;
        // If clock input triggered
        if (getPinSet().getInput(0, this)) {
            if (curVal == 0) { // If we've gotten to 0, reset if infinite mode
                if (inf) {
                    curVal = resetVal;
                }
            } else {
                curVal --;
            }

            // Set output to high if we're at 0, otherwise low
            getPinSet().setOutput(0, curVal == 0, this);
        } else if (getPinSet().getInput(1, this)) {
            curVal = resetVal;
            getPinSet().setOutput(0, false, this);
        }

        // Update counter value stored on sign if it's changed
        if (curVal != oldVal) {
            setLine(3, Text.of(String.valueOf(curVal)));
        }
    }

    public static class Factory implements ICFactory<DownCounter> {

        @Override
        public DownCounter createInstance(Location<World> location) {
            return new DownCounter(this, location);
        }

        @Override
        public String[] getLineHelp() {
            return new String[] {
                    "Start Value{:INF}",
                    "Current Value"
            };
        }

        @Override
        public String[][] getPinHelp() {
            return new String[][] {
                    new String[] {
                            "Decrement Counter",
                            "Reset Counter",
                            "Nothing"
                    },
                    new String[] {
                            "High on Counter Complete"
                    }
            };
        }
    }
}
