package com.karthik.main.flixDB;

import com.karthik.main.flixDB.exception.ItemNotFoundException;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.github.jamm.MemoryMeter;

/**
 * DBStore implementation that is supposed to maintain all the in-memory
 * data structures necessary for the DBStore. Concurrent data
 * structures are used where ever necessary.
 */
public class DBStore implements DBInterface {
    private final ConcurrentHashMap<String, String> dbMap;
    private final CopyOnWriteArrayList<String> orderList;
    private final long maxMemorySize;

    /**
     * Constructs a DBStore instance with the given memory size.
     *
     *
     * @return DBResponse with OK even if the key doesn't exist
     * @param maxMemorySize Max memory size of the DBStore. Zero sets
     *                      it to memory available to the JVM
     */
    public DBStore(int maxMemorySize) {
        dbMap = new ConcurrentHashMap<String, String>();
        orderList = new CopyOnWriteArrayList<String>();
        if (maxMemorySize > 0) {
            this.maxMemorySize = maxMemorySize;
        } else {
            this.maxMemorySize = Runtime.getRuntime().freeMemory();
        }
    }

    /**
     * Helper method to update key-value in the store and in the LRU list.
     * The last write wins if there is a conflict.
     *
     * @param key to insert or update
     * @param value to insert or update
     */
    private void updateKey(String key, String value) {
        dbMap.put(key, value);
        updateOrderList(key);
        checkMemoryUsage();
    }

    /**
     * Insert a new key-value pair into the store and the LRU list.
     * The last write wins if there is a conflict.
     *
     * @param key to insert or update
     * @param value to insert or update
     */
    private void addKey(String key, String value) {
        dbMap.put(key, value);
        orderList.add(key);
        checkMemoryUsage();
    }

    /**
     * Insert or update new key-value pair into the store.
     * The last write wins if there is a conflict.
     *
     * @param key to insert or update
     * @param value to insert or update
     */
    public void set(String key, String value) {
        boolean keyExists = dbMap.containsKey(key);
        if (keyExists) {
            updateKey(key, value);
        } else {
            addKey(key, value);
        }
    }

    /**
     * Fetch the value for the given key from the store and
     * update the LRU list
     *
     * @return value of the given key
     * @param key to fetch from the DBStore
     */
    public String get(String key) throws ItemNotFoundException {
        if (dbMap.containsKey(key)) {
            updateOrderList(key);
            return dbMap.get(key);
        } else {
            throw new ItemNotFoundException("The item does not exist in the store");
        }
    }

    /**
     * Delete the key from the DBStore and the LRU list
     *
     * @param key to delete from the store
     */
    public void delete(String key) throws ItemNotFoundException {
        if (dbMap.containsKey(key)) {
            dbMap.remove(key);
            orderList.remove(key);
        } else {
            throw new ItemNotFoundException("The item does not exist in the store");
        }
    }

    /**
     * Handle the STREAM request from a client by fetching all the
     * key-value pairs from the DBStore.
     *
     * @return DBResponse with all the key-value pairs
     */
    public DBItem[] stream() {
        int noOfItems = orderList.size();
        if (noOfItems == 0) {
            return null;
        }
        DBItem[] items = new DBItem[noOfItems];
        int itemNo = 0;
        for (int i = orderList.size() - 1; i >= 0; i--) {
            DBItem item = new DBItem(orderList.get(i), dbMap.get(orderList.get(i)));
            items[itemNo] = item;
            itemNo++;
        }
        return items;
    }

    /**
     * Handle the DELETE request from a client by updating the
     * DBStore.
     *
     * @param key to put in front of the LRU
     */
    private void updateOrderList(String key) {
        orderList.remove(key);
        orderList.add(key);
    }

    /**
     * Handle the STREAM request from a client by fetching all the
     * key-value pairs from the DBStore.
     *
     * @return DBResponse with all the key-value pairs
     */
    private synchronized void checkMemoryUsage() {
        MemoryMeter meter = new MemoryMeter();
        long memoryUsed = meter.measureDeep(this);
        System.out.println("Memory consumed by the store: " + memoryUsed + "bytes");
        while (memoryUsed > this.maxMemorySize) {
            System.out.println("Memory consumption meets or exceeds set bound of "
                    + maxMemorySize + ". Removing LRU item");

            if (orderList.size() <= 0) {
                break;
            }
            dbMap.remove(orderList.get(0));
            orderList.remove(0);
            memoryUsed = meter.measureDeep(this);
            System.out.println("Memory consumption after LRU removal: " + memoryUsed + "bytes");
        }
    }
}
