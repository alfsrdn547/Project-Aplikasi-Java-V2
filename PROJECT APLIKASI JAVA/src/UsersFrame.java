import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UsersFrame extends JFrame {
    private JTable tabelUsers;
    private DefaultTableModel tableModel;

    public UsersFrame() {
        if (!Database.canAccessAdminFeatures()) {
            JOptionPane.showMessageDialog(null, "Fitur manajemen pengguna hanya tersedia untuk akun admin.", "Akses Ditolak", JOptionPane.WARNING_MESSAGE);
            dispose();
            return;
        }

        // Pengaturan Window
        setTitle("Daftar Pengguna - Manajer Keuangan");
        setSize(600, 450);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(20, 20));

        // --- Panel Judul (Atas) ---
        JPanel panelJudul = new JPanel();
        panelJudul.setBackground(new Color(41, 128, 185));
        JLabel lblJudul = new JLabel("DAFTAR PENGGUNA TERDAFTAR");
        lblJudul.setForeground(Color.WHITE);
        lblJudul.setFont(new Font("Arial", Font.BOLD, 24));
        panelJudul.add(lblJudul);
        add(panelJudul, BorderLayout.NORTH);

        // --- Tabel Pengguna (Tengah) ---
        String[] kolom = { "ID", "Username", "Role" };
        tableModel = new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelUsers = new JTable(tableModel);
        tabelUsers.setFont(new Font("Arial", Font.PLAIN, 12));
        tabelUsers.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tabelUsers.setRowHeight(25);
        add(new JScrollPane(tabelUsers), BorderLayout.CENTER);

        // --- Panel Tombol (Bawah) ---
        JPanel panelBawah = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panelBawah.setBackground(Color.WHITE);

        JButton btnRefresh = UIHelper.createAccentButton("Refresh");
        btnRefresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadData();
            }
        });
        panelBawah.add(btnRefresh);

        JButton btnViewTransactions = UIHelper.createPrimaryButton("Lihat Transaksi");
        btnViewTransactions.setEnabled(Database.isCurrentUserAdmin());
        btnViewTransactions.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = tabelUsers.getSelectedRow();
                if (selectedRow < 0) {
                    JOptionPane.showMessageDialog(UsersFrame.this, "Pilih pengguna terlebih dahulu.", "Info", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                String username = tableModel.getValueAt(selectedRow, 1).toString();
                SwingUtilities.invokeLater(() -> {
                    UserTransactionsFrame frame = new UserTransactionsFrame(username);
                    frame.setVisible(true);
                });
            }
        });
        panelBawah.add(btnViewTransactions);

        JButton btnDeleteAllUsers = UIHelper.createDangerButton("Hapus Semua User");
        btnDeleteAllUsers.setEnabled(Database.isCurrentUserAdmin());
        btnDeleteAllUsers.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int confirm = JOptionPane.showConfirmDialog(UsersFrame.this,
                        "Semua user selain admin akan dihapus. Lanjutkan?",
                        "Konfirmasi Hapus Semua User",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    boolean success = Database.deleteAllUsersExceptAdmin();
                    if (success) {
                        JOptionPane.showMessageDialog(UsersFrame.this, "Semua user selain admin berhasil dihapus.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                        loadData();
                    } else {
                        JOptionPane.showMessageDialog(UsersFrame.this, "Gagal menghapus user.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        panelBawah.add(btnDeleteAllUsers);

        JButton btnClose = UIHelper.createNeutralButton("Tutup");
        btnClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        panelBawah.add(btnClose);

        add(panelBawah, BorderLayout.SOUTH);

        loadData();
        setBackground(Color.WHITE);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        for (Database.User user : Database.getAllUsers()) {
            tableModel.addRow(new Object[] { user.id, user.username, user.role });
        }
    }

    public static void main(String[] args) {
        try {
            Database.initDatabase();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(null,
                    "Gagal menghubungkan database: " + ex.getMessage(),
                    "Koneksi Database",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        SwingUtilities.invokeLater(() -> {
            UsersFrame usersFrame = new UsersFrame();
            usersFrame.setVisible(true);
        });
    }
}
