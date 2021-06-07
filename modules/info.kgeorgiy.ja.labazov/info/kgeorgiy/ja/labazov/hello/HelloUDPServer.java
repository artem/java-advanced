package info.kgeorgiy.ja.labazov.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class HelloUDPServer implements HelloServer {

    private DatagramSocket socket;
    private ExecutorService workers;

    public static void main(final String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: [port] [threads]");
            return;
        }

        final int port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (final NumberFormatException e) {
            System.err.println("Invalid port specified");
            return;
        }

        final int threads;
        try {
            threads = Integer.parseInt(args[1]);
        } catch (final NumberFormatException e) {
            System.err.println("Invalid threads count specified");
            return;
        }

        try (final HelloServer server = new HelloUDPServer()) {
            server.start(port, threads);
            System.out.println("Stopping in 10 secs...");
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException ignored) {
        }
    }

    @Override
    public void start(final int port, final int threads) {
        workers = Executors.newFixedThreadPool(threads);
        try {
            socket = new DatagramSocket(port);
            final int bufferSize = socket.getReceiveBufferSize();
            IntStream.range(0, threads).forEach(i -> workers.submit(() -> handleRequest(bufferSize)));
        } catch (SocketException e) {
            System.err.println("Failed to create socket on port " + port);
        }
    }

    private void handleRequest(final int bufferSize) {
        final byte[] requestBuf = new byte[bufferSize];
        final DatagramPacket packet = new DatagramPacket(requestBuf, requestBuf.length);

        while (!Thread.interrupted() && !socket.isClosed()) {
            packet.setData(requestBuf);
            try {
                socket.receive(packet);

                final byte[] responseBuf = new byte[HelloUtils.PREFIX.length + packet.getLength()];
                System.arraycopy(HelloUtils.PREFIX, 0, responseBuf, 0, HelloUtils.PREFIX.length);
                System.arraycopy(packet.getData(),
                        packet.getOffset(),
                        responseBuf,
                        HelloUtils.PREFIX.length,
                        packet.getLength());

                packet.setData(responseBuf);
                socket.send(packet);
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public void close() {
        if (socket != null) {
            socket.close();
        }

        if (workers != null) {
            workers.shutdownNow();
        }
    }
}
