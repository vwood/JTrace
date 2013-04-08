package src;

public class JGlasses {
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.err.println("Usage: java JTrace <JDI info> <class> <method>");
            return;
        }

        String jdiInfo = args[0];
        String className = args[1];
        String methodName = args[2];
        System.out.println("JDI info: " + jdiInfo);
        System.out.println("Class name: " + className);
        System.out.println("Method name: " + methodName);
    }
}
