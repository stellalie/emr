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
            new File("records01.txt"), 
            new File("instructions01.txt"), 
            new File("out01.txt"),
            new File("rep01.txt"));
        Record record2 = new Record(
            new File("records02.txt"), 
            new File("instructions02.txt"), 
            new File("out02.txt"),
            new File("rep02.txt")); 
        Record record3 = new Record(
            new File("records03.txt"), 
            new File("instructions03.txt"), 
            new File("out03.txt"),
            new File("rep03.txt"));
        Record record4 = new Record(
            new File("records04.txt"), 
            new File("instructions04.txt"), 
            new File("out04.txt"),
            new File("rep04.txt"));
    }
}
