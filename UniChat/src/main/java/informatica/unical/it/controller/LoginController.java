package informatica.unical.it.controller;

import informatica.unical.it.client.Protocol;
import informatica.unical.it.model.UserInfo;
import informatica.unical.it.client.Client;
import informatica.unical.it.client.SceneHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController
{

    @FXML
    private Button RegisterButton;

    @FXML
    private TextField EmailTextArea;

    @FXML
    private PasswordField PasswordField;

    @FXML
    private Button LoginButton;

    @FXML
    void OnRegisterPressed(ActionEvent event) throws Exception
    {
        SceneHandler.getInstance().setRegisterScene();
    }

    @FXML
    void OnLoginPressed(ActionEvent event) throws Exception
    {
        if(EmailTextArea.getText().isEmpty() || PasswordField.getText().isEmpty())
        {
            SceneHandler.getInstance().showError("You must fill all the camps");
            EmailTextArea.clear();
            PasswordField.clear();
            return;
        }

        String res=Client.getInstance().authenticate(new UserInfo(EmailTextArea.getText(),PasswordField.getText(),"",""),true);
        if(res.equals(Protocol.OK))
        {
            MainInterfaceController.setMyEmail(EmailTextArea.getText());
            SettingsController.setMyEmail(EmailTextArea.getText());
            SceneHandler.getInstance().setChatScene();
        }
        else
        {
            SceneHandler.getInstance().showError(res);
            Client.getInstance().reset();
        }

    }

}
