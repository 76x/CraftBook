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
package com.sk89q.craftbook.sponge.mechanics;

import static com.sk89q.craftbook.sponge.util.locale.TranslationsManager.USE_PERMISSIONS;

import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.core.util.PermissionNode;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeSignMechanic;
import com.sk89q.craftbook.sponge.mechanics.variable.Variables;
import com.sk89q.craftbook.sponge.util.SignUtil;
import com.sk89q.craftbook.sponge.util.SpongePermissionNode;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.text.Text;

@Module(id = "marquee", name = "Marquee", onEnable="onInitialize", onDisable="onDisable")
public class Marquee extends SpongeSignMechanic implements DocumentationProvider {

    private SpongePermissionNode createPermission = new SpongePermissionNode("craftbook.marquee", "Allows for creation of the marquee.",
            PermissionDescription.ROLE_STAFF);

    private SpongePermissionNode usePermission = new SpongePermissionNode("craftbook.marquee.use", "Allows for usage of the marquee.",
            PermissionDescription.ROLE_USER);

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        createPermission.register();
        usePermission.register();
    }

    @Listener
    public void onInteract(InteractBlockEvent.Secondary.MainHand event, @First Player player) {
        event.getTargetBlock().getLocation().ifPresent((location) -> {
            if (SignUtil.isSign(location)) {
                Sign sign = (Sign) location.getTileEntity().get();
                if (isMechanicSign(sign)) {
                    if (!player.hasPermission(usePermission.getNode())) {
                        player.sendMessage(USE_PERMISSIONS);
                        return;
                    }

                    String variable = SignUtil.getTextRaw(sign, 2);
                    String line3 = SignUtil.getTextRaw(sign, 3);

                    String var = Variables.instance.getVariable(line3.isEmpty() ? Variables.GLOBAL_NAMESPACE : line3, variable);
                    if(var == null || var.isEmpty()) var = "variable.missing";
                    player.sendMessage(Text.of(var));

                    event.setCancelled(true);
                }
            }
        });
    }

    public String getPath() {
        return "mechanics/marquee";
    }

    @Override
    public String[] getValidSigns() {
        return new String[] {
                "[Marquee]"
        };
    }

    @Override
    public SpongePermissionNode getCreatePermission() {
        return createPermission;
    }

    @Override
    public PermissionNode[] getPermissionNodes() {
        return new PermissionNode[] {
                createPermission,
                usePermission
        };
    }
}
