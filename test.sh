#!/bin/sh

DEBUG=-Xrunjdwp:transport=dt_shmem,address=jtrace,server=y,suspend=n

if [ `uname` = 'Linux' ]
then
	"$JAVA_HOME/bin/java" $DEBUG -cp "$JAVA_HOME/lib/tools.jar:bin/JTrace.jar" src.JTrace test 0 0 0
else
	"$JAVA_HOME/bin/java" $DEBUG -cp "$JAVA_HOME/lib/tools.jar;bin/JTrace.jar" src.JTrace test 0 0 0
fi
