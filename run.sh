#!/bin/sh

if [ `uname` = 'Linux' ]
then
	"$JAVA_HOME/bin/java" -cp "$JAVA_HOME/lib/tools.jar:bin/JTrace.jar" src.JTrace $@
else
	"$JAVA_HOME/bin/java" -cp "$JAVA_HOME/lib/tools.jar;bin/JTrace.jar" src.JTrace $@
fi
