import javax.tools.ToolProvider;
class Compile {
    public static void main(String[] args) {
        int result = ToolProvider.getSystemJavaCompiler().run(null, System.out, System.err, args);
        if (result != 0) System.exit(result);
    }
}
