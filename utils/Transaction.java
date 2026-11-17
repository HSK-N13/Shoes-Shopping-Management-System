package utils;

import java.sql.Connection;
import java.sql.SQLException;

import connectDB.ConnectDB;

/**
 * Chạy một khối lệnh trong một giao dịch JDBC: setAutoCommit(false),
 * commit() khi thành công, rollback() khi có lỗi (BR04, BR05, BR11).
 * Các DAO được gọi bên trong khối dùng chung kết nối của ConnectDB nên
 * tự động nằm trong cùng giao dịch.
 */
public final class Transaction {

    /**
     * Khối lệnh chạy trong giao dịch và trả về kết quả.
     *
     * @param <T> kiểu kết quả
     */
    @FunctionalInterface
    public interface Work<T> {
        /**
         * Thực hiện công việc với kết nối đang ở chế độ giao dịch.
         *
         * @param conn kết nối dùng chung
         * @return kết quả công việc
         * @throws SQLException lỗi database (sẽ gây rollback)
         */
        T run(Connection conn) throws SQLException;
    }

    /**
     * Không cho tạo đối tượng vì lớp chỉ chứa phương thức tĩnh.
     */
    private Transaction() {
    }

    /**
     * Thực thi công việc trong một giao dịch. Bất kỳ ngoại lệ nào
     * (SQLException hay BusinessException) đều làm hoàn tác toàn bộ.
     *
     * @param <T>  kiểu kết quả
     * @param work công việc cần chạy
     * @return kết quả của công việc
     * @throws BusinessException khi vi phạm nghiệp vụ hoặc lỗi database
     */
    public static <T> T execute(Work<T> work) {
        Connection conn;
        try {
            conn = ConnectDB.getConnection();
        } catch (SQLException e) {
            throw new BusinessException(e.getMessage(), e);
        }
        boolean oldAutoCommit = true;
        try {
            oldAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            T result = work.run(conn);
            conn.commit();
            return result;
        } catch (SQLException e) {
            rollback(conn);
            throw new BusinessException("Lỗi cơ sở dữ liệu, thao tác đã được hoàn tác: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            rollback(conn);
            throw e;
        } finally {
            try {
                conn.setAutoCommit(oldAutoCommit);
            } catch (SQLException ignored) {
                // Kết nối có thể đã hỏng; lần gọi sau ConnectDB sẽ kết nối lại
            }
        }
    }

    /**
     * Hoàn tác giao dịch, bỏ qua lỗi phát sinh khi rollback.
     *
     * @param conn kết nối đang trong giao dịch
     */
    private static void rollback(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException ignored) {
            // Không làm gì thêm
        }
    }
}
