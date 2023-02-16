package informatica.unical.it.client;

import informatica.unical.it.model.AllFiles;
import informatica.unical.it.model.ContactInfo;
import informatica.unical.it.model.Messages;
import informatica.unical.it.model.UserInfo;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Client implements Runnable
{
    private static Client instance=null;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Client()
    {
        try
        {
            socket=new Socket("localhost",8080);
            out=new ObjectOutputStream(socket.getOutputStream());
        }
        catch (IOException e)
        {
            e.printStackTrace();
            out=null;
        }
    }


    public static Client getInstance()
    {
        if(instance==null)
            instance=new Client();

        return instance;
    }

    public void reset()
    {
        instance = null;
        out = null;
        in = null;
        socket = null;
    }

    public String authenticate(UserInfo user,boolean login)
    {
        if(login)
        {
            sendMessageToServer(Protocol.LOGIN);
        }
        else
        {
            sendMessageToServer(Protocol.REGISTER);
        }

        sendMessageToServer(user);

        try
        {
            this.in = new ObjectInputStream(socket.getInputStream());
            return (String)in.readObject();
        }
        catch(Exception e)
        {
            out=null;
            e.printStackTrace();
            return Protocol.ERROR;
        }
    }

    @Override
    public void run()
    {
        while(out != null && in != null)
        {
            try
            {
                String comando=(String)in.readObject();

                if(comando.equals(Protocol.FETCH_INFO))
                {
                    ArrayList<String> data=(ArrayList<String>)in.readObject();
                    ContactInfo.getInstance().updateMyNameAndSurname(data);
                }
                else if(comando.equals(Protocol.RETRIEVE_CONTACTS))
                {
                    ContactInfo.getInstance().updateMyContacts((HashMap<String,ArrayList<String>>) in.readObject());
                }
                else if(comando.equals(Protocol.RETRIEVE_FILE))
                {
                    String nomeFile=(String)in.readObject();
                    String email=(String)in.readObject();
                    HashMap<String,byte[]> file=(HashMap<String, byte[]>) in.readObject();
                    if(nomeFile.equals("profile") && !file.isEmpty())
                    {
                        ContactInfo.getInstance().UpdateProfileImages(email,new ImageView(new Image(new ByteArrayInputStream(file.get(nomeFile)))));
                    }
                    else
                        AllFiles.getInstance().updateMyFiles(file);
                }
                else if(comando.equals(Protocol.CHANGE_PASSWORD))
                {
                    ContactInfo.getInstance().setChangePassword((String)in.readObject());
                }
                else
                    Messages.getInstance().updateMessages((ArrayList<String>) in.readObject());
            }
            catch (ClassNotFoundException e)
            {
                e.printStackTrace();
                out = null;
            }
            catch(RuntimeException e)
            {
                e.printStackTrace();
                out = null;
            }
            catch(IOException e)
            {
                e.printStackTrace();
                out = null;
            }
        }
    }

    public boolean sendMessageToServer(Object message)
    {
        if(out==null)
            return false;

        try
        {
            out.writeObject(message);
            out.flush();
        }
        catch (IOException e)
        {
            out=null;
            return false;
        }
        return false;
    }
}
