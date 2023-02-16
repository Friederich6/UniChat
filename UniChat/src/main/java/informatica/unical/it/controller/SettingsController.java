package informatica.unical.it.controller;

import informatica.unical.it.client.Protocol;
import informatica.unical.it.client.Client;
import informatica.unical.it.client.SceneHandler;
import informatica.unical.it.model.ContactInfo;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class SettingsController
{

    @FXML
    private Button DeleteAccountButton;

    @FXML
    private ToggleGroup tg;

    @FXML
    private RadioButton LightRadio;

    @FXML
    private Button ThemeButton;

    @FXML
    private ImageView ProfileImage;

    @FXML
    private RadioButton DarkRadio;

    @FXML
    private Button ChangeImageButton;

    @FXML
    private Button BackButton;

    @FXML
    private Button AccountButton;

    @FXML
    private AnchorPane AccountPane;

    @FXML
    private AnchorPane ThemePane;

    @FXML
    private Button ChangePasswordButton;
    @FXML
    private PasswordField CurrentPassword;
    @FXML
    private PasswordField NewPassword;

    private static String myEmail;
    public static void setMyEmail(String myEmail)
    {
        SettingsController.myEmail =myEmail;
    }
    @FXML private void initialize()
    {
        Platform.runLater(() ->
        {
            if(ContactInfo.getInstance().getProfileImages().get(myEmail)==null)
                ProfileImage.setImage(new Image("Images/DefaultUserImage.png"));
            else
                ProfileImage.setImage(ContactInfo.getInstance().getProfileImages().get(myEmail).getImage());
        });
    }
    @FXML
    void OnAccountPressed(ActionEvent event)
    {
        ThemePane.setVisible(false);
        AccountPane.setVisible(true);
    }

    @FXML
    void OnThemeClicked(ActionEvent event)
    {
        ThemePane.setVisible(true);
        AccountPane.setVisible(false);
    }

    @FXML
    void OnChangePasswordClicked(ActionEvent event) throws InterruptedException
    {
        if (NewPassword.getText().isEmpty() || CurrentPassword.getText().isEmpty())
        {
            SceneHandler.getInstance().showError("You must fill both camps!");
            return;
        }
        if(NewPassword.getText().equals(CurrentPassword.getText()))
        {
            SceneHandler.getInstance().showError("Current password and new password can't be the same!");
            return;
        }
        ArrayList<String> passwords=new ArrayList<>();
        passwords.add(myEmail);
        passwords.add(CurrentPassword.getText());
        passwords.add(NewPassword.getText());
        Client.getInstance().sendMessageToServer(Protocol.CHANGE_PASSWORD);
        Client.getInstance().sendMessageToServer(passwords);

        TimeUnit.MILLISECONDS.sleep(1000L);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText("");
        alert.setContentText(ContactInfo.getInstance().getChangePassword());
        alert.show();
    }

    @FXML
    void OnDeleteAccountClicked(ActionEvent event) {

    }

    @FXML
    void OnChangeButtonPressed(ActionEvent event) throws IOException
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.jpg");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(null);
        if(file!=null)
        {
            byte[] image = Files.readAllBytes(file.toPath());
            Client.getInstance().sendMessageToServer(Protocol.INSERT_FILE);
            Client.getInstance().sendMessageToServer(myEmail);
            Client.getInstance().sendMessageToServer("profile");
            Client.getInstance().sendMessageToServer(image);
            ProfileImage.setImage(new Image(new ByteArrayInputStream(image)));
        }
    }

    @FXML
    void OnLightRadioCLicked(ActionEvent event) throws Exception
    {
        SceneHandler.getInstance().setLightMode();
    }

    @FXML
    void OnDarkRadioClicked(ActionEvent event) throws Exception
    {
        SceneHandler.getInstance().setDarkMode();
    }

    @FXML
    void OnBackClicked(ActionEvent event)
    {
        SceneHandler.getInstance().closeSettings();
    }

}
