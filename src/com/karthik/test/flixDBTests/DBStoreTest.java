package com.karthik.test.flixDBTests;

import com.karthik.main.flixDB.DBItem;
import com.karthik.main.flixDB.DBStore;
import com.karthik.main.flixDB.exception.ItemNotFoundException;
import static org.junit.Assert.*;

import org.junit.*;


public class DBStoreTest {
    DBStore dbStore;
    public static String KEY1 = "foo";
    public static String VALUE1 = "bar";
    public static String KEY2 = "foo1";
    public static String VALUE2 = "bar1";
    public static String KEY3 = "foo2";
    public static String VALUE3 = "bar2";

    @Before
    public void setUp() {
        dbStore =  new DBStore(1000);

    }

    @After
    public void tearDown() {
    }

    @Test
    public void setValueToKeyTest() throws ItemNotFoundException {
        //Act
        dbStore.set(KEY1,VALUE1);

        //Verify
        String value = dbStore.get(KEY1);
        assertEquals(VALUE1, value);
    }

    @Test
    public void updateValueToKeyTest() throws ItemNotFoundException {
        //Act
        dbStore.set(KEY1,VALUE1);

        //Verify
        String value = dbStore.get(KEY1);
        assertEquals(VALUE1, value);

        //Act
        dbStore.set(KEY1,VALUE2);

        //Verify
        value = dbStore.get(KEY1);
        assertEquals(VALUE2, value);
    }

    @Test
    public void getExistingKeyTest() throws ItemNotFoundException {
        //Arrange
        dbStore.set(KEY1, VALUE1);

        //Act
        String value = dbStore.get(KEY1);

        //Verify
        assertEquals(VALUE1, value);
    }

    @Test(expected = ItemNotFoundException.class)
    public void getNonExistingKeyThrowsExceptionTest() throws ItemNotFoundException {

        //Act & Verify
        dbStore.get(KEY1);
    }

    @Test(expected = ItemNotFoundException.class)
    public void deleteEntryOnLRUTest1() throws ItemNotFoundException{
        //Arrange
        dbStore = new DBStore(300);
        dbStore.set(KEY1, VALUE1);
        dbStore.set(KEY2, VALUE2);

        //Act & Verify
        dbStore.get(KEY1);
    }

    @Test(expected = ItemNotFoundException.class)
    public void deleteEntryOnLRUTest2() throws ItemNotFoundException{
        //Arrange
        dbStore = new DBStore(600);
        dbStore.set(KEY1, VALUE1);
        dbStore.set(KEY2, VALUE2);
        dbStore.get(KEY1);
        dbStore.set(KEY3, VALUE3);

        //Act & Verify
        assertEquals(dbStore.get(KEY1), VALUE1);
        assertEquals(dbStore.get(KEY3), VALUE3);
        dbStore.get(KEY2);
    }

    @Test(expected = ItemNotFoundException.class)
    public void deleteTest() throws ItemNotFoundException {
        //Arrange & Verify
        dbStore.set(KEY1, VALUE1);
        assertEquals(dbStore.get(KEY1), VALUE1);

        //Act
        dbStore.delete(KEY1);

        //Verify
        dbStore.get(KEY1);
    }

    @Test
    public void streamTest() {
        //Arrange
        dbStore.set(KEY1, VALUE1);
        dbStore.set(KEY2, VALUE2);
        DBItem[] items = new DBItem[2];
        items[0] = new DBItem(KEY2, VALUE2);
        items[1] = new DBItem(KEY1, VALUE1);

        //Act
        DBItem[] retrievedItems = dbStore.stream();

        //Verify
        assertEquals(retrievedItems[0].getKey(), KEY2);
        assertEquals(retrievedItems[0].getValue(), VALUE2);
        assertEquals(retrievedItems[1].getKey(), KEY1);
        assertEquals(retrievedItems[1].getValue(), VALUE1);
    }

    @Test
    public void streamEmptyStoreTest() {
        //Act & Verify
        assertNull(dbStore.stream());
    }

    @Test
    public void setVariableLengthEntriesTest() throws ItemNotFoundException {
        // Test various string len here to check memory consumption
        String key = "TestKey";
        String val = "TestValue";

        dbStore.set(key, val);
        assertEquals(val,dbStore.get(key));

    }


}