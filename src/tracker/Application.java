package tracker;

import java.time.LocalDate;


public final class Application {
    private final int id;
    private final String company;
    private final String role;
    private final Status status;
    private final LocalDate applied;
    private final LocalDate lastEmailDate; // null if no email recorded yet
    private final String lastEmailSubject; // "" if no email recorded yet
    private final String notes;

    public Application(int id, String company, String role, Status status,
                       LocalDate applied, LocalDate lastEmailDate,
                       String lastEmailSubject, String notes) {
        if (id <= 0) {
            throw new IllegalArgumentException("id must be positive.");
        }
        if (status == null || applied == null) {
            throw new IllegalArgumentException("status and applied date are required.");
        }
        this.id = id;
        this.company = Validator.text(company, "Company", Validator.MAX_COMPANY, true);
        this.role = Validator.text(role, "Role", Validator.MAX_ROLE, true);
        this.status = status;
        this.applied = applied;
        this.lastEmailDate = lastEmailDate;
        this.lastEmailSubject =
                Validator.text(lastEmailSubject, "Email subject", Validator.MAX_SUBJECT, false);
        this.notes = Validator.text(notes, "Notes", Validator.MAX_NOTES, false);
    }

    public int id() { return id; }
    public String company() { return company; }
    public String role() { return role; }
    public Status status() { return status; }
    public LocalDate applied() { return applied; }
    public LocalDate lastEmailDate() { return lastEmailDate; }
    public String lastEmailSubject() { return lastEmailSubject; }
    public String notes() { return notes; }

    public Application withStatus(Status newStatus) {
        return new Application(id, company, role, newStatus, applied,
                lastEmailDate, lastEmailSubject, notes);
    }

    public Application withNotes(String newNotes) {
        return new Application(id, company, role, status, applied,
                lastEmailDate, lastEmailSubject, newNotes);
    }

    public Application withLastEmail(LocalDate date, String subject) {
        return new Application(id, company, role, status, applied,
                date, subject, notes);
    }

    /** True if this is the same company and role as the arguments (ignoring case). */
    public boolean sameJob(String otherCompany, String otherRole) {
        return company.equalsIgnoreCase(otherCompany.trim())
                && role.equalsIgnoreCase(otherRole.trim());
    }
}
