package tracker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

/** Simple tests with no libraries. Run with: make test */
public final class TrackerTest {
    private static int passed = 0;

    private TrackerTest() { }

    public static void main(String[] args) throws IOException {
        sameCompanyDifferentRoles();
        duplicateJobRejected();
        statusCountsAndToday();
        deleteAndMissingId();
        validatorRejectsBadInput();
        csvRoundTrip();
        csvRejectsCorruptFile();
        goalMessages();
        System.out.println("All " + passed + " tests passed.");
    }

    private static void check(boolean condition, String name) {
        if (!condition) {
            throw new AssertionError("FAILED: " + name);
        }
        passed += 1;
    }

    private static void sameCompanyDifferentRoles() {
        Tracker t = new Tracker(List.of());
        t.add("Amazon", "SDE Intern", LocalDate.of(2026, 9, 1), "");
        t.add("Amazon", "Data Intern", LocalDate.of(2026, 9, 1), "");
        check(t.total() == 2, "two roles at one company are kept separate");
    }

    private static void duplicateJobRejected() {
        Tracker t = new Tracker(List.of());
        t.add("Amazon", "SDE Intern", LocalDate.of(2026, 9, 1), "");
        boolean threw = false;
        try {
            t.add("amazon", " sde intern ", LocalDate.of(2026, 9, 2), "");
        } catch (IllegalArgumentException e) {
            threw = true;
        }
        check(threw, "same company and role is rejected");
    }

    private static void statusCountsAndToday() {
        Tracker t = new Tracker(List.of());
        LocalDate today = LocalDate.of(2026, 9, 28);
        Application a = t.add("A", "x", today, "");
        t.add("B", "x", today, "");
        t.add("C", "x", today.minusDays(1), "");
        t.setStatus(a.id(), Status.REJECTED);
        check(t.countAppliedOn(today) == 2, "counts applications on a date");
        check(t.statusCounts().get(Status.REJECTED) == 1, "counts rejected");
        check(t.statusCounts().get(Status.APPLIED) == 2, "counts applied");
    }

    private static void deleteAndMissingId() {
        Tracker t = new Tracker(List.of());
        Application a = t.add("A", "x", LocalDate.of(2026, 9, 1), "");
        t.delete(a.id());
        check(t.total() == 0, "delete removes the application");
        boolean threw = false;
        try {
            t.delete(a.id());
        } catch (NoSuchElementException e) {
            threw = true;
        }
        check(threw, "deleting a missing id fails cleanly");
    }

    private static void validatorRejectsBadInput() {
        check(throwsIllegal(() -> Validator.text("bad\nline", "Notes", 50, false)),
                "newline rejected");
        check(throwsIllegal(() -> Validator.text("x".repeat(51), "Notes", 50, false)),
                "too long rejected");
        check(throwsIllegal(() -> Validator.text("   ", "Company", 50, true)),
                "blank required field rejected");
        check(throwsIllegal(() -> Validator.date("09/28/2026", "Date")),
                "wrong date format rejected");
        check(throwsIllegal(() -> Validator.notInFuture(LocalDate.now().plusDays(1), "Date")),
                "future date rejected");
    }

    private static void csvRoundTrip() throws IOException {
        Path dir = Files.createTempDirectory("tracker-test");
        CsvStore store = new CsvStore(dir.resolve("applications.csv"));
        Tracker t = new Tracker(List.of());
        Application a = t.add("Acme, Inc.", "Intern \"SWE\"", LocalDate.of(2026, 9, 1), "=SUM(A1)");
        t.recordEmail(a.id(), LocalDate.of(2026, 9, 5), "-Re: your application");
        t.setStatus(a.id(), Status.INTERVIEW);
        store.save(t.all());
        List<Application> loaded = store.load();
        check(loaded.size() == 1, "one row loaded");
        Application b = loaded.get(0);
        check(b.company().equals("Acme, Inc."), "comma survives round trip");
        check(b.role().equals("Intern \"SWE\""), "quotes survive round trip");
        check(b.notes().equals("=SUM(A1)"), "formula-looking text survives round trip");
        check(b.lastEmailSubject().equals("-Re: your application"), "subject survives");
        check(b.status() == Status.INTERVIEW, "status survives");
        String raw = Files.readString(dir.resolve("applications.csv"));
        check(raw.contains("\"'=SUM(A1)\""), "formula text is neutralized in the file");
    }

    private static void csvRejectsCorruptFile() throws IOException {
        Path dir = Files.createTempDirectory("tracker-test");
        Path file = dir.resolve("applications.csv");
        Files.writeString(file, CsvStore.HEADER + "\n\"1\",\"A\",\"x\",\"Nonsense\",\"2026-09-01\",\"\",\"\",\"\"\n");
        boolean threw = false;
        try {
            new CsvStore(file).load();
        } catch (IOException e) {
            threw = true;
        }
        check(threw, "corrupt row is reported, not silently dropped");
    }

    private static void goalMessages() {
        check(Report.goalLine(3).contains("2 to go"), "under goal message");
        check(Report.goalLine(5).contains("goal reached"), "goal reached message");
        check(Report.goalLine(7).contains("passed your goal by 2"), "passed goal message");
    }

    private static boolean throwsIllegal(Runnable r) {
        try {
            r.run();
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }
}
