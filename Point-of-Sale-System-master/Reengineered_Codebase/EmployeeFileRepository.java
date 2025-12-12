import java.io.*;
import java.util.*;

public class EmployeeFileRepository {

    private final String employeeDbPath;
    private final String tempDbPath;

    public EmployeeFileRepository(String employeeDbPath, String tempDbPath) {
        this.employeeDbPath = employeeDbPath;
        this.tempDbPath = tempDbPath;
    }

    public List<Employee> loadEmployees() {
        List<Employee> list = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(employeeDbPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                list.add(parseEmployee(line));
            }
        } catch (Exception e) {
            System.out.println("Error reading employee database: " + e.getMessage());
        }
        return list;
    }

    public void saveEmployees(List<Employee> employees) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempDbPath))) {
            for (Employee e : employees) {
                writer.write(formatEmployee(e));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error writing employees: " + e.getMessage());
        }

        File original = new File(employeeDbPath);
        File temp = new File(tempDbPath);

        original.delete();
        temp.renameTo(original);
    }

    public void appendEmployee(Employee emp) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(employeeDbPath, true))) {
            writer.write(formatEmployee(emp));
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Unable to append employee: " + e.getMessage());
        }
    }

    private Employee parseEmployee(String line) {
        String[] tokens = line.split(" ");
        String username = tokens[0];
        String position = tokens[1];
        String name = tokens[2] + " " + tokens[3];
        String password = tokens[4];
        return new Employee(username, name, position, password);
    }

    private String formatEmployee(Employee e) {
        return e.getUsername() + " " +
               e.getPosition() + " " +
               e.getName() + " " +
               e.getPassword();
    }
}
