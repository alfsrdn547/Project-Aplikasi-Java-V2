import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class RegisterFrame extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword, txtConfirmPassword;
    private JButton btnRegister, btnBack;
    private JLabel lblError;

    public RegisterFrame() {
        // Pengaturan Window
        setTitle("Register - Manajer Keuangan");
        setSize(600, 520);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        // --- Panel Judul (Atas) ---
        JPanel panelJudul = new JPanel();
        panelJudul.setBackground(new Color(41, 128, 185));
        JLabel lblJudul = new JLabel("REGISTER AKUN");
        lblJudul.setForeground(Color.WHITE);
        lblJudul.setFont(new Font("Arial", Font.BOLD, 24));
        panelJudul.add(lblJudul);
        add(panelJudul, BorderLayout.NORTH);

        // --- Panel Form (Tengah) ---
        JPanel panelForm = new JPanel(new GridLayout(6, 1, 10, 20));
        panelForm.setBorder(BorderFactory.createEmptyBorder(50, 70, 50, 70));
        panelForm.setBackground(Color.WHITE);

        // Label Username
        JLabel lblUsername = new JLabel("Username:");
        lblUsername.setFont(new Font("Arial", Font.PLAIN, 14));
        panelForm.add(lblUsername);

        // Text Field Username
        txtUsername = new JTextField();
        txtUsername.setFont(new Font("Arial", Font.PLAIN, 16));
        txtUsername.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    txtPassword.requestFocus();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

            @Override
            public void keyTyped(KeyEvent e) {
            }
        });
        panelForm.add(txtUsername);

        // Label Password
        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        panelForm.add(lblPassword);

        // Text Field Password
        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Arial", Font.PLAIN, 16));
        txtPassword.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    txtConfirmPassword.requestFocus();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

            @Override
            public void keyTyped(KeyEvent e) {
            }
        });
        panelForm.add(txtPassword);

        // Label Confirm Password
        JLabel lblConfirmPassword = new JLabel("Konfirmasi Password:");
        lblConfirmPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        panelForm.add(lblConfirmPassword);

        // Text Field Confirm Password
        txtConfirmPassword = new JPasswordField();
        txtConfirmPassword.setFont(new Font("Arial", Font.PLAIN, 16));
        txtConfirmPassword.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    prosesRegister();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

            @Override
            public void keyTyped(KeyEvent e) {
            }
        });
        panelForm.add(txtConfirmPassword);

        add(panelForm, BorderLayout.CENTER);

        // --- Panel Error dan Tombol (Bawah) ---
        JPanel panelBawah = new JPanel(new BorderLayout(10, 10));
        panelBawah.setBorder(BorderFactory.createEmptyBorder(10, 50, 20, 50));
        panelBawah.setBackground(Color.WHITE);

        // Label Error
        lblError = new JLabel("");
        lblError.setForeground(new Color(192, 57, 43));
        lblError.setFont(new Font("Arial", Font.PLAIN, 13));
        panelBawah.add(lblError, BorderLayout.NORTH);

        // Panel Tombol
        JPanel panelTombol = new JPanel(new GridLayout(1, 2, 10, 0));

        btnRegister = UIHelper.createPrimaryButton("Register");
        btnRegister.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prosesRegister();
            }
        });
        panelTombol.add(btnRegister);

        btnBack = UIHelper.createNeutralButton("Kembali ke Login");
        btnBack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                kembaliKeLogin();
            }
        });
        panelTombol.add(btnBack);

        panelBawah.add(panelTombol, BorderLayout.CENTER);

        add(panelBawah, BorderLayout.SOUTH);

        // Set Background
        setBackground(Color.WHITE);
    }

    private void prosesRegister() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        String confirmPassword = new String(txtConfirmPassword.getPassword());

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            lblError.setText("Semua field harus diisi!");
            return;
        }

        if (!password.equals(confirmPassword)) {
            lblError.setText("Password dan konfirmasi password tidak cocok!");
            return;
        }

        boolean registered = Database.registerUser(username, password);
        if (registered) {
            JOptionPane.showMessageDialog(this, "Registrasi berhasil! Silakan login dengan akun baru.", "Sukses",
                    JOptionPane.INFORMATION_MESSAGE);
            kembaliKeLogin();
        } else {
            lblError.setText("Username sudah terdaftar atau terjadi kesalahan. Silakan coba lagi.");
        }
    }

    private void kembaliKeLogin() {
        // Tutup register frame
        dispose();

        // Tampilkan login frame
        SwingUtilities.invokeLater(() -> {
            LoginFrame login = new LoginFrame();
            login.setVisible(true);
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
            RegisterFrame register = new RegisterFrame();
            register.setVisible(true);
        });
    }
}