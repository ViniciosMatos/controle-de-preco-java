import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class IO {
    private static final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public static String readln() {
        try {
            return reader.readLine();
        } catch (IOException e) {
            return "";
        }
    }

    public static String readln(String prompt) {
        System.out.print(prompt);
        return readln();
    }

    public static void println(String s) {
        System.out.println(s);
    }
}
