import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

public class UserTransactionsFrame extends JFrame {
    private final String targetUsername;
    private final JTable tableTransactions;
    private final DefaultTableModel tableModel;
    private final JTextField txtDescription;
    private final JTextField txtAmount;
    private final JComboBox<String> cbType;
    private int editingTransactionId = -1;

    public UserTransactionsFrame(String username) {
        this.targetUsername = username;
        setTitle("Transaksi User - " + username);
        setSize(780, 560);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(12, 12));

        JPanel panelTitle = new JPanel();
        panelTitle.setBackground(new Color(41, 128, 185));
        JLabel lblTitle = new JLabel("TRANSAKSI PENGGUNA: " + username.toUpperCase());
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        panelTitle.add(lblTitle);
        add(panelTitle, BorderLayout.NORTH);

        JPanel panelContent = new JPanel(new BorderLayout(10, 10));
        panelContent.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panelContent.setBackground(Color.WHITE);

        JPanel panelForm = new JPanel(new GridLayout(4, 2, 8, 8));
        panelForm.setBorder(BorderFactory.createTitledBorder("Tambah / Edit Transaksi"));
        panelForm.setBackground(Color.WHITE);

        panelForm.add(new JLabel("Keterangan:"));
        txtDescription = new JTextField();
        panelForm.add(txtDescription);

        panelForm.add(new JLabel("Jumlah (Rp):"));
        txtAmount = new JTextField();
        panelForm.add(txtAmount);

        panelForm.add(new JLabel("Jenis:"));
        cbType = new JComboBox<>(new String[] { "Pemasukan", "Pengeluaran" });
        panelForm.add(cbType);

        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelButtons.setBackground(Color.WHITE);
        JButton btnSave = UIHelper.createPrimaryButton("Simpan");
        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveTransaction();
            }
        });
        panelButtons.add(btnSave);

        JButton btnDelete = UIHelper.createDangerButton("Hapus");
        btnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteTransaction();
            }
        });
        panelButtons.add(btnDelete);

        JButton btnBack = UIHelper.createNeutralButton("Kembali");
        btnBack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        panelButtons.add(btnBack);

        panelForm.add(panelButtons);
        panelContent.add(panelForm, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new Object[]{"ID", "Keterangan", "Jenis", "Jumlah", "Tanggal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableTransactions = new JTable(tableModel);
        tableTransactions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableTransactions.setRowHeight(26);
        tableTransactions.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tableTransactions.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tableTransactions.getSelectedRow() >= 0) {
                int selectedRow = tableTransactions.getSelectedRow();
                editingTransactionId = (int) tableModel.getValueAt(selectedRow, 0);
                txtDescription.setText(tableModel.getValueAt(selectedRow, 1).toString());
                txtAmount.setText(String.valueOf(extractAmount(tableModel.getValueAt(selectedRow, 3).toString())));
                cbType.setSelectedItem(tableModel.getValueAt(selectedRow, 2).toString());
            }
        });
        panelContent.add(new JScrollPane(tableTransactions), BorderLayout.CENTER);

        add(panelContent, BorderLayout.CENTER);
        loadData();
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<Database.Transaction> transactions = Database.getTransactionsForUser(targetUsername);
        for (Database.Transaction tx : transactions) {
            tableModel.addRow(new Object[]{
                    tx.id,
                    tx.description,
                    tx.type,
                    formatRupiah(tx.amount),
                    formatTimestamp(tx.createdAt)
            });
        }
        clearForm();
    }

    private void saveTransaction() {
        String description = txtDescription.getText().trim();
        String amountText = txtAmount.getText().trim();
        if (description.isEmpty() || amountText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Keterangan dan jumlah wajib diisi.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            long amount = Long.parseLong(amountText);
            boolean success;
            if (editingTransactionId > 0) {
                success = Database.updateTransaction(editingTransactionId, description, (String) cbType.getSelectedItem(), amount);
            } else {
                success = Database.addTransactionForUser(targetUsername, description, (String) cbType.getSelectedItem(), amount);
            }
            if (success) {
                JOptionPane.showMessageDialog(this, "Transaksi berhasil disimpan.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menyimpan transaksi.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Jumlah harus berupa angka.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteTransaction() {
        if (editingTransactionId <= 0) {
            JOptionPane.showMessageDialog(this, "Pilih transaksi yang ingin dihapus.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Hapus transaksi ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        boolean success = Database.deleteTransaction(editingTransactionId);
        if (success) {
            JOptionPane.showMessageDialog(this, "Transaksi berhasil dihapus.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, "Gagal menghapus transaksi.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        editingTransactionId = -1;
        txtDescription.setText("");
        txtAmount.setText("");
        cbType.setSelectedIndex(0);
        tableTransactions.clearSelection();
    }

    private long extractAmount(String value) {
        try {
            return Long.parseLong(value.replace("Rp ", "").replace(".", "").replace(",", ""));
        } catch (Exception ex) {
            return 0;
        }
    }

    private String formatRupiah(long amount) {
        return "Rp " + String.format("%,d", amount).replace(",", ".");
    }

    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) {
            return "-";
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(timestamp);
    }
}
