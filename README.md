# Internship Tracker (local, Java)

A command-line program that keeps track of internship applications: who you
applied to, what stage each one is at, when the last email arrived, and how you
are doing against a goal of 5 applications a day. It is written with plain Java
and no libraries, using ideas from 61A/61B/61C: classes and immutability,
lists and maps, file I/O, and testing.

## Run it

You need a JDK (version 17 or newer) and `make`.

    make run      # build and start the menu
    make test     # build and run the tests
    make clean    # delete compiled files

Your data is saved to `data/applications.csv` after every change. The previous
version is kept as `data/applications.csv.bak`.

## Files

| File | Job |
|------|-----|
| `Status.java` | Enum of the stages (Applied, Interview, Rejected, ...) |
| `Application.java` | One immutable application; company + role identify it |
| `Tracker.java` | The list of applications and the rules between them |
| `Validator.java` | Checks all text and dates before they are accepted |
| `CsvStore.java` | Saves and loads the CSV file |
| `Report.java` | Daily goal message, 7-day chart, pipeline summary |
| `Main.java` | Text menu |
| `TrackerTest.java` | Tests, run with `make test` |

Two roles at the same company are separate applications. Adding the same
company and role twice is rejected.

## Safety

- No network access, no running other programs, no `eval`, and no Java
  object deserialization. The data file is read as plain text only.
- Every input goes through `Validator`: length limits, no control characters,
  strict date format, no future applied dates.
- A corrupt data file is reported with its line number and never overwritten.
- Saves write a temporary file first and then swap it in, so a crash cannot
  leave a half-written file.
- Text starting with `=`, `+`, `-` or `@` is prefixed in the CSV so Excel or
  Sheets cannot run it as a formula.

The file is not encrypted. Anyone who can log in to your computer can read it,
so use your normal account protections.

## Putting it on GitHub

`.gitignore` already excludes `data/` and `out/`, so your real applications are
not uploaded. Check with `git status` before your first commit.

## What is different from the web version

The web version reads Gmail through Claude. This one has no email sync: you
record an email yourself with menu option 5 (date and subject). Adding Gmail
sign-in would need Google's OAuth setup and is a good follow-up project.

## Ideas to extend

- A `follow-up date` field and a list of applications with no reply after 14 days
- Export the pipeline as a text or CSV report
- A JavaFX or Swing window on top of `Tracker`
