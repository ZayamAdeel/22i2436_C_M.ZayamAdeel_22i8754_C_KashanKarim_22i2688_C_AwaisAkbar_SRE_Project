import java.io.*;
import java.util.*;

abstract class PointOfSale {

    // -----------------------------
    // Constants & Fields
    // -----------------------------
    private static final float DISCOUNT_RATE = 0.90f;
    private static final String COUPON_FILE = "Database/couponNumber.txt";
    private static final String TEMP_FILE = "Database/temp.txt";

    protected double totalPrice = 0;
    protected double tax = 1.06;
    protected Inventory inventory = Inventory.getInstance();

    protected List<Item> databaseItem = new ArrayList<>();
    protected List<Item> transactionItem = new ArrayList<>();

    // -----------------------------
    // Initialize Database
    // -----------------------------
    public boolean startNew(String databaseFile) {
        return inventory.accessInventory(databaseFile, databaseItem);
    }

    // -----------------------------
    // Item Entry
    // -----------------------------
    public boolean enterItem(int itemID, int amount) {
        return databaseItem.stream()
                .filter(i -> i.getItemID() == itemID)
                .findFirst()
                .map(item -> {
                    transactionItem.add(new Item(
                            itemID,
                            item.getItemName(),
                            item.getPrice(),
                            amount
                    ));
                    return true;
                })
                .orElse(false);
    }

    // -----------------------------
    // Running Total
    // -----------------------------
    public double updateTotal() {
        Item last = transactionItem.get(transactionItem.size() - 1);
        totalPrice += last.getPrice() * last.getAmount();
        return totalPrice;
    }

    // -----------------------------
    // Coupon Handling
    // -----------------------------
    public boolean coupon(String couponCode) {
        List<String> couponList = loadCouponList();
        boolean valid = couponList.contains(couponCode);

        if (valid) {
            totalPrice *= DISCOUNT_RATE;
        }
        return valid;
    }

    private List<String> loadCouponList() {
        List<String> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(COUPON_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                list.add(line.trim());
            }
        } catch (IOException ignored) {
        }
        return list;
    }

    // -----------------------------
    // Temp File Operations
    // -----------------------------
    public void createTemp(int id, int amount) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(TEMP_FILE, true))) {
            bw.write(id + " " + amount);
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Error writing temp file.");
        }
    }

    public boolean removeItems(int itemID) {
        Optional<Item> target = transactionItem.stream()
                .filter(i -> i.getItemID() == itemID)
                .findFirst();

        if (target.isEmpty()) return false;

        Item item = target.get();
        totalPrice -= item.getPrice() * item.getAmount();
        deleteTempItem(itemID);
        transactionItem.remove(item);

        if (transactionItem.isEmpty()) {
            new File(TEMP_FILE).delete();
        }

        return true;
    }

    // -----------------------------
    // Utilities
    // -----------------------------
    public double getTotal() { return totalPrice; }

    public boolean creditCard(String card) {
        return card.matches("\\d{16}");
    }

    public Item lastAddedItem() {
        return transactionItem.get(transactionItem.size() - 1);
    }

    public List<Item> getCart() { return transactionItem; }

    public int getCartSize() { return transactionItem.size(); }

    // -----------------------------
    // Abstract Methods
    // -----------------------------
    public abstract double endPOS(String textFile);
    public abstract void deleteTempItem(int id);
    public abstract void retrieveTemp(String textFile);
}
