package view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import utils.Formatter;

/**
 * Bộ tiện ích dựng giao diện dùng chung: bảng chỉ đọc, form dạng lưới,
 * nút, ô chọn ngày và hộp thoại thông báo.
 */
public final class UiKit {
    /** Màu chủ đạo của ứng dụng. */
    public static final Color PRIMARY = new Color(33, 150, 243);
    /** Màu cảnh báo (hàng dưới ngưỡng, lỗi). */
    public static final Color WARNING = new Color(255, 235, 238);
    /** Phông chữ tiêu đề. */
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);

    /**
     * Không cho tạo đối tượng vì lớp chỉ chứa phương thức tĩnh.
     */
    private UiKit() {
    }

    /**
     * Tạo bảng chỉ đọc, chọn một dòng.
     *
     * @param columns tên các cột
     * @return bảng mới
     */
    public static JTable table(String... columns) {
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            private static final long serialVersionUID = 1L;

            /**
             * Không cho sửa trực tiếp trên bảng.
             *
             * @param row    dòng
             * @param column cột
             * @return luôn false
             */
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable t = new JTable(model);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setRowHeight(24);
        t.setAutoCreateRowSorter(true);
        t.getTableHeader().setReorderingAllowed(false);
        return t;
    }

    /**
     * Xóa dữ liệu cũ và nạp các dòng mới vào bảng.
     *
     * @param table bảng cần nạp
     * @param rows  danh sách dòng, mỗi dòng là mảng giá trị theo cột
     */
    public static void fill(JTable table, List<Object[]> rows) {
        DefaultTableModel m = (DefaultTableModel) table.getModel();
        m.setRowCount(0);
        for (Object[] r : rows) {
            m.addRow(r);
        }
    }

    /**
     * Lấy chỉ số dòng trong model của dòng đang chọn (đã tính sắp xếp).
     *
     * @param table bảng
     * @return chỉ số dòng trong model, -1 nếu chưa chọn
     */
    public static int selectedModelRow(JTable table) {
        int v = table.getSelectedRow();
        return v < 0 ? -1 : table.convertRowIndexToModel(v);
    }

    /**
     * Căn phải các cột số tiền/số lượng.
     *
     * @param table   bảng
     * @param columns chỉ số các cột cần căn phải
     */
    public static void alignRight(JTable table, int... columns) {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int c : columns) {
            table.getColumnModel().getColumn(c).setCellRenderer(r);
        }
    }

    /**
     * Ẩn một cột (thường là cột id) nhưng vẫn giữ dữ liệu trong model.
     *
     * @param table  bảng
     * @param column chỉ số cột
     */
    public static void hideColumn(JTable table, int column) {
        table.getColumnModel().removeColumn(table.getColumnModel().getColumn(column));
    }

    /**
     * Bọc bảng trong thanh cuộn có tiêu đề viền.
     *
     * @param table bảng
     * @param title tiêu đề (null = không viền)
     * @return thanh cuộn chứa bảng
     */
    public static JScrollPane scroll(JTable table, String title) {
        JScrollPane sp = new JScrollPane(table);
        if (title != null) {
            sp.setBorder(BorderFactory.createTitledBorder(title));
        }
        return sp;
    }

    /**
     * Tạo panel form 2 cột (nhãn - ô nhập) dùng GridBagLayout.
     *
     * @param title  tiêu đề viền (null = không viền)
     * @param labels nhãn các dòng
     * @param fields ô nhập tương ứng
     * @return panel form
     */
    public static JPanel form(String title, String[] labels, JComponent[] fields) {
        JPanel p = new JPanel(new GridBagLayout());
        if (title != null) {
            p.setBorder(BorderFactory.createTitledBorder(title));
        }
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        for (int i = 0; i < labels.length; i++) {
            c.gridx = 0;
            c.gridy = i;
            c.weightx = 0;
            c.fill = GridBagConstraints.NONE;
            p.add(new JLabel(labels[i]), c);
            c.gridx = 1;
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            p.add(fields[i], c);
        }
        return p;
    }

    /**
     * Tạo nút với kích thước tối thiểu thống nhất.
     *
     * @param text chữ trên nút
     * @return nút mới
     */
    public static JButton button(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(Math.max(100, b.getPreferredSize().width + 10), 30));
        return b;
    }

    /**
     * Tạo nhãn tiêu đề màn hình.
     *
     * @param text nội dung
     * @return nhãn tiêu đề
     */
    public static JLabel title(String text) {
        JLabel l = new JLabel(text);
        l.setFont(TITLE_FONT);
        l.setForeground(PRIMARY.darker());
        l.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        return l;
    }

    /**
     * Tạo ô chọn ngày (JSpinner định dạng dd/MM/yyyy).
     *
     * @param initial ngày ban đầu
     * @return ô chọn ngày
     */
    public static JSpinner dateSpinner(LocalDate initial) {
        JSpinner s = new JSpinner(new SpinnerDateModel(Formatter.toDate(initial), null, null, Calendar.DAY_OF_MONTH));
        s.setEditor(new JSpinner.DateEditor(s, "dd/MM/yyyy"));
        s.setPreferredSize(new Dimension(120, 26));
        return s;
    }

    /**
     * Đọc ngày đang chọn trong ô chọn ngày.
     *
     * @param s ô chọn ngày
     * @return ngày đã chọn
     */
    public static LocalDate dateOf(JSpinner s) {
        return Formatter.toLocalDate((java.util.Date) s.getValue());
    }

    /**
     * Hiện thông báo thông tin.
     *
     * @param parent  thành phần cha
     * @param message nội dung
     */
    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Hiện thông báo lỗi.
     *
     * @param parent  thành phần cha
     * @param message nội dung
     */
    public static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Hỏi xác nhận Có/Không.
     *
     * @param parent  thành phần cha
     * @param message câu hỏi
     * @return true nếu người dùng chọn Có
     */
    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Xác nhận",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    /**
     * Tạo panel chứa các nút xếp ngang, căn trái.
     *
     * @param components các nút/thành phần
     * @return panel hàng nút
     */
    public static JPanel row(Component... components) {
        JPanel p = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 6, 4));
        for (Component c : components) {
            p.add(c);
        }
        return p;
    }
}
