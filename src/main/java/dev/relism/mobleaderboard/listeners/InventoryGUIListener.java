package dev.relism.mobleaderboard.listeners;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import dev.relism.mobleaderboard.Mobleaderboard;
import dev.relism.mobleaderboard.storage.MongoWrapper;
import dev.relism.mobleaderboard.storage.PlayerStorage;
import dev.relism.mobleaderboard.utils.msg;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;


/**
 * Listener for handling inventory-related events and interactions in Mobleaderboard.
 * Manages actions related to the admin GUI, such as resetting player kills, rewarding top players,
 * spawning test plugin mobs, and handling inventory clicks and closures.
 */
public class InventoryGUIListener implements Listener {

    private static final Mobleaderboard plugin = Mobleaderboard.getPlugin();
    private static final MongoWrapper mwi = plugin.getMongoWrapperInstance();

    /**
     * Handles inventory click events.
     *
     * @param e The InventoryClickEvent
     */
    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();

        if (player.hasMetadata("OpenedTestGUI")) {
            e.setCancelled(true);
        }

        if (player.hasMetadata("OpenedAdminTestGUI")) {
            handleAdminGUIClicks(e, player);
        }
    }

    /**
     * Handles inventory close events.
     *
     * @param e The InventoryCloseEvent
     */
    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();

        if (player.hasMetadata("OpenedTestGUI")) {
            player.removeMetadata("OpenedTestGUI", plugin);
        }

        if (player.hasMetadata("OpenedAdminTestGUI")) {
            player.removeMetadata("OpenedAdminTestGUI", plugin);
        }
    }

    /**
     * Handles admin GUI clicks based on the clicked slot.
     *
     * @param e      The InventoryClickEvent
     * @param player The player who clicked
     */
    private void handleAdminGUIClicks(InventoryClickEvent e, Player player) {
        int clickedSlot = e.getRawSlot();

        switch (clickedSlot) {
            case 2:
                resetAllPlayerKills(player);
                msg.send(player, "All player kills reset.");
                break;

            case 4:
                rewardTopPlayers(player);
                msg.send(player, "Top players rewarded.");
                break;

            case 6:
                spawnTestPluginZombie(player);
                break;

            default:
                break;
        }

        e.setCancelled(true);
    }

    /**
     * Resets all player kills in the database.
     *
     * @param executor The player executing the action
     */
    private void resetAllPlayerKills(Player executor) {
        MongoClient mongoClient = mwi.getMongoClient();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                MongoDatabase database = mongoClient.getDatabase("mbl-players");

                // Iterate through all collections in the database
                for (String collectionName : database.listCollectionNames()) {
                    if (collectionName.equals("system.indexes")) {
                        continue;
                    }

                    MongoCollection<Document> collection = database.getCollection(collectionName);

                    FindIterable<Document> playerDataDocuments = collection.find(new Document("type", "playerdata"));

                    playerDataDocuments.forEach((Consumer<? super Document>) document -> {
                        // Update the "kills" field to 0
                        mwi.setFieldValue("mbl-players", collectionName, "type", "playerdata", "kills", 0);
                    });
                }

                msg.send(executor, "All player kills reset.");
            } catch (Exception e) {
                msg.send(executor, "&cError resetting player kills: " + e.getMessage());
            }
        });
    }

    /**
     * Rewards top players and resets kills for others.
     *
     * @param executor The player executing the action
     */
    private void rewardTopPlayers(Player executor) {
        CompletableFuture<List<Document>> topPlayersFuture = mwi.fetchSortedTopPlayersAsync(10);

        topPlayersFuture.thenAcceptAsync(topPlayers -> {
            if (topPlayers.isEmpty()) {
                msg.send(executor, "&cNo data found.");
                return;
            }

            // Reward top players
            for (int i = 0; i < Math.min(topPlayers.size(), 10); i++) {
                Document playerData = topPlayers.get(i);
                UUID playerUUID = UUID.fromString(playerData.getString("uuid"));
                Player player = Bukkit.getPlayer(playerUUID);
                int position = i + 1;
                Material rewardMaterial = getRewardMaterial(position);

                // Give rewards to top players
                giveReward(player, rewardMaterial, executor);

                msg.log("Rewarding " + player.getName() + " with a " + rewardMaterial.name());
            }

            // Reset kills for others
            resetKillsForOthers(topPlayers, executor);
        });
    }

    /**
     * Gives a reward to a player, handling online and offline cases.
     *
     * @param targetPlayer The player to receive the reward
     * @param material     The material of the reward
     * @param executor     The player executing the action
     */
    private void giveReward(Player targetPlayer, Material material, Player executor) {
        if (targetPlayer != null) {
            ItemStack rewardItem = new ItemStack(material, 1);

            if (hasAvaliableSlot(targetPlayer)) {
                // Inventory has space, reward given successfully
                targetPlayer.getInventory().addItem(rewardItem);
                msg.send(targetPlayer, "&aYou received a reward: " + material.name());
            } else {
                // Inventory is full, make the reward pending
                storePendingReward(targetPlayer, material);
                msg.send(targetPlayer, "&eYour inventory is full. The reward will be given when you have space.");
            }
        } else {
            // Player is offline, make the reward pending
            storePendingReward(targetPlayer, material);
        }
    }

    /**
     * Stores a pending reward for the specified player in the player data.
     * If the player is offline, the reward material is stored as pending in the database.
     * If the player is online but has a full inventory, the reward material is stored as pending in the database.
     * If the player is online and has a free slot, the reward will be given immediately.
     *
     * @param player   The player to store the pending reward for
     * @param material The material of the pending reward
     */
    private void storePendingReward(Player player, Material material) {
        PlayerStorage ps = new PlayerStorage(plugin, player);
        ps.setFieldValue("playerdata", "pendingRewardMaterial", material.name());
    }

    /**
     * Resets kills to 0 for non-top players in the database.
     *
     * @param topPlayers The list of top players
     * @param executor    The player executing the action
     */
    private void resetKillsForOthers(List<Document> topPlayers, Player executor) {
        MongoClient mongoClient = mwi.getMongoClient();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                MongoDatabase database = mongoClient.getDatabase("mbl-players");

                // Iterate through all collections in the database
                for (String collectionName : database.listCollectionNames()) {
                    if (collectionName.equals("system.indexes")) {
                        continue;
                    }

                    MongoCollection<Document> collection = database.getCollection(collectionName);

                    FindIterable<Document> playerDataDocuments = collection.find(new Document("type", "playerdata"));

                    // Reset kills to 0 for all documents not in the top players list
                    playerDataDocuments.forEach((Consumer<? super Document>) document -> {
                        if (!topPlayers.stream().anyMatch(player -> player.getString("name").equals(document.getString("name")))) {
                            // Update the "kills" field to 0 for players not in the top list
                            mwi.setFieldValue("mbl-players", collectionName, "type", "playerdata", "kills", 0);
                            msg.send(executor, "Succesfully reset kills for non-top players");
                        }
                    });
                }
            } catch (Exception e) {
                msg.send(executor, "Error resetting kills for non-top players: " + e.getMessage());
                msg.log("Error resetting kills for others: " + e.getMessage());
            }
        });
    }

    /**
     * Spawns a test plugin zombie for each online player.
     *
     * @param executor The player executing the action
     */
    private void spawnTestPluginZombie(Player executor) {
        // Iterate through all online players and summon a test plugin zombie for each
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            summonTestPluginZombie(onlinePlayer);
            msg.send(onlinePlayer, "Summoned a test zombie on you!");
        }
        msg.send(executor, "Succesfully summoned test zombies on all " + Bukkit.getOnlinePlayers().size() + " online players");
    }

    /**
     * Retrieves the reward material based on the position on the leaderboard.
     *
     * @param position The position of the player
     * @return The reward material
     */
    private Material getRewardMaterial(int position) {
        switch (position) {
            case 1:
                return Material.DIAMOND;
            case 2:
                return Material.GOLD_INGOT;
            case 3:
                return Material.IRON_INGOT;
            default:
                return Material.COAL;
        }
    }

    /**
     * Summons a test plugin zombie at the player's location.
     *
     * @param player The player for whom the zombie is summoned
     */
    private void summonTestPluginZombie(Player player) {
        LivingEntity zombie = (LivingEntity) player.getWorld().spawnEntity(player.getLocation(), EntityType.ZOMBIE);
        zombie.setMetadata("testPlugin", new FixedMetadataValue(plugin, true));
        msg.debug("Summoned a testPlugin zombie at location: " + player.getLocation());
    }

    /**
     * Checks if the player's inventory has an available slot.
     *
     * @param player The player
     * @return True if there's an available slot, false otherwise
     */
    public boolean hasAvaliableSlot(Player player){
        Inventory inv = player.getInventory();
        for (ItemStack item: inv.getContents()) {
            if(item == null) {
                return true;
            }
        }
        return false;
    }
}

