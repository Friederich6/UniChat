package informatica.unical.it.model;

import java.util.ArrayList;

public class Messages
{
    private ArrayList<String> messages=new ArrayList<String>();
    private static Messages instance=null;
    private ArrayList<String> newMessages=new ArrayList<String>();

    public static Messages getInstance()
    {
        if(instance==null)
            instance=new Messages();

        return instance;
    }
    public synchronized void updateMessages(ArrayList<String> databaseMessages)
    {
        if(!databaseMessages.equals(messages))
        {
            for(int i=messages.size();i<databaseMessages.size();++i)
                newMessages.add(databaseMessages.get(i));

            messages.addAll(newMessages);
        }
    }
    public synchronized ArrayList<String> downloadNewMessages()
    {
        ArrayList<String> tmp=new ArrayList<String>();
        tmp.addAll(newMessages);
        newMessages.clear();
        return tmp;
    }

    public void clear()
    {
        messages.clear();
        newMessages.clear();
    }
}
