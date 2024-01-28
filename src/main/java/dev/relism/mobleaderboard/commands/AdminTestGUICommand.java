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

/**
 * Executor class that handles an admin-specific GUI.
 * It allows operators to open an inventory with various options such as resetting all players' kills,
 * rewarding the top players, and spawning a mob with metadata.
 */
public class AdminTestGUICommand implements CommandExecutor {

    private final Mobleaderboard plugin;
    private MongoWrapper mwi;
    private final int rows = 1;

    /**
     * Constructs a new instance of AdminTestGUICommand.
     *
     * @param plugin The main plugin instance.
     */
    public AdminTestGUICommand(Mobleaderboard plugin) {
        this.plugin = plugin;
        this.mwi = plugin.getMongoWrapperInstance();
    }

    /**
     * Executes the command, opening the admin GUI for the player.
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

        if (!player.isOp()) {
            msg.send(player, "&cYou must be an operator to use this command.");
            return true;
        }

        Inventory inventory = createAdminGUI();
        player.openInventory(inventory);
        player.setMetadata("OpenedAdminTestGUI", new FixedMetadataValue(plugin, inventory));

        return true;
    }

    /**
     * Creates the admin GUI with different options.
     *
     * @return The created admin GUI inventory.
     */
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

    /**
     * Creates an item to reset all players' kills.
     *
     * @return The item to reset kills.
     */
    private ItemStack createResetKillsItem() {
        ItemStack item = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Reset Kills");
        item.setItemMeta(meta);

        return item;
    }

    /**
     * Creates an item to reward top players.
     *
     * @return The item to reward top players.
     */
    private ItemStack createRewardTopPlayersItem() {
        ItemStack item = new ItemStack(Material.GOLD_BLOCK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Reward Top Players");
        item.setItemMeta(meta);

        return item;
    }

    /**
     * Creates an item to spawn a mob with metadata.
     *
     * @return The item to spawn a mob with metadata.
     */
    private ItemStack createSpawnMobItem() {
        ItemStack item = new ItemStack(Material.MOB_SPAWNER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Spawn Mob with Metadata");
        item.setItemMeta(meta);

        return item;
    }

}

