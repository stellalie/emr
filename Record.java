import java.io.*;
import java.util.*;
import java.text.ParseException;
import java.util.regex.Pattern;

/**
 * This class read, load and process medical record file, executes instructions
 * on instruction file, and print the output. Query results are stored as a
 * report file.
 * @author VG
 *
 */

public class Record {
	private LinkedList<Patient> patients = new LinkedList<Patient>();
	private int lastUsedId = 0;
	
	/**
	 * This is a class constructors for report. Upon creation, it read, load, and
	 * process medical record file, executes instructions on instruction file and
	 * print the output. Query results are stored on a report file.
	 * @param medicalRecordFile
	 * @param instructionFile
	 * @param outputFile
	 * @param reportFile
	 * @throws FileNotFoundException
	 * @throws java.text.ParseException
	 */
	public Record(File medicalRecordFile, File instructionFile,
			File outputFile, File reportFile) throws FileNotFoundException,
			java.text.ParseException {
		LinkedList<Patient> records = createPatientRecord(medicalRecordFile);
		if (reportFile.exists()) reportFile.delete();
		this.executeInstructions(instructionFile, reportFile, records);
		this.printOutput(outputFile);
	}
	
	/**
	 * Parse instruction file to a set of command data pairs then execute them.
	 * @param instructionFile File that contains instructions
	 * @param reportFile File to write query results to
	 * @param records List of Patient the instructions are ran against
	 * @throws FileNotFoundException
	 * @throws java.text.ParseException
	 */
	private void executeInstructions(File instructionFile, File reportFile,
			LinkedList<Patient> records) throws FileNotFoundException,
			java.text.ParseException {
		Scanner scanner = new Scanner(instructionFile);
		String command = "", data = "";
		while (scanner.hasNext()) {
			command = scanner.next();
			if (scanner.hasNextLine()) data = scanner.nextLine();
			this.execute(command,
					this.readInstructionData(command, data.trim()), 
					reportFile, records);
			command = "";
			data = "";
		}
	}
	
	/**
	 * Execute commands given a command data pairs set. E.g: command would be
	 * "delete" and data pairs set {name=Jeff Vader, birthday=08=09=1901}
	 * @param command
	 * @param data
	 * @param reportFile
	 * @param records
	 * @throws java.text.ParseException
	 */
	private void execute(String command, Map<String, String> data, 
			File reportFile, LinkedList<Patient> records)
			throws java.text.ParseException {
		if (command.equals(Command.SAVE)) executeSave(records);
		else if (command.equals(Command.QUERY)) executeQuery(data, reportFile, records);
		else if (command.equals(Command.ADD)) executeAdd(data, records);
		else if (command.equals(Command.DELETE)) executeDelete(data, records);
		else System.out.println("Invalid command!");
	}

	private void executeSave(LinkedList<Patient> records) {
		patients.addAll(records);
	}
	
	/**
	 * Perform query by patient name or birthday, patient ID then append the result
	 * to report file. All query results are displayed in ascending order or patient
	 * name and birthday.
	 * @param instructionData
	 * @param reportFile
	 * @param records
	 * @throws java.text.ParseException
	 */
	private void executeQuery(Map<String, String> instructionData, File reportFile,
			LinkedList<Patient> records) throws java.text.ParseException {
		
		// Query by name
		if (instructionData.get(Attribute.NAME) != null)
			this.appendQueryResult(this.findPatient(
					instructionData.get(Attribute.NAME), records), 
					reportFile, instructionData);
 		
		// Query by birthday
		if (instructionData.get(Attribute.BIRTHDAY) != null) {
			Date birthday = EMRUtil.stringToDate(instructionData
					.get(Attribute.BIRTHDAY));
			this.appendQueryResult(this.findPatient(birthday, records), 
					reportFile, instructionData);
		}
		
		// Query by id
		if (instructionData.get(Attribute.PATIENTID) != null) {
			int id = Integer.parseInt(instructionData.get(Attribute.PATIENTID));
			if (this.findPatient(id, records) != null) {
				LinkedList<Patient> results = new LinkedList<Patient>();
				System.out.println();
				results.add(this.findPatient(id, records));
				this.appendQueryResult(results, reportFile, instructionData);
			}
		}
	}
	
