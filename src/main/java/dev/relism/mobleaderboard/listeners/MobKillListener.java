package dev.relism.mobleaderboard.listeners;

import dev.relism.mobleaderboard.storage.PlayerStorage;
import dev.relism.mobleaderboard.utils.msg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import dev.relism.mobleaderboard.Mobleaderboard;

/**
 * Listener for handling mob kill events. Increments the kill count for players when they kill special mobs.
 */
public class MobKillListener implements Listener {

    private final Mobleaderboard plugin = Mobleaderboard.getPlugin();

    /**
     * Handles the EntityDeathEvent triggered when a mob is killed.
     * Increments the "kills" field for the player who killed a special mob with the metadata "testPlugin".
     *
     * @param event The EntityDeathEvent
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity killedEntity = event.getEntity();
        Player killer = event.getEntity().getKiller();

        if (killer != null && killedEntity.hasMetadata("testPlugin")) {
            PlayerStorage ps = new PlayerStorage(plugin, killer);

            // Increment the "kills" field by 1
            ps.getFieldValue("playerdata", "kills").thenAccept(kills -> {
                int newKills = (kills != null) ? ((int) kills) + 1 : 1;
                ps.setFieldValue("playerdata", "kills", newKills);
                msg.send(killer, "• You've slain a special mob!");
                msg.send(killer, "• &aSpecial mob kills&f: &b" + (newKills - 1) + "&f -> &b" + newKills);
            });
        }
    }

}

