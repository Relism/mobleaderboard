package dev.relism.mobleaderboard.storage;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import dev.relism.mobleaderboard.Mobleaderboard;
import dev.relism.mobleaderboard.utils.msg;
import org.bson.Document;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;

public class MongoWrapper {

    /**
     * A wrapper class for interacting with MongoDB asynchronously.
     */
    private final MongoClient mongoClient;

    private final Mobleaderboard plugin = Mobleaderboard.getPlugin();

    /**
     * Constructs a new MongoWrapper with the provided connection string.
     *
     * @param connectionString the MongoDB connection string
     */
    public MongoWrapper(String connectionString) {
        this.mongoClient = MongoClients.create(connectionString);
    }

    /**
     * Inserts a document into the specified collection asynchronously.
     *
     * @param databaseName   the name of the database
     * @param collectionName the name of the collection
     * @param document       the document to insert
     * @return a CompletableFuture that completes when the document is inserted.
     *         May encounter an exception if the database or collection doesn't exist.
     */
    public CompletableFuture<Void> insertDocument(String databaseName, String collectionName, Document document) {
        String queryType = "insertDocument";
        CompletableFuture<Void> result = CompletableFuture.runAsync(() -> {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            if (database == null) { sendErrorMsg(queryType, "Database doesn't exist", databaseName, collectionName, document.toString()); return; }
            MongoCollection<Document> collection = database.getCollection(collectionName);
            if (collection == null) { sendErrorMsg(queryType, "Collection doesn't exist", databaseName, collectionName, document.toString()); return; }
            collection.insertOne(document);
        });
        result.exceptionally(ex -> { String errorMsg = "Query Exception occurred: " + ex.getMessage();  msg.log(errorMsg); return null; });
        return result;
    }


    /**
     * Finds documents in the specified collection based on a field and its value asynchronously.
     *
     * @param databaseName   the name of the database
     * @param collectionName the name of the collection
     * @param fieldName      the name of the field to search
     * @param value          the value to search for in the field
     * @return a CompletableFuture containing a FindIterable of matching documents.
     *         May return null if the database, collection, or field is null or if an exception occurs during execution.
     */
    public CompletableFuture<FindIterable<Document>> findDocuments(String databaseName, String collectionName, String fieldName, Object value) {
        String queryType = "findDocuments";
        return CompletableFuture.supplyAsync(() -> {
            try {
                MongoDatabase database = mongoClient.getDatabase(databaseName);
                if (database == null) { sendErrorMsg(queryType, "Database doesn't exist", databaseName, collectionName, fieldName, value.toString()); return null; }
                MongoCollection<Document> collection = database.getCollection(collectionName);
                if (collection == null) { sendErrorMsg(queryType, "Collection doesn't exist", databaseName, collectionName, fieldName, value.toString()); return null; }
                return collection.find(Filters.eq(fieldName, value));
            } catch (Exception ex) { String errorMsg = "Exception occurred: " + ex.getMessage(); msg.log(errorMsg); return null; }
        }).exceptionally(ex -> { String errorMsg = "Query Exception occurred: " + ex.getMessage();  msg.log(errorMsg); return null; });
    }

    /**
     * Retrieves a specific field value from a document in the specified collection asynchronously.
     *
     * @param databaseName   the name of the database
     * @param collectionName the name of the collection
     * @param fieldName      the name of the field to search
     * @param value          the value to search for in the field
     * @param fieldToGet     the name of the field to retrieve
     * @return a CompletableFuture containing the value of the specified field.
     *         May return null if the document, field, or database doesn't exist, or if an exception occurs during execution.
     */
    public CompletableFuture<Object> getFieldValue(String databaseName, String collectionName, String fieldName, Object value, String fieldToGet) {
        String queryType = "getFieldValue";
        CompletableFuture<Object> result = CompletableFuture.supplyAsync(() -> {
            try {
                MongoDatabase database = mongoClient.getDatabase(databaseName);
                if (database == null) { sendErrorMsg(queryType, "Database doesn't exist", databaseName, collectionName, fieldName, value.toString(), fieldToGet); return null; }
                MongoCollection<Document> collection = database.getCollection(collectionName);
                if (collection == null) { sendErrorMsg(queryType, "Collection doesn't exist", databaseName, collectionName, fieldName, value.toString(), fieldToGet); return null; }
                Document document = collection.find(Filters.eq(fieldName, value)).first();
                if (document == null) { sendErrorMsg(queryType, "Document doesn't exist", databaseName, collectionName, fieldName, value.toString(), fieldToGet); return null; }
                if(document.get(fieldToGet) == null) { sendErrorMsg(queryType, "Field doesn't exist, or its data is null (very unlikely)", databaseName, collectionName, fieldName, value.toString(), fieldToGet); return null; };
                return document.get(fieldToGet);
            } catch (Exception ex) { String errorMsg = "Exception occurred: " + ex.getMessage(); msg.log(errorMsg); return null; }
        });
        result.exceptionally(ex -> { String errorMsg = "Query Exception occurred: " + ex.getMessage();  msg.log(errorMsg); return null; });
        return result;
    }

