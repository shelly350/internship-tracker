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

## Ideas to extend

- A `follow-up date` field and a list of applications with no reply after 14 days
- Export the pipeline as a text or CSV report
- A JavaFX or Swing window on top of `Tracker`
