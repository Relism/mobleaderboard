package dev.relism.mobleaderboard.commands;

import dev.relism.mobleaderboard.Mobleaderboard;
import dev.relism.mobleaderboard.storage.MongoWrapper;
import dev.relism.mobleaderboard.utils.msg;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

public class AdminTestGUICommand implements CommandExecutor {

    private final Mobleaderboard plugin;
    private MongoWrapper mwi;
    private final int rows = 1;

    public AdminTestGUICommand(Mobleaderboard plugin) {
        this.plugin = plugin;
        this.mwi = plugin.getMongoWrapperInstance();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player player = (Player) sender;

        if (!player.isOp()) {
            msg.send(player, "&cYou must be an operator to use this command.");
            return true;
        }

        Inventory inventory = createAdminGUI();
        player.openInventory(inventory);
        player.setMetadata("OpenedAdminTestGUI", new FixedMetadataValue(plugin, inventory));

        return true;
    }

    private Inventory createAdminGUI() {
        Inventory inventory = Bukkit.createInventory(null, 9 * rows, "Admin Test GUI");

        // Slot 2: Reset all players' kills
        inventory.setItem(2, createResetKillsItem());

        // Slot 4: Reward 1st, 2nd, and 3rd place
        inventory.setItem(4, createRewardTopPlayersItem());

        // Slot 6: Spawn a mob with metadata "testPlugin" at all players
        inventory.setItem(6, createSpawnMobItem());

        return inventory;
    }

    private ItemStack createResetKillsItem() {
        ItemStack item = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Reset Kills");
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createRewardTopPlayersItem() {
        ItemStack item = new ItemStack(Material.GOLD_BLOCK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Reward Top Players");
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createSpawnMobItem() {
        ItemStack item = new ItemStack(Material.MOB_SPAWNER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Spawn Mob with Metadata");
        item.setItemMeta(meta);

        return item;
    }

}

