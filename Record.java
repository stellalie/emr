import java.io.*;
import java.util.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.regex.Pattern;

public class Record {
	private LinkedList<Patient> patients = new LinkedList<Patient>();
	private File reportFile;
	private int lastUsedId = 0;

	public Record(File medicalRecordFile, File instructionFile,
			File outputFile, File reportFile) throws FileNotFoundException,
			java.text.ParseException {
		LinkedList<Patient> records = createPatientRecord(medicalRecordFile);
		if (reportFile.exists())
			reportFile.delete();
		this.reportFile = reportFile;
		this.executeInstructions(instructionFile, records);
		this.printOutput(outputFile);
	}

	private void executeInstructions(File instructionFile,
			LinkedList<Patient> records) throws FileNotFoundException,
			java.text.ParseException {
		Scanner scanner = new Scanner(instructionFile);
		String command = "", data = "";
		while (scanner.hasNext()) {
			command = scanner.next();
			if (scanner.hasNextLine())
				data = scanner.nextLine();

			this.execute(command, data.trim(),
					this.readInstructionData(command, data.trim()), records);
			command = "";
			data = "";
		}
	}

	private void execute(String command, String rawData,
			Map<String, String> data, LinkedList<Patient> records)
			throws java.text.ParseException {
		if (command.equals(Command.SAVE)) executeSave(records);
		else if (command.equals(Command.QUERY)) executeQuery(rawData, data, records);
		else if (command.equals(Command.ADD)) executeAdd(data, records);
		else if (command.equals(Command.DELETE)) executeDelete(data, records);
		else System.out.println("Invalid command!");
	}

	private void executeSave(LinkedList<Patient> records) {
		patients.addAll(records);
	}

	private void executeQuery(String rawData,
			Map<String, String> instructionData, LinkedList<Patient> records)
			throws java.text.ParseException {
		
		// Query by name
		if (instructionData.get(Attribute.NAME) != null)
			this.appendQueryResult(this.findPatient(
					instructionData.get(Attribute.NAME), records), rawData);
 		
		// Query by birthday
		if (instructionData.get(Attribute.BIRTHDAY) != null) {
			Date birthday = EMRUtil.stringToDate(instructionData
					.get(Attribute.BIRTHDAY));
			this.appendQueryResult(this.findPatient(birthday, records), rawData);
		}
		
		// Query by id
		if (instructionData.get(Attribute.PATIENTID) != null) {
			int id = Integer.parseInt(instructionData.get(Attribute.PATIENTID));
			if (this.findPatient(id, records) != null) {
				LinkedList<Patient> results = new LinkedList<Patient>();
				System.out.println();
				results.add(this.findPatient(id, records));
				this.appendQueryResult(results, rawData);
			}
		}
	}

	private void appendQueryResult(LinkedList<Patient> results, String rawData) {
		try {
			PrintWriter out = new PrintWriter(new FileWriter(this.reportFile,
					true));
			out.println("---------------------  " + "query " + rawData
					+ "  ---------------------");
			out.println();
			for (Patient p : results) {
				out.println(p.toString());
			}
			out.println();
			out.println("---------------------- end of query ----------------------");
			out.println();
			out.println();
			out.close();
		} catch (Exception e) {
			System.out.println("Report file not found!");
		}
	}

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
			System.out.println(attributeValuePairs);
		}
		scanner.close();
		return attributeValuePairs;
	}

	private void printOutput(File file) {
		if (patients.size() == 0)
			return;
		try {
			PrintWriter out = new PrintWriter(file);
			for (Patient p : patients) {
				out.println(p.toString());
				out.println();
			}
			out.close();
		} catch (FileNotFoundException e) {
			System.out.println("Output file not found!");
		}
	}

	private void print() {
		for (Patient p : patients) {
			System.out.println(p.toString());
			System.out.println();
		}
	}

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

	private Patient findPatient(int id, LinkedList<Patient> records) {
		for (Patient p : records) {
			if (id == p.getId())
				return p;
		}
		return null;
	}

	private LinkedList<Patient> findPatient(String name,
			LinkedList<Patient> records) {
		LinkedList<Patient> results = new LinkedList<Patient>();
		for (Patient p : records) {
			if (name.equals(p.getName()))
				results.add(p);
		}
		return results;
	}

	private LinkedList<Patient> findPatient(Date birthday,
			LinkedList<Patient> records) {
		LinkedList<Patient> results = new LinkedList<Patient>();
		for (Patient p : records) {
			if (birthday.equals(p.getBirthday()))
				results.add(p);
		}
		return results;
	}

	private Patient createPatient(Map<String, String> attributeValuePairs)
			throws java.text.ParseException {
		
		// Set fields with assumptions data is valid
		String email = EMRUtil.validEmail(attributeValuePairs.get(Attribute.EMAIL));
		int phone = EMRUtil.validPhone(attributeValuePairs.get(Attribute.PHONE));
		String name = attributeValuePairs.get(Attribute.NAME);
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

	private LinkedList<Patient> createPatientRecord(File file)
			throws FileNotFoundException, java.text.ParseException {
		
		// Scan each record delimited by a blank line
		Scanner scanner = new Scanner(file).useDelimiter(Pattern.compile(
				"^\\s*$", Pattern.MULTILINE));
		LinkedList<Patient> records = new LinkedList<Patient>();
		while (scanner.hasNext()) {
			String record = scanner.next().trim();
			Map<String, String> preparedRecord = this.readPatientRecord(record);
			if (this.validPatientRecord(preparedRecord))
				records.add(this.createPatient(preparedRecord));
		}
		scanner.close();
		return records;
	}
	
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
