package view;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.print.PrinterException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Hộp thoại xem và in hóa đơn bán hàng (bước 6 của luồng bán).
 */
public class InvoiceDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private final JTextArea txtInvoice = new JTextArea(30, 70);

    /**
     * Dựng hộp thoại với nội dung hóa đơn.
     *
     * @param owner   cửa sổ cha
     * @param content nội dung hóa đơn dạng văn bản
     */
    public InvoiceDialog(Window owner, String content) {
        super(owner, "Hóa đơn", ModalityType.APPLICATION_MODAL);
        txtInvoice.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        txtInvoice.setEditable(false);
        txtInvoice.setText(content);
        txtInvoice.setCaretPosition(0);
        JButton btnPrint = UiKit.button("In hóa đơn");
        JButton btnClose = UiKit.button("Đóng");
        btnPrint.addActionListener(e -> print());
        btnClose.addActionListener(e -> dispose());
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        root.add(new JScrollPane(txtInvoice), BorderLayout.CENTER);
        root.add(UiKit.row(btnPrint, btnClose), BorderLayout.SOUTH);
        setContentPane(root);
        pack();
        setLocationRelativeTo(owner);
    }

    /**
     * Gửi nội dung hóa đơn tới máy in qua hộp thoại in của hệ điều hành.
     */
    private void print() {
        try {
            txtInvoice.print();
        } catch (PrinterException ex) {
            UiKit.error(this, "Không in được hóa đơn: " + ex.getMessage());
        }
    }
}
