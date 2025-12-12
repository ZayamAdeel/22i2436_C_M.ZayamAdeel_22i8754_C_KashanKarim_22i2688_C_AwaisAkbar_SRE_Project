import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;

public class Admin_Interface extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;

    private JButton addCashierBtn;
    private JButton addAdminBtn;
    private JButton removeBtn;
    private JButton updateBtn;
    private JButton cashierViewBtn;
    private JButton logoutBtn;

    private JTextArea textArea;
    private JScrollPane scrollPane;

    private POSSystem system;
    private EmployeeManagement management = new EmployeeManagement();

    private List<Employee> employeeList;

    public Admin_Interface(POSSystem system) {
        super("SG Technologies - Administrator View");
        this.system = system;

        setupWindow();
        setupComponents();
        setupListeners();
        updateTextArea();
    }

    // ---------------------------------------------------------
    // WINDOW SETUP
    // ---------------------------------------------------------
    private void setupWindow() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        setLayout(null);

        int width = (int) tk.getScreenSize().getWidth();
        int height = (int) tk.getScreenSize().getHeight();

        setSize(width, height);
    }

    // ---------------------------------------------------------
    // UI COMPONENT INITIALIZATION
    // ---------------------------------------------------------
    private void setupComponents() {

        Toolkit tk = Toolkit.getDefaultToolkit();
        int width = (int) tk.getScreenSize().getWidth();
        int height = (int) tk.getScreenSize().getHeight();
        int buttonX = width * 4 / 5;
        int buttonW = 150;
        int buttonH = 80;

        addCashierBtn = createButton("Add Cashier", buttonX, height / 8, buttonW, buttonH);
        addAdminBtn = createButton("Add Admin", buttonX, height * 2 / 8, buttonW, buttonH);
        removeBtn = createButton("Remove Employee", buttonX, height * 3 / 8, buttonW, buttonH);
        updateBtn = createButton("Update Employee", buttonX, height * 4 / 8, buttonW, buttonH);
        cashierViewBtn = createButton("Cashier View", buttonX, height * 5 / 8, buttonW, buttonH);
        logoutBtn = createButton("Log Out", buttonX, height * 6 / 8, buttonW, buttonH);

        textArea = new JTextArea();
        textArea.setBackground(Color.white);
        textArea.setForeground(Color.black);
        textArea.setEditable(false);

        Font font = textArea.getFont();
        textArea.setFont(font.deriveFont(font.getSize() + 5f));

        scrollPane = new JScrollPane(
                textArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        scrollPane.setBounds(width / 16, height / 16, 3 * width / 5, 4 * height / 5);
        add(scrollPane);
    }

    private JButton createButton(String text, int x, int y, int w, int h) {
        JButton btn = new JButton(text);
        btn.setBounds(x, y, w, h);
        add(btn);
        return btn;
    }

    // ---------------------------------------------------------
    // ACTION LISTENERS
    // ---------------------------------------------------------
    private void setupListeners() {
        addCashierBtn.addActionListener(this);
        addAdminBtn.addActionListener(this);
        removeBtn.addActionListener(this);
        updateBtn.addActionListener(this);
        cashierViewBtn.addActionListener(this);
        logoutBtn.addActionListener(this);
    }

    // ---------------------------------------------------------
    // EVENT HANDLING
    // ---------------------------------------------------------
    @Override
    public void actionPerformed(ActionEvent event) {

        Object src = event.getSource();

        if (src == addCashierBtn) {
            openAddEmployee(true);
        } 
        else if (src == addAdminBtn) {
            openAddEmployee(false);
        } 
        else if (src == removeBtn) {
            removeEmployee();
        } 
        else if (src == updateBtn) {
            openUpdateEmployee();
        } 
        else if (src == cashierViewBtn) {
            openCashierView();
        } 
        else if (src == logoutBtn) {
            logout();
        }
    }

    private void openAddEmployee(boolean isCashier) {
        AddEmployee_Interface ui = new AddEmployee_Interface(isCashier, this);
        ui.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        ui.setVisible(true);
    }

    private void openUpdateEmployee() {
        UpdateEmployee_Interface ui = new UpdateEmployee_Interface(this);
        ui.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        ui.setVisible(true);
    }

    private void removeEmployee() {
        String employeeID = JOptionPane.showInputDialog("Enter employee ID");

        if (!management.delete(employeeID)) {
            JOptionPane.showMessageDialog(null, "No employee with such ID");
        } else {
            updateTextArea();
        }
    }

    private void openCashierView() {
        POSSystem sys = new POSSystem();
        Cashier_Interface cashier = new Cashier_Interface(sys);
        cashier.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        cashier.setVisible(true);

        this.dispose();
    }

    private void logout() {
        system.logOut("Admin");

        Login_Interface login = new Login_Interface();
        login.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        login.setVisible(true);

        this.dispose();
    }

    // ---------------------------------------------------------
    // REFRESH EMPLOYEE LIST
    // ---------------------------------------------------------
    public void updateTextArea() {
        textArea.setText("");

        employeeList = management.getEmployeeList();

        for (Employee emp : employeeList) {
            String line = formatEmployee(emp);
            textArea.append(line);
        }
    }

    private String formatEmployee(Employee emp) {
        String base = emp.getUsername() + "\t" + emp.getPosition() + " \t" + emp.getName();
        String spacing = emp.getName().length() >= 12 ? "\t" : "\t\t";
        return base + spacing + emp.getPassword() + "\n";
    }
}
