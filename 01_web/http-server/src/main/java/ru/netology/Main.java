package ru.netology;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        Server server = new Server();

        Handler handlerGET = (request, out) -> {
            try {
                final var filePath = Path.of(".", "public", request.getPath());
                final var mimeType = Files.probeContentType(filePath);
                final var template = Files.readString(filePath);
                final var content = template.replace(
                        "{time}",
                        LocalDateTime.now().toString()
                ).getBytes();
                out.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + content.length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.write(content);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        Handler handlerPOST = (request, out) -> {
            try {
                Path filePath = Path.of(".", "public", request.getPath());
                String mimeType = Files.probeContentType(filePath);
                long sizeFile = Files.size(filePath);
                out.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + sizeFile + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                Files.copy(filePath, out);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };


        server.addHandler("GET", "/forms.html", handlerGET);
        server.addHandler("POST", "/message", handlerPOST);

        try {
            server.startServer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}


