import java.util.*;
import java.text.SimpleDateFormat;

/**
 * This class consists exclusively of static methods used by EMR. It contains
 * attribute validation methods, conversions between date to strings (vice
 * versa), and a few other odds and ends.
 * @author VG
 *
 */

public class EMRUtil {
	public static String[] attributes = { Attribute.NAME, Attribute.PATIENTID,
		Attribute.BIRTHDAY, Attribute.PHONE, Attribute.EMAIL,
		Attribute.MEDICALHISTORY, Attribute.ADDRESS };
	
	/**
	 * Check and return string as integer if a valid phone number. Valid entry
	 * will consists of only digits. Leading zeroes are ignored.
	 * @param s Patient's phone number
	 * @return Patient's phone number in integer (-1 is returned if entry is invalid)
	 */
	public static int validPhone(String s) {
		if (s == null) return -1;
		if (!s.matches("-?\\d+(\\.\\d+)?")) return -1;
		return Integer.parseInt(s);
	}
	
	/**
	 * Check and return email if a valid email. Valid email address consists
	 * a string with alphabetic, numeric, and punctuation character and an
	 * "at" (@) symbol inside and with no gaps, such abcdefg.123@gmail.com
	 * @param s Patient's email address
	 * @return valid Patient's email address (null is returned if invalid)
	 */
	public static String validEmail(String s) {
		if (s == null) return null;
		if (!(s.contains(" ") == false && s.matches(".+@.+\\.[a-z]+"))) return null;
		if ((s.length() - s.replaceAll("\\@", "").length()) != 1) return null;
		if ((s.length() - s.replaceAll("\\.", "").length()) != 1) return null;
		return s;
	}

	public static String validName(String s) {
		if (!EMRUtil.nameIsValid(s))
			try {
				throw new Exception();
			} catch (Exception e) {
				e.printStackTrace();
			}
		return s.split("\n")[0];
	}
	
	/**
	 * Check whether patient's name is valid for EMR input. Name should
	 * be in the form of a string of forename(s) and surname, all on one line;
	 * and the name cannot include numeric or punctuation characters.
	 * @param s Patient's name
	 * @return boolean whether patient's name is valid or not
	 */
	public static boolean nameIsValid(String s) {
		if (s == null) return false;
		if (s.matches(".*\\d.*")) return false;
		return true;
	}
	
	/**
	 * Check whether date is valid for EMR input. Being used to read patient's
	 * birthday and diagnosis date. Date should be in form of dd-mm-yyyy.
	 * @param s date
	 * @return boolean whether date is valid or not
	 */
	public static boolean dateIsValid(String s) {
		if (s == null) return false;
		if (!s.matches("\\d{1,2}-\\d{1,2}-\\d{4}")) return false;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("d-M-y", Locale.ENGLISH);
			sdf.setLenient(false);
			sdf.parse(s);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Convert string to date
	 * @param s string
	 * @return date
	 * @throws java.text.ParseException
	 */
	public static Date stringToDate(String s) throws java.text.ParseException {
		return new SimpleDateFormat("d-M-yyyy", Locale.ENGLISH).parse(s);
	}
	
	/**
	 * Convert date to string
	 * @param date
	 * @return string in form of dd-mm-yyyy
	 */
	public static String dateToStringBirthday(Date date) {
		return new SimpleDateFormat("d-M-yyyy", Locale.ENGLISH).format(date);
	}
	
	/**
	 * Convert date to string
	 * @param date
	 * @return string in form of dd-MM-yyyy
	 */
	public static String dateToStringDiagnosis(Date date) {
		return new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(date);
	}
	
	/**
	 * Check whether word equals to any keywords being used as patient's 
	 * attributes.
	 * @param word
	 */
	public static boolean wordIsAttribute(String word) {
		for (String attribute : EMRUtil.attributes) {
			if (word.equalsIgnoreCase(attribute))
				return true;
		}
		return false;
	}
	
	/**
	 * Check whether scanner's next word contains keywords being used as
	 * patient's attributes.
	 * @param scanner
	 */
	public static boolean scannerHasNextAttributeWord(Scanner scanner) {
		for (String attribute : EMRUtil.attributes) {
			if (scanner.hasNext(attribute))
				return true;
		}
		return false;
	}
}
