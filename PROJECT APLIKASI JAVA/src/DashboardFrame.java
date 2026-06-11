import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardFrame extends JFrame {
    private JLabel lblWelcome, lblTotalSaldo, lblTotalPemasukan, lblTotalPengeluaran;
    private JButton btnTambahTransaksi, btnLihatRiwayat, btnLihatUsers, btnLogout;
    private PieChartPanel chartPanel;

    public DashboardFrame() {
        // Pengaturan Window
        setTitle("Dashboard - Manajer Keuangan");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(20, 20));

        // --- Panel Judul (Atas) ---
        JPanel panelJudul = new JPanel();
        panelJudul.setBackground(new Color(41, 128, 185));
        lblWelcome = new JLabel("Dashboard Keuangan");
        lblWelcome.setForeground(Color.WHITE);
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 24));
        panelJudul.add(lblWelcome);
        add(panelJudul, BorderLayout.NORTH);

        // --- Panel Ringkasan (Tengah) ---
        JPanel panelRingkasan = new JPanel(new BorderLayout(10, 10));
        panelRingkasan.setBorder(BorderFactory.createTitledBorder("Ringkasan Keuangan"));
        panelRingkasan.setBackground(Color.WHITE);

        chartPanel = new PieChartPanel();
        chartPanel.setPreferredSize(new Dimension(450, 350));
        panelRingkasan.add(chartPanel, BorderLayout.CENTER);

        JPanel panelLabelRingkasan = new JPanel(new GridLayout(3, 1, 10, 10));
        panelLabelRingkasan.setBackground(Color.WHITE);

        lblTotalSaldo = new JLabel("Total Saldo: Rp 0");
        lblTotalSaldo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTotalSaldo.setHorizontalAlignment(SwingConstants.CENTER);
        panelLabelRingkasan.add(lblTotalSaldo);

        lblTotalPemasukan = new JLabel("Total Pemasukan: Rp 0");
        lblTotalPemasukan.setFont(new Font("Arial", Font.PLAIN, 16));
        lblTotalPemasukan.setHorizontalAlignment(SwingConstants.CENTER);
        lblTotalPemasukan.setForeground(new Color(46, 204, 113));
        panelLabelRingkasan.add(lblTotalPemasukan);

        lblTotalPengeluaran = new JLabel("Total Pengeluaran: Rp 0");
        lblTotalPengeluaran.setFont(new Font("Arial", Font.PLAIN, 16));
        lblTotalPengeluaran.setHorizontalAlignment(SwingConstants.CENTER);
        lblTotalPengeluaran.setForeground(new Color(192, 57, 43));
        panelLabelRingkasan.add(lblTotalPengeluaran);

        panelRingkasan.add(panelLabelRingkasan, BorderLayout.SOUTH);
        add(panelRingkasan, BorderLayout.CENTER);

        // --- Panel Tombol (Bawah) ---
        JPanel panelTombol = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        panelTombol.setBackground(Color.WHITE);

        btnTambahTransaksi = UIHelper.createPrimaryButton("Tambah Transaksi");
        btnTambahTransaksi.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Buka halaman tambah transaksi
                SwingUtilities.invokeLater(() -> {
                    AplikasiKeuangan app = new AplikasiKeuangan();
                    app.setVisible(true);
                    setVisible(false); // Sembunyikan dashboard
                });
            }
        });
        panelTombol.add(btnTambahTransaksi);

        JButton btnRefresh = UIHelper.createButton("Refresh", new Color(155, 89, 182), Color.WHITE);
        btnRefresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Refresh data real-time
                updateRingkasan();
            }
        });
        panelTombol.add(btnRefresh);

        btnLihatUsers = UIHelper.createAccentButton("Lihat Pengguna");
        btnLihatUsers.setVisible(Database.canAccessAdminFeatures());
        btnLihatUsers.setEnabled(Database.canAccessAdminFeatures());
        btnLihatUsers.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showUsers();
            }
        });
        panelTombol.add(btnLihatUsers);

        JButton btnReset = UIHelper.createDangerButton("Reset Saldo");
        btnReset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetSaldo();
            }
        });
        panelTombol.add(btnReset);

        btnLihatRiwayat = UIHelper.createAccentButton("Lihat Riwayat");
        btnLihatRiwayat.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showRiwayat();
            }
        });
        panelTombol.add(btnLihatRiwayat);

        btnLogout = UIHelper.createDangerButton("Logout");
        btnLogout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Logout dan kembali ke login
                int confirm = JOptionPane.showConfirmDialog(DashboardFrame.this, "Apakah Anda yakin ingin logout?",
                        "Konfirmasi Logout", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    dispose();
                    SwingUtilities.invokeLater(() -> {
                        LoginFrame login = new LoginFrame();
                        login.setVisible(true);
                    });
                }
            }
        });
        panelTombol.add(btnLogout);

        add(panelTombol, BorderLayout.SOUTH);

        // Update ringkasan (simulasi)
        updateRingkasan();
    }

    private void showUsers() {
        if (!Database.canAccessAdminFeatures()) {
            JOptionPane.showMessageDialog(this, "Fitur manajemen pengguna hanya tersedia untuk akun admin.", "Akses Ditolak", JOptionPane.WARNING_MESSAGE);
            return;
        }
        SwingUtilities.invokeLater(() -> {
            UsersFrame usersFrame = new UsersFrame();
            usersFrame.setLocationRelativeTo(this);
            usersFrame.setVisible(true);
        });
    }

    private void showRiwayat() {
        SwingUtilities.invokeLater(() -> {
            RiwayatFrame riwayat = new RiwayatFrame();
            riwayat.setLocationRelativeTo(this);
            riwayat.setVisible(true);
        });
    }

    private void updateRingkasan() {
        Database.Totals totals = Database.getTotals();
        lblTotalSaldo.setText("Total Saldo: " + formatRupiah(totals.totalSaldo));
        lblTotalPemasukan.setText("Total Pemasukan: " + formatRupiah(totals.totalPemasukan));
        lblTotalPengeluaran.setText("Total Pengeluaran: " + formatRupiah(totals.totalPengeluaran));

        java.util.List<Database.Transaction> transactions = Database.getTransactions();
        chartPanel.setSegments(buildChartSegments(transactions));
    }

    private java.util.List<PieChartPanel.Segment> buildChartSegments(java.util.List<Database.Transaction> transactions) {
        Map<String, Long> totalsByCategory = new LinkedHashMap<>();
        for (Database.Transaction tx : transactions) {
            String label = tx.description.trim();
            if (label.isEmpty()) {
                label = tx.type;
            }
            totalsByCategory.put(label, totalsByCategory.getOrDefault(label, 0L) + Math.abs(tx.amount));
        }

        java.util.List<PieChartPanel.Segment> segments = new ArrayList<>();
        for (Map.Entry<String, Long> entry : totalsByCategory.entrySet()) {
            segments.add(new PieChartPanel.Segment(entry.getKey(), entry.getValue(), getCategoryColor(entry.getKey())));
        }
        return segments;
    }

    private Color getCategoryColor(String category) {
        String lower = category.toLowerCase();
        if (lower.contains("belanja") || lower.contains("shopping") || lower.contains("grocery") || lower.contains("makan")) {
            return new Color(52, 152, 219); // biru
        }
        if (lower.contains("tagihan") || lower.contains("bill") || lower.contains("listrik") || lower.contains("air") || lower.contains("internet")) {
            return new Color(155, 89, 182); // ungu
        }
        if (lower.contains("transport") || lower.contains("tol") || lower.contains("bus") || lower.contains("grab") || lower.contains("ojek")) {
            return new Color(241, 196, 15); // kuning/orange
        }
        if (lower.contains("gaji") || lower.contains("income") || lower.contains("pendapatan")) {
            return new Color(46, 204, 113); // hijau
        }
        if (lower.contains("utang") || lower.contains("hutang") || lower.contains("pinjaman")) {
            return new Color(230, 126, 34); // jingga
        }
        return new Color(52, 73, 94); // default
    }

    private void resetSaldo() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin mereset semua data transaksi? Tindakan ini tidak dapat dibatalkan.",
                "Konfirmasi Reset", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (Database.resetTransactions()) {
                updateRingkasan();
                JOptionPane.showMessageDialog(this, "Saldo berhasil direset!", "Reset Berhasil",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Error saat mereset data!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String formatRupiah(long nominal) {
        return "Rp " + String.format("%,d", nominal).replace(",", ".");
    }

    private class PieChartPanel extends JPanel {
        private java.util.List<Segment> segments = new ArrayList<>();

        public PieChartPanel() {
            setBackground(Color.WHITE);
        }

        public void setSegments(java.util.List<Segment> segments) {
            this.segments = segments;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int legendHeight = 90;
            int availableHeight = height - legendHeight - 30;
            int size = Math.min(width, availableHeight) - 40;
            int x = (width - size) / 2;
            int y = 20;

            long total = 0;
            for (Segment s : segments) {
                total += s.value;
            }

            if (total <= 0) {
                g2.setColor(Color.LIGHT_GRAY);
                g2.fillOval(x, y, size, size);
                g2.setColor(Color.DARK_GRAY);
                g2.setFont(new Font("Arial", Font.PLAIN, 14));
                FontMetrics fm = g2.getFontMetrics();
                String text = "Belum ada data transaksi";
                int textWidth = fm.stringWidth(text);
                g2.drawString(text, x + (size - textWidth) / 2, y + size / 2 + fm.getAscent() / 2);
                return;
            }

            int startAngle = 0;
            for (Segment segment : segments) {
                int angle = (int) Math.round((double) segment.value / total * 360);
                g2.setColor(segment.color);
                g2.fillArc(x, y, size, size, startAngle, angle);
                startAngle += angle;
            }

            int inset = size / 3;
            g2.setColor(Color.WHITE);
            g2.fillOval(x + inset, y + inset, size - inset * 2, size - inset * 2);

            int legendX = 30;
            int legendY = y + size + 20;
            int boxSize = 14;
            g2.setFont(new Font("Arial", Font.PLAIN, 13));
            for (int i = 0; i < segments.size(); i++) {
                Segment segment = segments.get(i);
                g2.setColor(segment.color);
                g2.fillRect(legendX, legendY + i * 24, boxSize, boxSize);
                g2.setColor(Color.BLACK);
                String label = segment.label + " (" + formatRupiah(segment.value) + ")";
                g2.drawString(label, legendX + boxSize + 8, legendY + i * 24 + boxSize - 2);
            }
        }

        private static class Segment {
            public final String label;
            public final long value;
            public final Color color;

            public Segment(String label, long value, Color color) {
                this.label = label;
                this.value = value;
                this.color = color;
            }
        }
    }
}