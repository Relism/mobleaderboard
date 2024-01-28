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
        msg.log("&aPlugin has been enabled!");
        plugin = this;
        saveDefaultConfig();
        connectDatabase();

        // Registering listeners
        msg.log("&eRegistering listeners...");
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new MobKillListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryGUIListener(), this);

        // Registering commands
        msg.log("&eRegistering commands...");
        getCommand("test").setExecutor(new TestGUICommand(plugin));
        getCommand("atest").setExecutor(new AdminTestGUICommand(plugin));

        msg.log("&aInitialization complete!");
    }


    @Override
    public void onDisable() {
        disconnectDatabase();
        msg.log("&cPlugin has been disabled!");
    }

    /**
     * Connects to the database using the MongoDB URI provided in the config.yml.
     */
    public void connectDatabase() {
        String mongoUri = getConfig().getString("mongouri");

        if (mongoUri == null || !(mongoUri instanceof String)) {
            msg.log("Invalid MongoDB URI in the config.yml. Please provide a valid string URI.");
            return;
        }

        msg.log("&aEstablishing database connection...");
        this.mongoWrapperInstance = new MongoWrapper(mongoUri);
    }


    /**
     * Disconnects from the database.
     */
    public void disconnectDatabase(){
        msg.log("&eClosing database connection...");
        mongoWrapperInstance.close().thenRun(() -> msg.log("&aClosed database connection."));
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
