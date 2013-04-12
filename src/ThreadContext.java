package src;

import java.util.*;
import java.io.*;
import com.sun.jdi.*;
import com.sun.jdi.request.*;
import com.sun.jdi.event.*;

/**
 * Keeps information about the thread to format events correctly
 */
class ThreadContext {
    final ThreadReference thread;
    final String prefix;
    final int id;
    final PrintWriter output;
    final VirtualMachine vm;
    StringBuffer indent;
    int localDepth;
    int startDepth;
    boolean hasStarted;

    
    ThreadContext(ThreadReference thread, PrintWriter output, VirtualMachine vm) {
        this.thread = thread;
        this.id = TraceThread.allocateThreadID();
        this.prefix = String.format("%3d ", id);
        this.indent = new StringBuffer();
        this.output = output;
        this.vm = vm;
        this.localDepth = 0;
        this.startDepth = 0;
        this.hasStarted = false;
        
        println("*** New Thread :'" + thread.name() + "' ***");
    }

    private void println(String str) {
        output.printf("%s%s+-%s\n", prefix, indent, str);
    }

    void methodEntryEvent(MethodEntryEvent event)  {
        try {
            if (!hasStarted) {
                hasStarted = true;
                startDepth = thread.frameCount() - 1;
            }
        } catch (IncompatibleThreadStateException e) {}

        println(event.method().declaringType().name() + "." + event.method().name() + "()");
        indent.append("| ");
        localDepth++;
    }
	
    void methodExitEvent(MethodExitEvent event)  {
        indent.setLength(Math.max(indent.length()-2, 0));
        localDepth--;
    }
	
    void fieldWatchEvent(ModificationWatchpointEvent event)  {
        Field field = event.field();
        Value value = event.valueToBe();
        println("    " + field.name() + " = " + value);
    }
	
    void exceptionEvent(ExceptionEvent event) {
        println("Exception: " + event.exception());
        println("Caught: " + event.catchLocation());
        
        StepRequest request = vm.eventRequestManager().createStepRequest(thread,
                                                                         StepRequest.STEP_MIN,
                                                                         StepRequest.STEP_INTO);
        request.addCountFilter(1);
        request.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        request.enable();
    }

    /* Step to exception catch */
    void stepEvent(StepEvent event)  {
        /* Fix depth */
        indent = new StringBuffer();
        localDepth = 0;
        
        try {
            localDepth = thread.frameCount() - startDepth;
            for (int i = 0; i < localDepth ; i++) {
                indent.append("| ");
            }

        } catch (IncompatibleThreadStateException e) { }

        vm.eventRequestManager().deleteEventRequest(event.request());
    }

    void threadDeathEvent(ThreadDeathEvent event)  {
        indent.setLength(0);
        println("*** Dead Thread :'" + thread.name() + "' ***");
    }

    int getDepth() {
        return localDepth;
    }
}	
