import java.util.*;

public class EmployeeManagement {

    private final EmployeeFileRepository repository;
    private List<Employee> employees;

    public EmployeeManagement() {
        this.repository = new EmployeeFileRepository(
                "Database/employeeDatabase.txt",
                "Database/newEmployeeDatabase.txt"
        );
        this.employees = new ArrayList<>();
    }

    public List<Employee> getEmployeeList() {
        employees = repository.loadEmployees();
        return employees;
    }

    public void add(String name, String password, boolean isEmployee) {
        employees = repository.loadEmployees();

        int nextUsername = generateNextUsername();

        Employee newEmp = new Employee(
                Integer.toString(nextUsername),
                name,
                isEmployee ? "Cashier" : "Admin",
                password
        );

        repository.appendEmployee(newEmp);
    }

    public boolean delete(String username) {
        employees = repository.loadEmployees();

        boolean removed = employees.removeIf(e -> e.getUsername().equals(username));
        if (removed) {
            repository.saveEmployees(employees);
        }

        return removed;
    }

    public int update(String username, String password, String position, String name) {
        employees = repository.loadEmployees();

        Optional<Employee> match = employees.stream()
                .filter(e -> e.getUsername().equals(username))
                .findFirst();

        if (!match.isPresent()) return -1;
        if (!isValidPosition(position)) return -2;

        Employee emp = match.get();

        if (!password.isEmpty()) emp.setPassword(password);
        if (!position.isEmpty()) emp.setPosition(position);
        if (!name.isEmpty()) emp.setName(name);

        repository.saveEmployees(employees);
        return 0;
    }

    private boolean isValidPosition(String position) {
        return position.isEmpty() ||
               position.equals("Admin") ||
               position.equals("Cashier");
    }

    private int generateNextUsername() {
        if (employees.isEmpty()) return 1;

        String last = employees.get(employees.size() - 1).getUsername();
        return Integer.parseInt(last) + 1;
    }
}
