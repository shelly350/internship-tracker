SRC = $(wildcard src/tracker/*.java)

all: out/.built

out/.built: $(SRC)
	mkdir -p out
	javac -Xlint:all -d out $(SRC)
	touch out/.built

run: all
	java -cp out tracker.Main

test: all
	java -cp out tracker.TrackerTest

clean:
	rm -rf out

.PHONY: run test clean
