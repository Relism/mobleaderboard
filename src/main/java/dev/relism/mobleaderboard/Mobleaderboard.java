package dev.relism.mobleaderboard;

import dev.relism.mobleaderboard.commands.AdminTestGUICommand;
import dev.relism.mobleaderboard.commands.TestGUICommand;
import dev.relism.mobleaderboard.listeners.MobKillListener;
import dev.relism.mobleaderboard.listeners.PlayerJoinListener;
import dev.relism.mobleaderboard.listeners.InventoryGUIListener;
import dev.relism.mobleaderboard.storage.MongoWrapper;
import dev.relism.mobleaderboard.utils.msg;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * This plugin manages mob kills leaderboards within the game environment.
 * Author: Relism
 * Start Date: 08/01/2024
 * GitHub Repository: <a href="https://github.com/Relism/MobleaderboardPlugin">Relism/MobleaderboardPlugin</a>
 */
public final class Mobleaderboard extends JavaPlugin {
    private static Mobleaderboard plugin;
    private static MongoWrapper mongoWrapperInstance;

    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();
        connectDatabase();
        //listeners registering
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new MobKillListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryGUIListener(), this);

        //commands registering
        getCommand("test").setExecutor(new TestGUICommand(plugin));
        getCommand("atest").setExecutor(new AdminTestGUICommand(plugin));
    }

    @Override
    public void onDisable() {
        disconnectDatabase();
    }

    /**
     * Connects to the database using the MongoDB URI provided in the config.yml.
     */
    public void connectDatabase(){
        msg.log("Establishing database connection...");
        this.mongoWrapperInstance = new MongoWrapper(getConfig().getString("mongouri"));
    }

    /**
     * Disconnects from the database.
     */
    public void disconnectDatabase(){
        msg.log("Closing database connection...");
        mongoWrapperInstance.close().thenRun(() -> msg.log("Closed database connection."));
    }

    public MongoWrapper getMongoWrapperInstance() { return mongoWrapperInstance; }

    /**
     * Retrieves the plugin instance.
     *
     * @return The instance of the Mobleaderboard plugin.
     */
    public static Mobleaderboard getPlugin() {
        return plugin;
    }
}
