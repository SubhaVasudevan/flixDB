package com.karthik.test.flixDBClientTests;

import com.karthik.main.flixDB.Constants;
import com.karthik.main.flixDB.DBItem;
import com.karthik.main.flixDB.DBRequest;
import com.karthik.main.flixDB.DBResponse;
import com.karthik.main.flixDBClient.DBClientImplementation;
import static org.junit.Assert.*;

import org.junit.*;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.mockito.Matchers.any;

public class DBClientImplementationTest {

    DBClientImplementation clientImplementation;
    DBResponse dbResponse;

    private static int PORT = 0;
    private static String SERVER = "0.0.0.0";
    private static String VALUE = "BAR";
    private static String KEY = "FOO";
    private static String OK = "< OK\n";

    @Before
    public void setUp() {
        clientImplementation = new DBClientImplementation(SERVER, PORT);
        dbResponse = new DBResponse("OK", new DBItem[]{new DBItem(KEY, VALUE)});
    }

    @After
    public void tearDown() {
    }

    @Test
    public void setCallsCloseHostInvalidKeyTest() throws IOException, ClassNotFoundException {
        //Arrange

        DBClientImplementation mockClient = Mockito.spy(clientImplementation);
        Mockito.doReturn(false).when(mockClient).verifyKey(Mockito.anyString());
        Mockito.doNothing().when(mockClient).closeHost();
        Mockito.doReturn(dbResponse).when(mockClient).sendRequest(any(DBRequest.class));

        //Act
        mockClient.set(KEY,VALUE);

        //Verify
        Mockito.verify(mockClient, Mockito.times(1)).closeHost();
        Mockito.verify(mockClient, Mockito.times(0)).sendRequest(any(DBRequest.class));

    }

    @Test
    public void setHappyCaseTest() throws Exception {
        //Arrange
        DBClientImplementation mockClient = Mockito.spy(clientImplementation);
        Mockito.doReturn(true).when(mockClient).verifyKey(Mockito.anyString());
        Mockito.doNothing().when(mockClient).closeHost();
        Mockito.doReturn(dbResponse).when(mockClient).sendRequest(any(DBRequest.class));

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        //Act
        mockClient.set(KEY,VALUE);

        //Verify
        Mockito.verify(mockClient, Mockito.times(0)).closeHost();
        Mockito.verify(mockClient, Mockito.times(1)).sendRequest(any(DBRequest.class));
        assertEquals(OK, outContent.toString());
    }

    @Test
    public void setHappyErrorCaseTest() throws Exception {
        //Arrange
        DBClientImplementation mockClient = Mockito.spy(clientImplementation);
        Mockito.doReturn(true).when(mockClient).verifyKey(Mockito.anyString());
        dbResponse.setResponseStatus(Constants.ERROR);
        Mockito.doNothing().when(mockClient).closeHost();
        Mockito.doReturn(dbResponse).when(mockClient).sendRequest(any(DBRequest.class));

        //Act
        mockClient.set(KEY,VALUE);

        //Verify
        Mockito.verify(mockClient, Mockito.times(1)).closeHost();
        Mockito.verify(mockClient, Mockito.times(1)).sendRequest(any(DBRequest.class));
    }

    @Test
    public void setCatchExceptionTest() throws Exception {
        //Arrange
        DBClientImplementation mockClient = Mockito.spy(clientImplementation);
        Mockito.doReturn(true).when(mockClient).verifyKey(Mockito.anyString());
        Mockito.doNothing().when(mockClient).closeHost();
        Mockito.doThrow(IOException.class).when(mockClient).sendRequest(any(DBRequest.class));

        //Act
        mockClient.set(KEY,VALUE);

        //Verify
        Mockito.verify(mockClient, Mockito.times(1)).closeHost();
        Mockito.verify(mockClient, Mockito.times(1)).sendRequest(any(DBRequest.class));
    }

    @Test
    public void getCallsCloseHostInvalidKeyTest() throws IOException, ClassNotFoundException {
        //Arrange
        DBClientImplementation mockClient = Mockito.spy(clientImplementation);
        Mockito.doReturn(false).when(mockClient).verifyKey(Mockito.anyString());
        Mockito.doNothing().when(mockClient).closeHost();

        //Act
        mockClient.get(KEY);

        //Verify
        Mockito.verify(mockClient, Mockito.times(1)).closeHost();
        Mockito.verify(mockClient, Mockito.times(0)).sendRequest(any(DBRequest.class));

    }

