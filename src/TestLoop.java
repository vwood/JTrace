package src;

import java.lang.management.*;

public class TestLoop {
    public static void loop() {
        System.out.println(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
        while (true) {
            a();
            b();
            c();
            e();
        }
    }

    public static void a() {
        b();
    }
    
    public static void b() {
        c();
        c();
    }
    
    public static void c() {
        d();
    }

    
    public static void d() {
        try {
            Thread.sleep(100);
        } catch (Exception e) { /* Nop */ }
    }

    public static void e() {
        recurse(10);
    }

    /*
     * Test returning from recursive methods
     */
    public static void recurse(int n) {
        if (n > 0) {
            recurse(n - 1);
            f();
        }
    }

    public static void f() {
        /* nop */
    }
}