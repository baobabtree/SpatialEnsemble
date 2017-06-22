import java.io.*;
import java.util.*;

public class FileIO {
	public static void WriteStringToFile(String filename, String content){
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(filename));
            bw.write(content);
            bw.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
	}
}
