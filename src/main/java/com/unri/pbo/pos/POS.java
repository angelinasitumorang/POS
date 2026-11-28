package com.unri.pbo.pos;

import com.unri.pbo.pos.gui.MainFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Titik masuk aplikasi KhasKita. Jalankan class ini (Run Project) untuk membuka aplikasi.
 * Pastikan MySQL di XAMPP sudah Start dan database sudah diimpor dari aplikasi_pos.sql.
 */
public class POS {

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            // pakai tampilan bawaan kalau Nimbus tidak ada
        }
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
