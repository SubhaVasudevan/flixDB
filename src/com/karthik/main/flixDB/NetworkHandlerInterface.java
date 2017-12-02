package com.karthik.main.flixDB;

import java.net.Socket;

/**
 * Interface for network handlers.
 */
public interface NetworkHandlerInterface {

    /**
     * Creates a job to service the request for a socket and returns immediately
     * after enqueuing that job.
     *
     * @param sock Socket connected to another endpoint with the request
     */
    void handle(Socket sock);

}
