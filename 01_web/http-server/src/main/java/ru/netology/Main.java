package ru.netology;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        final int THREADS = 64;
        final var validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

        Server server = new Server();
        server.startServer();

        ExecutorService executor = Executors.newFixedThreadPool(THREADS);

        executor.submit(() -> {
            server.newConnection(validPaths);
        });
        executor.shutdown();
    }
}


