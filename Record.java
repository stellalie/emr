import java.io.*;
import java.util.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.regex.Pattern;

public class Record
{
   private LinkedList<Patient> patients = new LinkedList<Patient>();

   public Record(File medicalRecordFile, File instructionFile, File outputFile) 
   		throws FileNotFoundException, java.text.ParseException
   {
      LinkedList<Patient> records = createPatientRecord(medicalRecordFile);
      this.executeInstructions(instructionFile, records);
      this.printOutput(outputFile);
      // print();
   }

   private void executeInstructions(File instructionFile, LinkedList<Patient> records) 
   		throws FileNotFoundException, java.text.ParseException
   {
      LinkedList<String[]> instructions = new LinkedList<String[]>();
      Scanner scanner = new Scanner(instructionFile);
      String command = "", data = "";
      while (scanner.hasNext())
      {
         command = scanner.next();
         if (scanner.hasNextLine())
            data = scanner.nextLine();

         this.execute(command, this.readInstructionData(command, data.trim()), records);
         command = "";
         data = "";
      }
   }

   private void execute(String command, Map<String, String> data, LinkedList<Patient> records) 
   		throws java.text.ParseException
   {
      if (command.equals(Command.SAVE))
         executeSave(records);
      else if (command.equals(Command.QUERY))
         executeQuery(data, records);
      else if (command.equals(Command.ADD))
         executeAdd(data, records);
      else if (command.equals(Command.DELETE))
         executeDelete(data, records);
      else
         System.out.println("Invalid command");
   }

   private void executeSave(LinkedList<Patient> records)
   {
      patients.addAll(records);
   }

   private void executeQuery(Map<String, String> instructionData, LinkedList<Patient> records)
   {
   }

   private void executeDelete(Map<String, String> instructionData, LinkedList<Patient> records) 
   		throws java.text.ParseException
   {
   	// delete by id
   	if (instructionData.get(Attribute.PATIENTID) != null) {
   		int id = Integer.parseInt(instructionData.get(Attribute.PATIENTID)); 
   		records.remove(this.findPatient(id, records));
   		
   	// delete by name & birthday
   	} else if (instructionData.get(Attribute.NAME) != null 
   			&& instructionData.get(Attribute.BIRTHDAY) !=null) {
   		String name = instructionData.get(Attribute.NAME);
   		Date birthday = EMRUtil.stringToDate(instructionData.get(Attribute.BIRTHDAY));
   		records.remove(this.findPatient(birthday, records));
   	}
   }

   private void executeAdd(Map<String, String> instructionData, LinkedList<Patient> records) 
   		throws ParseException
   {
   	// TODO: assuming all data correct & exists, might need to check valid/missing data
      Patient tempPatient = createPatient(instructionData);
      Patient patient = findPatient(tempPatient.getName(), tempPatient.getBirthday(), records);
      
      if (patient == null) // Patient does not already exist
         records.add(tempPatient);
      else
      {
         // Patient already exists. 
         EMRUtil.lastUsedId--;

         records.get(records.indexOf(patient)).setPhone(tempPatient.getPhone());
         records.get(records.indexOf(patient)).setAddress(tempPatient.getAddress());
         records.get(records.indexOf(patient)).setEmail(tempPatient.getEmail());
         records.get(records.indexOf(patient)).addDiagnoses(tempPatient.getMedicalHistory());
      }
   }
   
   private Map<String, String> readInstructionData(String command, String data) {
      Scanner scanner = new Scanner(data.trim());
	   scanner.useDelimiter(Pattern.compile(";", Pattern.MULTILINE));
		Map<String, String> attributeValuePairs = new HashMap<String, String>();

		while (scanner.hasNext()) {
			String curr = scanner.next();
			curr = curr.trim();
			String[] pair = curr.split("\\s", 2);
			//System.out.println(pair[0] + " " + pair[1]);
			if (command.equals("save"))
				break;
			if (command.equals("query"))
				break;
			attributeValuePairs.put(pair[0], pair[1]);
		}
		scanner.close();
		return attributeValuePairs;
   }

   private void printOutput(File file)
   {
      if (patients.size() == 0)
         return;
      try
      {
         PrintWriter out = new PrintWriter(file);
         for (Patient p : patients)
         {
            out.println(p.toString());
            out.println();
         }
         out.close();
      } catch (FileNotFoundException e)
      {
         System.out.println("Output file not found!");
      }
   }

   private void print()
   {
      for (Patient p : patients)
      {
         System.out.println(p.toString());
         System.out.println();
      }
   }

   private Patient findPatient(String name, Date birthday, LinkedList<Patient> records)
   {
      LinkedList<Patient> resultByName = this.findPatient(name, records);
      LinkedList<Patient> resultByBirthday = this.findPatient(birthday, records);
      for (Patient p : resultByName)
      {
         if (resultByBirthday.contains(p))
            return p;
      }
      return null;
   }

