package com.karthik.main.flixDB;

import java.io.FileInputStream;
import java.io.File;
import java.net.InetAddress;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;

public class DBServer {

    public static void main(String[] args) {
        try {

            if (args.length != 1) {
                throw new IllegalArgumentException("Need flixDB.yaml file as argument " +
                        "Usage: ./flixDBServer <path_to_flixDB.yaml>");
            }

            FileInputStream input = new FileInputStream(new File(args[0]));
            Constructor constructor = new Constructor(Config.class);
            Yaml yaml = new Yaml(constructor);
            Config conf = yaml.loadAs(input, Config.class);

            String hostname = InetAddress.getLocalHost().getHostAddress();
            SocketServer ss = new SocketServer(hostname, conf.server_port);
            ss.addHandler(new ServerClientHandler(new DBStore(conf.max_keyspace_memory), conf.max_concurrent_client_connections));
            ss.connect();

            System.out.println("FlixDB listening for FlixDB clients at " + ss.getHostname());

            ss.start();
        } catch (YAMLException e) {
            throw new YAMLException("Invalid yaml: ", e);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
