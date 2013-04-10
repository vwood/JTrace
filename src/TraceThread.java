package src;

import java.util.*;
import java.io.*;
import com.sun.jdi.*;
import com.sun.jdi.request.*;
import com.sun.jdi.event.*;

public class TraceThread extends Thread {
    private final VirtualMachine vm;
    private final String[] excludedPackages;
    private final PrintWriter output;

    private static int lastThreadID = 0;

    private boolean vmConnected = true;
    private boolean vmRunning = true;

    private Map<ThreadReference, ThreadTrace> traceMap = new HashMap<ThreadReference, ThreadTrace>();

    TraceThread(VirtualMachine vm, String[] excludedPackages, PrintWriter output) {
        super("Tracer");
        this.vm = vm;
        this.excludedPackages = excludedPackages;
        this.output = output;
    }

    /**
     * Allocate an id for a thread to mark all of its events
     */
    public static int allocateThreadID() {
        lastThreadID += 1;
        return lastThreadID;
    }
    
    /**
     * While connected, record JDI events
     */
    public void run() {
        EventQueue queue = vm.eventQueue();
        while (vmConnected) {
            try {
                EventSet eventSet = queue.remove();
                for (Event event : eventSet) {
                    handleEvent(event);
                }
                eventSet.resume();
            } catch (VMDisconnectedException discExc) {
                handleDisconnectedException();
                break;
            } catch (InterruptedException exc) {}
        }
    }

    /**
     * Request to be notified about basic events
     */
    void setDefaultEventRequests() {
        EventRequestManager mgr = vm.eventRequestManager();

        ExceptionRequest excReq = mgr.createExceptionRequest(null, true, true); 
        excReq.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        excReq.enable();

        MethodEntryRequest menr = mgr.createMethodEntryRequest();
        
        for (String pkg : excludedPackages) {
            menr.addClassExclusionFilter(pkg);
        }
        menr.setSuspendPolicy(EventRequest.SUSPEND_NONE);
        menr.enable();

        MethodExitRequest mexr = mgr.createMethodExitRequest();
        for (String pkg : excludedPackages) {
            mexr.addClassExclusionFilter(pkg);
        }
        mexr.setSuspendPolicy(EventRequest.SUSPEND_NONE);
        mexr.enable();

        ThreadDeathRequest tdr = mgr.createThreadDeathRequest();
        tdr.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        tdr.enable();
    }

    /**
     * Request to be notified about field events
     */
    void setFieldEventRequests() {
        EventRequestManager mgr = vm.eventRequestManager();

        ClassPrepareRequest cpr = mgr.createClassPrepareRequest();
        for (String pkg : excludedPackages) {
            cpr.addClassExclusionFilter(pkg);
        }
        cpr.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        cpr.enable();
    }

    /**
     * Create/get the ThreadTrace instance for the thread,
     */
    ThreadTrace threadTrace(ThreadReference thread) {
        ThreadTrace trace = traceMap.get(thread);
        if (trace == null) {
            trace = new ThreadTrace(thread, output, vm);
            traceMap.put(thread, trace);
        }
        return trace;
    }

    /**
     * Dispatch incoming events
     */
    private void handleEvent(Event event) {
        if (event instanceof ExceptionEvent) {
            exceptionEvent((ExceptionEvent)event);
        } else if (event instanceof ModificationWatchpointEvent) {
            fieldWatchEvent((ModificationWatchpointEvent)event);
        } else if (event instanceof MethodEntryEvent) {
            methodEntryEvent((MethodEntryEvent)event);
        } else if (event instanceof MethodExitEvent) {
            methodExitEvent((MethodExitEvent)event);
        } else if (event instanceof StepEvent) {
            stepEvent((StepEvent)event);
        } else if (event instanceof ThreadDeathEvent) {
            threadDeathEvent((ThreadDeathEvent)event);
        } else if (event instanceof ClassPrepareEvent) {
            classPrepareEvent((ClassPrepareEvent)event);
        } else if (event instanceof VMStartEvent) {
            vmStartEvent((VMStartEvent)event);
        } else if (event instanceof VMDeathEvent) {
            vmDeathEvent((VMDeathEvent)event);
        } else if (event instanceof VMDisconnectEvent) {
            vmDisconnectEvent((VMDisconnectEvent)event);
        } else {
            throw new Error("Unexpected event type");
        }
    }

    /***
     * Handle disconnection cleanly
     */
    synchronized void handleDisconnectedException() {
        EventQueue queue = vm.eventQueue();
        while (vmConnected) {
            try {
                EventSet eventSet = queue.remove();
                EventIterator iter = eventSet.eventIterator();
                while (iter.hasNext()) {
                    Event event = iter.nextEvent();
                    if (event instanceof VMDeathEvent) {
                        vmDeathEvent((VMDeathEvent)event);
                    } else if (event instanceof VMDisconnectEvent) {
                        vmDisconnectEvent((VMDisconnectEvent)event);
                    } 
                }
                eventSet.resume();
            } catch (InterruptedException exc) { /* ignore */ }
        }
    }

    private void vmStartEvent(VMStartEvent event)  {
        output.println("-- VM Started --");
    }

    /* Pass event to the correct ThreadTrace */
    private void methodEntryEvent(MethodEntryEvent event)  {
        threadTrace(event.thread()).methodEntryEvent(event);
    }
 
    /* Pass event to the correct ThreadTrace */
    private void methodExitEvent(MethodExitEvent event)  {
        threadTrace(event.thread()).methodExitEvent(event);
    }

    /* Pass event to the correct ThreadTrace */
    private void stepEvent(StepEvent event)  {
        threadTrace(event.thread()).stepEvent(event);
    }

    /* Pass event to the correct ThreadTrace */
    private void fieldWatchEvent(ModificationWatchpointEvent event)  {
        threadTrace(event.thread()).fieldWatchEvent(event);
    }

    /* Only pass death events to ThreadTrace if it already exists */
    void threadDeathEvent(ThreadDeathEvent event)  {
        ThreadTrace trace = (ThreadTrace)traceMap.get(event.thread());
        if (trace != null) {
            trace.threadDeathEvent(event);
        }
    }

    /**
     * A new class has been loaded.  
     * Set watchpoints on each of its fields
     */
    private void classPrepareEvent(ClassPrepareEvent event)  {
        EventRequestManager mgr = vm.eventRequestManager();
        List fields = event.referenceType().visibleFields();
        for (Iterator it = fields.iterator(); it.hasNext(); ) {
            Field field = (Field)it.next();
            ModificationWatchpointRequest req = 
                mgr.createModificationWatchpointRequest(field);
            for (String pkg : excludedPackages) {
                req.addClassExclusionFilter(pkg);
            }
            req.setSuspendPolicy(EventRequest.SUSPEND_NONE);
            req.enable();
        }
    }

    /* Pass on exception events to our Tracer */
    private void exceptionEvent(ExceptionEvent event) {
        ThreadTrace trace = (ThreadTrace)traceMap.get(event.thread());
        if (trace != null) {
            trace.exceptionEvent(event);
        }
    }

    public void vmDeathEvent(VMDeathEvent event) {
        vmRunning = false;
        output.println("The application exited.");
    }

    public void vmDisconnectEvent(VMDisconnectEvent event) {
        vmConnected = false;
        if (vmRunning) {
            output.println("The application has been disconnected.");
        }
    }
}