   private Patient findPatient(int id, LinkedList<Patient> records)
   {
      for (Patient p : records)
      {
         if (id == p.getId())
            return p;
      }
      return null;
   }

   private LinkedList<Patient> findPatient(String name, LinkedList<Patient> records)
   {
      LinkedList<Patient> results = new LinkedList<Patient>();
      for (Patient p : records)
      {
         if (name.equals(p.getName()))
            results.add(p);
      }
      return results;
   }

   private LinkedList<Patient> findPatient(Date birthday, LinkedList<Patient> records)
   {
      LinkedList<Patient> results = new LinkedList<Patient>();
      for (Patient p : records)
      {
         if (birthday.equals(p.getBirthday()))
            results.add(p);
      }
      return results;
   }

   private Patient createPatient(Map<String, String> attributeValuePairs) throws java.text.ParseException
   {
      // Set and validate email, if invalid make null
      String email = attributeValuePairs.get(Attribute.EMAIL);
      if (!EMRUtil.emailIsValid(attributeValuePairs.get(Attribute.EMAIL)))
         email = null;
      // Set and validate phone, if invalid make -1
      int phone = -1;
      if (!EMRUtil.phoneIsValid(attributeValuePairs.get(Attribute.PHONE)))
         phone = -1;
      else
         phone = Integer.parseInt(attributeValuePairs.get(Attribute.PHONE));
      // Set other fields
      String name = attributeValuePairs.get(Attribute.NAME);
      Date birthday = EMRUtil.stringToDate(attributeValuePairs.get(Attribute.BIRTHDAY));
      String address = attributeValuePairs.get(Attribute.ADDRESS);
      LinkedList<Diagnosis> medicalHistory = readMedicalHistory(attributeValuePairs.get(Attribute.MEDICALHISTORY));

      return new Patient(name, birthday, phone, address, email, medicalHistory);
   }

   private boolean validPatientRecord(Map<String, String> attributeValuePairs)
   {
      // Ensure birthday is valid
      if (!EMRUtil.dateIsValid(attributeValuePairs.get(Attribute.BIRTHDAY)))
         return false;
      // Ensure name is valid
      if (!EMRUtil.nameIsValid(attributeValuePairs.get(Attribute.NAME)))
         return false;
      // Ensure name & birthday exist
      if (attributeValuePairs.get(Attribute.NAME) == null || attributeValuePairs.get(Attribute.BIRTHDAY) == null)
         return false;
      return true;
   }

   private LinkedList<Patient> createPatientRecord(File file) throws FileNotFoundException, java.text.ParseException
   {
      /* Scan each record delimited by a blank line */
      Scanner scanner = new Scanner(file).useDelimiter(Pattern.compile("^\\s*$", Pattern.MULTILINE));
      LinkedList<Patient> records = new LinkedList<Patient>();
      while (scanner.hasNext())
      {
         String record = scanner.next().trim();
         Map<String, String> preparedRecord = this.readPatientRecord(record);
         if (this.validPatientRecord(preparedRecord))
            records.add(this.createPatient(preparedRecord));
      }
      scanner.close();
      return records;
   }

   private LinkedList<Diagnosis> readMedicalHistory(String medicalHistory) throws java.text.ParseException
   {
      if (medicalHistory == null)
         return null;
      
      // Handles commas from 'add' instructions
      medicalHistory = medicalHistory.replaceAll(",\\s*", "\n");
      
      LinkedList<Diagnosis> diagnoses = new LinkedList<Diagnosis>();
      Scanner scanner = new Scanner(medicalHistory);

      if (medicalHistory.split("\\s+").length < 2)
         return null;
      while (scanner.hasNextLine())
      {
         String date = "", information = "";
         String[] words = scanner.nextLine().split("\\s+");
         for (int i = 0; i < words.length; i++)
         {
            if (EMRUtil.dateIsValid(words[i]))
               date = words[i];
            else
               information += words[i] + " ";
         }
         diagnoses.add(new Diagnosis(EMRUtil.stringToDate(date.trim()), information.trim()));
      }
      scanner.close();
      return diagnoses;
   }

   private Map<String, String> readPatientRecord(String record)
   {
      Map<String, String> attributeValuePairs = new HashMap<String, String>();
      String attribute = "", value = "";
      Scanner scanner = new Scanner(record);

      while (scanner.hasNextLine())
      {
         if (EMRUtil.scannerHasNextAttributeWord(scanner))
         {
            attribute = scanner.next();
            while (!EMRUtil.scannerHasNextAttributeWord(scanner))
            {
               if (!scanner.hasNext())
                  break;
               value += scanner.nextLine().replaceAll("\\s+", " ").trim() + "\n";
            }
            attributeValuePairs.put(attribute.trim(), value.trim());
            value = "";
         }
      }
      scanner.close();
      return attributeValuePairs;
   }

}
