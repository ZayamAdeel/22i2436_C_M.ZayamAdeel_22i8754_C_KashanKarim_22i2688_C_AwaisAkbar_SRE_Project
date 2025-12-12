import java.io.*;

public class POR extends PointOfSale {

    private static final String TEMP_NEW = "Database/newTemp.txt";
    private static final String TEMP_FILE = "Database/temp.txt";

    private long phoneNum;

    public POR(long phoneNum) {
        this.phoneNum = phoneNum;
    }

    // ---------------------------------------------------------
    // Delete a temporary item entry
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
            System.out.println("Error reading/writing temp file");
            return;
        }

        new File(TEMP_FILE).delete();
        new File(TEMP_NEW).renameTo(new File(TEMP_FILE));
    }

    // ---------------------------------------------------------
    // Finalize Rental Transaction
    // ---------------------------------------------------------
    @Override
    public double endPOS(String textFile) {

        if (!transactionItem.isEmpty()) {

            Management man = new Management();
            man.addRental(this.phoneNum, this.transactionItem);

            detectSystem();
            totalPrice = totalPrice * tax;

            inventory.updateInventory(textFile, transactionItem, databaseItem, true);
        }

        new File(TEMP_FILE).delete();

        databaseItem.clear();
        transactionItem.clear();

        return totalPrice;
    }

    // ---------------------------------------------------------
    // Rebuild transaction from temp file
    // ---------------------------------------------------------
    @Override
    public void retrieveTemp(String textFile) {
        try (BufferedReader br = new BufferedReader(new FileReader(TEMP_FILE))) {

            String type = br.readLine();
            inventory.accessInventory(textFile, databaseItem);

            String phone = br.readLine();
            System.out.println("Phone number:");
            System.out.println(phone);

            String line;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ");
                int itemNo = Integer.parseInt(parts[0]);
                int amount = Integer.parseInt(parts[1]);
                enterItem(itemNo, amount);
            }

            updateTotal();

        } catch (IOException e) {
            System.out.println("Error reading temp rental file");
        }
    }
}
