.PHONY: default
default: run

.PHONY: clean
clean:
	@find . -type f -name "*.class" -exec rm {} \;
	@find . -type f -name "*.jar" -exec rm {} \;

.PHONY: compile
compile: clean
	@javac -Xlint:unchecked App.java

.PHONY: run
run: compile
	@java App

.PHONY: jar
jar: compile
	@jar cfe twiki.jar App *.class style.css icon.png
	@chmod +x twiki.jar
