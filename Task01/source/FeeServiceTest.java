import java.util.ArrayList;
import java.util.List;

public class FeeServiceTest {
    public static void main(String[] args) throws Exception {
        List<String> writes = new ArrayList<>();
        FeeService service = new FeeService((id, amount) -> writes.add(id + ":" + amount));
        String result = service.pay("12345", "100.5");
        if (!result.equals("12345 paid INR 100.50")) throw new AssertionError(result);
        System.out.println(result);
        for (String amount : new String[]{"", "-1", "1.234", "abc"}) {
            try {
                service.pay("12345", amount);
                throw new AssertionError(amount);
            } catch (IllegalArgumentException e) {
                System.out.println("Rejected '" + amount + "': " + e.getMessage());
            }
        }
        if (!writes.toString().equals("[12345:100.50]")) throw new AssertionError(writes);
    }
}
