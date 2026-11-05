import org.mindrot.jbcrypt.BCrypt;

public class HashGen {
    public static void main(String[] args) {
        String[] plains = {"admin123", "lec123", "stu123"};
        for (String p : plains) {
            String hash = BCrypt.hashpw(p, BCrypt.gensalt());
            System.out.println("UPDATE users SET password = '" + hash + "' WHERE password = '" + p + "';");
        }
    }
}