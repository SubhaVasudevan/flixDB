package com.karthik.main.flixDB;

import com.karthik.main.flixDB.exception.ItemNotFoundException;

/**
 * Interface for the DBStore.
 */
public interface DBInterface {

    /**
     * Insert Key, Value pair into the DBStore
     * @param key is the object used to index into the store
     * @param value is the object corresponding to a unique key
     */
    void set(String key, String value);

    /**
     * Retrieve the object corresponding to the provided key
     * @param key is the object used to index into the store
     * @return the value corresponding to the provided key
     * @throws ItemNotFoundException if there is an error when looking up the object store
     */
    String get(String key) throws ItemNotFoundException;

    /**
     * Delete the object corresponding to the provided key
     * @param key is the object used to index into the store
     * @throws ItemNotFoundException if there is an error when looking up the object store
     */
    void delete(String key) throws ItemNotFoundException;

    /**
     * Stream all the key-value pairs in the store
     */
    DBItem[] stream();
}
