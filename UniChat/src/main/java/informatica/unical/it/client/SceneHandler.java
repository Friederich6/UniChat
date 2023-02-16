package informatica.unical.it.client;

import informatica.unical.it.controller.MainInterfaceController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class SceneHandler
{
    private Scene scene;
    private Stage stage;

    private Scene scene2;
    private Stage stage2=new Stage();
    private boolean isLight=false;

    private static SceneHandler instance = null;

    public static SceneHandler getInstance()
    {
        if(instance == null)
            instance = new SceneHandler();

        return instance;
    }

    public void closeSettings()
    {
        stage2.close();
    }

    public void init(Stage primaryStage) throws Exception
    {
        stage = primaryStage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
        Parent root = (Parent) loader.load();
        scene = new Scene(root, 450, 200);
        stage.setScene(scene);
        stage.setTitle("UniChat");
        stage.setResizable(false);
        Theme(scene);
        stage.show();
    }

    public void setSettingsScene() throws Exception
    {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Settings.fxml"));
        Parent root=loader.load();
        scene2=new Scene(root,600,440);
        stage2.setScene(scene2);
        stage2.setMinWidth(600);
        stage2.setMinHeight(440);
        stage2.setWidth(600);
        stage2.setHeight(440);
        stage2.setResizable(false);
        Theme(scene);
        Theme(scene2);
        stage2.show();
    }

    private void Theme(Scene scene)
    {
        if (isLight)
        {
            scene.getStylesheets().remove(getClass().getResource("/CSS/Dark.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/CSS/Light.css").toExternalForm());
        }
        else
        {
            scene.getStylesheets().remove(getClass().getResource("/CSS/Light.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/CSS/Dark.css").toExternalForm());
        }
    }

    public boolean isLight()
    {
        return isLight;
    }

    public void setLoginScene() throws Exception
    {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
        scene.setRoot(loader.load());
        stage.setResizable(false);
        stage.setWidth(450);
        stage.setHeight(240);
        Theme(scene);
        stage.show();
    }

    public void setChatScene() throws Exception
    {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mainInterface.fxml"));
        scene.setRoot(loader.load());
        stage.setResizable(true);
        stage.setMinHeight(700);
        stage.setMinWidth(1200);
        stage.setWidth(1200);
        stage.setHeight(700);
        Theme(scene);
        stage.show();
    }


    public void setRegisterScene() throws Exception
    {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Register.fxml"));
        scene.setRoot(loader.load());
        stage.setResizable(false);
        stage.setWidth(500);
        stage.setHeight(360);
        Theme(scene);
        stage.show();
    }

    public void setLightMode() throws Exception
    {
        isLight=true;
        SceneHandler.getInstance().setSettingsScene();
    }
    public void setDarkMode() throws Exception
    {
        isLight=false;
        SceneHandler.getInstance().setSettingsScene();
    }

    public void showError(String message)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("");
        alert.setContentText(message);
        alert.show();
    }
}