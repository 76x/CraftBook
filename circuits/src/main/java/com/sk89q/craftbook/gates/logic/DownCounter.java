package com.sk89q.craftbook.gates.logic;

import com.sk89q.craftbook.ic.*;
import org.bukkit.Server;
import org.bukkit.block.Sign;

/**
 * Counter counts down each time clock input toggles from low to high, it starts
 * from a predefined value to 0. Output is high when counter reaches 0. If in
 * 'infinite' mode, it will automatically reset the next time clock is toggled.
 * Otherwise, it only resets when the 'reset' input toggles from low to high.
 * Configuration:
 * Line 3: ##:ONCE or ##:INF -- where ## is the counter reset value, and ONCE or INF
 * specifies if the counter should repeat or not.
 * Inputs:
 * 1 - Clock
 * 2 - Reset
 * 3 - (unused)
 * Output: HIGH when counter reaches 0, LOW otherwise
 *
 * @author davr
 */
public class DownCounter extends AbstractIC {

    private int resetVal;
    private boolean inf;

    public DownCounter(Server server, Sign sign, ICFactory factory) {

        super(server, sign, factory);
        load();
    }

    private void load() {

        try {
            // Get IC configuration data from line 3 of sign
            String line2 = getSign().getLine(2);
            String[] config = line2.split(":");

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
            getSign().setLine(2, resetVal + (inf ? ":INF" : ""));
            getSign().update();
        } catch (Exception ignored) {
        }
    }

    @Override
    public String getTitle() {

        return "Down Counter";
    }

    @Override
    public String getSignTitle() {

        return "DOWN COUNTER";
    }

    @Override
    public void trigger(ChipState chip) {
        // Get current counter value from line 4 of sign
        String line3 = getSign().getLine(3);
        int curVal;

        try {
            curVal = Integer.parseInt(line3);
        } catch (Exception e) {
            curVal = resetVal;
        }

        int oldVal = curVal;
        try {
            // If clock input triggered
            if (chip.isTriggered(0) && chip.get(0)) {
                if (curVal == 0) { // If we've gotten to 0, reset if infinite mode
                    if (inf) {
                        curVal = resetVal;
                    }
                } else {
                    curVal--;
                }

                // Set output to high if we're at 0, otherwise low
                chip.setOutput(0, curVal == 0);
                // If reset input triggered, reset counter value
            } else if (chip.isTriggered(1) && chip.get(1)) {
                curVal = resetVal;
                chip.setOutput(0, false);
            }
        } catch (Exception ignored) {
        }

        // Update counter value stored on sign if it's changed
        if (curVal != oldVal) {
            getSign().setLine(3, curVal + "");
            getSign().update();
        }
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new DownCounter(getServer(), sign, this);
        }
    }
}