	/**
	 * Append a formatted string to existing report file
	 * @param results
	 * @param reportFile
	 * @param instructionData
	 */
	private void appendQueryResult(LinkedList<Patient> results, File reportFile,
			Map<String, String> instructionData) {
		try {
			PrintWriter out = new PrintWriter(new FileWriter(reportFile, true));
			out.print(this.getQueryResult(results, instructionData));
			out.close();
		} catch (Exception e) {
			System.out.println("Report file not found!");
		}
	}
	
	/**
	 * Get a formatted string ready for printing given a list of patients result
	 * and instructions. All query results are displayed in ascending order or patient
	 * name and birthday. Generate header and footer.
	 * @param results a list patient as a result of querying
	 * @param instructionData instructions used to generate the results
	 * @throws ParseException
	 */
	private String getQueryResult(LinkedList<Patient> results, 
			Map<String, String> instructionData) throws ParseException {
		String s = "";
		
		// Sort patient results
		this.sortPatients(results);
		
		// Build result string if there are date limits on medical history
		if ((instructionData.get("start") != null) && (instructionData.get("end") != null)) {
			Date start = EMRUtil.stringToDate(instructionData.get("start"));
			Date end = EMRUtil.stringToDate(instructionData.get("end"));
			// Print only if date limits are valid and end date is not earlier than start date
			if (start.before(end)) {
				s += this.getQueryResultHeader(instructionData) + "\n";
				for (Patient p : results) s += p.toString(start, end) + "\n";
				s += this.getQueryResultFooter(instructionData) + "\n";
			}
			
		// Build result string if no date limits on medical history
		} else {
			s += this.getQueryResultHeader(instructionData) + "\n";
			for (Patient p : results) s += p.toString() + "\n";
			s += this.getQueryResultFooter(instructionData) + "\n";
		}
		return s;
	}
	
	private String getQueryResultHeader(Map<String, String> instructionData) {
		String instruction = "";
		
		// build query command string
		if (instructionData.get(Attribute.PATIENTID) != null) 
			instruction += "patient " 
						+ Integer.parseInt(instructionData.get(Attribute.PATIENTID));
		if (instructionData.get(Attribute.NAME) != null) 
			instruction += Attribute.NAME + " " 
						+ instructionData.get(Attribute.NAME);
		if (instructionData.get(Attribute.BIRTHDAY) != null) 
			instruction += Attribute.BIRTHDAY + " " 
						+ instructionData.get(Attribute.BIRTHDAY);
		if (instructionData.get("start") != null && instructionData.get("start") != null) 
			instruction += "; " + instructionData.get("start")
						+ "; " + instructionData.get("end")
						+ "; ";
		String s = "";
		s += "---------------------  " + "query " + instruction 
				+ "  ---------------------\n";
		return s;
	}
	
	private String getQueryResultFooter(Map<String, String> instructionData) {
		String s = "";
		s += "--------------------- End of Query -----------------------------\n";
		s += " \n";
		s += " \n";
		s += " \n";
		return s;
	}
	
	/**
	 * Delete a record from given list by name and birthday OR by patient ID
	 * @param instructionData
	 * @param records
	 * @throws java.text.ParseException
	 */
	private void executeDelete(Map<String, String> instructionData,
			LinkedList<Patient> records) throws java.text.ParseException {
		
		// Delete by id
		if (instructionData.get(Attribute.PATIENTID) != null) {
			int id = Integer.parseInt(instructionData.get(Attribute.PATIENTID));
			records.remove(this.findPatient(id, records));
		
		// Delete by name & birthday
		} else if (instructionData.get(Attribute.NAME) != null
				&& instructionData.get(Attribute.BIRTHDAY) != null) {
			String name = instructionData.get(Attribute.NAME);
			Date birthday = EMRUtil.stringToDate(instructionData
					.get(Attribute.BIRTHDAY));
			records.remove(this.findPatient(name, birthday, records));
		}
	}
	
