package tracker;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/** Holds all applications in memory and enforces the rules between them. */
public class Tracker {
    private final List<Application> apps = new ArrayList<>();

    /** Builds a tracker from saved data; rejects duplicate ids or duplicate jobs. */
    public Tracker(List<Application> initial) {
        for (Application a : initial) {
            if (indexOf(a.id()) >= 0) {
                throw new IllegalArgumentException("Duplicate id " + a.id() + ".");
            }
            if (findJob(a.company(), a.role()) >= 0) {
                throw new IllegalArgumentException(
                        "Duplicate application: " + a.company() + " / " + a.role() + ".");
            }
            apps.add(a);
        }
    }

    /** Adds a new application. Two roles at one company are allowed. */
    public Application add(String company, String role, LocalDate applied, String notes) {
        if (findJob(company, role) >= 0) {
            throw new IllegalArgumentException(
                    "You already have an application for that company and role.");
        }
        Application a = new Application(nextId(), company, role, Status.APPLIED,
                applied, null, "", notes);
        apps.add(a);
        return a;
    }

    public List<Application> all() {
        return Collections.unmodifiableList(new ArrayList<>(apps));
    }

    public List<Application> withStatus(Status s) {
        List<Application> result = new ArrayList<>();
        for (Application a : apps) {
            if (a.status() == s) {
                result.add(a);
            }
        }
        return result;
    }

    public Application get(int id) {
        return apps.get(requireIndex(id));
    }

    public void setStatus(int id, Status s) {
        int i = requireIndex(id);
        apps.set(i, apps.get(i).withStatus(s));
    }

    public void setNotes(int id, String notes) {
        int i = requireIndex(id);
        apps.set(i, apps.get(i).withNotes(notes));
    }

    public void recordEmail(int id, LocalDate date, String subject) {
        int i = requireIndex(id);
        apps.set(i, apps.get(i).withLastEmail(date, subject));
    }

    public void delete(int id) {
        apps.remove(requireIndex(id));
    }

    public int total() {
        return apps.size();
    }

    /** How many applications have this applied date. */
    public int countAppliedOn(LocalDate day) {
        int count = 0;
        for (Application a : apps) {
            if (a.applied().equals(day)) {
                count += 1;
            }
        }
        return count;
    }

    /** Number of applications in each status (every status is present, maybe 0). */
    public Map<Status, Integer> statusCounts() {
        Map<Status, Integer> counts = new EnumMap<>(Status.class);
        for (Status s : Status.values()) {
            counts.put(s, 0);
        }
        for (Application a : apps) {
            counts.put(a.status(), counts.get(a.status()) + 1);
        }
        return counts;
    }

    private int nextId() {
        int max = 0;
        for (Application a : apps) {
            max = Math.max(max, a.id());
        }
        return max + 1;
    }

    private int indexOf(int id) {
        for (int i = 0; i < apps.size(); i += 1) {
            if (apps.get(i).id() == id) {
                return i;
            }
        }
        return -1;
    }

    private int requireIndex(int id) {
        int i = indexOf(id);
        if (i < 0) {
            throw new NoSuchElementException("No application with id " + id + ".");
        }
        return i;
    }

    private int findJob(String company, String role) {
        for (int i = 0; i < apps.size(); i += 1) {
            if (apps.get(i).sameJob(company, role)) {
                return i;
            }
        }
        return -1;
    }
}
