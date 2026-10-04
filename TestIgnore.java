import java.lang.reflect.Method;
public class TestIgnore {
    public static void main(String[] args) throws Exception {
        Class<?> clazz = Class.forName("com.baomidou.mybatisplus.annotation.InterceptorIgnore");
        for (Method m : clazz.getDeclaredMethods()) {
            System.out.println(m.getName());
        }
    }
}
