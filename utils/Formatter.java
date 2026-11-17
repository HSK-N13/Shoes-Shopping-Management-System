package utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

/**
 * Định dạng và phân tích tiền tệ, ngày giờ để hiển thị trên giao diện.
 */
public final class Formatter {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Không cho tạo đối tượng vì lớp chỉ chứa phương thức tĩnh.
     */
    private Formatter() {
    }

    /**
     * Tạo bộ định dạng số kiểu Việt Nam: dấu chấm phân cách hàng nghìn.
     *
     * @return đối tượng DecimalFormat mới (không dùng chung vì không thread-safe)
     */
    private static DecimalFormat moneyFormat() {
        DecimalFormatSymbols s = new DecimalFormatSymbols(Locale.US);
        s.setGroupingSeparator('.');
        s.setDecimalSeparator(',');
        DecimalFormat f = new DecimalFormat("#,##0.##", s);
        f.setParseBigDecimal(true);
        return f;
    }

    /**
     * Định dạng số tiền, ví dụ 2500000 -> "2.500.000".
     *
     * @param value số tiền (null được coi là 0)
     * @return chuỗi đã định dạng
     */
    public static String money(BigDecimal value) {
        return moneyFormat().format(value == null ? BigDecimal.ZERO : value);
    }

    /**
     * Phân tích chuỗi tiền người dùng nhập ("2.500.000" hoặc "2500000").
     *
     * @param text chuỗi nhập
     * @return số tiền, chuỗi rỗng trả về 0
     * @throws BusinessException nếu chuỗi không phải số hợp lệ
     */
    public static BigDecimal parseMoney(String text) {
        if (text == null || text.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        String t = text.trim().replace(" ", "");
        try {
            return (BigDecimal) moneyFormat().parseObject(t);
        } catch (ParseException | ClassCastException e) {
            throw new BusinessException("Số tiền không hợp lệ: " + text);
        }
    }

    /**
     * Phân tích số nguyên người dùng nhập.
     *
     * @param text      chuỗi nhập
     * @param fieldName tên trường để báo lỗi
     * @return giá trị số nguyên
     * @throws BusinessException nếu không phải số nguyên
     */
    public static int parseInt(String text, String fieldName) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException | NullPointerException e) {
            throw new BusinessException(fieldName + " phải là số nguyên.");
        }
    }

    /**
     * Định dạng ngày giờ dd/MM/yyyy HH:mm.
     *
     * @param dt ngày giờ
     * @return chuỗi hiển thị, rỗng nếu null
     */
    public static String dateTime(LocalDateTime dt) {
        return dt == null ? "" : dt.format(DATE_TIME);
    }

    /**
     * Định dạng ngày dd/MM/yyyy.
     *
     * @param d ngày
     * @return chuỗi hiển thị, rỗng nếu null
     */
    public static String date(LocalDate d) {
        return d == null ? "" : d.format(DATE);
    }

    /**
     * Chuyển java.util.Date (từ JSpinner) sang LocalDate.
     *
     * @param d ngày kiểu cũ
     * @return LocalDate tương ứng
     */
    public static LocalDate toLocalDate(Date d) {
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * Chuyển LocalDate sang java.util.Date (để gán cho JSpinner).
     *
     * @param d ngày
     * @return java.util.Date tương ứng lúc 00:00
     */
    public static Date toDate(LocalDate d) {
        return Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
