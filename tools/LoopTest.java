import java.net.*;
import java.io.*;

public class LoopTest {
    public static void main(String[] a) throws Exception {
        ServerSocket ss = new ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"));
        int port = ss.getLocalPort();
        Thread t = new Thread(() -> {
            try { Socket s = ss.accept(); s.close(); } catch (Exception e) { }
        });
        t.start();
        Socket c = new Socket();
        c.connect(new InetSocketAddress("127.0.0.1", port), 3000);
        System.out.println("LOOPBACK_OK port=" + port);
        c.close();
        ss.close();
    }
}
