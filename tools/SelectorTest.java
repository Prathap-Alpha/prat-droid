import java.nio.channels.Selector;

public class SelectorTest {
    public static void main(String[] a) throws Exception {
        Selector s = Selector.open();
        System.out.println("SELECTOR_OK provider=" + s.provider().getClass().getName());
        s.close();
    }
}
