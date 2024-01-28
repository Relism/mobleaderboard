package dev.relism.mobleaderboard.commands;

import dev.relism.mobleaderboard.Mobleaderboard;
import dev.relism.mobleaderboard.storage.MongoWrapper;
import dev.relism.mobleaderboard.utils.msg;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Executor class that handles displaying a leaderboard GUI
 * containing information about the top players and their kills.
 */
public class TestGUICommand implements CommandExecutor {
    private final Mobleaderboard plugin;
    private MongoWrapper mwi;
    private final int rows = 3;

    /**
     * Constructs a new instance of TestGUICommand.
     *
     * @param plugin The main plugin instance.
     */
    public TestGUICommand(Mobleaderboard plugin) {
        this.plugin = plugin;
        this.mwi = plugin.getMongoWrapperInstance();
    }

    /**
     * Executes the command, displaying a GUI with the top players and their kills.
     *
     * @param sender   The command sender.
     * @param command  The command being executed.
     * @param label    The alias of the command used.
     * @param args     The arguments provided with the command.
     * @return true if the command was executed successfully, false otherwise.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        CompletableFuture<List<Document>> topPlayersFuture = mwi.fetchSortedTopPlayersAsync(10);

        // Execute the GUI creation after fetching top players
        topPlayersFuture.thenAcceptAsync(topPlayers -> {
            if (topPlayers.isEmpty()) {
                msg.send(player, "&cNo data available.");
                return;
            }

            // Basically checks if the #1 player has 0 or less kills (so everybody else is also on 0 kills)
            if(topPlayers.get(0).getInteger("kills") <= 0){
                msg.send(player, "&cEveryone is still on 0 kills, no leaderboard to be shown!");
                return;
            }

            Inventory inventory = createTopPlayersInventory(topPlayers);
            player.openInventory(inventory);
            player.setMetadata("OpenedTestGUI", new FixedMetadataValue(plugin, inventory));
        });

        return true;
    }

    /**
     * Creates the GUI inventory displaying the top players and their kills.
     *
     * @param topPlayers The list of top players' data.
     * @return The created GUI inventory.
     */
    private Inventory createTopPlayersInventory(List<Document> topPlayers) {
        Inventory inventory = Bukkit.createInventory(null, 9 * rows, "Kills leaderboard");

        // Populate the inventory with player data
        for (int i = 0; i < Math.min(topPlayers.size(), 10); i++) {
            Document playerData = topPlayers.get(i);
            String playerName = playerData.getString("name");
            int playerKills = playerData.getInteger("kills");
            ItemStack playerItem = createPlayerItem(playerName, playerKills, i + 1);
            inventory.setItem(getInventorySlot(i), playerItem);
        }

        return inventory;
    }

    /**
     * Creates an ItemStack representing a player with their kills and position on the leaderboard.
     *
     * @param playerName The name of the player.
     * @param playerKills The number of kills the player has.
     * @param position    The position of the player on the leaderboard.
     * @return The created player ItemStack.
     */
    private ItemStack createPlayerItem(String playerName, int playerKills, int position) {
        // Create a player head ItemStack
        ItemStack playerHead = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());

        //Get meta and start setting name and owner
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
        skullMeta.setOwner(playerName);
        skullMeta.setDisplayName("#" + position + " " + playerName);

        // Setting lore
        List<String> lore = new ArrayList<>();
        lore.add("Special Kills: " + playerKills);
        skullMeta.setLore(lore);

        // Apply the meta to the ItemStack
        playerHead.setItemMeta(skullMeta);

        return playerHead;
    }

    /**
     * Returns the inventory slot based on the position of a player on the leaderboard.
     *
     * @param position The position of the player on the leaderboard.
     * @return The corresponding inventory slot.
     */
    private int getInventorySlot(int position) {
        if (position == 0) {
            return 4;  // First place
        } else if (position == 1) {
            return 12; // Second place
        } else if (position == 2) {
            return 14; // Third place
        } else if (position >= 3 && position <= 9) {
            return 19 + (position - 3); // Fourth to Tenth place
        } else {
            return -1; // Invalid position
        }
    }
}
