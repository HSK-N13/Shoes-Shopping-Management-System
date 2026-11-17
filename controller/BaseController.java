package controller;

import java.awt.Component;
import java.awt.Cursor;

import utils.BusinessException;
import view.UiKit;

/**
 * Lớp cha của các Controller: chạy hành động của người dùng, bắt lỗi nghiệp vụ
 * và hiển thị thông báo, để View không phải tự xử lý ngoại lệ.
 */
public abstract class BaseController {

    /**
     * Một hành động người dùng (bấm nút, chọn dòng...).
     */
    @FunctionalInterface
    protected interface Action {
        /**
         * Thực hiện hành động.
         */
        void run();
    }

    /**
     * Thành phần giao diện dùng làm cha cho hộp thoại thông báo.
     *
     * @return thành phần cha
     */
    protected abstract Component parent();

    /**
     * Chạy hành động; lỗi nghiệp vụ hiện thông báo, lỗi bất ngờ hiện chi tiết.
     *
     * @param action hành động cần chạy
     */
    protected void handle(Action action) {
        Component p = parent();
        Cursor old = p == null ? null : p.getCursor();
        try {
            if (p != null) {
                p.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            }
            action.run();
        } catch (BusinessException e) {
            UiKit.error(p, e.getMessage());
        } catch (RuntimeException e) {
            e.printStackTrace();
            UiKit.error(p, "Lỗi không mong muốn: " + e);
        } finally {
            if (p != null) {
                p.setCursor(old);
            }
        }
    }
}
