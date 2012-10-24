import java.util.Date;
import java.util.LinkedList;

public class Patient {
    private final int id;
    private String name;
    private Date birthday;
    private int phone;
    private String address;
    private String email;
    private LinkedList<Diagnosis> medicalHistory; 

    public Patient(String name, Date birthday, int phone, String address, String email, LinkedList<Diagnosis>medicalHistory) {
        this.id = EMRUtil.generatePatientId();
        this.name = name;
        this.birthday = birthday;
        this.phone = phone;
        this.address = address;
        this.email = email;
        if (medicalHistory == null) this.medicalHistory = new LinkedList<Diagnosis>();
        else this.medicalHistory = medicalHistory;
    }
    
    public void setName(String name) { this.name = name; }
    public void setBirthday(Date birthday) { this.birthday = birthday; }
    public void setPhone(int phone) { this.phone = phone; }
    public void setAddress(String address) { this.address = address; }
    public void setEmail(String email) { this.email = email; }
    
    public int getId() { return this.id; }
    public String getName() { return this.name; }
    public Date getBirthday() { return this.birthday; }
    public int getPhone() { return this.phone; }
    public String getAddress() { return this.address; }
    public String getEmail() { return this.email; }
    public LinkedList<Diagnosis> getMedicalHistory() {return this.medicalHistory; }
    
    public void addDiagnoses(LinkedList<Diagnosis> diagnoses) {
        this.medicalHistory.addAll(diagnoses);
    }
    
    public String toString() {
        String s = ""; 
        s += String.format("%-20s %-40s %n", "patientID", id);
        s += this.toStringName();
        s += this.toStringBirthday();
        s += this.toStringPhone();
        s += this.toStringEmail();
        s += this.toStringAddress();
        s += this.toStringMedicalHistory();
        return s;
    }
    
    private String toStringName() {
        if (name == null) return "";
        return String.format("%-20s %-40s %n", "name", name);
    }
    
    private String toStringBirthday() {
        if (birthday == null) return "";
        return String.format("%-20s %-40s %n", "birthday", EMRUtil.dateToStringBirthday(birthday));
    }
    
    private String toStringPhone() {
        if (phone == -1) return "";
        return String.format("%-20s %-40s %n", "phone", phone);
    }
    
    private String toStringAddress() {
        if (address == null) return "";
        String s = "";
        boolean firstLine = true;
        String[] lines = address.split("\\r?\\n");
        for (int i = 0; i < lines.length; i++) {
            if (firstLine) {
                s += String.format("%-20s %-40s %n", "address", lines[i].trim());
                firstLine = false;
            } else {
                s += String.format("%-20s %-40s %n", "", lines[i].trim());
            }
        }
        return s;
    }
    
    private String toStringEmail() {
        if (email == null) return "";
        return String.format("%-20s %-40s %n", "email", email);
    }
    
    private String toStringMedicalHistory() {
        if (medicalHistory == null) return String.format("%-20s %-40s %n", "medicalHistory", "None");
        String s = "";
        boolean firstLine = true;
        for (Diagnosis d: medicalHistory) {
            if (firstLine) {
                s += String.format("%-20s %-40s %n", "medicalHistory", d.toString());
                firstLine = false;
            } else {
                s += String.format("%-20s %-40s %n", "", d.toString());
            }
        }
        return s;
    }
}
