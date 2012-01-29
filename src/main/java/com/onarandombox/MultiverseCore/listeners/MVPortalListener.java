/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2012.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.listeners;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.enums.AllowedPortalType;
import org.bukkit.Material;
import org.bukkit.PortalType;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCreatePortalEvent;
import org.bukkit.event.world.PortalCreateEvent;

/**
 * A custom listener for portal related events.
 */
public class MVPortalListener implements Listener {

    private MultiverseCore plugin;

    public MVPortalListener(MultiverseCore core) {
        this.plugin = core;
    }

    /**
     * This is called when an entity creates a portal.
     *
     * @param event The event where an entity created a portal.
     */
    @EventHandler
    public void entityPortalCreate(EntityCreatePortalEvent event) {
        if (event.isCancelled() || event.getBlocks().size() == 0) {
            return;
        }
        MultiverseWorld world = this.plugin.getMVWorldManager().getMVWorld(event.getBlocks().get(0).getWorld());
        event.setCancelled(this.cancelPortalEvent(world, event.getPortalType()));
    }

    /**
     * This is called when a portal is created as the result of another world being linked.
     * @param event
     */
    @EventHandler
    public void portalForm(PortalCreateEvent event) {
        if (event.isCancelled() || event.getBlocks().size() == 0) {
            return;
        }
        // There's no type attribute (as of 1.1-R1), so we have to iterate.
        for (Block b : event.getBlocks()) {
            if (b.getType() == Material.PORTAL) {
                MultiverseWorld world = this.plugin.getMVWorldManager().getMVWorld(b.getWorld());
                event.setCancelled(this.cancelPortalEvent(world, PortalType.NETHER));
                return;
            }
        }
        // If We're here, then the Portal was an Ender type:
        MultiverseWorld world = this.plugin.getMVWorldManager().getMVWorld(event.getBlocks().get(0).getWorld());
        event.setCancelled(this.cancelPortalEvent(world, PortalType.ENDER));
    }

    private boolean cancelPortalEvent(MultiverseWorld world, PortalType type) {
        if (world.getAllowedPortals() == AllowedPortalType.NONE) {
            return true;
        } else if (world.getAllowedPortals() != AllowedPortalType.ALL) {
            if (type != world.getAllowedPortals().getActualPortalType()) {
                return true;
            }
        }
        return false;
    }
}
