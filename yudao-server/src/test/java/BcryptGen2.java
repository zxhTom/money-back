import cn.hutool.crypto.digest.BCrypt;
public class BcryptGen2 {
    public static void main(String[] args) {
        System.out.println(BCrypt.hashpw("123456", BCrypt.gensalt(4)));
    }
}
