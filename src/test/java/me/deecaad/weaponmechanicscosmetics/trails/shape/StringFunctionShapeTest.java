package me.deecaad.weaponmechanicscosmetics.trails.shape;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class StringFunctionShapeTest {

    @Test
    void test() throws IOException, ClassNotFoundException {
        String path = "src/test/java/me/deecaad/weaponmechanicscosmetics/trails/shape";
        StringFunctionShape shape = new StringFunctionShape(4, 1, "2*sin(theta)", new File(path));

        assertEquals(0, shape.radiusFunction(0), 1E-8);
        assertEquals(2, shape.radiusFunction(Math.PI), 1E-8);
    }
}
