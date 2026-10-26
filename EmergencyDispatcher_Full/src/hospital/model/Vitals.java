package hospital.model;
public class Vitals {
    private final int systolic, pulse, spo2, respiratoryRate;
    private final double temperature;
    public Vitals(int systolic, int pulse, int spo2, double temperature, int respiratoryRate) {
        this.systolic=systolic; this.pulse=pulse; this.spo2=spo2; this.temperature=temperature; this.respiratoryRate=respiratoryRate;
    }
    public int getSystolic(){return systolic;} public int getPulse(){return pulse;}
    public int getSpo2(){return spo2;} public double getTemperature(){return temperature;}
    public int getRespiratoryRate(){return respiratoryRate;}
    public boolean hasAlert(){return systolic>=180 || pulse>=120 || spo2<=90 || temperature>=39.0 || respiratoryRate>=30;}
}
