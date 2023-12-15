.PHONY: default
default: run

.PHONY: clean
clean:
	@find . -type f -name "*.class" -exec rm {} \;
	@rm -f twiki.jar

.PHONY: compile
compile: clean
	@javac -Xlint:unchecked -cp ".:lib/*" App.java

.PHONY: run
run: compile
	@java -cp ".:lib/*" App

.PHONY: jar
jar: compile
	@jar --create --file twiki.jar --manifest MANIFEST.mf *.class style.css icon.png lib/
	@chmod +x twiki.jar
