import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BcryptGen {
    public static void main(String[] args) {
        BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
        System.out.println("admin123=" + enc.encode("admin123"));
        System.out.println("teacher123=" + enc.encode("teacher123"));
        System.out.println("student123=" + enc.encode("student123"));
    }
}
