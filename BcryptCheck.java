import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BcryptCheck {
    public static void main(String[] args) {
        BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
        String hash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi";
        System.out.println("admin123=" + enc.matches("admin123", hash));
        System.out.println("teacher123=" + enc.matches("teacher123", hash));
        System.out.println("student123=" + enc.matches("student123", hash));
        System.out.println("123456=" + enc.matches("123456", hash));
    }
}
