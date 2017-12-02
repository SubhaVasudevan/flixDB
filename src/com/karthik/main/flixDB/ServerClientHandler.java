package com.karthik.main.flixDB;

import com.karthik.main.flixDB.exception.ItemNotFoundException;

import java.net.Socket;
import java.io.*;

public class ServerClientHandler implements NetworkHandlerInterface {
    private DBStore dbStore;
    private ThreadPool threadPool;

    /**
     * Constructs a ServerClientHandler with ThreadPool of a single thread.
     *
     * @param dbStore dbStore to carry out requests
     */
    public ServerClientHandler(DBStore dbStore) {
        this(dbStore, 1);
    }

    /**
     * Constructs a ServerClientHandler with ThreadPool of thread equal to
     * the number given as connections.
     *
     * @param dbStore dbStore to carry out requests
     * @param connections number of threads in threadPool to service requests
     */
    public ServerClientHandler(DBStore dbStore, int connections) {
        threadPool = new ThreadPool(connections);
        this.dbStore = dbStore;
    }

    /**
     * Creates a job to service the request for a socket and enqueues that job
     * in the thread pool. Ignore all InterruptedExceptions.
     *
     * @param client Socket connected to the client with the request
     */
    @Override
    public void handle(Socket client) {
        try {
            threadPool.addJob(new ClientHandler(client));
        }
        catch(Exception e) {
        }
    }

    /**
     * Runnable class containing routine to service a request from the client.
     */
    private class ClientHandler implements Runnable {
        private Socket server;

        /**
         * Construct a ClientHandler.
         *
         * @param server Socket connected to client with the request
         */
        public ClientHandler(Socket server) {
            this.server = server;
        }

        /**
         * Processes request from client and sends back a response with the
         * result. The delivery of the response is best-effort. If we are
         * unable to return any response, there is nothing else we can do.
         */
        @Override
        public void run() {

            while (true) {

                DBRequest request = null;
                DBResponse response = null;
                try {
                    ObjectInputStream ois = new ObjectInputStream(server.getInputStream());
                    request = (DBRequest) ois.readObject();
                }
                catch(Exception e) {
                    response = new DBResponse(Constants.ERROR);
                }
                if (request == null) {
                    break;
                }
                switch (request.getRequestType()) {
                    case Constants.GET:
                        response = handleGetRequest(request);
                        break;
                    case Constants.SET:
                        response = handleSetRequest(request);
                        break;
                    case Constants.DELETE:
                        response = handleDeleteRequest(request);
                        break;
                    case Constants.STREAM:
                        response = handleStreamRequest();
                        break;
                }
                ObjectOutputStream oos;
                try {
                    oos = new ObjectOutputStream(server.getOutputStream());
                    oos.writeObject(response);
                } catch (IOException e) {
                    break;
                }
            }

        }
        /**
         * Handle the GET request from a client by getting the
         * item from the DBStore and generating a DBResponse
         *
         * @return DBResponse with the requested key-value pair
         * @param request DBRequest from the client
         */
        private DBResponse handleGetRequest(DBRequest request){
            String value;
            try {
                value = dbStore.get(request.getItem().getKey());
            } catch (ItemNotFoundException e) {
                value = "";
            }
            DBItem item = new DBItem(request.getItem().getKey(), value);
            return new DBResponse(Constants.OK, new DBItem[]{item});
        }

        /**
         * Handle the SET request from a client by updating the
         * DBStore with a new key-value pair
         *
         * @return DBResponse with OK
         * @param request DBRequest from the client
         */
        private DBResponse handleSetRequest(DBRequest request){
            dbStore.set(request.getItem().getKey(), request.getItem().getValue());
            return new DBResponse(Constants.OK);
        }

        /**
         * Handle the DELETE request from a client by updating the
         * DBStore.
         *
         * @return DBResponse with OK even if the key doesn't exist
         * @param request DBRequest from the client
         */
        private DBResponse handleDeleteRequest(DBRequest request) {
            DBResponse response = new DBResponse(Constants.OK);
            try {
                dbStore.delete(request.getItem().getKey());
            } catch (ItemNotFoundException e) {
                e.printStackTrace();
            }
            return response;
        }

        /**
         * Handle the STREAM request from a client by fetching all the
         * key-value pairs from the DBStore.
         *
         * @return DBResponse with all the key-value pairs
         */
        private DBResponse handleStreamRequest() {
            DBResponse response = new DBResponse(Constants.OK);
            DBItem[] items = dbStore.stream();
            response.setItems(items);
            return response;
        }
    }
}