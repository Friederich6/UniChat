package informatica.unical.it.server;

import informatica.unical.it.client.Protocol;
import informatica.unical.it.model.UserInfo;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ClientToServerMessages implements Runnable
{
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String email;
    public ClientToServerMessages(Socket socket) throws IOException
    {
        this.socket = socket;
        this.out = new ObjectOutputStream(socket.getOutputStream());
    }

    private void closeStreams() throws IOException
    {
        if (out != null)
            out.close();
        out=null;

        if (in != null)
            in.close();
        in=null;

        if(socket!=null)
            socket.close();
        socket=null;
    }

    @Override
    public void run()
    {
        try
        {
            this.in = new ObjectInputStream(socket.getInputStream());

                    String input = (String) in.readObject();
                    if(input.equals(Protocol.REGISTER))
                    {
                        UserInfo user = (UserInfo) in.readObject();
                        if(!Database.getInstance().register(user))
                        {
                            sendMessage(Protocol.USERS_EXISTS);
                            closeStreams();
                            return;
                        }
                        email=user.getEmail();
                    }
                    else if(input.equals(Protocol.LOGIN))
                    {
                        UserInfo user=(UserInfo) in.readObject();
                        if(!Database.getInstance().login(user))
                        {
                            sendMessage(Protocol.PASSWORD_INCORRECT);
                            closeStreams();
                            return;
                        }
                        email= user.getEmail();
                    }
                    else
                    {
                        sendMessage(Protocol.ERROR);
                        closeStreams();
                        return;
                    }

                    sendMessage(Protocol.OK);

                    while(true)
                    {
                        String comando=(String)in.readObject();
                        if(comando.equals(Protocol.FETCH_INFO))
                        {
                            sendMessage(Protocol.FETCH_INFO);
                            sendMessage(Database.getInstance().fetchUserInfo(email));
                        }
                        else if(comando.equals(Protocol.INSERT_CONTACT))
                        {
                            String contactEmail=(String) in.readObject();
                            if(Database.getInstance().existEmail(contactEmail))
                                Database.getInstance().insertContact(email,contactEmail);
                        }
                        else if(comando.equals(Protocol.INSERT_FILE))
                        {
                            String dest=(String)in.readObject();
                            String nomeFile=(String)in.readObject();
                            byte[] b=(byte[])in.readObject();
                            Database.getInstance().insertFile(email,nomeFile,b,dest);
                        }
                        else if(comando.equals(Protocol.RETRIEVE_CONTACTS))
                        {
                            HashMap<String,ArrayList<String>> contacts=Database.getInstance().retrieveContacts(email);
                            if(!contacts.isEmpty())
                            {
                                sendMessage(Protocol.RETRIEVE_CONTACTS);
                                sendMessage(contacts);
                            }
                        }
                        else if(comando.equals(Protocol.RETRIEVE_FILE))
                        {
                            String emailMitt=(String)in.readObject();
                            String nomeFile=(String) in.readObject();
                            String emailDest=(String) in.readObject();
                            HashMap<String,byte[]> file=Database.getInstance().retrieveFiles(emailMitt,nomeFile,emailDest);

                            sendMessage(Protocol.RETRIEVE_FILE);
                            sendMessage(nomeFile);
                            sendMessage(emailMitt);
                            sendMessage(file);

                        }
                        else if(comando.equals(Protocol.CHANGE_PASSWORD))
                        {
                            ArrayList<String> data=(ArrayList<String>) in.readObject();
                            sendMessage(Protocol.CHANGE_PASSWORD);
                            sendMessage(Database.getInstance().updatePassword(data));
                        }
                        else
                        {
                            String dest=(String) in.readObject();
                            if(comando.equals(Protocol.RETRIEVE_MESSAGES))
                            {
                                sendMessage("");
                                sendMessage(Database.getInstance().retrieveMessages(email,dest));
                            }
                            else
                            {
                                String mess=(String) in.readObject();
                                Database.getInstance().insertMessage(email,mess,dest);
                            }
                        }
                    }
                }
        catch (Exception e)
        {
            sendMessage(Protocol.ERROR);
            out = null;
        }
    }

    public void sendMessage(Object message)
    {
        if (out == null)
            return;
        try {
            out.writeObject(message);
            out.flush();
        }
        catch (IOException e)
        {
            out=null;
            e.printStackTrace();
        }
    }
}
