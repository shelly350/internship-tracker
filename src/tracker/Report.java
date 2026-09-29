package tracker;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;

/** Builds the text shown by the progress report: daily goal and pipeline. */
public final class Report {
    static final int DAILY_GOAL = 5;

    private Report() { }

    /** One line saying how today compares with the daily goal. */
    public static String goalLine(int appliedToday) {
        if (appliedToday < DAILY_GOAL) {
            return "Today: " + appliedToday + " of " + DAILY_GOAL
                    + " (" + (DAILY_GOAL - appliedToday) + " to go)";
        }
        if (appliedToday == DAILY_GOAL) {
            return "Today: " + appliedToday + " of " + DAILY_GOAL + " - goal reached!";
        }
        return "Today: " + appliedToday + " of " + DAILY_GOAL
                + " - passed your goal by " + (appliedToday - DAILY_GOAL) + ".";
    }

    /** The full report for the given day. */
    public static String render(Tracker tracker, LocalDate today) {
        StringBuilder sb = new StringBuilder();
        sb.append("Applied to ").append(tracker.total()).append(" internships\n");
        sb.append(goalLine(tracker.countAppliedOn(today))).append("\n\n");

        sb.append("Last 7 days (goal ").append(DAILY_GOAL).append("):\n");
        for (int i = 6; i >= 0; i -= 1) {
            LocalDate day = today.minusDays(i);
            int n = tracker.countAppliedOn(day);
            sb.append(String.format("  %s %s  %s %d%n",
                    day.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.US),
                    day, bar(n), n));
        }

        Map<Status, Integer> c = tracker.statusCounts();
        int waiting = c.get(Status.APPLIED) + c.get(Status.WAITING);
        int inProcess = c.get(Status.ONLINE_ASSESSMENT) + c.get(Status.INTERVIEW)
                + c.get(Status.SECOND_INTERVIEW) + c.get(Status.OFFER);
        sb.append("\nPipeline (by current status):\n");
        sb.append(line(0, "Applied", tracker.total()));
        sb.append(line(1, "No response yet", waiting));
        sb.append(line(1, "In process", inProcess));
        sb.append(line(2, "Online assessment", c.get(Status.ONLINE_ASSESSMENT)));
        sb.append(line(2, "Interview", c.get(Status.INTERVIEW)));
        sb.append(line(2, "Second interview", c.get(Status.SECOND_INTERVIEW)));
        sb.append(line(2, "Offer", c.get(Status.OFFER)));
        sb.append(line(1, "Rejected", c.get(Status.REJECTED)));
        sb.append(line(1, "Withdrawn", c.get(Status.WITHDRAWN)));
        return sb.toString();
    }

    private static String line(int indent, String name, int count) {
        String pad = "  ".repeat(indent + 1);
        return String.format("%s%-" + (22 - 2 * indent) + "s %3d  %s%n",
                pad, name, count, bar(count));
    }

    private static String bar(int n) {
        return "#".repeat(Math.min(n, 30));
    }
}