	/**
	 * Execute "add" command to given Patient list. If both patient name and 
	 * birthday are identical to those of an existing record in the list, 
	 * the existing record will be updated with the new input information. Otherwise
	 * a new valid medical record will be added to the list
	 * @param instructionData
	 * @param records
	 * @throws ParseException
	 */
	private void executeAdd(Map<String, String> instructionData,
			LinkedList<Patient> records) throws ParseException {
		String name = instructionData.get(Attribute.NAME);
		Date birthday = EMRUtil.stringToDate(instructionData.get(Attribute.BIRTHDAY));
		int phone = EMRUtil.validPhone(instructionData.get(Attribute.PHONE));
		String address = instructionData.get(Attribute.ADDRESS);
		String email = EMRUtil.validEmail(instructionData.get(Attribute.EMAIL));
		String medicalHistory = instructionData.get(Attribute.MEDICALHISTORY);
		
		Patient patient = findPatient(name, birthday, records);
		// Patient does not already exist, create and patient to existing record
		if (patient == null) {
			if (this.validPatientRecord(instructionData)) records.add(this.createPatient(instructionData));
		// Patient already exists, update existing record
		} else {
			if (phone != -1) patient.setPhone(phone);
			if (address != null) patient.setAddress(address);
			if (email != null) patient.setEmail(email);
			if (medicalHistory != null) patient.addDiagnoses(this.readMedicalHistory(medicalHistory));
		}
	}
	
	/**
	 * Return set of command data pairs given command (raw) and data (raw).
	 * @param command
	 * @param data
	 * @return command data pairs, e.g: {"name", "Mary Beor"}
	 */
	private Map<String, String> readInstructionData(String command, String data) {
		if (command.equals(Command.SAVE)) return null;
		
		Scanner scanner = new Scanner(data.trim());
		scanner.useDelimiter(Pattern.compile(";", Pattern.MULTILINE));
		Map<String, String> attributeValuePairs = new HashMap<String, String>();
		
		// store attributes for add and delete commands
		if (command.equals(Command.ADD) || command.equals(Command.DELETE)) {
			while (scanner.hasNext()) { 
				String[] pair = scanner.next().trim().split("\\s", 2);
				attributeValuePairs.put(pair[0], pair[1]);
			}
		}
		
		// store query name, birthday, start date and end date
		if (command.equals(Command.QUERY)) {
			// store name or birthday
			if (scanner.hasNext()) {
				String[] pair = scanner.next().trim().split("\\s", 2);
				attributeValuePairs.put(pair[0], pair[1]);
			}
			if (scanner.hasNext()) attributeValuePairs.put("start", scanner.next().trim());
			if (scanner.hasNext()) attributeValuePairs.put("end", scanner.next().trim());
		}
		scanner.close();
		return attributeValuePairs;
	}
	
	/**
	 * Print EMR record's list of patients to file
	 * @param outputFile
	 */
	private void printOutput(File outputFile) {
		if (patients.size() == 0)
			return;
		try {
			PrintWriter out = new PrintWriter(outputFile);
			for (Patient p : patients) {
				out.println(p.toString());
				out.println();
			}
			out.close();
		} catch (FileNotFoundException e) {
			System.out.println("Output file not found!");
		}
	}
	
	/**
	 * Find patient by name and birthday given a list of patients
	 * @param name
	 * @param birthday
	 * @param records
	 * @return
	 */
	private Patient findPatient(String name, Date birthday,
			LinkedList<Patient> records) {
		LinkedList<Patient> resultByName = this.findPatient(name, records);
		LinkedList<Patient> resultByBirthday = this.findPatient(birthday,
				records);
		for (Patient p : resultByName) {
			if (resultByBirthday.contains(p))
				return p;
		}
		return null;
	}
	
	/**
	 * Find patient by id given a list of patients
	 * @param id
	 * @param records
	 * @return
	 */
	private Patient findPatient(int id, LinkedList<Patient> records) {
		for (Patient p : records) {
			if (id == p.getId())
				return p;
		}
		return null;
	}
	
