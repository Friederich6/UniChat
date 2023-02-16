package informatica.unical.it.model;

import java.util.HashMap;
import java.util.Map;

public class AllFiles
{
    private HashMap<String, byte[]> allMyFiles =new HashMap<>();
    private static AllFiles instance=null;

    public static AllFiles getInstance()
    {
        if(instance==null)
            instance=new AllFiles();
        return instance;
    }
    public void updateMyFiles(HashMap<String,byte[]> databaseFiles)
    {
        for(Map.Entry<String,byte[]> entry:databaseFiles.entrySet())
            allMyFiles.put(entry.getKey(),entry.getValue());

    }
    public HashMap<String,byte[]> getAllMyFiles()
    {
        return allMyFiles;
    }

}
