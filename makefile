JFLAGS = -Xlint:unchecked -classpath "$(JAVA_HOME)/lib/tools.jar;." -d bin

JC = javac

.SUFFIXES: .java .class

.java.class:
	$(JC) $(JFLAGS) $*.java

SOURCES = $(shell find . -type f)
CLASSES = $(SOURCES:.java=.class)

default: jar

classes: $(CLASSES)

jar: classes
	cd bin; jar cvfe JTrace.jar src.JTrace `find . -iname *.class`

run:
	$(JRE) -jar 'bin/JTrace.jar'

clean: 
	$(RM) $(shell find bin/ -iname *.class)
