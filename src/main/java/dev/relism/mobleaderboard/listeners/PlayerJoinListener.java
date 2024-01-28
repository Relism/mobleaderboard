package dev.relism.mobleaderboard.listeners;

import dev.relism.mobleaderboard.utils.msg;
import org.bson.Document;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;
import dev.relism.mobleaderboard.Mobleaderboard;
import dev.relism.mobleaderboard.storage.PlayerStorage;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.CompletableFuture;

/**
 * Listener for handling player join events.
 */
public class PlayerJoinListener implements Listener {

    private static final Mobleaderboard plugin = Mobleaderboard.getPlugin();

    /**
     * Handles player join events. Checks if the player has an existing document in the MongoDB database
     * with type "mobdata". If not, initializes player data by creating a new document.
     *
     * @param event the PlayerJoinEvent
     */
    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        initializePlayerData(player);
        rewardManager(player);
    }

    /**
     * Initializes player data by checking if the player has an existing document in the MongoDB database.
     * If not, creates a new document with the required fields.
     *
     * @param player the player to initialize data for
     */
    private void initializePlayerData(Player player) {
        PlayerStorage playerStorage = new PlayerStorage(plugin, player);

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            if (!playerStorage.playerDataExists()) {
                // Player data doesn't exist, create a new document
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    msg.log("Initializing data for player: " + player.getName());
                    playerStorage.insertDocument(createPlayerDataDocument(player));
                });
            }
        });
    }

    /**
     * Creates a new document with the required fields for player data.
     *
     * @param player the player for whom the document is created
     * @return the created document
     */
    private Document createPlayerDataDocument(Player player) {
        return new Document("type", "playerdata")
                .append("name", player.getName())
                .append("uuid", player.getUniqueId().toString())
                .append("kills", 0);
    }

    /**
     * Manages rewards when a player joins. Checks for pending rewards and gives them to the player.
     *
     * @param player the player for whom rewards are checked
     */
    private void rewardManager(Player player) {
        PlayerStorage playerStorage = new PlayerStorage(plugin, player);

        CompletableFuture<Object> pendingRewardFuture = playerStorage.getFieldValue("playerdata", "pendingRewardMaterial");

        pendingRewardFuture.thenAccept(pendingReward -> {
            if (pendingReward != null) {
                // Player has a pending reward
                String pendingRewardMaterial = pendingReward.toString();  // Assuming the result is a String
                Material material = Material.getMaterial(pendingRewardMaterial);

                if (material != null) {
                    ItemStack rewardItem = new ItemStack(material, 1);
                    player.getInventory().addItem(rewardItem);
                    msg.send(player, "&aYou received a pending reward: " + material.name());

                    // Set pendingRewardMaterial to null
                    playerStorage.setFieldValue("playerdata", "pendingRewardMaterial", 0);
                }
            }
        });
    }

}
