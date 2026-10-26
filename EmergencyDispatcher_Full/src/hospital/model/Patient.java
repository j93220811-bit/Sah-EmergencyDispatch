package hospital.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Encapsulates a patient and their triage state. */
public class Patient {
    private final int id;
    private final String name;
    private final int age;
    private final String contact;
    private SeverityLevel severity;
    private int severityScore;
    private String notes;
    private String status;
    private final String arrivalTime;
    private boolean vitalsAlert;

    public Patient(int id, String name, int age, String contact, SeverityLevel severity,
                   int severityScore, String notes, String status, String arrivalTime, boolean vitalsAlert) {
        this.id=id; this.name=name; this.age=age; this.contact=contact; this.severity=severity;
        this.severityScore=severityScore; this.notes=notes; this.status=status;
        this.arrivalTime=arrivalTime; this.vitalsAlert=vitalsAlert;
    }
    public int getId(){return id;} public String getName(){return name;} public int getAge(){return age;}
    public String getContact(){return contact;} public SeverityLevel getSeverity(){return severity;}
    public int getSeverityScore(){return severityScore;} public String getNotes(){return notes;}
    public String getStatus(){return status;} public String getArrivalTime(){return arrivalTime;}
    public boolean hasVitalsAlert(){return vitalsAlert;}
    public void setSeverity(SeverityLevel v){severity=v;} public void setSeverityScore(int v){severityScore=v;}
    public void setNotes(String v){notes=v;} public void setStatus(String v){status=v;}
    public void setVitalsAlert(boolean v){vitalsAlert=v;}
    public static String now(){return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));}
}
