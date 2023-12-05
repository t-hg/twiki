.PHONY: default
default: run

.PHONY: clean
clean:
	@find . -type f -name "*.class" -exec rm {} \;

.PHONY: compile
compile: clean
	@javac -Xlint:unchecked App.java

.PHONY: run
run: compile
	@java App
