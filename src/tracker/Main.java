package tracker;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/** Text menu for the internship tracker. Run with: make run */
public final class Main {
    private static final Path DATA_FILE = Path.of("data", "applications.csv");
    private static final int MAX_MENU_CHOICE = 9;

    private final Scanner in = new Scanner(System.in);
    private final CsvStore store = new CsvStore(DATA_FILE);
    private Tracker tracker;

    public static void main(String[] args) {
        Main app = new Main();
        try {
            app.tracker = new Tracker(app.store.load());
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Could not load your data: " + e.getMessage());
            System.err.println("Nothing was changed. Fix or move " + DATA_FILE + " and try again.");
            return;
        }
        app.run();
    }

    private void run() {
        System.out.println(Report.goalLine(tracker.countAppliedOn(LocalDate.now())));
        while (true) {
            printMenu();
            int choice = readInt("Choose", 1, MAX_MENU_CHOICE);
            if (choice < 0 || choice == MAX_MENU_CHOICE) {
                System.out.println("Bye.");
                return;
            }
            try {
                dispatch(choice);
            } catch (IllegalArgumentException | NoSuchElementException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("Could not save: " + e.getMessage());
            }
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("1) Add application      6) Edit notes");
        System.out.println("2) List all             7) Delete");
        System.out.println("3) List by status       8) Progress report");
        System.out.println("4) Update status        9) Quit");
        System.out.println("5) Record an email");
    }

    private void dispatch(int choice) throws IOException {
        switch (choice) {
            case 1 -> add();
            case 2 -> printList(tracker.all());
            case 3 -> printList(tracker.withStatus(readStatus()));
            case 4 -> updateStatus();
            case 5 -> recordEmail();
            case 6 -> editNotes();
            case 7 -> delete();
            case 8 -> System.out.print(Report.render(tracker, LocalDate.now()));
            default -> throw new IllegalStateException("Unhandled menu choice " + choice);
        }
    }

    private void add() throws IOException {
        String company = ask("Company");
        String role = ask("Role");
        String dateText = ask("Date applied (blank for today)");
        LocalDate applied = dateText.isBlank()
                ? LocalDate.now()
                : Validator.notInFuture(Validator.date(dateText, "Date applied"), "Date applied");
        String notes = ask("Notes (optional)");
        Application a = tracker.add(company, role, applied, notes);
        store.save(tracker.all());
        System.out.println("Added #" + a.id() + ". " + Report.goalLine(
                tracker.countAppliedOn(LocalDate.now())));
    }

    private void updateStatus() throws IOException {
        int id = readInt("Application id", 1, Integer.MAX_VALUE);
        tracker.get(id);
        tracker.setStatus(id, readStatus());
        store.save(tracker.all());
        System.out.println("Updated.");
    }

    private void recordEmail() throws IOException {
        int id = readInt("Application id", 1, Integer.MAX_VALUE);
        tracker.get(id);
        LocalDate date = Validator.notInFuture(
                Validator.date(ask("Date the email was sent (2026-09-28)"), "Email date"),
                "Email date");
        String subject = ask("Subject line");
        tracker.recordEmail(id, date, subject);
        store.save(tracker.all());
        System.out.println("Saved.");
    }

    private void editNotes() throws IOException {
        int id = readInt("Application id", 1, Integer.MAX_VALUE);
        tracker.get(id);
        tracker.setNotes(id, ask("New notes"));
        store.save(tracker.all());
        System.out.println("Saved.");
    }

    private void delete() throws IOException {
        int id = readInt("Application id", 1, Integer.MAX_VALUE);
        Application a = tracker.get(id);
        String answer = ask("Delete " + a.company() + " / " + a.role() + "? Type yes to confirm");
        if (answer.equalsIgnoreCase("yes")) {
            tracker.delete(id);
            store.save(tracker.all());
            System.out.println("Deleted.");
        } else {
            System.out.println("Cancelled.");
        }
    }

    private void printList(List<Application> apps) {
        if (apps.isEmpty()) {
            System.out.println("(none)");
            return;
        }
        System.out.printf("%-4s %-20s %-24s %-17s %-10s %-10s %s%n",
                "ID", "Company", "Role", "Status", "Applied", "Last email", "Subject");
        for (Application a : apps) {
            String emailDate = a.lastEmailDate() == null ? "-" : a.lastEmailDate().toString();
            System.out.printf("%-4d %-20s %-24s %-17s %-10s %-10s %s%n",
                    a.id(), cut(a.company(), 20), cut(a.role(), 24), a.status().label(),
                    a.applied(), emailDate, cut(a.lastEmailSubject(), 40));
        }
    }

    private Status readStatus() {
        Status[] all = Status.values();
        for (int i = 0; i < all.length; i += 1) {
            System.out.println("  " + (i + 1) + ") " + all[i].label());
        }
        int n = readInt("Status number", 1, all.length);
        if (n < 0) {
            throw new IllegalArgumentException("No input.");
        }
        return all[n - 1];
    }

    private String ask(String prompt) {
        System.out.print(prompt + ": ");
        return in.hasNextLine() ? in.nextLine() : "";
    }

    /** Reads an integer in [low, high]. Returns -1 if input has ended. */
    private int readInt(String prompt, int low, int high) {
        while (true) {
            System.out.print(prompt + " (" + low + "-" + (high == Integer.MAX_VALUE ? "..." : high) + "): ");
            if (!in.hasNextLine()) {
                return -1;
            }
            try {
                int n = Integer.parseInt(in.nextLine().trim());
                if (n >= low && n <= high) {
                    return n;
                }
            } catch (NumberFormatException e) {
                // fall through and ask again
            }
            System.out.println("Please enter a whole number in range.");
        }
    }

    private static String cut(String s, int width) {
        return s.length() <= width ? s : s.substring(0, width - 3) + "...";
    }
}
