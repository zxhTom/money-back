package cn.iocoder.yudao.server;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
public class BcryptGen {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
        System.out.println(encoder.encode("123456"));
    }
}