	/**
	 * Find patient by name given a list of patients
	 * @param name
	 * @param records
	 * @return
	 */
	private LinkedList<Patient> findPatient(String name,
			LinkedList<Patient> records) {
		LinkedList<Patient> results = new LinkedList<Patient>();
		for (Patient p : records) {
			if (name.equals(p.getName()))
				results.add(p);
		}
		return results;
	}
	
	/**
	 * Find patient by birthday given a list of patients
	 * @param birthday
	 * @param records
	 * @return
	 */
	private LinkedList<Patient> findPatient(Date birthday,
			LinkedList<Patient> records) {
		LinkedList<Patient> results = new LinkedList<Patient>();
		for (Patient p : records) {
			if (birthday.equals(p.getBirthday()))
				results.add(p);
		}
		return results;
	}
	
	private LinkedList<Patient> sortPatients(LinkedList<Patient> records) {
		this.sortPatientsByName(records);
		this.sortPatientsByBirthday(records);
		return records;
	}
	
	/**
	 * Sort patients by name (ascending; a to z) using simple insertion sort algorithm
	 * @param records
	 * @return sorted Patient list
	 */
	private LinkedList<Patient> sortPatientsByName(LinkedList<Patient> records) {
		int i, j, iMin;
		// Advance the position through the entire array
		for (j = 0; j < records.size() - 1; j++) {
			// Find the min element in the unsorted records.get(j .. records.size())
			// Assume the min is the first element
			iMin = j;
			// Test against element after j to find the smallest
			for (i = j + 1; i < records.size(); i ++) {
				// Compare Patient's name first character
				if (records.get(i).getName().charAt(0) < records.get(iMin).getName().charAt(0))
					// Found new minimum; remember its index
					iMin = i;
			}
			// iMin is the index of the minimum element. Swap it with current position
			if (iMin != j) {
				Patient temp = records.get(j);
				records.set(j, records.get(iMin));
				records.set(iMin, temp);
			}
		}
		return records;
	}
	
	/**
	 * Sort patients by birthday (ascending; earliest to latest) using simple 
	 * insertion sort algorithm.
	 * @param records
	 * @return sorted Patient list
	 */
	private LinkedList<Patient> sortPatientsByBirthday(LinkedList<Patient> records) {
		int i, j, iMin;
		// Advance the position through the entire array
		for (j = 0; j < records.size() - 1; j++) {
			// Find the min element in the unsorted records.get(j .. records.size())
			// Assume the min is the first element
			iMin = j;
			// Test against element after j to find the smallest
			for (i = j + 1; i < records.size(); i ++) {
				// Compare Patient's birthday
				if (records.get(i).getBirthday().before(records.get(iMin).getBirthday()))
					// Found new minimum; remember its index
					iMin = i;
			}
			// iMin is the index of the minimum element. Swap it with current position
			if (iMin != j) {
				Patient temp = records.get(j);
				records.set(j, records.get(iMin));
				records.set(iMin, temp);
			}
		}
		return records;
	}
	
	/**
	 * Create and return a Patient object given attribute value pairs set. With the
	 * assumption that data pairs are valid.
	 * @param attributeValuePairs
	 * @return
	 * @throws java.text.ParseException
	 */
	private Patient createPatient(Map<String, String> attributeValuePairs)
			throws java.text.ParseException {
		
		// Set fields with assumptions data is valid
		String email = EMRUtil.validEmail(attributeValuePairs.get(Attribute.EMAIL));
		int phone = EMRUtil.validPhone(attributeValuePairs.get(Attribute.PHONE));
		String name = EMRUtil.validName(attributeValuePairs.get(Attribute.NAME));
		Date birthday = EMRUtil.stringToDate(attributeValuePairs
				.get(Attribute.BIRTHDAY));
		String address = attributeValuePairs.get(Attribute.ADDRESS);
		LinkedList<Diagnosis> medicalHistory = readMedicalHistory(attributeValuePairs
				.get(Attribute.MEDICALHISTORY));
		
		// Update lastUsedId count and return the new patient
		this.lastUsedId++;
		return new Patient(this.lastUsedId, name, birthday, phone, address, email,
				medicalHistory);
	}
	
