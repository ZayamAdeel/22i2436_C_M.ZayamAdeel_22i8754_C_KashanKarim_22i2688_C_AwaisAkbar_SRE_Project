import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;

public class POSSystem {

    private static final String EMPLOYEE_DB = "Database/employeeDatabase.txt";
    private static final String RENTAL_DB = "Database/rentalDatabase.txt";
    private static final String ITEM_DB = "Database/itemDatabase.txt";
    private static final String LOG_FILE = "Database/employeeLogfile.txt";
    private static final String TEMP_FILE = "Database/temp.txt";

    private final List<Employee> employees = new ArrayList<>();
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private String username = "";
    private String name = "";
    private int index = -1;

    // -----------------------------
    // File Reading (Extract Method)
    // -----------------------------
    private void loadEmployees() {
        try (BufferedReader reader = new BufferedReader(new FileReader(EMPLOYEE_DB))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                String fullName = parts[2] + " " + parts[3];
                employees.add(new Employee(parts[0], fullName, parts[1], parts[4]));
            }
        } catch (IOException e) {
            System.out.println("Error reading employee database.");
        }
    }

    // -----------------------------
    // Logging (Extract Method)
    // -----------------------------
    private void writeLog(String message) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            bw.write(message);
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Unable to write to log file.");
        }
    }

    private void logInLog(String username, String fullName, String position) {
        String log = fullName + " (" + username + " " + position + 
                     ") logs into POS System. Time: " +
                     dateFormat.format(Calendar.getInstance().getTime());
        writeLog(log);
    }

    private void logOutLog(String username, String fullName, String position) {
        String log = fullName + " (" + username + " " + position +
                     ") logs out of POS System. Time: " +
                     dateFormat.format(Calendar.getInstance().getTime());
        writeLog(log);
    }

    // -----------------------------
    // Login Workflow
    // -----------------------------
    public int logIn(String username, String password) {
        loadEmployees();
        this.username = username;

        Optional<Employee> empOpt = employees.stream()
                                             .filter(e -> e.getUsername().equals(username))
                                             .findFirst();

        if (empOpt.isEmpty()) {
            return 0;
        }

        Employee emp = empOpt.get();
        this.index = employees.indexOf(emp);

        if (!emp.getPassword().equals(password)) {
            return 0;
        }

        this.name = emp.getName();
        logInLog(emp.getUsername(), emp.getName(), emp.getPosition());

        return emp.getPosition().equals("Admin") ? 2 : 1;
    }

    public void logOut(String position) {
        logOutLog(username, name, position);
    }

    // -----------------------------
    // Temp File Handling
    // -----------------------------
    public boolean checkTemp() {
        File f = new File(TEMP_FILE);
        return f.exists() && f.length() > 0;
    }

    public String continueFromTemp(long phone) {
        try (BufferedReader reader = new BufferedReader(new FileReader(TEMP_FILE))) {

            String type = reader.readLine();
            if (type == null) {
                new File(TEMP_FILE).delete();
                return "";
            }

            return switch (type) {
                case "Sale" -> {
                    new POS().retrieveTemp(ITEM_DB);
                    yield "Sale";
                }
                case "Rental" -> {
                    new POR(phone).retrieveTemp(RENTAL_DB);
                    yield "Rental";
                }
                case "Return" -> {
                    new POH(phone).retrieveTemp(RENTAL_DB);
                    yield "Return";
                }
                default -> "";
            };

        } catch (IOException ignored) {
            return "";
        }
    }
}
