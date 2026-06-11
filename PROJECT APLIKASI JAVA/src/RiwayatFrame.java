import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.List;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class RiwayatFrame extends JFrame {
    private JTable tabelRiwayat;
    private DefaultTableModel tableModel;
    private long totalSaldo = 0;
    private boolean showTotal = true;
    private JCheckBox cbShowTotal;

    public RiwayatFrame() {
        // Pengaturan Window
        setTitle("Riwayat Transaksi - Manajer Keuangan");
        setSize(700, 500);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(20, 20));

        // --- Panel Judul (Atas) ---
        JPanel panelJudul = new JPanel();
        panelJudul.setBackground(new Color(41, 128, 185));
        JLabel lblJudul = new JLabel("RIWAYAT TRANSAKSI");
        lblJudul.setForeground(Color.WHITE);
        lblJudul.setFont(new Font("Arial", Font.BOLD, 24));
        panelJudul.add(lblJudul);
        add(panelJudul, BorderLayout.NORTH);

        // --- Tabel Riwayat (Tengah) ---
        String[] kolom = { "Keterangan", "Jenis", "Jumlah" };
        tableModel = new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tidak bisa diedit
            }
        };
        tabelRiwayat = new JTable(tableModel) {
            @Override
            public void changeSelection(int row, int column, boolean toggle, boolean extend) {
                // Prevent selecting the total row (when shown)
                if (showTotal && row == getRowCount() - 1) return;
                super.changeSelection(row, column, toggle, extend);
            }
        };
        tabelRiwayat.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelRiwayat.setFont(new Font("Arial", Font.PLAIN, 12));
        tabelRiwayat.setRowHeight(28);
        tabelRiwayat.setShowGrid(false);
        tabelRiwayat.setIntercellSpacing(new Dimension(0, 0));
        tabelRiwayat.setSelectionBackground(new Color(229, 242, 255));
        tabelRiwayat.setSelectionForeground(Color.BLACK);

        JTableHeader header = tabelRiwayat.getTableHeader();
        header.setBackground(new Color(41, 128, 185));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Arial", Font.BOLD, 13));
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 30));

        JScrollPane scrollPane = new JScrollPane(tabelRiwayat);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        add(scrollPane, BorderLayout.CENTER);

        // Adjust column widths and renderers
        tabelRiwayat.getColumnModel().getColumn(0).setPreferredWidth(360);
        tabelRiwayat.getColumnModel().getColumn(1).setPreferredWidth(140);
        tabelRiwayat.getColumnModel().getColumn(2).setPreferredWidth(140);

        // Custom renderers that highlight the total row identified by the first-column marker
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                boolean isTotalRow = false;
                try {
                    Object marker = table.getValueAt(row, 0);
                    isTotalRow = marker != null && "Total Saldo:".equals(marker.toString());
                } catch (Exception ex) {
                    isTotalRow = false;
                }
                Component c = super.getTableCellRendererComponent(table, value, isSelected && !isTotalRow, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.RIGHT);
                if (isTotalRow) {
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                    c.setBackground(new Color(250, 250, 250));
                    if (c instanceof JComponent) {
                        ((JComponent) c).setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)));
                    }
                } else {
                    c.setBackground(Color.WHITE);
                    if (c instanceof JComponent) {
                        ((JComponent) c).setBorder(null);
                    }
                }
                return c;
            }
        };

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                boolean isTotalRow = false;
                try {
                    Object marker = table.getValueAt(row, 0);
                    isTotalRow = marker != null && "Total Saldo:".equals(marker.toString());
                } catch (Exception ex) {
                    isTotalRow = false;
                }
                Component c = super.getTableCellRendererComponent(table, value, isSelected && !isTotalRow, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (isTotalRow) {
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                    c.setBackground(new Color(250, 250, 250));
                    if (c instanceof JComponent) {
                        ((JComponent) c).setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)));
                    }
                } else {
                    c.setBackground(Color.WHITE);
                    if (c instanceof JComponent) {
                        ((JComponent) c).setBorder(null);
                    }
                }
                return c;
            }
        };

        tabelRiwayat.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        tabelRiwayat.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);

        // --- Panel Status dan Tombol (Bawah) ---
        JPanel panelBawah = new JPanel(new BorderLayout(10, 0));
        panelBawah.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        panelBawah.setBackground(Color.WHITE);
        panelBawah.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.NORTH);

        // Checkbox to toggle total row visibility
        cbShowTotal = new JCheckBox("Tampilkan Total", true);
        cbShowTotal.setBackground(Color.WHITE);
        cbShowTotal.setFont(new Font("Arial", Font.PLAIN, 12));
        cbShowTotal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showTotal = cbShowTotal.isSelected();
                loadData();
            }
        });
        panelBawah.add(cbShowTotal, BorderLayout.WEST);

        // Panel Tombol
        JPanel panelTombol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        panelTombol.setBackground(Color.WHITE);

        JButton btnRefresh = UIHelper.createAccentButton("Refresh");
        btnRefresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshData();
            }
        });
        styleButton(btnRefresh);
        panelTombol.add(btnRefresh);

        JPanel panelExportGroup = new JPanel(new GridLayout(1, 3, 10, 0));
        panelExportGroup.setBackground(Color.WHITE);
        panelExportGroup.setOpaque(false);

        JButton btnExportMinggu = UIHelper.createButton("Export Mingguan", new Color(39, 174, 96), Color.WHITE);
        btnExportMinggu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportByPeriod("minggu");
            }
        });
        styleButton(btnExportMinggu);
        btnExportMinggu.setPreferredSize(new Dimension(130, 34));
        panelExportGroup.add(btnExportMinggu);

        JButton btnExportBulan = UIHelper.createButton("Export Bulanan", new Color(39, 174, 96), Color.WHITE);
        btnExportBulan.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportByPeriod("bulan");
            }
        });
        styleButton(btnExportBulan);
        btnExportBulan.setPreferredSize(new Dimension(130, 34));
        panelExportGroup.add(btnExportBulan);

        JButton btnExportTahun = UIHelper.createButton("Export Tahunan", new Color(39, 174, 96), Color.WHITE);
        btnExportTahun.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportByPeriod("tahun");
            }
        });
        styleButton(btnExportTahun);
        btnExportTahun.setPreferredSize(new Dimension(130, 34));
        panelExportGroup.add(btnExportTahun);

        panelTombol.add(panelExportGroup);

        JButton btnKembali = UIHelper.createNeutralButton("Kembali");
        btnKembali.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        styleButton(btnKembali);
        panelTombol.add(btnKembali);

        panelBawah.add(panelTombol, BorderLayout.EAST);
        add(panelBawah, BorderLayout.SOUTH);

        // Load data setelah semua komponen UI dibuat
        loadData();

        setBackground(Color.WHITE);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        totalSaldo = 0;

        for (Database.Transaction tx : Database.getTransactions()) {
            if (tx.type.equals("Pengeluaran")) {
                totalSaldo -= tx.amount;
                tableModel.addRow(new Object[] { tx.description, tx.type, "- " + formatRupiah(tx.amount) });
            } else {
                totalSaldo += tx.amount;
                tableModel.addRow(new Object[] { tx.description, tx.type, "+ " + formatRupiah(tx.amount) });
            }
        }

        // Tambahkan baris total di bagian paling bawah tabel jika diinginkan
        if (showTotal) {
            String totalText = (totalSaldo < 0 ? "- " : "+ ") + formatRupiah(Math.abs(totalSaldo));
            tableModel.addRow(new Object[] { "Total Saldo:", "", totalText });
        }
    }

    private void refreshData() {
        loadData();
        JOptionPane.showMessageDialog(this, "Data berhasil diperbarui!", "Refresh", JOptionPane.INFORMATION_MESSAGE);
    }

    // total saldo sekarang ditampilkan sebagai baris terakhir pada tabel

    private void styleButton(JButton button) {
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
    }

    private void exportByPeriod(String period) {
        LocalDate now = LocalDate.now();
        LocalDate startDate;
        LocalDate endDate;
        switch (period) {
            case "minggu":
                startDate = now.with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                endDate = startDate.plusDays(7);
                break;
            case "bulan":
                startDate = now.withDayOfMonth(1);
                endDate = startDate.plusMonths(1);
                break;
            case "tahun":
                startDate = now.withDayOfYear(1);
                endDate = startDate.plusYears(1);
                break;
            default:
                JOptionPane.showMessageDialog(this, "Periode tidak dikenali", "Error", JOptionPane.ERROR_MESSAGE);
                return;
        }

        Timestamp startTs = Timestamp.valueOf(startDate.atStartOfDay());
        Timestamp endTs = Timestamp.valueOf(endDate.atStartOfDay());

        List<Database.Transaction> list = Database.getTransactionsBetween(startTs, endTs);
        if (list.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tidak ada transaksi pada periode ini.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        String defaultName = "riwayat_" + period + ".csv";
        chooser.setSelectedFile(new File(defaultName));
        int retval = chooser.showSaveDialog(this);
        if (retval != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File target = chooser.getSelectedFile();
        try {
            exportTransactionsToCsv(list, target);
            JOptionPane.showMessageDialog(this, "Export berhasil: " + target.getAbsolutePath(), "Sukses", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Gagal export: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportTransactionsToCsv(List<Database.Transaction> list, File file) throws IOException {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        try (FileWriter fw = new FileWriter(file)) {
            fw.write("Tanggal,Keterangan,Jenis,Jumlah\n");
            for (Database.Transaction tx : list) {
                String tanggal = "";
                if (tx.createdAt != null) {
                    LocalDateTime ldt = tx.createdAt.toLocalDateTime();
                    tanggal = ldt.format(fmt);
                }
                String jumlah = (tx.type.equals("Pengeluaran") ? "- " : "+ ") + formatRupiah(tx.amount);
                // Escape double quotes in description
                String ket = tx.description.replace("\"", "\"\"");
                fw.write(String.format("%s,%s,%s,%s\n", tanggal, ket, tx.type, jumlah));
            }
        }
    }

    private String formatRupiah(long nominal) {
        return "Rp " + String.format("%,d", nominal).replace(",", ".");
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
            RiwayatFrame riwayat = new RiwayatFrame();
            riwayat.setVisible(true);
        });
    }
}