import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectTest {
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> klass = Class.forName("ReflectTest");
        Method method = klass.getMethod("trace", int.class);

        for (int i = 0; i < 20; ++i) {
            method.invoke(null, i);
        }
    }

    public static void trace(int i) {
        new Exception("# " + i).printStackTrace();
    }
}
