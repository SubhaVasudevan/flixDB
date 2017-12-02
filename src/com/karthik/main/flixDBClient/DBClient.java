package com.karthik.main.flixDBClient;

import com.karthik.main.flixDB.Constants;

import java.net.InetAddress;
import java.util.Scanner;

public class DBClient {
    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            throw new IllegalArgumentException("Need server IP address. Port defaults to 14567. " +
                    "Usage: ./flixDBClient <server> [optional_port]");
        }

        String hostname = args[0];

        // Default server port
        int server_port = 14567;

        if (args.length == 2) {
            server_port = Integer.parseInt(args[1]);
        }

        if (hostname.charAt(0) == '$') {
            hostname = InetAddress.getLocalHost().getHostAddress();
        }

        System.out.println("Connecting to FlixDB server at " + hostname + " Port:" + server_port);

        DBClientImplementation client = new DBClientImplementation(hostname, server_port);
        client.connectHost();

        Scanner scanner = new Scanner(System.in);
        String requestType;
        while (true) {
            if (client.endClient) {
                break;
            }
            try {
                System.out.print("> ");
                String input = scanner.nextLine();
                String[] inputSplit = input.split(" ");
                requestType = inputSplit[0];
                if (requestType.isEmpty()) {
                    requestType = Constants.EMPTY;
                }

                switch (requestType) {
                    case Constants.GET:
                        if (inputSplit.length != 2) {
                            client.closeHost();
                            continue;
                        }
                        client.get(inputSplit[1]);
                        break;

                    case Constants.SET:
                        if (inputSplit.length != 3) {
                            client.closeHost();
                            continue;
                        }
                        int valueSize = Integer.parseInt(inputSplit[2]);
                        System.out.print("> ");
                        String value = scanner.nextLine();
                        client.set(inputSplit[1], value.substring(0, valueSize));
                        break;

                    case Constants.DELETE:
                        if (inputSplit.length != 2) {
                            client.closeHost();
                            continue;
                        }
                        client.delete(inputSplit[1]);
                        break;

                    case Constants.STREAM:
                        if (inputSplit.length != 1) {
                            client.closeHost();
                            continue;
                        }
                        client.stream();
                        break;

                    case Constants.EMPTY:
                        continue;

                    default:
                        client.closeHost();
                }
            } catch (Exception e) {
                client.closeHost();
            }
        }
    }
}
