import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class AplikasiKeuangan extends JFrame {
    private JTextField txtKeterangan, txtJumlah;
    private JComboBox<String> cbJenis;
    private JTable tabelRiwayat;
    private DefaultTableModel tableModel;
    private JLabel lblTotalSaldo;
    private long totalSaldo = 0;

    public AplikasiKeuangan() {
        // Pengaturan Dasar Window
        setTitle("Manajer Keuangan Pribadi");
        setSize(600, 500);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); // Agar bisa konfirmasi logout
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(20, 20));

        // --- Menu Bar ---
        JMenuBar menuBar = new JMenuBar();
        JMenu menuFile = new JMenu("Menu");
        JMenuItem menuDashboard = new JMenuItem("Dashboard");
        menuDashboard.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showDashboard();
            }
        });
        menuFile.add(menuDashboard);
        JMenuItem menuRiwayat = new JMenuItem("Riwayat Transaksi");
        menuRiwayat.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showRiwayat();
            }
        });
        menuFile.add(menuRiwayat);
        JMenuItem menuLogout = new JMenuItem("Logout");
        menuLogout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });
        menuFile.add(menuLogout);
        menuBar.add(menuFile);
        setJMenuBar(menuBar);

        // Tambahkan WindowListener untuk menangani close button
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                logout();
            }
        });

        // --- Panel Input (Atas) ---
        JPanel panelInput = new JPanel(new GridLayout(4, 2, 5, 5));
        panelInput.setBorder(BorderFactory.createTitledBorder("Input Transaksi"));

        panelInput.add(new JLabel("Keterangan:"));
        txtKeterangan = new JTextField();
        panelInput.add(txtKeterangan);

        panelInput.add(new JLabel("Jumlah (Rp):"));
        txtJumlah = new JTextField();
        panelInput.add(txtJumlah);

        panelInput.add(new JLabel("Jenis:"));
        cbJenis = new JComboBox<>(new String[] { "Pemasukan", "Pengeluaran" });
        panelInput.add(cbJenis);

        JButton btnTambah = UIHelper.createPrimaryButton("Tambah Transaksi");
        panelInput.add(btnTambah);

        add(panelInput, BorderLayout.NORTH);

        // --- Tabel Riwayat (Tengah) ---
        String[] kolom = { "Keterangan", "Jenis", "Jumlah" };
        tableModel = new DefaultTableModel(kolom, 0);
        tabelRiwayat = new JTable(tableModel);
        add(new JScrollPane(tabelRiwayat), BorderLayout.CENTER);
        

        // --- Panel Status (Bawah) ---
        lblTotalSaldo = new JLabel("Total Saldo: Rp 0");
        lblTotalSaldo.setFont(new Font("Arial", Font.BOLD, 16));
        lblTotalSaldo.setHorizontalAlignment(SwingConstants.RIGHT);
        add(lblTotalSaldo, BorderLayout.SOUTH);

        // Load data dari file
        loadData();

        // --- Logika Tombol ---
        btnTambah.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tambahData();
            }
        });
    }

    private void tambahData() {
        try {
            String ket = txtKeterangan.getText();
            long jml = Long.parseLong(txtJumlah.getText());
            String jenis = (String) cbJenis.getSelectedItem();

            if (jenis.equals("Pengeluaran")) {
                totalSaldo -= jml;
                tableModel.addRow(new Object[] { ket, jenis, "- " + formatRupiah(jml) });
            } else {
                totalSaldo += jml;
                tableModel.addRow(new Object[] { ket, jenis, "+ " + formatRupiah(jml) });
            }

            if (!Database.addTransaction(ket, jenis, jml)) {
                JOptionPane.showMessageDialog(this, "Gagal menyimpan transaksi ke database.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
            updateSaldoLabel();
            txtKeterangan.setText("");
            txtJumlah.setText("");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Masukkan angka yang valid untuk jumlah!", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private String formatRupiah(long nominal) {
        return "Rp " + String.format("%,d", nominal).replace(",", ".");
    }

    private void updateSaldoLabel() {
        lblTotalSaldo.setText("Total Saldo: " + formatRupiah(totalSaldo));
    }

    private void loadData() {
        tableModel.setRowCount(0);
        totalSaldo = 0;
        List<Database.Transaction> transactions = Database.getTransactions();
        for (Database.Transaction tx : transactions) {
            if (tx.type.equals("Pengeluaran")) {
                totalSaldo -= tx.amount;
                tableModel.addRow(new Object[] { tx.description, tx.type, "- " + formatRupiah(tx.amount) });
            } else {
                totalSaldo += tx.amount;
                tableModel.addRow(new Object[] { tx.description, tx.type, "+ " + formatRupiah(tx.amount) });
            }
        }
        updateSaldoLabel();
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin logout?", "Konfirmasi Logout",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> {
                LoginFrame login = new LoginFrame();
                login.setVisible(true);
            });
        }
    }

    private void showDashboard() {
        dispose();
        SwingUtilities.invokeLater(() -> {
            DashboardFrame dashboard = new DashboardFrame();
            dashboard.setVisible(true);
        });
    }

    private void showRiwayat() {
        SwingUtilities.invokeLater(() -> {
            RiwayatFrame riwayat = new RiwayatFrame();
            riwayat.setLocationRelativeTo(this);
            riwayat.setVisible(true);
        });
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
            LoginFrame login = new LoginFrame();
            login.setVisible(true);
        });
    }
}