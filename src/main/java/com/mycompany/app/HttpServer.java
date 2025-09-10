package com.mycompany.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class HttpServer {
    private static List<Double> numbers = new ArrayList<>();

    public static void main(String[] args) throws IOException, URISyntaxException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(35000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }

        Socket clientSocket = null;
        boolean running = true;
        while (running) {
            try {
                System.out.println("Servidor esperando...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Fallo");
                System.exit(1);
            }
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String inputLine;
            String outputLine = "";
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Received: " + inputLine);
                if (inputLine.startsWith("GET")) {
                    URI uri = new URI(inputLine.split(" ")[1]);
                    String path = uri.getPath();
                    Map<String, String> queryParams = parseQuery(uri.getQuery());
                    outputLine = handleRequest(path, queryParams);
                }
                if (!in.ready()) {
                    break;
                }
            }
            out.println(outputLine);
            out.close();
            in.close();
        }
        serverSocket.close();
    }

    private static String handleRequest(String path, Map<String, String> queryParams) {
        String response = "";
        switch (path) {
            case "/add":
                if (queryParams.containsKey("number")) {
                    try {
                        double num = Double.parseDouble(queryParams.get("number"));
                        numbers.add(num);
                        response = "{\"status\": \"success\", \"message\": \"Number added\"}";
                    } catch (NumberFormatException e) {
                        response = "{\"status\": \"error\", \"message\": \"Invalid number\"}";
                    }
                } else {
                    response = "{\"status\": \"error\", \"message\": \"Missing number parameter\"}";
                }
                break;
            case "/clear":
                numbers.clear();
                response = "{\"status\": \"success\", \"message\": \"List cleared\"}";
                break;
            case "/list":
                response = "{\"numbers\": " + numbers.toString() + "}";
                break;
            case "/mean":
                if (numbers.isEmpty()) {
                    response = "{\"status\": \"error\", \"message\": \"No numbers in list\"}";
                } else {
                    double sum = 0;
                    for (double num : numbers) {
                        sum += num;
                    }
                    double mean = sum / numbers.size();
                    response = "{\"mean\": " + mean + "}";
                }
                break;
            case "/stddev":
                if (numbers.size() < 2) {
                    response = "{\"status\": \"error\", \"message\": \"Need at least 2 numbers for standard deviation\"}";
                } else {
                    double sum = 0;
                    for (double num : numbers) {
                        sum += num;
                    }
                    double mean = sum / numbers.size();
                    double sumSquaredDiffs = 0;
                    for (double num : numbers) {
                        sumSquaredDiffs += Math.pow(num - mean, 2);
                    }
                    double variance = sumSquaredDiffs / numbers.size();
                    double stddev = Math.sqrt(variance);
                    response = "{\"stddev\": " + stddev + "}";
                }
                break;
            default:
                response = "{\"status\": \"error\", \"message\": \"Unknown endpoint\"}";
        }
        return "HTTP/1.1 200 OK\r\n"
                + "Content-Type: application/json\r\n"
                + "\r\n"
                + response;
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return params;
    }
}
