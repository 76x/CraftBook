package com.sk89q.craftbook.gates.world;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.RestrictedIC;
import com.sk89q.craftbook.ic.SelfTriggeredIC;

/**
 * @author Me4502
 */
public class EntityTrapST extends EntityTrap implements SelfTriggeredIC {

    public EntityTrapST(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Self-triggered Entity Trap";
    }

    @Override
    public String getSignTitle() {

        return "ST ENTITY TRAP";
    }

    @Override
    public void think(ChipState chip) {

        chip.setOutput(0, hurt());
    }


    @Override
    public boolean isActive() {

        return true;
    }

    public static class Factory extends EntityTrap.Factory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new EntityTrapST(getServer(), sign, this);
        }
    }
}