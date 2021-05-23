package info.kgeorgiy.ja.labazov.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

public class HelloUDPServer implements HelloServer {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: [port] [threads]");
            return;
        }
        HelloServer server = new HelloUDPServer();
        server.start(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
    }

    @Override
    public void start(int port, int threads) {

    }

    @Override
    public void close() {

    }
}
