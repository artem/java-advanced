package info.kgeorgiy.ja.labazov.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HelloUDPClient implements HelloClient {
    public static void main(String[] args) {
        if (args.length != 5) {
            System.err.println("Usage: [host] [port] [prefix] [threads] [requests]");
            return;
        }
        new HelloUDPClient().run(args[0], Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]));
    }

    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        ExecutorService workers = Executors.newFixedThreadPool(threads);
        InetSocketAddress addr = new InetSocketAddress(host, port);
    }

    private static void sendRequest(String prefix, int threadId, int requestId, DatagramPacket packet) {

    }
}
