package me.deecaad.weaponmechanicscosmetics.trails.shape;

import me.deecaad.core.utils.ReflectionUtil;
import me.deecaad.weaponmechanicscosmetics.WeaponMechanicsCosmetics;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class StringFunctionShape extends FunctionShape {

    private DoubleFunction function;

    public StringFunctionShape(int points, int loops, String func) throws IOException, ClassNotFoundException {
        this(points, loops, func, WeaponMechanicsCosmetics.getInstance().getPlugin().getDataFolder());
    }

    public StringFunctionShape(int points, int loops, String func, File root) throws IOException, ClassNotFoundException {
        super(points, loops);

        String code =
                "package me.deecaad.weaponmechanicscosmetics.trails.shape;\n" +
                "\n" +
                "import me.deecaad.weaponmechanicscosmetics.trails.shape.StringFunctionShape;\n" +
                "import static java.lang.Math.*;\n" +
                "\n" +
                "public class ImplFunc implements StringFunctionShape.DoubleFunction {\n" +
                "\n" +
                "   @Override\n" +
                "   public double function(double theta) {\n" +
                "       return " + func + ";\n" +
                "   }\n" +
                "}\n";

        File sourceFile = new File(root, "ImplFunc.java");
        sourceFile.getParentFile().mkdirs();
        Files.write(sourceFile.toPath(), code.getBytes(StandardCharsets.UTF_8));

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        compiler.run(null, null, null, sourceFile.getPath());

        URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{root.toURI().toURL()});
        // noinspection unchecked
        Class<? extends DoubleFunction> cls = (Class<DoubleFunction>) Class.forName("me.deecaad.weaponmechanicscosmetics.trails.shape.ImplFunc", true, classLoader);
        this.function = ReflectionUtil.newInstance(cls);
    }

    public DoubleFunction getFunction() {
        return function;
    }

    public void setFunction(DoubleFunction function) {
        this.function = function;
    }

    @Override
    public double radiusFunction(double theta) {
        return function.function(theta);
    }


    @FunctionalInterface
    public interface DoubleFunction {
        double function(double theta);
    }
}
