JTrace
========

Record a trace of execution of a running JVM in a single method.

Running
-------

Thanks to java's wonderful classpath system, you'll need to add tools.jar from a JDK to your classpath to run.
Aren't you glad they implemented it this way, so you have to track down the jar yourself?


Attach to a process:
~~~
java -cp tools.jar;JTrace.jar src.JLaunch attach <pid> <class> <method>
~~~

Launch a process:
~~~
java -cp tools.jar;JTrace.jar src.JLaunch launch <CLI command>:<address> <class> <method>
~~~

