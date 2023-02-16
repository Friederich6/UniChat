package informatica.unical.it.client;

import javafx.application.Application;
import javafx.stage.Stage;

import java.net.URL;

public class MainApplication extends Application
{
    @Override
    public void start(Stage primaryStage) throws Exception
    {
        try
        {
            SceneHandler.getInstance().init(primaryStage);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}


