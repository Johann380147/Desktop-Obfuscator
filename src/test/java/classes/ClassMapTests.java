package classes;

import com.sim.application.classes.ClassMap;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class ClassMapTests {

    @Test
    public void containsValueTest() {
        var classMap = new ClassMap();
        classMap.put("a", "b");
        assertTrue(classMap.containsValue("b"));
        assertFalse(classMap.containsValue("c"));
    }
}
