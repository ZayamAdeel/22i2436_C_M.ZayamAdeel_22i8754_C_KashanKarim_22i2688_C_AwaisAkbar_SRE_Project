import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Transaction_Interface extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;
    private PointOfSale transaction;
    private Management management = new Management();

    private JButton addItem, removeItem, endTransaction, cancelTransaction;
    private String phone = "";
    private long phoneNum;
    private JTextArea transactionDialog;
    private JScrollPane scroll;

    public boolean returnOrNot;
    private String databaseFile;
    private String operation = "";
    private int choice = 3;

    public Transaction_Interface(String operation) {
        super("SG Technologies - Transaction View");
        this.operation = operation;
        setLayout(null);

        setupWindow();
        setupButtons();
        setupTransactionDialog();
        initializeTransaction();
    }

    private void setupWindow() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        setSize(tk.getScreenSize().width, tk.getScreenSize().height);
    }

    private void setupButtons() {
        int width = getWidth(), height = getHeight();
        addItem = createButton("Add Item", width * 4 / 5, height / 6, 150, 80);
        removeItem = createButton("Remove Item", width * 4 / 5, height * 2 / 6, 150, 80);
        endTransaction = createButton("End", width * 4 / 5, height * 3 / 6, 150, 80);
        cancelTransaction = createButton("Cancel", width * 4 / 5, height * 4 / 6, 150, 80);

        add(addItem); add(removeItem); add(endTransaction); add(cancelTransaction);

        addItem.addActionListener(this);
        removeItem.addActionListener(this);
        endTransaction.addActionListener(this);
        cancelTransaction.addActionListener(this);
    }

    private JButton createButton(String text, int x, int y, int width, int height) {
        JButton button = new JButton(text);
        button.setBounds(x, y, width, height);
        return button;
    }

    private void setupTransactionDialog() {
        transactionDialog = new JTextArea();
        transactionDialog.setBackground(Color.white);
        transactionDialog.setForeground(Color.black);
        transactionDialog.setEditable(false);
        transactionDialog.setFont(transactionDialog.getFont().deriveFont(transactionDialog.getFont().getSize() + 5.0f));

        scroll = new JScrollPane(transactionDialog, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBounds(getWidth() / 16, getHeight() / 16, 3 * getWidth() / 5, 4 * getHeight() / 5);
        add(scroll);
    }

    private void initializeTransaction() {
        switch (operation) {
            case "Sale":
                returnOrNot = false;
                transaction = new POS();
                databaseFile = "Database/itemDatabase.txt";
                break;

            case "Rental":
                returnOrNot = false;
                getCustomerPhone();
                transaction = new POR(phoneNum);
                databaseFile = "Database/rentalDatabase.txt";
                break;

            case "Return":
                returnOrNot = true;
                handleReturnChoice();
                break;

            default:
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }
        transaction.startNew(databaseFile);

        if ("Return".equals(operation) && choice != 0) {
            databaseFile = "";
            operation = "Unsatisfactory";
        }
    }

    private void handleReturnChoice() {
        Object[] options = {"Rented Items", "Unsatisfactory items"};
        choice = JOptionPane.showOptionDialog(
                null,
                "Returning rented items or unsatisfactory items?",
                "Choose an option",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) {
            getCustomerPhone();
            transaction = new POH(phoneNum);
            transaction.returnSale = false;
            databaseFile = "Database/rentalDatabase.txt";
        } else {
            transaction = new POH();
            transaction.returnSale = true;
            phone = "0000000000";
            databaseFile = "Database/itemDatabase.txt";
        }
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();

        if (source == addItem) openEnterItem(true);
        else if (source == removeItem) openEnterItem(false);
        else if (source == endTransaction) processEndTransaction();
        else if (source == cancelTransaction) cancelTransaction();
    }

    private void openEnterItem(boolean isAdd) {
        EnterItem_Interface itemInterface = new EnterItem_Interface(transaction, isAdd, transactionDialog, operation, choice);
        itemInterface.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        itemInterface.setVisible(true);
    }

    private void processEndTransaction() {
        if (transaction.getCartSize() <= 0) {
            JOptionPane.showMessageDialog(null, "Cart is currently empty. Please add items before ending transaction");
            return;
        }

        if ("Sale".equals(operation)) handleCoupon();

        if ("Unsatisfactory".equals(operation)) {
            transaction.endPOS(databaseFile);
            JOptionPane.showMessageDialog(null, "Returning items is complete");
            returnToCashier();
        } else {
            openPaymentInterface();
        }
    }

    private void handleCoupon() {
        String coupon = JOptionPane.showInputDialog("Enter coupon code if user has one");
        if (coupon != null && !coupon.isEmpty() && !transaction.coupon(coupon)) {
            JOptionPane.showMessageDialog(null, "Invalid coupon");
        }
    }

    private void openPaymentInterface() {
        Payment_Interface payment = new Payment_Interface(transaction, databaseFile, operation, phone, returnOrNot);
        payment.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        payment.setVisible(true);
        this.setVisible(false);
        dispose();
    }

    private void cancelTransaction() {
        JOptionPane.showMessageDialog(null, "Transaction Has Been Cancelled");
        returnToCashier();
    }

    private void returnToCashier() {
        POSSystem sys = new POSSystem();
        Cashier_Interface cashier = new Cashier_Interface(sys);
        cashier.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        cashier.setVisible(true);
        this.setVisible(false);
        dispose();
    }

    pr
