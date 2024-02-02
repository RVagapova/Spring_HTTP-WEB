package ru.netology;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    final int THREADS = 64;

    public void addHandler(String method, String path, Handler handler) {
        Map<String, Handler> handMap = new ConcurrentHashMap<>();
        if (Client.handlers.containsKey(method)) {
            handMap = Client.handlers.get(method);
        }
        handMap.put(path, handler);
        Client.handlers.put(method, handMap);
    }

    public void startServer() throws IOException {
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        try (ServerSocket serverSocket = new ServerSocket(8888)) {
            while (true) {
                Socket socket = serverSocket.accept();
                Client client = new Client(socket);
                executor.execute(client);
            }
        } finally {
            executor.shutdown();
        }
    }
}
