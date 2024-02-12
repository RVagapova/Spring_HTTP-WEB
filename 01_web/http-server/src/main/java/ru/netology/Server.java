package ru.netology;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    final int THREADS = 64;

    public void addHandler(String method, String path, Handler handler) {
        ConcurrentHashMap<String, Handler> handMap = new ConcurrentHashMap<>();
        if (ParsingRequest.handlers.containsKey(method)) {
            handMap = ParsingRequest.handlers.get(method);
        }
        handMap.put(path, handler);
        ParsingRequest.handlers.put(method, handMap);
    }

    public void start() throws IOException {
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        try (ServerSocket serverSocket = new ServerSocket(8888)) {
            while (true) {
                Socket socket = serverSocket.accept();
                ParsingRequest client = new ParsingRequest(socket);
                executor.execute(client);
            }
        } finally {
            executor.shutdown();
        }
    }
}
