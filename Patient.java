import java.util.Date;
import java.util.LinkedList;
import java.util.*;

public class Patient {
    private final int id;
    private String name;
    private Date birthday;
    private int phone;
    private String address;
    private String email;
    private LinkedList<Diagnosis> medicalHistory; 

    public Patient(int id, String name, Date birthday, int phone, String address, 
    		String email, LinkedList<Diagnosis>medicalHistory) {
        this.id = id;
        this.name = name;
        this.birthday = birthday;
        this.phone = phone;
        this.address = address;
        this.email = email;
        if (medicalHistory == null) this.medicalHistory = new LinkedList<Diagnosis>();
        else this.medicalHistory = medicalHistory;
        this.sortMedicalHistory();
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
    public LinkedList<Diagnosis> getMedicalHistory() { return this.medicalHistory; }
    
	public void addDiagnoses(LinkedList<Diagnosis> diagnoses) {
		this.medicalHistory.addAll(diagnoses);
		this.sortMedicalHistory();
	}

	private void sortMedicalHistory() {
		Collections.sort(this.medicalHistory, new Comparator<Diagnosis>() {
			@Override
			public int compare(Diagnosis d1, Diagnosis d2) {
				return d1.getDate().compareTo(d2.getDate());
			}
		});
	}
	
	public String toString() {
		return this.toString(null, null);
	}

	public String toString(Date start, Date end) {
		String format = "%-20s %-40s %n";
		String s = "";
		s += String.format(format, Attribute.PATIENTID, id);
		s += this.toStringName(format);
		s += this.toStringBirthday(format);
		s += this.toStringPhone(format);
		s += this.toStringEmail(format);
		s += this.toStringAddress(format);
		s += this.toStringMedicalHistory(format, start, end);
		return s;
	}
	
	private String toStringName(String format) {
		if (name == null) return "";
		return String.format(format, Attribute.NAME, name);
	}

	private String toStringBirthday(String format) {
		if (birthday == null) return "";
		return String.format(format, Attribute.BIRTHDAY, EMRUtil.dateToStringBirthday(birthday));
	}

	private String toStringPhone(String format) {
		if (phone == -1) return "";
		return String.format(format, Attribute.PHONE, phone);
	}

	private String toStringAddress(String format) {
		if (address == null) return "";
		return String.format(format, Attribute.ADDRESS, address.replaceAll("\n", " "));
	}

	private String toStringEmail(String format) {
		if (email == null) return "";
		return String.format(format, Attribute.EMAIL, email);
	}
	
	public LinkedList<Diagnosis> getMedicalHistory(Date start, Date end) { 
		LinkedList<Diagnosis> diagnoses = new LinkedList<Diagnosis>();
		if (start == null && end == null) {
			diagnoses = this.medicalHistory;
		} else {
			for (Diagnosis d: medicalHistory) {
				if (d.getDate().after(start) && d.getDate().before(end)) {
					diagnoses.add(d);
				}
			}
		}
		return diagnoses;
	}

	private String toStringMedicalHistory(String format, Date start, Date end) {
		LinkedList<Diagnosis> diagnoses = this.getMedicalHistory(start, end);
		
		if (diagnoses.size() == 0)
			return String.format(format, Attribute.MEDICALHISTORY, "None");
		
		String s = "";
		boolean firstLine = true;
		for (Diagnosis d : diagnoses) {
			if (firstLine) {
				s += String.format(format, Attribute.MEDICALHISTORY,
						d.toString());
				firstLine = false;
			} else {
				s += String.format(format, "", d.toString());
			}
		}
		return s;
	}
}
