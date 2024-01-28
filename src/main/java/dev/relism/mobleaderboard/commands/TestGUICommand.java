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

public class TestGUICommand implements CommandExecutor {
    private final Mobleaderboard plugin;
    private MongoWrapper mwi;
    private final int rows = 3;

    public TestGUICommand(Mobleaderboard plugin) {
        this.plugin = plugin;
        this.mwi = plugin.getMongoWrapperInstance();
    }

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

            Inventory inventory = createTopPlayersInventory(topPlayers);
            player.openInventory(inventory);
            player.setMetadata("OpenedTestGUI", new FixedMetadataValue(plugin, inventory));
        });

        return true;
    }

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
