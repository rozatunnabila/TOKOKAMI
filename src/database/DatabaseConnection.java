package database;

import java.sql.*;

/**
 * Kelas DatabaseConnection bertugas menangani koneksi ke database.
 * Pada versi ini, koneksi TIDAK disimpan sebagai variabel global.
 * Setiap pemanggilan getConnection() akan membuat koneksi baru.
 */
public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/arlene_jewelry";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    /**
     * Selalu membuat koneksi baru ke database.
     * Pendekatan ini mencegah error saat ada banyak transaksi paralel,
     * karena setiap operasi memiliki koneksi sendiri.
     *
     * @return Connection baru, atau null jika gagal.
     */
    public static Connection getConnection() {
        try {
            // Load driver MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Membuat koneksi baru ke database
            Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("✅ Database connected successfully!");
            return conn;

        } catch (Exception e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Digunakan saat aplikasi mulai.
     * Tujuannya hanya untuk mengecek apakah koneksi berhasil dibuat.
     */
    public static void initializeDatabase() {
        System.out.println("🔄 Initializing database connection...");
        testConnection(); // Test koneksi dulu
    }

    /**
     * Melakukan test koneksi dan menampilkan daftar tabel dalam database.
     * Jika getConnection() menghasilkan null, berarti koneksi gagal.
     */
    public static void testConnection() {
        try (Connection conn = getConnection()) {

            // Jika koneksi gagal dibuat
            if (conn == null) {
                System.err.println("❌ Database connection test: FAILED - Connection is null");
                return;
            }

            System.out.println("✅ Database connection test: SUCCESS");

            // Query sederhana untuk melihat tabel-tabel dalam database
            String testQuery = "SHOW TABLES";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(testQuery)) {

                System.out.println("📊 Database tables:");
                while (rs.next()) {
                    System.out.println("   - " + rs.getString(1));
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Database connection test: FAILED - " + e.getMessage());
        }
    }

    /**
     * Karena tidak menggunakan koneksi global, maka tidak ada yang perlu ditutup.
     * Method ini hanya sebagai placeholder agar struktur tetap konsisten.
     */
    public static void closeConnection() {
        System.out.println("ℹ️ No global connection to close.");
    }
}
