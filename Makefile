.PHONY: default
default: run

.PHONY: clean
clean:
	@find . -type f -name "*.class" -exec rm {} \;
	@rm -f MANIFEST.mf
	@rm -f twiki.jar

.PHONY: compile
compile:
	@javac -Xlint:unchecked -cp ".:lib/*" App.java

.PHONY: run
run: clean compile
	@java -cp ".:lib/*" App

.PHONY: manifest
manifest:
	@touch MANIFEST.mf
	@echo "Manifest-Version: 1.0" > MANIFEST.mf
	@echo "Main-Class: App" >> MANIFEST.mf
	@echo "Class-Path: `find lib -name "*.jar" | tr '\n' ' '`" >> MANIFEST.mf

.PHONY: jar
jar: clean compile manifest
	@jar --create --file twiki.jar --manifest MANIFEST.mf *.class style.css icon.png lib/
	@chmod +x twiki.jar
