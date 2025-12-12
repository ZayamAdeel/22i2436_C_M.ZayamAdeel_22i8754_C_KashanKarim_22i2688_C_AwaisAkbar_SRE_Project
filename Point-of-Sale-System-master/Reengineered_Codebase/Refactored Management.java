import java.text.*;
import java.util.*;

public class Management {

    private final UserFileRepository repo;

    public Management() {
        this.repo = new UserFileRepository("Database/userDatabase.txt");
    }

    public Boolean checkUser(Long phone) {
        return repo.loadRawLines()
                   .stream()
                   .skip(1)
                   .map(repo::parsePhone)
                   .anyMatch(p -> p == phone);
    }

    public List<ReturnItem> getLatestReturnDate(Long phone) {
        List<ReturnItem> results = new ArrayList<>();
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy");

        for (String line : repo.loadRawLines()) {
            if (repo.parsePhone(line) == phone) {

                for (String entry : repo.parseRentalEntries(line)) {
                    String[] parts = entry.split(",");

                    String itemId = parts[0];
                    String dueDate = parts[1];
                    boolean returned = parts[2].equalsIgnoreCase("true");
                    if (returned) continue;

                    try {
                        Date d = formatter.parse(dueDate);
                        Calendar c = Calendar.getInstance();
                        c.setTime(d);
                        int daysLate = daysBetween(c);
                        results.add(new ReturnItem(Integer.parseInt(itemId), daysLate));
                    } catch (Exception ignored) {}
                }
            }
        }

        return results;
    }

    public boolean createUser(Long phone) {
        List<String> lines = repo.loadRawLines();
        lines.add(Long.toString(phone));
        repo.saveRawLines(lines);
        return true;
    }

    public static int daysBetween(Calendar due) {
        Calendar now = Calendar.getInstance();
        long diff = now.getTimeInMillis() - due.getTimeInMillis();
        return (int)(diff / (1000 * 60 * 60 * 24));
    }

    public static void addRental(long phone, List<Item> rentalList) {
        UserFileRepository repo = new UserFileRepository("Database/userDatabase.txt");
        List<String> lines = repo.loadRawLines();
        SimpleDateFormat fmt = new SimpleDateFormat("MM/dd/yy");
        String today = fmt.format(new Date());

        List<String> updated = new ArrayList<>();

        for (String line : lines) {
            long ph = repo.parsePhone(line);
            if (ph == phone) {
                StringBuilder sb = new StringBuilder(line);
                for (Item item : rentalList) {
                    sb.append(" ").append(item.getItemID()).append(",")
                      .append(today).append(",false");
                }
                updated.add(sb.toString());
            } else {
                updated.add(line);
            }
        }

        repo.saveRawLines(updated);
    }

    public void updateRentalStatus(long phone, List<ReturnItem> returnedList) {
        List<String> lines = repo.loadRawLines();
        SimpleDateFormat fmt = new SimpleDateFormat("MM/dd/yy");
        String today = fmt.format(new Date());

        List<String> updated = new ArrayList<>();

        for (String line : lines) {
            long ph = repo.parsePhone(line);

            if (ph == phone) {
                StringBuilder sb = new StringBuilder(Long.toString(phone));

                for (String entry : repo.parseRentalEntries(line)) {
                    String[] parts = entry.split(",");
                    int itemId = Integer.parseInt(parts[0]);
                    boolean returned = parts[2].equals("true");

                    if (!returned && isBeingReturned(itemId, returnedList)) {
                        sb.append(" ").append(itemId).append(",").append(today).append(",true");
                    } else {
                        sb.append(" ").append(entry);
                    }
                }
                updated.add(sb.toString());
            } else {
                updated.add(line);
            }
        }

        repo.saveRawLines(updated);
    }

    private boolean isBeingReturned(int id, List<ReturnItem> list) {
        return list.stream().anyMatch(r -> r.getItemID() == id);
    }
}
