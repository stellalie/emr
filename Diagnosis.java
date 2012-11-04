import java.util.Date;

public class Diagnosis {
	Date date;
	String information;

	public Diagnosis(Date date, String information) {
		this.date = date;
		this.information = information;
	}

	public Date getDate() { return this.date; }
	public String getInformation() { return this.information; }

	public String toString() {
		return EMRUtil.dateToStringDiagnosis(this.date) + " "
				+ this.information;
	}
}
