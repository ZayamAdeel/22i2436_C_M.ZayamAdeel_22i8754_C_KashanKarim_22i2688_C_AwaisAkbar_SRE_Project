import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Cashier_Interface extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;
    private JButton saleButton;
    private JButton rentalButton;
    private JButton returnButton;
    private JButton logOutButton;
    private Transaction_Interface transaction;
    private POSSystem system;

    public Cashier_Interface(POSSystem system) {
        super("SG Technologies - Cashier View");
        this.system = system;
        initializeUI();
        handleUnfinishedTransaction();
    }

    private void initializeUI() {
        setLayout(null);
        Toolkit tk = Toolkit.getDefaultToolkit();
        int width = tk.getScreenSize().width;
        int height = tk.getScreenSize().height;
        setSize(width, height);

        saleButton = createButton("Sale", 0, height / 5, width, 100);
        rentalButton = createButton("Rental", 0, height * 2 / 5, width, 100);
        returnButton = createButton("Returns", 0, height * 3 / 5, width, 100);
        logOutButton = createButton("Log Out", 0, height * 4 / 5, width, 100);

        add(saleButton);
        add(rentalButton);
        add(returnButton);
        add(logOutButton);

        saleButton.addActionListener(this);
        rentalButton.addActionListener(this);
        returnButton.addActionListener(this);
        logOutButton.addActionListener(this);
    }

    private JButton createButton(String text, int x, int y, int width, int height) {
        JButton button = new JButton(text);
        button.setBounds(x, y, width, height);
        return button;
    }

    private void handleUnfinishedTransaction() {
        if (!system.checkTemp()) return;

        Object[] options = {"Yes", "No"};
        int choice = JOptionPane.showOptionDialog(
                null,
                "System was able to restore an unfinished transaction. Would you like to retrieve it?",
                "Choose an option",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1]
        );

        if (choice == JOptionPane.NO_OPTION) return;

        Management management = new Management();
        long phoneNum;
        String phone;
        do {
            phone = JOptionPane.showInputDialog("Please enter customer's phone number");
            phoneNum = Long.parseLong(phone);
            if (phoneNum < 1000000000L || phoneNum > 9999999999L) {
                JOptionPane.showMessageDialog(null, "Invalid phone number. Please enter again");
            }
        } while (phoneNum < 1000000000L || phoneNum > 9999999999L);

        if (!management.checkUser(phoneNum)) {
            if (management.createUser(phoneNum)) {
                JOptionPane.showMessageDialog(null, "New customer was registered");
            } else {
                JOptionPane.showMessageDialog(null, "New customer couldn't be registered");
            }
        }

        String operation = system.continueFromTemp(phoneNum);
        openTransaction(operation);
    }

    private void openTransaction(String type) {
        transaction = new Transaction_Interface(type);
        transaction.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        transaction.setVisible(true);
        this.setVisible(false);
        dispose();
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();

        if (source == saleButton) {
            openTransaction("Sale");
        } else if (source == rentalButton) {
            openTransaction("Rental");
        } else if (source == returnButton) {
            openTransaction("Return");
        } else if (source == logOutButton) {
            system.logOut("Cashier");
            Login_Interface login = new Login_Interface();
            login.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            login.setVisible(true);
            this.setVisible(false);
            dispose();
        }
    }
}
