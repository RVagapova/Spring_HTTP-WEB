package ru.netology;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class Client extends Thread {
    private final BufferedInputStream in;
    private final BufferedOutputStream out;
    private static final String GET = "GET";
    private static final String POST = "POST";
    public static final Map<String, Map<String, Handler>> handlers = new ConcurrentHashMap<>();

    public Client(Socket socket) throws IOException {
        in = new BufferedInputStream(socket.getInputStream());
        out = new BufferedOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        final var allowedMethods = List.of(GET, POST);

        try {
            // лимит на request line + заголовки
            final int limit = 4096;

            in.mark(limit);
            final byte[] buffer = new byte[limit];
            final int read = in.read(buffer);

            // ищем request line
            final var requestLineDelimiter = new byte[]{'\r', '\n'};
            final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
            if (requestLineEnd == -1) {
                badRequest(out);
            }

            // читаем request line
            final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
            System.out.println(requestLine);
            if (requestLine.length != 3) {
                badRequest(out);
            }

            final var method = requestLine[0];
            if (!allowedMethods.contains(method)) {
                badRequest(out);
            }
//            System.out.println("method " + method);

            final var path = requestLine[1];
            if (!path.startsWith("/")) {
                badRequest(out);
            }
            System.out.println("path " + path);

            // ищем заголовки
            final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
            final var headersStart = requestLineEnd + requestLineDelimiter.length;
            final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
            if (headersEnd == -1) {
                badRequest(out);
            }

            // отматываем на начало буфера
            in.reset();

            // пропускаем requestLine
            in.skip(headersStart);

            final var headersBytes = in.readNBytes(headersEnd - headersStart);
            final var headers = Arrays.asList(new String(headersBytes).split("\r\n"));
            System.out.println("headers" + headers);

            Request request = new Request(method, path, headers);
            System.out.println(request.getMethod() + " ");

            // для GET тела нет
            if (!method.equals(GET)) {
                String body = null;
                in.skip(headersDelimiter.length);
                // вычитываем Content-Length, чтобы прочитать body
                final var contentLength = extractHeader(headers, "Content-Length");
                if (contentLength.isPresent()) {
                    final var length = Integer.parseInt(contentLength.get());
                    final var bodyBytes = in.readNBytes(length);

                    body = new String(bodyBytes);
                    System.out.println(body);
                }
                request.setBody(body);
            }

            // создаю
            Handler handler = handlers.get(request.getMethod()).get(request.getPath());
            if (handler == null) {
                badRequest(out);
                return;
            }
            handler.handle(request, out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    private static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }

    private static void badRequest(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 400 Bad Request\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }
}
