package informatica.unical.it.controller;

import informatica.unical.it.model.Messages;
import informatica.unical.it.client.Protocol;
import informatica.unical.it.model.AllFiles;
import informatica.unical.it.client.Client;
import informatica.unical.it.model.ContactInfo;
import informatica.unical.it.client.SceneHandler;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainInterfaceController
{
    @FXML
    private Text NameLabel;

    @FXML
    private Text SurnameLabel;
    @FXML
    private Button AttachmentButton;

    @FXML
    private VBox Contacts;

    @FXML
    private VBox AllMessagesTextArea;

    @FXML
    private Button SettingsButton;

    @FXML
    private TextField MyTextArea;

    @FXML
    private Button addButton;

    @FXML
    private ScrollPane MessagesSP;

    @FXML
    private Button SendButton;
    @FXML
    private Button ExitButton;
    @FXML
    private HBox right_Hbox;
    @FXML
    private Circle circle;
    @FXML
    private HBox left_hbox;

    private static String emailDest;
    private long orarioPrecedente=0;
    private long frequenza=250*1000000;
    private static String myEmail;
    public static void setMyEmail(String email)
    {
        myEmail=email;
    }
    public static String getMyEmail()
    {
        return myEmail;
    }
    @FXML private void initialize()
    {
        Platform.runLater(() ->
        {
            timer.start();

            Thread t=new Thread(Client.getInstance());
            t.setDaemon(true);
            t.start();
            ContactInfo.getInstance().UpdateProfileImages(myEmail,new ImageView(new Image("Images/DefaultUserImage.png")));
            Client.getInstance().sendMessageToServer(Protocol.FETCH_INFO);
            Client.getInstance().sendMessageToServer(Protocol.RETRIEVE_CONTACTS);
            Client.getInstance().sendMessageToServer(Protocol.RETRIEVE_FILE);
            Client.getInstance().sendMessageToServer(myEmail);
            Client.getInstance().sendMessageToServer("profile");
            Client.getInstance().sendMessageToServer(myEmail);
            sleep(100L);

            HashMap<String, ArrayList<String>> contact = ContactInfo.getInstance().getAllMyContacts();
            for (Map.Entry<String, ArrayList<String>> entry : contact.entrySet())
                Contacts.getChildren().add(createContact(entry.getKey(), entry.getValue()));

            AllMessagesTextArea.heightProperty().addListener(new ChangeListener<Number>()
            {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
                {
                    MessagesSP.setVvalue((double) newValue);
                }
            });

            NameLabel.setText(ContactInfo.getInstance().getMyName());
            SurnameLabel.setText(ContactInfo.getInstance().getMySurname());

            circle.setFill(new ImagePattern(ContactInfo.getInstance().getProfileImages().get(myEmail).getImage()));
        });
    }
    @FXML
    void OnSettingsClicked(ActionEvent event) throws Exception
    {
        SceneHandler.getInstance().setSettingsScene();
    }

    @FXML
    void OnExitClicked(ActionEvent event) throws Exception
    {
        Platform.exit();
    }

    @FXML
    void OnAttachmentClicked(ActionEvent event) throws IOException
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Documents and Images", "*.pdf","*.docx","*.jpeg","*.jpg","*.png");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(null);
        if(file!=null)
        {
            byte[] pdf = Files.readAllBytes(file.toPath());
            Client.getInstance().sendMessageToServer(Protocol.INSERT_FILE);
            Client.getInstance().sendMessageToServer(emailDest);
            Client.getInstance().sendMessageToServer(file.getName());
            Client.getInstance().sendMessageToServer(pdf);
            MyTextArea.setText(" ");
            send(file.getName());
        }
    }

    @FXML
    void OnSendClicked(ActionEvent event)
    {
        send(MyTextArea.getText());
    }

    @FXML
    void OnAddClicked(ActionEvent event)
    {
        TextInputDialog t = new TextInputDialog();
        t.setHeight(200);
        t.setWidth(200);
        t.setTitle("User search");
        t.setHeaderText("Insert the email of the person you want to add.");
        t.setContentText("Email: ");
        t.showAndWait();
        if(t.getResult()!=null)
        {
            if(ContactInfo.getInstance().getAllMyContacts().get(t.getResult())!=null)
            {
                SceneHandler.getInstance().showError("Already in your contact list!");
                return;
            }
            Client.getInstance().sendMessageToServer(Protocol.INSERT_CONTACT);
            Client.getInstance().sendMessageToServer(t.getResult());
            Client.getInstance().sendMessageToServer(Protocol.RETRIEVE_CONTACTS);
            sleep(100L);
            if(ContactInfo.getInstance().getAllMyContacts().get(t.getResult())!=null)
                Contacts.getChildren().add(createContact(t.getResult(),ContactInfo.getInstance().getLatestContact(t.getResult())));
            else
                SceneHandler.getInstance().showError("The person you are trying to add does not exists!");
        }
    }

    private void sleep(Long time)
    {
        try
        {
            TimeUnit.MILLISECONDS.sleep(time);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }
    private void send(String message)
    {
        if(!emailDest.equals("") && !MyTextArea.getText().isEmpty())
        {
            Client.getInstance().sendMessageToServer(Protocol.INSERT);
            Client.getInstance().sendMessageToServer(emailDest);
            Client.getInstance().sendMessageToServer(message);
            MyTextArea.clear();
        }
    }
    private HBox createContact(String email,ArrayList<String> nameAndSurname)
    {
        HBox contact=new HBox();
        contact.setAlignment(Pos.CENTER_LEFT);
        contact.setPadding(new Insets(3,0,3,120));

        Text text=new Text(nameAndSurname.get(0)+" "+nameAndSurname.get(1));
        text.setStyle("-fx-font-size: 17");



        text.setFill(Color.color(0.5,0.5,0.5));

        Client.getInstance().sendMessageToServer(Protocol.RETRIEVE_FILE);
        Client.getInstance().sendMessageToServer(email);
        Client.getInstance().sendMessageToServer("profile");
        Client.getInstance().sendMessageToServer(email);
        Circle c=new Circle(23);
        sleep(200L);
        if(ContactInfo.getInstance().getProfileImages().get(email)==null)
            c.setFill(new ImagePattern(new Image("Images/DefaultUserImage.png")));
        else
            c.setFill(new ImagePattern(ContactInfo.getInstance().getProfileImages().get(email).getImage()));

        contact.getChildren().add(c);


        contact.getChildren().add(text);
        contact.setId(email);
        contact.setMinWidth(200);
        contact.setMinHeight(60);
        contact.setOnMouseClicked(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent event)
            {
                right_Hbox.getChildren().clear();
                emailDest=contact.getId();
                System.out.println(emailDest);
                Messages.getInstance().clear();
                AllMessagesTextArea.getChildren().clear();
                Text t=new Text(nameAndSurname.get(0)+" "+nameAndSurname.get(1));
                t.setFill(Color.color(1,1,1));
                t.setStyle("-fx-font: 15 regular;");
                right_Hbox.setPadding(new Insets(right_Hbox.getHeight()/2,0,0,right_Hbox.getWidth()/2-15));
                right_Hbox.getChildren().add(t);
            }
        });
        shadow(contact);
        return contact;
    }

    private void shadow(Node node)
    {
        node.setOnMouseEntered(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent event)
            {
                DropShadow dropShadow=new DropShadow(BlurType.ONE_PASS_BOX,Color.color(0.85, 0.80, 0.70),20,0,0,0);
                node.setEffect(dropShadow);
            }
        });
        node.setOnMouseExited(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent event) {
                node.setEffect(null);
            }
        });
    }

    private void addMessage(Text t) throws IOException
    {
        HBox hBox=new HBox();
        Pattern pattern=Pattern.compile("\\^(\\d.*)\\^");
        Matcher matcher = pattern.matcher(t.getText());
        Text time=new Text();
        if(matcher.find())
            time.setText("  "+matcher.group(1));

        time.setStyle("-fx-font: 10 system");

        TextFlow textFlow=new TextFlow(t);
        textFlow.getChildren().add(time);
        t.setStyle("-fx-font: 16 arial;");
        hBox.setPadding(new Insets(5,5,5,10));
        textFlow.setPadding(new Insets(10,10,5,10));

        if(t.getText().contains("@"+myEmail+"@"))
        {
            t.setText(t.getText().replace("@"+myEmail+"@"+"^"+matcher.group(1)+"^",""));
            hBox.setAlignment(Pos.CENTER_LEFT);

            textFlow.setStyle("-fx-color:rgb(255,255,255);"+"-fx-background-color: rgb(255,255,255);"+"-fx-background-radius: 10px;");
        }
        else
        {
            t.setText(t.getText().replace("@"+emailDest+"@"+"^"+matcher.group(1)+"^",""));
            hBox.setAlignment(Pos.CENTER_RIGHT);

            textFlow.setStyle("-fx-color:rgb(255,255,255);"+"-fx-background-color: rgb(233, 75, 74);"+"-fx-background-radius: 10px;");
        }

        if(t.getText().endsWith(".pdf") || t.getText().endsWith(".docx") || t.getText().endsWith(".jpg") || t.getText().endsWith(".jpeg") || t.getText().endsWith(".png"))
        {
            Image img=new Image(Objects.requireNonNull(getClass().getResource("/Images/File1.png")).openStream());
            ImageView imageView=new ImageView(img);
            hBox.getChildren().add(imageView);
            shadow(hBox);

                hBox.setOnMouseClicked(new EventHandler<MouseEvent>()
                {
                    @Override
                    public void handle(MouseEvent event)
                    {
                        Client.getInstance().sendMessageToServer(Protocol.RETRIEVE_FILE);
                        Client.getInstance().sendMessageToServer(t.getText());
                        Client.getInstance().sendMessageToServer(emailDest);
                        HashMap<String,byte[]> files=AllFiles.getInstance().getAllMyFiles();
                        try
                        {
                            FileChooser fileChooser=new FileChooser();
                            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
                            fileChooser.setInitialFileName(t.getText());
                            int index = t.getText().lastIndexOf('.');
                            String extension = t.getText().substring(index + 1);
                            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(t.getText(), extension);
                            fileChooser.getExtensionFilters().add(extFilter);
                            fileChooser.setTitle("Save file");
                            File file=fileChooser.showSaveDialog(null);
                            if(file!=null)
                            {
                                OutputStream out = new FileOutputStream(file);
                                out.write(files.get(t.getText()));
                                out.close();
                            }
                        }
                        catch (IOException e)
                        {
                            throw new RuntimeException(e);
                        }
                    }
                });
        }

        hBox.getChildren().add(textFlow);
        hBox.setMinHeight(50);
        AllMessagesTextArea.getChildren().add(hBox);
        AllMessagesTextArea.getChildren().add(new Text(System.lineSeparator()));
    }

    AnimationTimer timer=new AnimationTimer()
    {
        @Override
        public void handle(long now)
        {
            if (now - orarioPrecedente >= frequenza)
            {
                Client.getInstance().sendMessageToServer(Protocol.RETRIEVE_MESSAGES);
                Client.getInstance().sendMessageToServer(emailDest);
                for(String s : Messages.getInstance().downloadNewMessages())
                {
                    Text t=new Text(s);
                    try
                    {
                        addMessage(t);
                    }
                    catch (IOException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
                orarioPrecedente = now;
            }
        }
    };
}