    @Test
    public void getExistingKeyHappyCaseTest() throws IOException, ClassNotFoundException {
        //Arrange
        DBClientImplementation mockClient = Mockito.spy(clientImplementation);
        Mockito.doReturn(true).when(mockClient).verifyKey(Mockito.anyString());
        Mockito.doNothing().when(mockClient).closeHost();
        Mockito.doReturn(dbResponse).when(mockClient).sendRequest(any(DBRequest.class));
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        //Act
        mockClient.get(KEY);

        //Verify
        Mockito.verify(mockClient, Mockito.times(0)).closeHost();
        Mockito.verify(mockClient, Mockito.times(1)).sendRequest(any(DBRequest.class));
        assertEquals("< " + Constants.VALUE + " " + VALUE.length() + "\n< " + VALUE + "\n", outContent.toString());
    }

    @Test
    public void getNonExistingKeyHappyCaseTest() throws IOException, ClassNotFoundException {
        //Arrange
        dbResponse.getItems()[0].setValue("");
        DBClientImplementation mockClient = Mockito.spy(clientImplementation);
        Mockito.doReturn(true).when(mockClient).verifyKey(Mockito.anyString());
        Mockito.doNothing().when(mockClient).closeHost();
        Mockito.doReturn(dbResponse).when(mockClient).sendRequest(any(DBRequest.class));
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        //Act
        mockClient.get(KEY);

        //Verify
        Mockito.verify(mockClient, Mockito.times(0)).closeHost();
        Mockito.verify(mockClient, Mockito.times(1)).sendRequest(any(DBRequest.class));
        assertEquals("< " + Constants.VALUE + " 0" + "\n", outContent.toString());
    }

    @Test
    public void getHappyErrorCaseTest() throws IOException, ClassNotFoundException {
        //Arrange
        dbResponse.setResponseStatus(Constants.ERROR);
        DBClientImplementation mockClient = Mockito.spy(clientImplementation);
        Mockito.doReturn(true).when(mockClient).verifyKey(Mockito.anyString());
        Mockito.doNothing().when(mockClient).closeHost();
        Mockito.doReturn(dbResponse).when(mockClient).sendRequest(any(DBRequest.class));
        //Act
        mockClient.get(KEY);

        //Verify
        Mockito.verify(mockClient, Mockito.times(1)).closeHost();
        Mockito.verify(mockClient, Mockito.times(1)).sendRequest(any(DBRequest.class));
    }

    @Test
    public void getCatchesExceptionTest() throws IOException, ClassNotFoundException {
        //Arrange
        DBClientImplementation mockClient = Mockito.spy(clientImplementation);
        Mockito.doReturn(true).when(mockClient).verifyKey(Mockito.anyString());
        Mockito.doNothing().when(mockClient).closeHost();
        Mockito.doThrow(IOException.class).when(mockClient).sendRequest(any(DBRequest.class));

        //Act
        mockClient.get(KEY);

        //Verify
        Mockito.verify(mockClient, Mockito.times(1)).closeHost();
        Mockito.verify(mockClient, Mockito.times(1)).sendRequest(any(DBRequest.class));
    }


    @Test
    public void deleteCallsCloseHostInvalidKeyTest() throws IOException, ClassNotFoundException {
        //Arrange
        DBClientImplementation mockClient = Mockito.spy(clientImplementation);
        Mockito.doReturn(false).when(mockClient).verifyKey(Mockito.anyString());
        Mockito.doNothing().when(mockClient).closeHost();
        Mockito.doReturn(dbResponse).when(mockClient).sendRequest(any(DBRequest.class));

        //Act
        mockClient.delete(KEY);

        //Verify
        Mockito.verify(mockClient, Mockito.times(1)).closeHost();
        Mockito.verify(mockClient, Mockito.times(0)).sendRequest(any(DBRequest.class));

    }

    @Test
    public void deleteExistingKeyHappyCaseTest() throws IOException, ClassNotFoundException {
        //Arrange
        DBClientImplementation mockClient = Mockito.spy(clientImplementation);
        Mockito.doReturn(true).when(mockClient).verifyKey(Mockito.anyString());
        Mockito.doNothing().when(mockClient).closeHost();
        Mockito.doReturn(dbResponse).when(mockClient).sendRequest(any(DBRequest.class));
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        //Act
        mockClient.delete(KEY);

        //Verify
        Mockito.verify(mockClient, Mockito.times(0)).closeHost();
        Mockito.verify(mockClient, Mockito.times(1)).sendRequest(any(DBRequest.class));
        assertEquals(OK, outContent.toString());
    }

