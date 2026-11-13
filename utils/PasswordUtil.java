package utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Băm và kiểm tra mật khẩu bằng PBKDF2WithHmacSHA256 (có sẵn trong JDK,
 * không cần thư viện ngoài). Chuỗi băm lưu trong cột users.password_hash có dạng:
 * <pre>pbkdf2$&lt;số vòng lặp&gt;$&lt;salt Base64&gt;$&lt;hash Base64&gt;</pre>
 * Mỗi mật khẩu có salt ngẫu nhiên riêng nên hai người cùng mật khẩu vẫn
 * có chuỗi băm khác nhau.
 */
public final class PasswordUtil {
    private static final String PREFIX = "pbkdf2";
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_BYTES = 16;

    /**
     * Không cho tạo đối tượng vì lớp chỉ chứa phương thức tĩnh.
     */
    private PasswordUtil() {
    }

    /**
     * Băm mật khẩu dạng rõ thành chuỗi để lưu vào database.
     *
     * @param rawPassword mật khẩu người dùng nhập
     * @return chuỗi băm kèm salt và số vòng lặp
     */
    public static String hash(String rawPassword) {
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        byte[] hash = pbkdf2(rawPassword.toCharArray(), salt, ITERATIONS);
        Base64.Encoder enc = Base64.getEncoder();
        return PREFIX + "$" + ITERATIONS + "$" + enc.encodeToString(salt) + "$" + enc.encodeToString(hash);
    }

    /**
     * Kiểm tra mật khẩu nhập vào có khớp với chuỗi băm đã lưu hay không.
     *
     * @param rawPassword mật khẩu người dùng nhập
     * @param storedHash  chuỗi băm lấy từ database
     * @return true nếu khớp
     */
    public static boolean verify(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null) {
            return false;
        }
        String[] parts = storedHash.split("\\$");
        if (parts.length != 4 || !PREFIX.equals(parts[0])) {
            return false;
        }
        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            byte[] actual = pbkdf2(rawPassword.toCharArray(), salt, iterations);
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Tính PBKDF2 cho mật khẩu với salt và số vòng lặp cho trước.
     *
     * @param password   mật khẩu dạng mảng ký tự
     * @param salt       salt ngẫu nhiên
     * @param iterations số vòng lặp
     * @return mảng byte kết quả băm
     */
    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_LENGTH);
            SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return f.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Không hỗ trợ thuật toán băm mật khẩu", e);
        }
    }

    /**
     * Tiện ích dòng lệnh: in ra chuỗi băm của mật khẩu truyền vào, dùng để
     * tạo dữ liệu mẫu cho bảng users.
     *
     * @param args mật khẩu cần băm (mặc định "123456")
     */
    public static void main(String[] args) {
        String pw = args.length > 0 ? args[0] : "123456";
        System.out.println(hash(pw));
    }
}
