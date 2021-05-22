package info.kgeorgiy.ja.labazov.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class HelloUDPClient implements HelloClient {

    public static void main(String[] args) {
        if (args.length != 5) {
            System.err.println("Usage: host port prefix threads requests");
            return;
        }
        new HelloUDPClient().run(args[0], Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]));
    }

    @Override
    public void run(final String host, final int port, final String prefix, final int threads, final int requests) {
        final ExecutorService executorService = Executors.newFixedThreadPool(threads);
        final SocketAddress inetAddress = new InetSocketAddress(host, port);

        IntStream.range(0, threads).forEach(index -> executorService.submit(() -> doRequest(index, requests, prefix, inetAddress)));
        executorService.shutdown();
        try {
            executorService.awaitTermination(threads * requests * 5000L, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException ignored) {
        }
    }

    private boolean receivePacket(final DatagramSocket datagramSocket, final DatagramPacket datagramPacket,
                                  final String request, final byte[] responseStorage) {
        try {
            datagramPacket.setData(responseStorage, 0, responseStorage.length);
            datagramSocket.receive(datagramPacket);
            final String responseString = new String(
                    datagramPacket.getData(),
                    0,
                    datagramPacket.getLength(),
                    StandardCharsets.UTF_8);
            if (responseString.contains(request)) {
                System.out.println(responseString);
                return true;
            }
        } catch (final IOException ignored) {
        }
        return false;
    }

    private void doRequest(final int threadIndex, final int requests, final String prefix, final SocketAddress inetAddress) {
        try (final DatagramSocket datagramSocket = new DatagramSocket()) {
            datagramSocket.setSoTimeout(500);
            final byte[] responseStorage = new byte[datagramSocket.getReceiveBufferSize()];
            for (int j = 0; j < requests; j++) {
                final String request = (prefix + threadIndex + "_" + j);
                final byte[] requestInBytes = request.getBytes(StandardCharsets.UTF_8);
                System.out.println(request);
                while (true) {
                    try {
                        final DatagramPacket datagramPacket = new DatagramPacket(
                                requestInBytes,
                                requestInBytes.length,
                                inetAddress);

                        datagramSocket.send(datagramPacket);
                        if (receivePacket(datagramSocket, datagramPacket, request, responseStorage)) {
                            break;
                        }
                    } catch (final IOException ignored) {
                    }
                }
            }
        } catch (final SocketException e) {
            System.err.println("Socket problems: " + e.getMessage());
        }
    }
}