    @Test
    public void deleteHappyErrorCaseTest() throws IOException, ClassNotFoundException {
        //Arrange
        dbResponse.setResponseStatus(Constants.ERROR);
        DBClientImplementation mockClient = Mockito.spy(clientImplementation);
        Mockito.doReturn(true).when(mockClient).verifyKey(Mockito.anyString());
        Mockito.doNothing().when(mockClient).closeHost();
        Mockito.doReturn(dbResponse).when(mockClient).sendRequest(any(DBRequest.class));

        //Act
        mockClient.delete(KEY);

        //Verify
        Mockito.verify(mockClient, Mockito.times(1)).closeHost();
        Mockito.verify(mockClient, Mockito.times(1)).sendRequest(any(DBRequest.class));
    }

    @Test
    public void deleteCatchesExceptionTest() throws IOException, ClassNotFoundException {
        //Arrange
        DBClientImplementation mockClient = Mockito.spy(clientImplementation);
        Mockito.doReturn(true).when(mockClient).verifyKey(Mockito.anyString());
        Mockito.doNothing().when(mockClient).closeHost();
        Mockito.doThrow(IOException.class).when(mockClient).sendRequest(any(DBRequest.class));

        //Act
        mockClient.delete(KEY);

        //Verify
        Mockito.verify(mockClient, Mockito.times(1)).closeHost();
        Mockito.verify(mockClient, Mockito.times(1)).sendRequest(any(DBRequest.class));
    }

    @Test
    public void streamHappyTest() throws IOException, ClassNotFoundException {
        //Arrange
        DBClientImplementation mockClient = Mockito.spy(clientImplementation);
        Mockito.doNothing().when(mockClient).closeHost();
        Mockito.doReturn(dbResponse).when(mockClient).sendRequest(any(DBRequest.class));
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        //Act
        mockClient.stream();

        //Verify
        Mockito.verify(mockClient, Mockito.times(0)).closeHost();
        Mockito.verify(mockClient, Mockito.times(1)).sendRequest(any(DBRequest.class));
        String expectedOutput = "< " + Constants.KEY + " " + dbResponse.getItems()[0].getKey() + " " + Constants.VALUE + " "
                + dbResponse.getItems()[0].getValue().length() + "\r\n" + "< " + dbResponse.getItems()[0].getValue() + "\n";
        assertEquals(expectedOutput, outContent.toString());
    }

    @Test
    public void streamHappyEmptyStoreTest() throws IOException, ClassNotFoundException {
        //Arrange
        dbResponse.setItems(null);
        DBClientImplementation mockClient = Mockito.spy(clientImplementation);
        Mockito.doNothing().when(mockClient).closeHost();
        Mockito.doReturn(dbResponse).when(mockClient).sendRequest(any(DBRequest.class));
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        //Act
        mockClient.stream();

        //Verify
        Mockito.verify(mockClient, Mockito.times(0)).closeHost();
        Mockito.verify(mockClient, Mockito.times(1)).sendRequest(any(DBRequest.class));
        String expectedOutput = "< " + Constants.EMPTY_STORE + "\n";
        assertEquals(expectedOutput, outContent.toString());
    }

    @Test
    public void streamHappyErrorTest() throws IOException, ClassNotFoundException {
        //Arrange
        dbResponse.setResponseStatus(Constants.ERROR);
        DBClientImplementation mockClient = Mockito.spy(clientImplementation);
        Mockito.doNothing().when(mockClient).closeHost();
        Mockito.doReturn(dbResponse).when(mockClient).sendRequest(any(DBRequest.class));

        //Act
        mockClient.stream();

        //Verify
        Mockito.verify(mockClient, Mockito.times(1)).closeHost();
        Mockito.verify(mockClient, Mockito.times(1)).sendRequest(any(DBRequest.class));
    }

    @Test
    public void streamCatchesExceptionTest() throws IOException, ClassNotFoundException {
        //Arrange
        DBClientImplementation mockClient = Mockito.spy(clientImplementation);
        Mockito.doNothing().when(mockClient).closeHost();
        Mockito.doThrow(IOException.class).when(mockClient).sendRequest(any(DBRequest.class));

        //Act
        mockClient.stream();

        //Verify
        Mockito.verify(mockClient, Mockito.times(1)).closeHost();
        Mockito.verify(mockClient, Mockito.times(1)).sendRequest(any(DBRequest.class));
    }
}