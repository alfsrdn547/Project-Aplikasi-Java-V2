CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'user',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_date TIMESTAMP NULL DEFAULT NULL
);

CREATE TABLE IF NOT EXISTS transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL,
    amount BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_date TIMESTAMP NULL DEFAULT NULL,
    INDEX idx_transactions_username (username),
    INDEX idx_transactions_created_at (created_at)
);

INSERT IGNORE INTO users (username, password, role) VALUES
('admin', '123', 'admin');

INSERT IGNORE INTO transactions (username, description, type, amount) VALUES
('admin', 'Saldo awal', 'Pemasukan', 1000000),
('admin', 'Belanja bulanan', 'Pengeluaran', 250000);
