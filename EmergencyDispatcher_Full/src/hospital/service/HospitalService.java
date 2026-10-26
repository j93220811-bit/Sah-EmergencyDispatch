package hospital.service;

import hospital.data.Database;
import hospital.model.*;
import java.sql.*;
import java.util.*;

/** Business rules live here; Swing panels call these methods rather than SQL directly. */
public class HospitalService {
    private final User currentUser;
    public HospitalService(User user){currentUser=user;}
    public User getCurrentUser(){return currentUser;}

    public User authenticate(String username, String password) throws SQLException {
        try(Connection c=Database.connect(); PreparedStatement p=c.prepareStatement("SELECT password_hash,role,failed_attempts,locked FROM users WHERE username=?")) {
            p.setString(1,username.trim());
            try(ResultSet r=p.executeQuery()) {
                if(!r.next() || r.getInt("locked")==1) return null;
                if(Database.passwordHash(password).equals(r.getString("password_hash"))) {
                    try(PreparedStatement u=c.prepareStatement("UPDATE users SET failed_attempts=0 WHERE username=?")){u.setString(1,username.trim());u.executeUpdate();}
                    return new StaffUser(username.trim(),UserRole.valueOf(r.getString("role")));
                }
                int attempts=r.getInt("failed_attempts")+1;
                try(PreparedStatement u=c.prepareStatement("UPDATE users SET failed_attempts=?,locked=? WHERE username=?")){
                    u.setInt(1,attempts);u.setInt(2,attempts>=5?1:0);u.setString(3,username.trim());u.executeUpdate();
                }
                return null;
            }
        }
    }
    public void addPatient(String name,int age,String contact,SeverityLevel severity,int score,String notes) throws SQLException {
        if(name==null || name.trim().isEmpty()) throw new IllegalArgumentException("Patient name is required.");
        if(age<0 || age>120) throw new IllegalArgumentException("Enter a valid age (0–120).");
        if(score<1 || score>10) throw new IllegalArgumentException("Severity score must be between 1 and 10.");
        try(Connection c=Database.connect();PreparedStatement p=c.prepareStatement("INSERT INTO patients(name,age,contact,severity,severity_score,notes,status,arrival_time) VALUES(?,?,?,?,?,?,?,?)")) {
            p.setString(1,name.trim());p.setInt(2,age);p.setString(3,contact==null?"":contact.trim());p.setString(4,severity.name());p.setInt(5,score);p.setString(6,notes==null?"":notes.trim());p.setString(7,"WAITING");p.setString(8,Patient.now());p.executeUpdate();
        }
        audit("REGISTER_PATIENT",name+" / "+severity);
    }
    public List<Patient> getPatients() throws SQLException {
        List<Patient> out=new ArrayList<>();
        String sql="SELECT * FROM patients ORDER BY CASE severity WHEN 'RED' THEN 1 WHEN 'YELLOW' THEN 2 ELSE 3 END, severity_score DESC, arrival_time ASC";
        try(Connection c=Database.connect();Statement s=c.createStatement();ResultSet r=s.executeQuery(sql)){
            while(r.next())out.add(new Patient(r.getInt("id"),r.getString("name"),r.getInt("age"),r.getString("contact"),SeverityLevel.valueOf(r.getString("severity")),r.getInt("severity_score"),r.getString("notes"),r.getString("status"),r.getString("arrival_time"),r.getInt("vitals_alert")==1));
        } return out;
    }
    public void updateStatus(int patientId,String status) throws SQLException {
        try(Connection c=Database.connect();PreparedStatement p=c.prepareStatement("UPDATE patients SET status=? WHERE id=?")){p.setString(1,status);p.setInt(2,patientId);if(p.executeUpdate()==0)throw new IllegalArgumentException("Patient not found.");}
        audit("UPDATE_STATUS","Patient #"+patientId+" -> "+status);
    }
    public boolean addVitals(int patientId,Vitals v) throws SQLException {
        boolean alert=v.hasAlert();
        try(Connection c=Database.connect()){
            try(PreparedStatement p=c.prepareStatement("INSERT INTO vitals(patient_id,systolic,pulse,spo2,temperature,respiratory_rate,alert,recorded_at) VALUES(?,?,?,?,?,?,?,?)")){
                p.setInt(1,patientId);p.setInt(2,v.getSystolic());p.setInt(3,v.getPulse());p.setInt(4,v.getSpo2());p.setDouble(5,v.getTemperature());p.setInt(6,v.getRespiratoryRate());p.setInt(7,alert?1:0);p.setString(8,Patient.now());p.executeUpdate();
            }
            try(PreparedStatement p=c.prepareStatement("UPDATE patients SET vitals_alert=? WHERE id=?")){p.setInt(1,alert?1:0);p.setInt(2,patientId);p.executeUpdate();}
        }
        audit("RECORD_VITALS","Patient #"+patientId+(alert?" ALERT":" readings normal"));
        return alert;
    }
    public List<String[]> getResources() throws SQLException {
        List<String[]> out=new ArrayList<>();
        try(Connection c=Database.connect();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT id,name,type,status,patient_id FROM resources ORDER BY type,name")){
            while(r.next())out.add(new String[]{""+r.getInt(1),r.getString(2),r.getString(3),r.getString(4),""+r.getInt(5)});
        } return out;
    }
    public void setResourceStatus(int id,String status) throws SQLException {
        if(!currentUser.canManageResources())throw new SecurityException("Only a coordinator or administrator can manage resources.");
        try(Connection c=Database.connect();PreparedStatement p=c.prepareStatement("UPDATE resources SET status=?,patient_id=CASE WHEN ?='AVAILABLE' THEN 0 ELSE patient_id END WHERE id=?")){
            p.setString(1,status);p.setString(2,status);p.setInt(3,id);if(p.executeUpdate()==0)throw new IllegalArgumentException("Resource not found.");
        }
        audit("RESOURCE_STATUS","Resource #"+id+" -> "+status);
    }
    public String assignResources(int patientId) throws SQLException {
        if(!currentUser.canManageResources())throw new SecurityException("Only a coordinator or administrator can assign resources.");
        List<Integer> ids=new ArrayList<>();
        try(Connection c=Database.connect()){
            String[] types={"BAY","DOCTOR","NURSE","EQUIPMENT"};
            for(String type:types){
                try(PreparedStatement p=c.prepareStatement("SELECT id FROM resources WHERE type=? AND status='AVAILABLE' ORDER BY id LIMIT ?")){
                    p.setString(1,type);p.setInt(2,type.equals("EQUIPMENT")?1:1);
                    try(ResultSet r=p.executeQuery()){if(r.next())ids.add(r.getInt(1));}
                }
            }
            if(ids.size()<4)return "No complete resource match is available. Need an available bay, doctor, nurse and equipment.";
            String csv=ids.toString().replace("[","").replace("]","");
            try(PreparedStatement p=c.prepareStatement("INSERT INTO assignments(patient_id,resource_ids,status,created_at) VALUES(?,?,?,?)")){
                p.setInt(1,patientId);p.setString(2,csv);p.setString(3,"ACTIVE");p.setString(4,Patient.now());p.executeUpdate();
            }
            try(PreparedStatement p=c.prepareStatement("UPDATE resources SET status='OCCUPIED',patient_id=? WHERE id=?")){
                for(int id:ids){p.setInt(1,patientId);p.setInt(2,id);p.addBatch();}p.executeBatch();
            }
            try(PreparedStatement p=c.prepareStatement("UPDATE patients SET status='ASSIGNED' WHERE id=?")){p.setInt(1,patientId);p.executeUpdate();}
        }
        audit("ASSIGN_RESOURCES","Patient #"+patientId+" assigned resources "+ids);
        return "Resources assigned successfully to patient #"+patientId+".";
    }
    public String preemptForCritical(int patientId) throws SQLException {
        if(!currentUser.canManageResources())throw new SecurityException("Only a coordinator or administrator can approve pre-emption.");
        try(Connection c=Database.connect()){
            try(PreparedStatement p=c.prepareStatement("SELECT resource_ids,patient_id FROM assignments WHERE status='ACTIVE' ORDER BY id LIMIT 1");ResultSet r=p.executeQuery()){
                if(!r.next())return "No active assignment is available to pause.";
                String ids=r.getString(1);int affected=r.getInt(2);
                try(PreparedStatement u=c.prepareStatement("UPDATE assignments SET status='PAUSED' WHERE patient_id=? AND status='ACTIVE'")){u.setInt(1,affected);u.executeUpdate();}
                for(String token:ids.split(",")){
                    try(PreparedStatement u=c.prepareStatement("UPDATE resources SET status='AVAILABLE',patient_id=0 WHERE id=?")){u.setInt(1,Integer.parseInt(token.trim()));u.executeUpdate();}
                }
                try(PreparedStatement u=c.prepareStatement("UPDATE patients SET status='PAUSED' WHERE id=?")){u.setInt(1,affected);u.executeUpdate();}
            }
        }
        audit("PREEMPTION","Paused lower-priority assignment for critical patient #"+patientId);
        return assignResources(patientId);
    }
    public List<String[]> getAuditLogs() throws SQLException {
        List<String[]> out=new ArrayList<>();
        try(Connection c=Database.connect();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT time,username,action,details FROM audit_logs ORDER BY id DESC LIMIT 300")){
            while(r.next())out.add(new String[]{r.getString(1),r.getString(2),r.getString(3),r.getString(4)});
        }return out;
    }
    public List<String[]> getVitals(int patientId) throws SQLException {
        List<String[]> out=new ArrayList<>();
        try(Connection c=Database.connect();PreparedStatement p=c.prepareStatement("SELECT recorded_at,systolic,pulse,spo2,temperature,respiratory_rate,alert FROM vitals WHERE patient_id=? ORDER BY id DESC")){
            p.setInt(1,patientId);try(ResultSet r=p.executeQuery()){while(r.next())out.add(new String[]{r.getString(1),""+r.getInt(2),""+r.getInt(3),""+r.getInt(4),""+r.getDouble(5),""+r.getInt(6),r.getInt(7)==1?"ALERT":"Normal"});}
        }return out;
    }
    private void audit(String action,String details) throws SQLException {
        try(Connection c=Database.connect();PreparedStatement p=c.prepareStatement("INSERT INTO audit_logs(time,username,action,details) VALUES(?,?,?,?)")){
            p.setString(1,Patient.now());p.setString(2,currentUser.getUsername());p.setString(3,action);p.setString(4,details);p.executeUpdate();
        }
    }
}
