package informatica.unical.it.server;

import informatica.unical.it.model.UserInfo;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class Database
{
    private static Database instance=null;
    private Connection con=null;

    private boolean initialized=false;

    private Database()
    {
        try
        {
            con= DriverManager.getConnection("jdbc:sqlite:Database.db");
            if(con!=null && !con.isClosed())
                System.out.println("Connesso");
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public static Database getInstance()
    {
        if(instance==null)
            instance=new Database();

        return instance;
    }

    public void init() throws SQLException
    {
        if(initialized)
            return;
        initialized = true;

        PreparedStatement p= con.prepareStatement(
                """
                CREATE TABLE IF NOT EXISTS "users" (
                    "email" TEXT NOT NULL UNIQUE,
                    "password" TEXT NOT NULL,
                    "name" TEXT NOT NULL,
                    "surname" NOT NULL,
                    PRIMARY KEY("email")
                );
                """);
        p.execute();

        p=con.prepareStatement(
    """
        CREATE TABLE IF NOT EXISTS "files"
        (
            "emailMittente" TEXT NOT NULL,
            "nomeFile" TEXT NOT NULL,
            "file" BLOB NOT NULL,
            "emailDestinatario" TEXT NOT NULL
        )
        """);
        p.execute();

        p=con.prepareStatement(
                """
                CREATE TABLE IF NOT EXISTS "messages" (
                    "emailMittente" TEXT NOT NULL,
                    "messaggio" TEXT NOT NULL,
                    "time" TEXT NOT NULL,
                    "emailDestinatario" TEXT NOT NULL
                );
                """);
        p.execute();

        p=con.prepareStatement(
                """
                CREATE TABLE IF NOT EXISTS "contacts" (
                    "clientEmail" TEXT NOT NULL,
                    "contactEmail" TEXT NOT NULL
                );
                """);
        p.execute();
    }

    public synchronized boolean insertFile(String emailMitt,String nomeFile,byte[] file,String emailDest) throws SQLException
    {
        if(con == null || con.isClosed())
            return false;

        if(emailMitt.equals(emailDest))
        {
            PreparedStatement p=con.prepareStatement("SELECT* FROM files WHERE emailMittente=? AND emailDestinatario=?");
            p.setString(1,emailMitt);
            p.setString(2,emailDest);
            ResultSet r=p.executeQuery();
            if(r.next())
            {
                p= con.prepareStatement("UPDATE files SET file=? WHERE nomeFile=?");
                p.setBytes(1,file);
                p.setString(2,nomeFile);
                p.executeUpdate();
                return true;
            }
        }


        PreparedStatement p=con.prepareStatement("INSERT INTO files VALUES (?,?,?,?)");
        p.setString(1,emailMitt);
        p.setString(2,nomeFile);
        p.setBytes(3,file);
        p.setString(4,emailDest);
        p.executeUpdate();
        p.close();

        return true;
    }

    public synchronized HashMap<String,byte[]> retrieveFiles(String emailMitt,String nomeFile,String emailDest) throws SQLException
    {
        HashMap<String,byte[]> file=new HashMap<>();
        PreparedStatement p=con.prepareStatement("SELECT* FROM files WHERE emailMittente=? AND emailDestinatario=? AND nomeFile=? OR emailMittente=? AND emailDestinatario=? AND nomeFile=?");
        p.setString(1,emailMitt);
        p.setString(2,emailDest);
        p.setString(3,nomeFile);
        p.setString(4,emailDest);
        p.setString(5,emailMitt);
        p.setString(6,nomeFile);
        ResultSet r=p.executeQuery();

        while(r.next())
            file.put(r.getString("nomeFile"),r.getBytes("file"));
        return file;
    }

    public synchronized boolean insertMessage(String emailMitt,String messaggio,String emailDest) throws SQLException
    {
        if(con == null || con.isClosed())
            return false;

        PreparedStatement p=con.prepareStatement("INSERT INTO messages VALUES(?,?,?,?);");
        p.setString(1,emailMitt);
        p.setString(2,messaggio);
        p.setString(3,new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime()));
        p.setString(4,emailDest);
        p.executeUpdate();
        p.close();
        return true;
    }

    public synchronized String updatePassword(ArrayList<String> passwords) throws SQLException
    {
        PreparedStatement p=con.prepareStatement("SELECT* FROM users WHERE email=?");
        p.setString(1,passwords.get(0));
        ResultSet r=p.executeQuery();
        boolean result = false;
        if(r.next())
        {
            String password = r.getString("password");
            result = BCrypt.checkpw(passwords.get(1), password);
        }

        if(result)
        {
            p=con.prepareStatement("UPDATE users SET password=? WHERE email=?");
            p.setString(1,BCrypt.hashpw(passwords.get(2), BCrypt.gensalt(12)));
            p.setString(2,passwords.get(0));
            p.executeUpdate();
            return "Password changed!";
        }
        return "Current password is not correct!";
    }

    public synchronized ArrayList<String> retrieveMessages(String emailMitt,String emailDest) throws SQLException
    {
        if(con == null || con.isClosed())
            return null;

        ArrayList<String> messages=new ArrayList<>();

        PreparedStatement p=con.prepareStatement("SELECT* FROM messages WHERE emailMittente=? AND emailDestinatario=? OR emailMittente=? and emailDestinatario=?");
        p.setString(1,emailMitt);
        p.setString(2,emailDest);
        p.setString(3,emailDest);
        p.setString(4,emailMitt);
        ResultSet r=p.executeQuery();

        while(r.next())
        {
            messages.add(r.getString("messaggio").concat("@"+r.getString("emailMittente"))+"@"+"^"+r.getString("time")+"^");
        }

        return messages;
    }

    public synchronized boolean login(UserInfo user) throws SQLException
    {
        if(con == null || con.isClosed())
            return false;


            PreparedStatement p = con.prepareStatement("SELECT * FROM users WHERE email=?;");
            p.setString(1, user.getEmail());
            ResultSet r = p.executeQuery();
            boolean result = false;

            if(r.next())
            {
                String password = r.getString("password");
                result = BCrypt.checkpw(user.getPassword(), password);
            }
            p.close();

            return result;
    }

    public synchronized ArrayList<String> fetchUserInfo(String email) throws SQLException
    {
        ArrayList<String> info=new ArrayList<String>();

        PreparedStatement p= con.prepareStatement("Select* FROM users WHERE email=?");
        p.setString(1,email);
        ResultSet r=p.executeQuery();


        info.add(r.getString("name"));
        info.add(r.getString("surname"));

        return info;
    }

    public synchronized boolean insertContact(String clientEmail,String contactEmail) throws SQLException
    {

        if(retrieveContacts(clientEmail).containsKey(contactEmail))
            return false;

        PreparedStatement p=con.prepareStatement("INSERT INTO contacts VALUES (?,?)");
        p.setString(1,clientEmail);
        p.setString(2,contactEmail);
        p.executeUpdate();
        p.close();

        return true;
    }
    public synchronized HashMap<String,ArrayList<String>> retrieveContacts(String clientEmail) throws SQLException
    {
        HashMap<String,ArrayList<String>> contacts=new HashMap<>();

        PreparedStatement p=con.prepareStatement("SELECT* FROM contacts where clientEmail=?");
        p.setString(1,clientEmail);
        ResultSet r=p.executeQuery();
        while(r.next())
            contacts.put(r.getString("contactEmail"),fetchUserInfo(r.getString("contactEmail")));

        return contacts;
    }
    public synchronized boolean register(UserInfo userInfo) throws SQLException
    {
        if(existEmail(userInfo.getEmail()))
            return false;

        PreparedStatement p = con.prepareStatement("INSERT INTO users VALUES(?,?,?,?);");
        p.setString(1, userInfo.getEmail());
        p.setString(2, BCrypt.hashpw(userInfo.getPassword(), BCrypt.gensalt(12)));
        p.setString(3, userInfo.getName());
        p.setString(4, userInfo.getSurname());
        p.executeUpdate();
        p.close();
        return true;
    }

    public synchronized boolean existEmail(String userEmail) throws SQLException
    {
            if(con==null || con.isClosed())
                return false;

        PreparedStatement p=con.prepareStatement("SELECT* FROM users WHERE email=?;");
        p.setString(1,userEmail);
        ResultSet r=p.executeQuery();
        boolean result=r.next();
        p.close();

        return result;
    }
}
