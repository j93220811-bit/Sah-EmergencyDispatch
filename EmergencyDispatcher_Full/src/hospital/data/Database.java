package hospital.data;

import java.sql.*;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

/** Owns SQLite connection setup, schema creation and demo seed data. */
public final class Database {
    private static final String URL = "jdbc:sqlite:hospital.db";
    private Database() {}
    public static Connection connect() throws SQLException {
        Connection c = DriverManager.getConnection(URL);
        try (Statement s=c.createStatement()) { s.execute("PRAGMA foreign_keys=ON"); }
        return c;
    }
    public static void initialize() throws SQLException {
        try (Connection c=connect(); Statement s=c.createStatement()) {
            s.executeUpdate("CREATE TABLE IF NOT EXISTS users(username TEXT PRIMARY KEY, password_hash TEXT NOT NULL, role TEXT NOT NULL, failed_attempts INTEGER NOT NULL DEFAULT 0, locked INTEGER NOT NULL DEFAULT 0)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS patients(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, age INTEGER NOT NULL, contact TEXT, severity TEXT NOT NULL, severity_score INTEGER NOT NULL, notes TEXT, status TEXT NOT NULL, arrival_time TEXT NOT NULL, vitals_alert INTEGER NOT NULL DEFAULT 0)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS vitals(id INTEGER PRIMARY KEY AUTOINCREMENT, patient_id INTEGER NOT NULL, systolic INTEGER, pulse INTEGER, spo2 INTEGER, temperature REAL, respiratory_rate INTEGER, alert INTEGER, recorded_at TEXT, FOREIGN KEY(patient_id) REFERENCES patients(id))");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS resources(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, type TEXT NOT NULL, status TEXT NOT NULL DEFAULT 'AVAILABLE', patient_id INTEGER DEFAULT 0)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS assignments(id INTEGER PRIMARY KEY AUTOINCREMENT, patient_id INTEGER NOT NULL, resource_ids TEXT NOT NULL, status TEXT NOT NULL, created_at TEXT NOT NULL)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS audit_logs(id INTEGER PRIMARY KEY AUTOINCREMENT, time TEXT NOT NULL, username TEXT NOT NULL, action TEXT NOT NULL, details TEXT NOT NULL)");
        }
        seedUsers();
        seedResources();
    }
    private static String hash(String password) {
        try {
            byte[] bytes=MessageDigest.getInstance("SHA-256").digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder b=new StringBuilder();
            for(byte x:bytes) b.append(String.format("%02x", x));
            return b.toString();
        } catch(Exception e) { throw new IllegalStateException(e); }
    }
    private static void seedUsers() throws SQLException {
        String[][] users={{"nurse","nurse123","NURSE"},{"coordinator","coord123","COORDINATOR"},{"admin","admin123","ADMINISTRATOR"},{"doctor","doctor123","DOCTOR"}};
        try(Connection c=connect(); PreparedStatement p=c.prepareStatement("INSERT OR IGNORE INTO users(username,password_hash,role) VALUES(?,?,?)")) {
            for(String[] u:users){p.setString(1,u[0]);p.setString(2,hash(u[1]));p.setString(3,u[2]);p.addBatch();}
            p.executeBatch();
        }
    }
    private static void seedResources() throws SQLException {
        try(Connection c=connect(); Statement q=c.createStatement(); ResultSet r=q.executeQuery("SELECT COUNT(*) FROM resources")) {
            if(r.next() && r.getInt(1)>0) return;
        }
        String[][] data={{"Trauma Bay 1","BAY"},{"Trauma Bay 2","BAY"},{"Operating Theatre 1","THEATRE"},{"Dr. Asha - Emergency Medicine","DOCTOR"},{"Dr. Ravi - General Surgery","DOCTOR"},{"Dr. Meera - Anaesthesiology","DOCTOR"},{"Nurse Team 1","NURSE"},{"Nurse Team 2","NURSE"},{"Ventilator 1","EQUIPMENT"},{"Patient Monitor 1","EQUIPMENT"},{"Defibrillator 1","EQUIPMENT"}};
        try(Connection c=connect(); PreparedStatement p=c.prepareStatement("INSERT INTO resources(name,type,status,patient_id) VALUES(?,?,'AVAILABLE',0)")) {
            for(String[] x:data){p.setString(1,x[0]);p.setString(2,x[1]);p.addBatch();} p.executeBatch();
        }
    }
    public static String passwordHash(String password) { return hash(password); }
}