	/**
	 * Return a list of Patient given a record file. Patient data are separated
	 * by a blank line. Patients are created only if record data is valid.
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 * @throws java.text.ParseException
	 */
	private LinkedList<Patient> createPatientRecord(File file)
			throws FileNotFoundException, java.text.ParseException {
		
		// Scan each record delimited by a blank line
		Scanner scanner = new Scanner(file).useDelimiter(Pattern.compile(
				"^\\s*$", Pattern.MULTILINE));
		LinkedList<Patient> records = new LinkedList<Patient>();
		while (scanner.hasNext()) {
			String record = scanner.next().trim();
			Map<String, String> preparedRecord = this.readPatientRecord(record);
			
			// create and add patient if data is valid
			if (this.validPatientRecord(preparedRecord))
				records.add(this.createPatient(preparedRecord));
		}
		scanner.close();
		return records;
	}
	
	/**
	 * Determine whether a record is valid given a set of value pairs. A record is
	 * valid is birthday and name are not empty and valid.
	 * @param attributeValuePairs
	 */
	private boolean validPatientRecord(Map<String, String> attributeValuePairs) {
		
		// Ensure birthday is valid
		if (!EMRUtil.dateIsValid(attributeValuePairs.get(Attribute.BIRTHDAY)))
			return false;
		
		// Ensure name is valid
		if (!EMRUtil.nameIsValid(attributeValuePairs.get(Attribute.NAME)))
			return false;
		
		// Ensure name & birthday exist
		if (attributeValuePairs.get(Attribute.NAME) == null
				|| attributeValuePairs.get(Attribute.BIRTHDAY) == null)
			return false;
		return true;
	}
	
	/**
	 * Read a string of medical history and convert them to
	 * LinkedList<Diagnosis>, separated with either a newline or a comma
	 * @param medicalHistory
	 * @return LinkedList<Diagnosis>
	 * @throws java.text.ParseException
	 */
	private LinkedList<Diagnosis> readMedicalHistory(String medicalHistory)
			throws java.text.ParseException {
		if (medicalHistory == null) return null;
		if (medicalHistory.split("\\s+").length < 2) return null;
		
		// Handles commas from 'add' instructions
		medicalHistory = medicalHistory.replaceAll(",\\s*", "\n");
		
		LinkedList<Diagnosis> diagnoses = new LinkedList<Diagnosis>();
		Scanner scanner = new Scanner(medicalHistory);
		while (scanner.hasNextLine()) {
			String date = "", information = "";
			String[] words = scanner.nextLine().split("\\s+");
			for (int i = 0; i < words.length; i++) {
				if (EMRUtil.dateIsValid(words[i]))
					date = words[i];
				else
					information += words[i] + " ";
			}
			diagnoses.add(new Diagnosis(EMRUtil.stringToDate(date.trim()),
					information.trim()));
		}
		scanner.close();
		return diagnoses;
	}
	
	/**
	 * Read a chunk of patient record and convert them to attribute value pairs
	 * (pairs are in String) to be processed later. Value is a string following
	 * an attribute keyword.
	 * @param record
	 * @return attribute value pairs, e.g: {"name", "John Smith"}
	 */
	private Map<String, String> readPatientRecord(String record) {
		Map<String, String> attributeValuePairs = new HashMap<String, String>();
		String attribute = "", value = "";
		Scanner scanner = new Scanner(record);

		while (scanner.hasNextLine()) {
			if (EMRUtil.scannerHasNextAttributeWord(scanner)) {
				attribute = scanner.next();
				while (!EMRUtil.scannerHasNextAttributeWord(scanner)) {
					if (!scanner.hasNext())
						break;
					value += scanner.nextLine().replaceAll("\\s+", " ").trim()
							+ "\n";
				}
				attributeValuePairs.put(attribute.trim(), value.trim());
				value = "";
			}
		}
		scanner.close();
		return attributeValuePairs;
	}
}
