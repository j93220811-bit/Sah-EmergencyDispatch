package hospital.model;

/** Abstract resource; concrete resource types inherit shared state. */
public abstract class Resource {
    private final int id;
    private final String name;
    private final String type;
    private String status;
    private int patientId;
    protected Resource(int id, String name, String type, String status, int patientId) {
        this.id=id; this.name=name; this.type=type; this.status=status; this.patientId=patientId;
    }
    public int getId(){return id;} public String getName(){return name;} public String getType(){return type;}
    public String getStatus(){return status;} public int getPatientId(){return patientId;}
    public void setStatus(String s){status=s;} public void setPatientId(int id){patientId=id;}
}
