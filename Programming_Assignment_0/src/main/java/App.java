import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class App {
    public static void main(String[] args) {
        // Load the file from resources
        InputStream inputStream = App.class.getClassLoader().getResourceAsStream("alice.txt");

        if (inputStream == null) {
            System.out.println("File not found!");
            return;
        }

        // Read the file line by line
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}