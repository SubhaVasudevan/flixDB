package com.karthik.main.flixDBClient;

import com.karthik.main.flixDB.DBItem;
import com.karthik.main.flixDB.DBRequest;
import com.karthik.main.flixDB.DBResponse;
import com.karthik.main.flixDB.Constants;
import com.karthik.main.flixDB.exception.SocketInputException;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class DBClientImplementation{
    private final String server;
    private final int port;
    private final int socketTimeout;
    private Socket socket;
    public boolean endClient = false;

    private static final int TIMEOUT = 100;

    /**
     * Constructs a DBClientImplementation connected to a server.
     *
     * @param server is the DNS reference to the server
     * @param port   is the port on which the server is listening
     */
    public DBClientImplementation(String server, int port, int socketTimeout) {
        this.server = server;
        this.port = port;
        this.socketTimeout = socketTimeout;
    }

    public DBClientImplementation(String server, int port) {
        this(server, port, TIMEOUT);
    }

    /**
     * Creates a socket connected to the server to make a request.
     *
     * @return Socket connected to server
     * @throws SocketInputException if unable to create or connect socket
     */
    public void connectHost() throws SocketInputException {
        try {
            socket = new Socket(server, port);
            socket.setSoTimeout(socketTimeout);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            throw new SocketInputException("Error: Could not connect to the given host " + e);
        } catch (IOException f) {
            f.printStackTrace();
            throw new SocketInputException("Error:Could not create socket " + f);
        } catch (IllegalArgumentException g) {
            g.printStackTrace();
            throw new SocketInputException("Error: Bad argument. Could not connect to the given host " + g);
        }
    }

    /**
     * Closes a socket.
     * Best effort, ignores error since the response has already been received.
     *
     */
    public void closeHost() {
        try {
            System.out.println("< " + Constants.ERROR);
            socket.close();
            endClient = true;
        } catch (Exception e) {
            System.out.println("Error: Failed to close the socket " + e);
        }
    }

    /**
     * Verifies a given key
     *
     * Method Public for testing purposes.
     *
     * @param key to verify
     * @return true or false
     */
    public boolean verifyKey(String key) {
        if (key == null || key.equals("") || key.length() > 100) {
            return false;
        }
        return true;
    }

    /**
     * Sends the given request to the backend DBStore.
     * and receive the response.
     *
     * Method Public for testing purposes.
     *
     * @param request to be sent to the server
     * @return response from the server
     */
    public DBResponse sendRequest(DBRequest request) throws IOException, ClassNotFoundException {
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        oos.writeObject(request);
        oos.flush();
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        return (DBResponse) ois.readObject();
    }

    /**
     * Issues a SET request to the DBStore.
     *
     * @param key to write into the DBStore
     * @param value to write into the DBStore
     */
    public void set(String key, String value) {
        if(!verifyKey(key)) {
            closeHost();
            return;
        }

        DBItem item = new DBItem(key, value);
        DBRequest request = new DBRequest(Constants.SET, item);
        DBResponse response = null;
        try {
            response = sendRequest(request);
        } catch (Exception e){
            closeHost();
            return;
        }

        if (response != null) {
            if(response.getResponseStatus().equals(Constants.ERROR)) {
                closeHost();
                return;
            }
            System.out.println("< " + response.getResponseStatus());
        } else {
            closeHost();
        }
    }

    /**
     * Issues a GET request to the DBStore.
     *
     * @param key to get value from the DBStore
     */
    public void get(String key) {
        if(!verifyKey(key)) {
            closeHost();
            return;
        }

        DBItem item = new DBItem(key);
        DBRequest request = new DBRequest(Constants.GET, item);
        DBResponse response = null;

        try {
            response = sendRequest(request);
        } catch (Exception e) {
            closeHost();
            return;
        }

        if (response != null) {
            if(response.getResponseStatus().equals(Constants.ERROR)) {
                closeHost();
                return;
            }
            DBItem responseItem = response.getItems()[0];
            String value = responseItem.getValue();
            System.out.println("< " + Constants.VALUE + " " + value.length());
            if(value.length() > 0) {
                System.out.println("< " + value);
            }
        } else {
            closeHost();
        }
    }

    /**
     * Issues a DEL request to the DBStore.
     *
     * @param key to delete value from the DBStore
     */
    public void delete(String key) {
        if(!verifyKey(key)) {
            closeHost();
            return;
        }

        DBItem item = new DBItem(key);
        DBRequest request = new DBRequest(Constants.DELETE, item);
        DBResponse response = null;
        try {
            response = sendRequest(request);
        } catch (Exception e){
            closeHost();
            return;
        }

        if (response != null) {
            if(response.getResponseStatus().equals(Constants.ERROR)) {
                closeHost();
                return;
            }
            System.out.println("< " + response.getResponseStatus());
        } else {
            closeHost();
        }
    }

    /**
     * Issues a STREAM request to the DBStore.
     *
     */
    public void stream() {
        DBRequest request = new DBRequest(Constants.STREAM);
        DBResponse response = null;

        try {
            response = sendRequest(request);
        } catch (Exception e) {
            closeHost();
            return;
        }

        if (response != null) {
            if(response.getResponseStatus().equals(Constants.ERROR)) {
                closeHost();
            }
            if(response.getItems() == null) {
                System.out.println("< " + Constants.EMPTY_STORE);
                return;
            }
            int noOfDBItems = response.getItems().length;
            for(int i = 0 ; i < noOfDBItems; i++) {
                System.out.print("< " + Constants.KEY + " " + response.getItems()[i].getKey() + " " + Constants.VALUE + " "
                    + response.getItems()[i].getValue().length() + "\r\n");
                System.out.println("< " + response.getItems()[i].getValue());
            }
        } else {
            closeHost();
        }

    }
}
