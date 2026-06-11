import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class LoginFrame extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin, btnKeluar;
    private JLabel lblError;

    public LoginFrame() {
        // Pengaturan Window
        setTitle("Login - Manajer Keuangan");
        setSize(600, 520);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        // --- Panel Judul (Atas) ---
        JPanel panelJudul = new JPanel();
        panelJudul.setBackground(new Color(41, 128, 185));
        JLabel lblJudul = new JLabel("MANAJER KEUANGAN");
        lblJudul.setForeground(Color.WHITE);
        lblJudul.setFont(new Font("Arial", Font.BOLD, 24));
        panelJudul.add(lblJudul);
        add(panelJudul, BorderLayout.NORTH);

        // --- Panel Form (Tengah) ---
        JPanel panelForm = new JPanel(new GridLayout(4, 1, 10, 20));
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
                    prosesLogin();
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
        JPanel panelTombol = new JPanel(new GridLayout(1, 3, 10, 0));

        btnLogin = UIHelper.createPrimaryButton("Login");
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prosesLogin();
            }
        });
        panelTombol.add(btnLogin);

        JButton btnRegister = UIHelper.createSecondaryButton("Register");
        btnRegister.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Buka halaman register
                dispose();
                SwingUtilities.invokeLater(() -> {
                    RegisterFrame register = new RegisterFrame();
                    register.setVisible(true);
                });
            }
        });
        panelTombol.add(btnRegister);

        btnKeluar = UIHelper.createDangerButton("Keluar");
        btnKeluar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        panelTombol.add(btnKeluar);

        panelBawah.add(panelTombol, BorderLayout.CENTER);

        add(panelBawah, BorderLayout.SOUTH);

        // Set Background
        setBackground(Color.WHITE);
    }

    private void prosesLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Username dan Password tidak boleh kosong!");
            return;
        }

        if (Database.validateUser(username, password)) {
            Database.setCurrentUsername(username);
            loginBerhasil();
        } else {
            lblError.setText("Username atau Password salah!");
            txtPassword.setText("");
            txtUsername.requestFocus();
        }
    }

    private void loginBerhasil() {
        // Sembunyikan login frame
        setVisible(false);

        // Tampilkan dashboard
        SwingUtilities.invokeLater(() -> {
            DashboardFrame dashboard = new DashboardFrame();
            dashboard.setVisible(true);

            // Hapus login frame dari memori ketika dashboard ditutup
            dashboard.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        });
    }

    public static void main(String[] args) {
        try {
            Database.initDatabase();
        } catch (RuntimeException ex) {
            String errorMsg = ex.getMessage();
            String setupGuide = "\n\n" +
                    "=== DATABASE SETUP REQUIRED ===\n" +
                    "Lihat file SETUP_DATABASE.txt untuk instruksi.\n\n" +
                    "Atau jalankan dengan password MySQL:\n" +
                    "  set MYSQL_PASSWORD=your_password\n" +
                    "  java -cp \"lib/mysql-connector-j-9.6.0.jar;.\" LoginFrame";

            JOptionPane.showMessageDialog(null,
                    "Gagal koneksi database:\n" + errorMsg + setupGuide,
                    "Database Connection Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        SwingUtilities.invokeLater(() -> {
            LoginFrame login = new LoginFrame();
            login.setVisible(true);
        });
    }
}
