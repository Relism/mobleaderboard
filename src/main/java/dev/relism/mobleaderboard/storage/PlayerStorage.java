package dev.relism.mobleaderboard.storage;

import com.mongodb.client.FindIterable;
import dev.relism.mobleaderboard.Mobleaderboard;
import org.bson.Document;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

/**
 * A storage class for managing player-specific data asynchronously.
 */
public class PlayerStorage implements Storage {
    private final MongoWrapper mongoWrapper;
    private final String databaseName = "mbl-players";

    private final String playerUUID;

    /**
     * Constructs a PlayerStorage instance using the provided Plugin and Player.
     *
     * @param plugin the Plugin instance providing access to the MongoWrapper
     * @param player the Player whose data will be managed by this storage
     */
    public PlayerStorage(Mobleaderboard plugin, Player player) {
        this.mongoWrapper = plugin.getMongoWrapperInstance();
        this.playerUUID = String.valueOf(player.getUniqueId()); // Assuming getUniqueId() retrieves the UUID
    }

    /**
     * Checks if a player data exists in the MongoDB database.
     *
     * @return true if a document with type "playerdata" exists for the player, false otherwise.
     */
    public boolean playerDataExists() {
        // Check if the document with type "mobdata" exists for the player
        return findDocuments("type", "playerdata")
                .thenApply(findIterable -> findIterable.into(new ArrayList<>()))
                .thenApply(documents -> documents.size() > 0)
                .join();
    }

    /**
     * Retrieves the value of a specific field from a player's document asynchronously.
     *
     * @param documentType the identifier of the document
     * @param fieldToGet         the name of the field to retrieve
     * @return a CompletableFuture containing the value of the specified field
     */
    @Override
    public CompletableFuture<Object> getFieldValue(String documentType, String fieldToGet) {
        return mongoWrapper.getFieldValue(databaseName, playerUUID, "type", documentType, fieldToGet);
    }

    /**
     * Sets the value of a specific field in a player's document asynchronously.
     *
     * @param documentType the identifier of the document
     * @param fieldToSet         the name of the field to update
     * @param newValue           the new value to set for the field
     * @return a CompletableFuture that completes when the field is updated
     */
    @Override
    public CompletableFuture<Void> setFieldValue(String documentType, String fieldToSet, Object newValue) {
        return mongoWrapper.setFieldValue(databaseName, playerUUID, "type", documentType, fieldToSet, newValue);
    }

    /**
     * Inserts a document into the player's collection asynchronously.
     *
     * @param document the document to insert
     * @return a CompletableFuture that completes when the document is inserted
     */
    @Override
    public CompletableFuture<Void> insertDocument(Document document) {
        return mongoWrapper.insertDocument(databaseName, playerUUID, document);
    }

    /**
     * Finds documents in the player's collection based on a field and its value asynchronously.
     *
     * @param fieldIdentifier the name of the field to search
     * @param valueIdentifier the value to search for in the field
     * @return a CompletableFuture containing a FindIterable of matching documents
     */
    @Override
    public CompletableFuture<FindIterable<Document>> findDocuments(String fieldIdentifier, Object valueIdentifier) {
        return mongoWrapper.findDocuments(databaseName, playerUUID, fieldIdentifier, valueIdentifier);
    }

}
