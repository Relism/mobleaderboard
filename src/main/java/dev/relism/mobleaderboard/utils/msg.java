package dev.relism.mobleaderboard.utils;

import dev.relism.mobleaderboard.Mobleaderboard;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Utility class for handling messaging functionalities within the plugin.
 */
public class msg {

    private static Mobleaderboard main = Mobleaderboard.getPlugin();

    /**
     * Represents the color character used in Minecraft chat formatting.
     */
    public static final char COLOR_CHAR = '&';

    /**
     * Sends a color-translated message to a specific player.
     *
     * @param p       The player to whom the message will be sent.
     * @param message The message to be sent.
     */
    public static void send(Player p, String message) {
        p.sendMessage(translateColorCodes(message));
    }

    /**
     * Sends a color-translated message to all players on the server.
     *
     * @param message The message to be broadcasted.
     */
    public static void broadcast(String message) {
        Bukkit.broadcastMessage(translateColorCodes(message));
    }

    /**
     * Sends a color-translated debug message to the console if the debug configuration is enabled.
     *
     * @param message The debug message to be sent.
     */
    public static void debug(String message){
        if(main.getConfig().getBoolean("debug")){
            log(message);
        }
    }

    /**
     * Sends a color-translated message to the console.
     *
     * @param message The message to be logged in the console.
     */
    public static void log(String message) {
        Bukkit.getConsoleSender().sendMessage(translateColorCodes(message));
    }

    /**
     * Translates color codes in the provided text using the vanilla minecraft color code identifier.
     *
     * @param text The text containing color codes to be translated.
     * @return The text with translated color codes.
     */
    public static String translateColorCodes(String text) {
        return ChatColor.translateAlternateColorCodes(COLOR_CHAR, text);
    }
}
