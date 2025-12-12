import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class POH extends PointOfSale {

    private static final String TEMP_NEW = "Database/newTemp.txt";
    private static final String TEMP_FILE = "Database/temp.txt";
    private static final String RETURN_LOG = "Database/returnSale.txt";

    private List<ReturnItem> returnList = new ArrayList<>();
    private long phone;

    public POH() {
        this.phone = 0;
    }

    public POH(long phone) {
        this.phone = phone;
    }

    // ---------------------------------------------------------
    // Delete temp entry
    // ---------------------------------------------------------
    @Override
    public void deleteTempItem(int id) {
        try (BufferedReader reader = new BufferedReader(new FileReader(TEMP_FILE));
             BufferedWriter writer = new BufferedWriter(new FileWriter(TEMP_NEW))) {

            String type = reader.readLine();
            String phone = reader.readLine();

            writer.write(type);
            writer.newLine();
            writer.write(phone);
            writer.newLine();

            for (Item item : transactionItem) {
                if (item.getItemID() != id) {
                    writer.write(item.getItemID() + " " + item.getAmount());
                    writer.newLine();
                }
            }

        } catch (IOException e) {
            System.out.println("Error processing temp file.");
            return;
        }

        new File(TEMP_FILE).delete();
        new File(TEMP_NEW).renameTo(new File(TEMP_FILE));
    }

    // ---------------------------------------------------------
    // End POS (Rental Return)
    // ---------------------------------------------------------
    @Override
    public double endPOS(String textFile) {

        if (returnSale) {
            writeReturnLog();
        }

        if (transactionItem.isEmpty() || textFile.isEmpty()) {
            return totalPrice;
        }

        Management management = new Management();
        returnList = management.getLatestReturnDate(phone);

        double itemPrice;

        for (Item txItem : transactionItem) {
            for (ReturnItem r : returnList) {

                if (txItem.getItemID() == r.getItemID()) {

                    itemPrice = txItem.getAmount() *
                                txItem.getPrice() *
                                0.1 *
                                r.getDays();

                    totalPrice += itemPrice;

                    System.out.println("Item Name: " + txItem.getItemName() +
                            " | Days Late: " + r.getDays() +
                            " | Fee: " + itemPrice);

                    System.out.println("Running Total: " + totalPrice);
                }
            }
        }

        inventory.updateInventory(textFile, transactionItem, databaseItem, false);
        management.updateRentalStatus(phone, returnList);

        databaseItem.clear();
        transactionItem.clear();

        return totalPrice;
    }

    private void writeReturnLog() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RETURN_LOG, true))) {

            bw.newLine();

            for (Item item : transactionItem) {
                String log = item.getItemID() + " " +
                        item.getItemName() + " " +
                        item.getAmount() + " " +
                        (item.getPrice() * item.getAmount());
                bw.write(log);
                bw.newLine();
            }

        } catch (IOException e) {
            System.out.println("Error writing return log.");
        }
    }

    // ---------------------------------------------------------
    // Retrieve Temp
    // ---------------------------------------------------------
    @Override
    public void retrieveTemp(String textFile) {
        try (BufferedReader br = new BufferedReader(new FileReader(TEMP_FILE))) {

            String type = br.readLine();
            inventory.accessInventory(textFile, databaseItem);

            String phoneLine = br.readLine();
            System.out.println("Phone number:");
            System.out.println(phoneLine);

            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ");
                int itemNo = Integer.parseInt(parts[0]);
                int amount = Integer.parseInt(parts[1]);
                enterItem(itemNo, amount);
            }

            updateTotal();

        } catch (IOException e) {
            System.out.println("Error retrieving temp file.");
        }
    }
}
