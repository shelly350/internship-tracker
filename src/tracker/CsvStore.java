package tracker;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public final class CsvStore {
    static final String HEADER =
            "id,company,role,status,applied,last_email_date,last_email_subject,notes";
    private static final long MAX_FILE_BYTES = 5_000_000;
    private static final int FIELD_COUNT = 8;
    private static final String FORMULA_CHARS = "=+-@'";

    private final Path file;

    public CsvStore(Path file) {
        this.file = file;
    }

    /** Loads all applications. A missing file means an empty list. */
    public List<Application> load() throws IOException {
        List<Application> result = new ArrayList<>();
        if (!Files.exists(file)) {
            return result;
        }
        if (Files.size(file) > MAX_FILE_BYTES) {
            throw new IOException(file + " is unexpectedly large; refusing to read it.");
        }
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        if (lines.isEmpty() || !lines.get(0).equals(HEADER)) {
            throw new IOException(file + " does not have the expected header line.");
        }
        for (int i = 1; i < lines.size(); i += 1) {
            if (lines.get(i).isBlank()) {
                continue;
            }
            try {
                result.add(parseRow(lines.get(i)));
            } catch (IllegalArgumentException e) {
                throw new IOException("Line " + (i + 1) + " of " + file + ": " + e.getMessage());
            }
        }
        return result;
    }

    /** Saves all applications, keeping the previous file as a .bak backup. */
    public void save(List<Application> apps) throws IOException {
        Path dir = file.toAbsolutePath().getParent();
        Files.createDirectories(dir);
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        for (Application a : apps) {
            lines.add(toRow(a));
        }
        Path temp = Files.createTempFile(dir, "applications", ".tmp");
        try {
            Files.write(temp, lines, StandardCharsets.UTF_8);
            if (Files.exists(file)) {
                Files.copy(file, Path.of(file + ".bak"), StandardCopyOption.REPLACE_EXISTING);
            }
            try {
                Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    private static String toRow(Application a) {
        String emailDate = a.lastEmailDate() == null ? "" : a.lastEmailDate().toString();
        return encode(String.valueOf(a.id())) + "," + encode(a.company()) + ","
                + encode(a.role()) + "," + encode(a.status().label()) + ","
                + encode(a.applied().toString()) + "," + encode(emailDate) + ","
                + encode(a.lastEmailSubject()) + "," + encode(a.notes());
    }

    private static Application parseRow(String line) {
        List<String> f = splitLine(line);
        if (f.size() != FIELD_COUNT) {
            throw new IllegalArgumentException(
                    "expected " + FIELD_COUNT + " fields but found " + f.size() + ".");
        }
        int id;
        try {
            id = Integer.parseInt(f.get(0));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("id is not a number.");
        }
        LocalDate emailDate =
                f.get(5).isEmpty() ? null : Validator.date(f.get(5), "Email date");
        return new Application(id, f.get(1), f.get(2), Status.parse(f.get(3)),
                Validator.date(f.get(4), "Applied date"), emailDate, f.get(6), f.get(7));
    }

    /** Quotes one field for CSV output. */
    static String encode(String field) {
        String safe = field;
        if (!safe.isEmpty() && FORMULA_CHARS.indexOf(safe.charAt(0)) >= 0) {
            safe = "'" + safe;
        }
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }

    /** Splits one CSV line into decoded fields. */
    static List<String> splitLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i += 1) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"' && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i += 1;
                } else if (c == '"') {
                    inQuotes = false;
                } else {
                    current.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                fields.add(decode(current.toString()));
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        if (inQuotes) {
            throw new IllegalArgumentException("unterminated quote.");
        }
        fields.add(decode(current.toString()));
        return fields;
    }

    private static String decode(String field) {
        if (!field.isEmpty() && field.charAt(0) == '\'') {
            return field.substring(1);
        }
        return field;
    }
}
