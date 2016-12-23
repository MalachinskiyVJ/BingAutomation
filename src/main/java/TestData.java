
import java.io.*;
import java.util.HashMap;

public class TestData {

    private String fileLocation;

    public TestData(String fileLocation) {

        this.fileLocation = fileLocation;
    }

    public HashMap<String,String[]> getData(){
        FileInputStream fs;
        HashMap<String,String[]> keyValuePair=new HashMap<String,String[]>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileLocation))){
            String stringLine;

            while ((stringLine = br.readLine()) != null)   {

                String[] keyValue=stringLine.split("=");
                keyValuePair.put(keyValue[0],keyValue[1].split(","));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return keyValuePair;
    }


}
