package info.kgeorgiy.ja.labazov.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class HelloUDPClient implements HelloClient {
    private final static Pattern CORRECT = Pattern.compile("\\D*(\\d+)\\D+(\\d+)\\D*");

    public static void main(final String[] args) {
        if (args.length != 5) {
            System.err.println("Usage: [host] [port] [prefix] [threads] [requests]");
            return;
        }

        final int port;
        try {
            port = Integer.parseInt(args[1]);
        } catch (final NumberFormatException e) {
            System.err.println("Invalid port specified");
            return;
        }

        final int threads;
        try {
            threads = Integer.parseInt(args[3]);
        } catch (final NumberFormatException e) {
            System.err.println("Invalid threads count specified");
            return;
        }

        final int requests;
        try {
            requests = Integer.parseInt(args[4]);
        } catch (final NumberFormatException e) {
            System.err.println("Invalid request count specified");
            return;
        }
        new HelloUDPClient().run(args[0], port, args[2], threads, requests);
    }

    private static boolean validateResponse(final String response, final int threadId, final int requestId) {
        final Matcher matcher = CORRECT.matcher(response);
        return matcher.matches() && matcher.group(1).equals(String.valueOf(threadId)) && matcher.group(2).equals(String.valueOf(requestId));
    }

    private static void threadRequest(final String prefix, final int threadId, final int requests, final SocketAddress address) {
        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(200);

            final byte[] responseBuf = new byte[socket.getReceiveBufferSize()];
            final DatagramPacket packet = new DatagramPacket(responseBuf, responseBuf.length, address);

            for (int i = 0; i < requests; i++) {
                final String request = prefix + threadId + '_' + i;
                final byte[] requestBuf = request.getBytes(StandardCharsets.UTF_8);

                while (true) {
                    try {
                        System.out.println(request);
                        packet.setData(requestBuf);
                        socket.send(packet);

                        packet.setData(responseBuf);
                        socket.receive(packet);
                        final String response = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                        System.out.println(response);

                        if (validateResponse(response, threadId, i)) {
                            break;
                        }
                    } catch (final IOException ignored) {
                    }
                }
            }
        } catch (final SocketException e) {
            System.err.println("Failed to create socket on thread " + threadId);
        }
    }

    @Override
    public void run(final String host, final int port, final String prefix, final int threads, final int requests) {
        final ExecutorService workers = Executors.newFixedThreadPool(threads);
        final InetSocketAddress address = new InetSocketAddress(host, port);

        IntStream.range(0, threads).forEach(i -> workers.submit(() -> threadRequest(prefix, i, requests, address)));
        workers.shutdown();

        try {
            workers.awaitTermination(5, TimeUnit.SECONDS);
        } catch (final InterruptedException ignored) {
        }
    }
}
