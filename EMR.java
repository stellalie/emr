import java.io.*;

public class EMR {  
    public static void main(String[] args) throws Exception {
   	 /*
   	 Record record1 = new Record(
             new File(args[0]), 
             new File(args[1]), 
             new File(args[2]),
             new File(args[3]));
   	 */
        Record record1 = new Record(
            new File("samples/records01.txt"), 
            new File("samples/instructions01.txt"), 
            new File("outputs/output01.txt"),
            new File("outputs/report01.txt"));
        Record record2 = new Record(
            new File("samples/records02.txt"), 
            new File("samples/instructions02.txt"), 
            new File("outputs/output02.txt"),
            new File("outputs/report02.txt"));
        Record record3 = new Record(
            new File("samples/records03.txt"), 
            new File("samples/instructions03.txt"), 
            new File("outputs/output03.txt"),
            new File("outputs/report03.txt"));
        Record record4 = new Record(
            new File("samples/records04.txt"), 
            new File("samples/instructions04.txt"), 
            new File("outputs/output04.txt"),
            new File("outputs/report04.txt"));
    }
}
