package informatica.unical.it.model;

import java.io.Serializable;

public class UserInfo implements Serializable
{
    private String email;
    private String password;
    private String name;
    private String surname;

    public UserInfo(String email,String password,String name,String surname)
    {
        this.email=email;
        this.password=password;
        this.name=name;
        this.surname=surname;
    }

    public String getEmail()
    {
        return email;
    }
    public String getName()
    {
        return name;
    }
    public String getSurname()
    {
        return surname;
    }
    public String getPassword()
    {
        return password;
    }
}
