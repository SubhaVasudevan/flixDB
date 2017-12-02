package com.karthik.main.flixDB;

/**
 * A class that contains configuration properties for the FlixDB server.
 */
class Config {
    public int max_concurrent_client_connections = 10;
    public int max_keyspace_memory = 1000;
    public int server_port = 14567;
    public String log_level = "INFO";
}