    /**
     * Sets a specific field value in a document in the specified collection asynchronously.
     *
     * @param databaseName   the name of the database
     * @param collectionName the name of the collection
     * @param fieldName      the name of the field to search
     * @param value          the value to search for in the field
     * @param fieldToSet     the name of the field to update
     * @param newValue       the new value to set for the field
     * @return a CompletableFuture that completes when the field is updated.
     *         May return null if the database, collection, field, or newValue is null, or if an exception occurs during execution.
     */
    public CompletableFuture<Void> setFieldValue(String databaseName, String collectionName, String fieldName, Object value, String fieldToSet, Object newValue) {
        String queryType = "setFieldValue";
        return CompletableFuture.runAsync(() -> {
            try {
                MongoDatabase database = mongoClient.getDatabase(databaseName);
                if (database == null) { sendErrorMsg(queryType, "Database doesn't exist", databaseName, collectionName, fieldName, value.toString(), fieldToSet, newValue.toString()); return; }
                MongoCollection<Document> collection = database.getCollection(collectionName);
                if (collection == null) { sendErrorMsg(queryType, "Collection doesn't exist", databaseName, collectionName, fieldName, value.toString(), fieldToSet, newValue.toString()); return; }
                collection.updateOne(Filters.eq(fieldName, value), new Document("$set", new Document(fieldToSet, newValue)));
            } catch (Exception ex) { String errorMsg = "Exception occurred: " + ex.getMessage(); msg.log(errorMsg); }
        }).exceptionally(ex -> { String errorMsg = "Query Exception occurred: " + ex.getMessage();  msg.log(errorMsg); return null; });
    }

    /**
     * Asynchronously fetches a sorted list of top players based on kills.
     *
     * @param size The maximum number of top players to retrieve.
     * @return A CompletableFuture containing a sorted list of top players' documents based on special kills.
     *         The CompletableFuture may complete exceptionally if an error occurs during the operation.
     */
    public CompletableFuture<List<Document>> fetchSortedTopPlayersAsync(int size) {
        CompletableFuture<List<Document>> futureResult = new CompletableFuture<>();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                MongoDatabase database = mongoClient.getDatabase("mbl-players");
                List<Document> topPlayers = new ArrayList<>();
                for (String collectionName : database.listCollectionNames()) {
                    if (collectionName.equals("system.indexes")) {
                        continue;
                    }
                    MongoCollection<Document> collection = database.getCollection(collectionName);
                    FindIterable<Document> playerDataDocuments = collection.find(new Document("type", "playerdata"));
                    try (MongoCursor<Document> cursor = playerDataDocuments.iterator()) {
                        while (cursor.hasNext()) {
                            topPlayers.add(cursor.next());
                        }
                    }
                }
                topPlayers.sort(Comparator.comparingInt(doc -> -doc.getInteger("kills")));

                if (topPlayers.size() > size) {
                    topPlayers = topPlayers.subList(0, size);
                }
                futureResult.complete(topPlayers);
            } catch (Exception e) {
                futureResult.completeExceptionally(e);
            }
        });

        return futureResult;
    }


    /**
     * returns the MongoDB client instance used by the MongoWrapper.
     *
     * @return The MongoDB client instance.
     */
    public MongoClient getMongoClient(){
        return mongoClient;
    }

    /**
     * Closes the MongoDB client.
     *
     * @return a CompletableFuture that completes when the client is closed
     */
    public CompletableFuture<Void> close() {
        return CompletableFuture.runAsync(() -> mongoClient.close());
    }

    /**
     * Logs an error message related to MongoDB operations on the MongoWrapper.
     *
     * @param queryType    the type of query or operation being performed
     * @param errorMessage the specific error message describing the issue
     * @param queryParams  additional parameters used to describe the reference of the query
     */
    private void sendErrorMsg(String queryType, String errorMessage, String... queryParams) {
        StringJoiner errorParams = new StringJoiner(";");
        for (String param : queryParams) {
            errorParams.add(param == null ? "null" : param);
        }
        msg.log("&cMongoWrapperError on &b" + queryType + "&c, reference &f-> &e" + errorParams);
        msg.log("&f ↳  &c&n" + errorMessage);
    }

}
