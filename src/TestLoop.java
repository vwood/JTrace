package src;

import java.lang.management.*;

public class TestLoop {
    public static void loop() {
        System.out.println(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
        while (true) {
            tick();
        }
    }

    public static void tick() {
        a();
        b();
        c();
        e();
        g();
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

    public static void g() {
        try {
            h();
            j();
        } catch (Exception e) {
            k();
        } finally {
            l();
        }
    }

    public static void h() throws Exception {
        throw new Exception();
    }
    
    public static void j() {
        /* nop */
    }

    public static void k() {
        /* nop */
    }
    
    public static void l() {
        /* nop */
    }
}