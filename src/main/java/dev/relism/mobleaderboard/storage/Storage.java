package dev.relism.mobleaderboard.storage;

import com.mongodb.client.FindIterable;
import org.bson.Document;

import java.util.concurrent.CompletableFuture;

/**
 * A generic interface representing asynchronous storage operations.
 */
public interface Storage {

    /**
     * Retrieves the value of a specific field from a document asynchronously.
     *
     * @param documentType the identifier of the document
     * @param fieldToGet         the name of the field to retrieve
     * @return a CompletableFuture containing the value of the specified field
     */
    CompletableFuture<Object> getFieldValue(String documentType, String fieldToGet);

    /**
     * Sets the value of a specific field in a document asynchronously.
     *
     * @param documentType the identifier of the document
     * @param fieldToSet         the name of the field to update
     * @param newValue           the new value to set for the field
     * @return a CompletableFuture that completes when the field is updated
     */
    CompletableFuture<Void> setFieldValue(String documentType, String fieldToSet, Object newValue);

    /**
     * Inserts a document asynchronously.
     *
     * @param document the document to insert
     * @return a CompletableFuture that completes when the document is inserted
     */
    CompletableFuture<Void> insertDocument(Document document);

    /**
     * Finds documents based on a field and its value asynchronously.
     *
     * @param fieldIdentifier the name of the field to search
     * @param valueIdentifier the value to search for in the field
     * @return a CompletableFuture containing a FindIterable of matching documents
     */
    CompletableFuture<FindIterable<Document>> findDocuments(String fieldIdentifier, Object valueIdentifier);
}
