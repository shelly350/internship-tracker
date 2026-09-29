package tracker;

/** The stages an internship application can be in. */
public enum Status {
    APPLIED("Applied"),
    WAITING("Waiting"),
    ONLINE_ASSESSMENT("Online assessment"),
    INTERVIEW("Interview"),
    SECOND_INTERVIEW("Second interview"),
    OFFER("Offer"),
    REJECTED("Rejected"),
    WITHDRAWN("Withdrawn");

    private final String label;

    Status(String label) {
        this.label = label;
    }

    /** Returns the human-readable name, e.g. "Online assessment". */
    public String label() {
        return label;
    }

    /** Parses a label such as "interview" (case-insensitive). */
    public static Status parse(String text) {
        String trimmed = text.trim();
        for (Status s : values()) {
            if (s.label.equalsIgnoreCase(trimmed)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + text);
    }
}
