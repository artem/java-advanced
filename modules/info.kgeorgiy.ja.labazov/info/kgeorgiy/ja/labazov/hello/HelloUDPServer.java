package info.kgeorgiy.ja.labazov.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class HelloUDPServer implements HelloServer {

    private static final byte[] PREFIX_ANSWER = "Hello, ".getBytes(StandardCharsets.UTF_8);
    private DatagramSocket udpSocket;
    private ExecutorService executorService;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: port threads");
            return;
        }
        HelloServer server = new HelloUDPServer();
        server.start(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
    }

    @Override
    public void start(final int port, final int threads) {
        executorService = Executors.newFixedThreadPool(threads);
        try {
            udpSocket = new DatagramSocket(port);
        } catch (final SocketException e) {
            System.err.println("Failed to open socket on port " + port);
            return;
        }
        IntStream.range(0, threads).forEach(index -> executorService.submit(this::listenSocket));
    }

    private void listenSocket() {
        final byte[] byteBuffer;
        try {
            byteBuffer = new byte[udpSocket.getReceiveBufferSize()];
        } catch (final SocketException e) {
            System.err.println("Can't get size of socket's buffer: " + e.getMessage());
            return;
        }

        while (!udpSocket.isClosed() && !Thread.currentThread().isInterrupted()) {
            try {
                final DatagramPacket receivePacket = new DatagramPacket(byteBuffer, 0, byteBuffer.length);
                udpSocket.receive(receivePacket);
                sendDatagram(udpSocket, receivePacket);
            } catch (final IOException e) {
                System.err.println("Receiving datagram failed: " + e.getMessage());
            }
        }
    }

    private void sendDatagram(final DatagramSocket datagramSocket, final DatagramPacket receivePacket) {
        final byte[] responseByteArray = new byte[receivePacket.getLength() + PREFIX_ANSWER.length];
        System.arraycopy(PREFIX_ANSWER, 0, responseByteArray, 0, PREFIX_ANSWER.length);
        System.arraycopy(receivePacket.getData(), receivePacket.getOffset(),
                responseByteArray, PREFIX_ANSWER.length, receivePacket.getLength());
        receivePacket.setData(responseByteArray, 0, responseByteArray.length);
        try {
            datagramSocket.send(new DatagramPacket(responseByteArray, 0, responseByteArray.length,
                    receivePacket.getAddress(), receivePacket.getPort()));
        } catch (final IOException e) {
            System.err.println("Failed to send packet: " + e.getMessage());
        }
    }

    @Override
    public void close() {
        if (udpSocket != null) {
            udpSocket.close();
        }
        executorService.shutdownNow();
    }
}
