/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.listeners;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.utils.WorldManager;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import java.util.List;
import java.util.logging.Level;

//import org.bukkit.event.entity.ExplosionPrimedEvent;

public class MVEntityListener extends EntityListener {

    private MultiverseCore plugin;
    private WorldManager worldManager;

    public MVEntityListener(MultiverseCore plugin) {
        this.plugin = plugin;
        this.worldManager = plugin.getMVWorldManager();
    }

    @Override
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getEntity() instanceof Player) {
            Player p = (Player) event.getEntity();
            MultiverseWorld w = this.plugin.getMVWorldManager().getMVWorld(p.getWorld().getName());
            if (w != null && !w.getHunger()) {
                // If the world has hunger set to false, do not let the level go down
                if (event.getFoodLevel() < ((Player) event.getEntity()).getFoodLevel()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @Override
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        if (event.isCancelled()) {
            return;
        }
        RegainReason reason = event.getRegainReason();
        MultiverseWorld world = this.worldManager.getMVWorld(event.getEntity().getLocation().getWorld());
        if (world != null && reason == RegainReason.REGEN && !world.getAutoHeal()) {
            event.setCancelled(true);
        }
    }

    /** Handle Animal/Monster Spawn settings, seems like a more concrete method than using CraftBukkit. */
    @Override
    public void onCreatureSpawn(CreatureSpawnEvent event) {

        // Check to see if the Creature is spawned by a plugin, we don't want to prevent this behaviour.
        if (event.getSpawnReason() == SpawnReason.CUSTOM) {
            return;
        }

        World world = event.getEntity().getWorld();
        if (event.isCancelled())
            return;

        // Check if it's a world which we are meant to be managing.
        if (!(this.worldManager.isMVWorld(world.getName())))
            return;

        CreatureType creature = event.getCreatureType();

        MultiverseWorld mvworld = this.worldManager.getMVWorld(world.getName());

        /**
         * Handle people with non-standard animals: ie a patched craftbukkit.
         */
        if (creature == null) {
            this.plugin.log(Level.FINER, "Found a null typed creature.");
            return;
        }

        /**
         * Animal Handling
         */
        if (event.getEntity() instanceof Animals || event.getEntity() instanceof Squid) {
            event.setCancelled(this.shouldWeKillThisCreature(mvworld.getAnimalList(), mvworld.canAnimalsSpawn(), creature.toString().toUpperCase()));
        }
        /**
         * Monster Handling
         */
        if (event.getEntity() instanceof Monster || event.getEntity() instanceof Ghast || event.getEntity() instanceof Slime) {
            event.setCancelled(this.shouldWeKillThisCreature(mvworld.getMonsterList(), mvworld.canMonstersSpawn(), creature.toString().toUpperCase()));
        }
    }

    private boolean shouldWeKillThisCreature(List<String> creatureList, boolean allowCreatureSpawning, String creature) {
        if (creatureList.isEmpty() && allowCreatureSpawning) {
            // 1. There are no exceptions and animals are allowed. Save it.
            return false;
        } else if (creatureList.isEmpty()) {
            // 2. There are no exceptions and animals are NOT allowed. Kill it.
            return true;
        } else if (creatureList.contains(creature) && allowCreatureSpawning) {
            // 3. There ARE exceptions and animals ARE allowed. Kill it.
            return true;
        } else if (!creatureList.contains(creature.toUpperCase()) && allowCreatureSpawning) {
            // 4. There ARE exceptions and animals ARE NOT allowed. SAVE it.
            return false;
        } else if (creatureList.contains(creature.toUpperCase()) && !allowCreatureSpawning) {
            // 5. No animals are allowed to be spawned, BUT this one can stay...
            return false;
        } else if (!creatureList.contains(creature.toUpperCase()) && !allowCreatureSpawning) {
            // 6. Animals are NOT allowed to spawn, and this creature is not in the save list... KILL IT
            return true;
        }
        // This code should NEVER execute. I just left the verbose conditions in right now.
        return false;
    }

}
