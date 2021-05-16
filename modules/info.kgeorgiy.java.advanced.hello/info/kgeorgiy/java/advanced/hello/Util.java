//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package info.kgeorgiy.java.advanced.hello;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.junit.Assert;

public final class Util {
    public static final Charset CHARSET;

    private Util() {
    }

    public static String getString(DatagramPacket var0) {
        return getString(var0.getData(), var0.getOffset(), var0.getLength());
    }

    public static String getString(byte[] var0, int var1, int var2) {
        return new String(var0, var1, var2, CHARSET);
    }

    public static void setString(DatagramPacket var0, String var1) {
        byte[] var2 = var1.getBytes(CHARSET);
        var0.setData(var2);
        var0.setLength(var0.getData().length);
    }

    public static DatagramPacket createPacket(DatagramSocket var0) throws SocketException {
        return new DatagramPacket(new byte[var0.getReceiveBufferSize()], var0.getReceiveBufferSize());
    }

    public static String request(String var0, DatagramSocket var1, SocketAddress var2) throws IOException {
        send(var1, var0, var2);
        return receive(var1);
    }

    public static String receive(DatagramSocket var0) throws IOException {
        DatagramPacket var1 = createPacket(var0);
        var0.receive(var1);
        return getString(var1);
    }

    public static void send(DatagramSocket var0, String var1, SocketAddress var2) throws IOException {
        DatagramPacket var3 = new DatagramPacket(new byte[0], 0);
        setString(var3, var1);
        var3.setSocketAddress(var2);
        var0.send(var3);
    }

    public static String response(String var0) {
        return String.format("Hello, %s", var0);
    }

    public static AtomicInteger[] server(String var0, int var1, double var2, DatagramSocket var4) {
        AtomicInteger[] var5 = Stream.generate(AtomicInteger::new).limit(var1).toArray(AtomicInteger[]::new);
        (new Thread(() -> {
            Random var5x = new Random(7845743984534545453L);

            try {
                while(true) {
                    DatagramPacket var6 = createPacket(var4);
                    var4.receive(var6);
                    String var7 = getString(var6);
                    String var8 = "Invalid or unexpected request " + var7;
                    Assert.assertTrue(var8, var7.startsWith(var0));
                    String[] var9 = var7.substring(var0.length()).split("_");
                    Assert.assertEquals(var8, 2L, var9.length);

                    try {
                        int var10 = Integer.parseInt(var9[0]);
                        int var11 = Integer.parseInt(var9[1]);
                        Assert.assertTrue(var8, 0 <= var10 && var10 < var5.length);
                        Assert.assertEquals(var8, var11, var5[var10].get());
                        if (var2 >= var5x.nextDouble()) {
                            var5[var10].incrementAndGet();
                            setString(var6, response(var7));
                            var4.send(var6);
                        }
                    } catch (NumberFormatException var12) {
                        throw new AssertionError(var8);
                    }
                }
            } catch (IOException var13) {
                System.err.println(var13.getMessage());
            }
        })).start();
        return var5;
    }

    static void setMode(String var0) {
    }

    static {
        CHARSET = StandardCharsets.UTF_8;
    }
}
