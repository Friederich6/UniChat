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

public class RegisterController {

    @FXML
    private Button RegisterButton;

    @FXML
    private TextField SurnameTextField;

    @FXML
    private PasswordField PasswordTextArea;

    @FXML
    private TextField NameTextField;

    @FXML
    private Button LoginButton;

    @FXML
    private TextField EmailTextField;


    @FXML
    void OnRegisterPressed(ActionEvent event) throws Exception
    {
        if(PasswordTextArea.getText().length()<8)
        {
            SceneHandler.getInstance().showError("The password must be at least 8 characters");
            PasswordTextArea.clear();
            return;
        }

        String res=Client.getInstance().authenticate(new UserInfo(EmailTextField.getText(),PasswordTextArea.getText(),NameTextField.getText(),SurnameTextField.getText()),false);
        if(res.equals(Protocol.OK))
        {
            Client.getInstance().reset();
            SceneHandler.getInstance().setLoginScene();
        }

        else
            SceneHandler.getInstance().showError(res);
    }

    @FXML
    void OnLoginPressed(ActionEvent event) throws Exception
    {
        SceneHandler.getInstance().setLoginScene();
    }
}
