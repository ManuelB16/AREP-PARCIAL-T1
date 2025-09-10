package com.mycompany.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.HashMap;

public class FacadeServer {

    private static final String USER_AGENT = "Mozilla/5.0";
    private static final String BACKEND_URL = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=fb&apikey=Q1QZFVJQ21K7C6XM";

    public static void main(String[] args) throws IOException, URISyntaxException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(36000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 36000.");
            System.exit(1);
        }

        Socket clientSocket = null;
        boolean running = true;
        while (running) {
            try {
                System.out.println("Fachada recibiendo ...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Fallo.");
                System.exit(1);
            }
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String inputLine;
            String outputLine = "";
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Recibido: " + inputLine);
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
        if (path.equals("/") || path.equals("/index.html")) {
            return serveHtmlClient();
        }
        return forwardRequest(path, queryParams);
    }

    private static String serveHtmlClient() {
        String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Calculadora Web</title>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "</head>\n" +
                "<body>\n" +
                "    <h1>Calculadora de Media y Desviación Estándar</h1>\n" +
                "    <div>\n" +
                "        <input type=\"number\" id=\"numberInput\" step=\"any\" placeholder=\"Ingrese un número\">\n" +
                "        <button onclick=\"addNumber()\">Agregar Número</button>\n" +
                "    </div>\n" +
                "    <div>\n" +
                "        <button onclick=\"clearList()\">Limpiar Lista</button>\n" +
                "        <button onclick=\"showList()\">Mostrar Lista</button>\n" +
                "        <button onclick=\"calculateMean()\">Calcular Media</button>\n" +
                "        <button onclick=\"calculateStdDev()\">Calcular Desviación Estándar</button>\n" +
                "    </div>\n" +
                "    <div id=\"result\"></div>\n" +
                "\n" +
                "    <script>\n" +
                "        function makeRequest(endpoint, params) {\n" +
                "            const xhttp = new XMLHttpRequest();\n" +
                "            xhttp.onload = function() {\n" +
                "                document.getElementById('result').innerHTML = this.responseText;\n" +
                "            }\n" +
                "            let url = endpoint;\n" +
                "            if (params) {\n" +
                "                url += '?' + params;\n" +
                "            }\n" +
                "            xhttp.open('GET', url);\n" +
                "            xhttp.send();\n" +
                "        }\n" +
                "\n" +
                "        function addNumber() {\n" +
                "            const number = document.getElementById('numberInput').value;\n" +
                "            if (number) {\n" +
                "                makeRequest('/add', 'number=' + encodeURIComponent(number));\n" +
                "                document.getElementById('numberInput').value = '';\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        function clearList() {\n" +
                "            makeRequest('/clear');\n" +
                "        }\n" +
                "\n" +
                "        function showList() {\n" +
                "            makeRequest('/list');\n" +
                "        }\n" +
                "\n" +
                "        function calculateMean() {\n" +
                "            makeRequest('/mean');\n" +
                "        }\n" +
                "\n" +
                "        function calculateStdDev() {\n" +
                "            makeRequest('/stddev');\n" +
                "        }\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";

        return "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/html\r\n"
                + "\r\n"
                + html;
    }

    private static String forwardRequest(String path, Map<String, String> queryParams) {
        try {
            String queryString = buildQueryString(queryParams);
            String fullUrl = BACKEND_URL + path + (queryString.isEmpty() ? "" : "?" + queryString);

            URL obj = new URL(fullUrl);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", USER_AGENT);

            int responseCode = con.getResponseCode();
            System.out.println("Backend Response Code :: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                return "HTTP/1.1 200 OK\r\n"
                        + "Content-Type: application/json\r\n"
                        + "\r\n"
                        + response.toString();
            } else {
                return "HTTP/1.1 500 Internal Server Error\r\n"
                        + "Content-Type: application/json\r\n"
                        + "\r\n"
                        + "{\"status\": \"error\", \"message\": \"Backend server error\"}";
            }
        } catch (IOException e) {
            System.err.println("Error forwarding request: " + e.getMessage());
            return "HTTP/1.1 500 Internal Server Error\r\n"
                    + "Content-Type: application/json\r\n"
                    + "\r\n"
                    + "{\"status\": \"error\", \"message\": \"Failed to connect to backend\"}";
        }
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

    private static String buildQueryString(Map<String, String> params) {
        if (params.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return sb.toString();
    }
}
