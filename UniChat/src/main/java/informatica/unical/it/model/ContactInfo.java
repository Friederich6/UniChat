package informatica.unical.it.model;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class ContactInfo
{
    private String myName;
    private String mySurname;
    private HashMap<String,ImageView> profileImages =new HashMap<>();
    private String changePassword;
    private HashMap<String,ArrayList<String>> allMyContacts=new HashMap<>();
    private static ContactInfo instance=null;

    public static ContactInfo getInstance()
    {
        if(instance==null)
            instance=new ContactInfo();

        return instance;
    }

    public void UpdateProfileImages(String email, ImageView image)
    {
        profileImages.put(email,image);
    }

    public HashMap<String,ImageView> getProfileImages()
    {
        return profileImages;
    }

    public void updateMyNameAndSurname(ArrayList<String> databaseArray)
    {
        myName =databaseArray.get(0);
        mySurname =databaseArray.get(1);
    }
    public void updateMyContacts(HashMap<String,ArrayList<String>> databaseContacts)
    {
        allMyContacts.putAll(databaseContacts);
    }

    public HashMap<String,ArrayList<String>> getAllMyContacts()
    {
        return allMyContacts;
    }

    public ArrayList<String> getLatestContact(String email)
    {
        return allMyContacts.get(email);
    }

    public String getMyName()
    {
        return myName;
    }

    public String getMySurname()
    {
        return mySurname;
    }

    public void setChangePassword(String changePassword)
    {
        this.changePassword=changePassword;
    }

    public String getChangePassword()
    {
        return  changePassword;
    }
}
