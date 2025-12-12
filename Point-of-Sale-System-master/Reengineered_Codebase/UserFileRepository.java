import java.io.*;
import java.text.*;
import java.util.*;

public class UserFileRepository {

    private final String dbPath;

    public UserFileRepository(String dbPath) {
        this.dbPath = dbPath;
    }

    public List<String> loadRawLines() {
        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(dbPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (Exception e) {
            System.out.println("Error reading DB: " + e.getMessage());
        }

        return lines;
    }

    public void saveRawLines(List<String> lines) {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(dbPath)))) {
            for (String s : lines) {
                writer.println(s);
            }
        } catch (Exception e) {
            System.out.println("Error writing DB: " + e.getMessage());
        }
    }

    public long parsePhone(String line) {
        try {
            return Long.parseLong(line.split(" ")[0]);
        } catch (Exception e) {
            return -1;
        }
    }

    public List<String> parseRentalEntries(String line) {
        List<String> rentals = new ArrayList<>();
        String[] parts = line.split(" ");
        for (int i = 1; i < parts.length; i++) {
            rentals.add(parts[i]);
        }
        return rentals;
    }
}
