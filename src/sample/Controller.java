package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.*;
import javafx.scene.control.CheckBox;
import javafx.scene.text.Font;
import javafx.util.Pair;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

public class Controller implements Initializable {

    @FXML
    private TabPane tabs;
    @FXML
    private FlowPane currentFrame;
    @FXML
    private ImageView addButton;
    @FXML
    private ScrollPane liveViewOne;

    public static Map<StreamGet, Pair<VBox, VBox>> streamNDisplay = new HashMap<>();
    private final ObservableList<Integer> recordDuration = FXCollections.observableArrayList(5,10,15);
    private final BufferedImage addButtonOn = ImageIO.read(getClass().getResource("/resources/images/AddShadow.png"));
    private final BufferedImage addButtonOff = ImageIO.read(getClass().getResource("/resources/images/Add.png"));
    private final BufferedImage deleteIcon = ImageIO.read(getClass().getResource("/resources/images/delete.png"));
    private final BufferedImage settingsIcon = ImageIO.read(getClass().getResource("/resources/images/settings.png"));
    private final BufferedImage upIcon = ImageIO.read(getClass().getResource("/resources/images/up.png"));
    private final BufferedImage downIcon = ImageIO.read(getClass().getResource("/resources/images/down.png"));
    private final BufferedImage leftIcon = ImageIO.read(getClass().getResource("/resources/images/left.png"));
    private final BufferedImage rightIcon = ImageIO.read(getClass().getResource("/resources/images/right.png"));
    ContextMenu context = new ContextMenu();

    static GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    static int width = gd.getDisplayMode().getWidth();
    static int height = gd.getDisplayMode().getHeight();

    boolean toAdd = true;

    public Controller() throws IOException {
    }

    private Pair<VBox,VBox> cameraNLabel(StreamGet stream){
        ImageView display = new ImageView(); //display stream on main page (small, with others)
        ImageView displayCopy = new ImageView(); //display stream on selected page (larger, single)
        displayCopy.setFitWidth(width);
        displayCopy.setFitHeight(height-150);
        StackPane displayNControls = new StackPane();
        StackPane displayNCamControls = new StackPane();
        VBox displayNControlsNLabel = new VBox(); //for main page
        VBox displayNCamControlsNLabel = new VBox(); //for selected page
        Pair<VBox, VBox> cameraNLabelTwice = new Pair<>(displayNControlsNLabel, displayNCamControlsNLabel);

        HBox controlButtons = controlButtons(stream, cameraNLabelTwice);
        HBox camControlButtons = camControlButtons(stream);
        Label label = new Label();
        Label labelCopy = new Label();

        labelCopy.setText(stream.getCameraName());
        label.setText(stream.getCameraName());
        label.setAlignment(Pos.CENTER);
        labelCopy.setAlignment(Pos.CENTER);
        label.setFont(new Font(24));
        labelCopy.setFont(new Font(30));

        display.setFitWidth((double)(width-30)/3);
        display.setFitHeight((3*display.getFitWidth())/4);
        display.setOnMouseEntered(handler -> controlButtons.setVisible(true));
        display.setOnMouseExited(handler -> controlButtons.setVisible(false));

        displayNControls.getChildren().addAll(display, controlButtons);
        displayNControls.setAlignment(Pos.CENTER_RIGHT);
        displayNCamControls.getChildren().addAll(displayCopy, camControlButtons);

        displayNControlsNLabel.getChildren().addAll(displayNControls, label);
        displayNControlsNLabel.setAlignment(Pos.CENTER);
        displayNCamControlsNLabel.getChildren().addAll(displayNCamControls, labelCopy);
        displayNCamControlsNLabel.setAlignment(Pos.CENTER);

        currentFrame.setHgap(5);
        currentFrame.setVgap(5);
        currentFrame.setPrefWrapLength(display.getFitWidth()*3+currentFrame.getHgap()*2);
        label.setOnMouseClicked(handler -> {
            liveViewOne.setContent(displayNCamControlsNLabel);
            tabs.getSelectionModel().select(1);
        });
        return cameraNLabelTwice;
    }

    public void addNewStream(StreamGet stream) {
    //public void addNewStream(String Name, String URL, boolean IsPreviewed, boolean IsRecorded, boolean IsMotionDetected, int RecordLoop) {
        //StreamGet stream = new StreamGet(Name, URL, IsPreviewed, IsRecorded, IsMotionDetected, RecordLoop);
        Pair<VBox,VBox> cameraNLabel = cameraNLabel(stream);
        for(Node stack : cameraNLabel.getKey().getChildren()){
            if(stack instanceof StackPane){
                for(Node display : ((StackPane) stack).getChildren()){
                    if(display instanceof ImageView){
                        for(Node stack2 : cameraNLabel.getValue().getChildren()){
                            if(stack2 instanceof StackPane){
                                for(Node display2 : ((StackPane)stack2).getChildren()){
                                    if(display2 instanceof ImageView) {
                                        try{
                                            if(stream.getIsPreviewed()){
                                                stream.runStream((ImageView) display, (ImageView)display2);
                                                currentFrame.getChildren().add(cameraNLabel.getKey());
                                            }
                                            else{
                                                stream.runStream(null, null);
                                            }
                                            streamNDisplay.put(stream, cameraNLabel);
                                        }catch (Exception e){
                                            Alert alert = new Alert(Alert.AlertType.ERROR);
                                            alert.setTitle("Error!");
                                            alert.setHeaderText("An error occurred: ");
                                            alert.setContentText(e.toString());
                                            alert.showAndWait();
                                            return;
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if(stream.getIsRecorded()){
            try{
                stream.recordStream();
            }catch (Exception e){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error!");
                alert.setHeaderText("An error occurred: ");
                alert.setContentText(e.toString());
                alert.showAndWait();
                return;
            }
        }
        if(stream.getIsMotionDetected()){
            try{
                stream.motionDetect();
            }catch (Exception e){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error!");
                alert.setHeaderText("An error occurred: ");
                alert.setContentText(e.toString());
                alert.showAndWait();
            }
        }
    }

    private HBox camControlButtons(StreamGet stream){
        HBox camControl = new HBox();
        ImageView up = new ImageView(SwingFXUtils.toFXImage(upIcon , null));
        ImageView down = new ImageView(SwingFXUtils.toFXImage(downIcon , null));
        ImageView left = new ImageView(SwingFXUtils.toFXImage(leftIcon , null));
        ImageView right = new ImageView(SwingFXUtils.toFXImage(rightIcon , null));
        camControl.setAlignment(Pos.BOTTOM_LEFT);
        camControl.getChildren().addAll(left, up, down, right);
        up.setOnMouseEntered(handler -> up.setCursor(Cursor.HAND));
        down.setOnMouseEntered(handler -> down.setCursor(Cursor.HAND));
        right.setOnMouseEntered(handler -> right.setCursor(Cursor.HAND));
        left.setOnMouseEntered(handler -> left.setCursor(Cursor.HAND));
        up.setOnMouseClicked(handler -> {
            try {
                stream.getCameraControl().turnCamera(CameraControl.direction.up);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        down.setOnMouseClicked(handler -> {
            try {
                stream.getCameraControl().turnCamera(CameraControl.direction.down);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        left.setOnMouseClicked(handler -> {
            try {
                stream.getCameraControl().turnCamera(CameraControl.direction.left);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        right.setOnMouseClicked(handler -> {
            try {
                stream.getCameraControl().turnCamera(CameraControl.direction.right);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return camControl;
    }

    private HBox controlButtons(StreamGet stream, Pair<VBox,VBox> cameraNLabel){
        ImageView delete = new ImageView(SwingFXUtils.toFXImage(deleteIcon, null));
        ImageView settings = new ImageView(SwingFXUtils.toFXImage(settingsIcon, null));
        HBox streamControl = new HBox();
        streamControl.setAlignment(Pos.TOP_RIGHT);
        streamControl.setVisible(false);
        streamControl.getChildren().addAll(settings, delete);
        streamControl.setOnMouseEntered(handler -> streamControl.setVisible(true));
        streamControl.setOnMouseExited(handler -> streamControl.setVisible(false));
        delete.setOnMouseClicked(handler -> {
            currentFrame.getChildren().remove(cameraNLabel.getKey());
            stream.stopStream();
            stream.stopRecording();
            stream.stopDetecting();

            streamNDisplay.remove(stream, cameraNLabel);
        });
        delete.setOnMouseEntered(handler -> delete.setCursor(Cursor.HAND));
        settings.setOnMouseClicked(handler -> streamSettings(stream, cameraNLabel));
        settings.setOnMouseEntered(handler -> settings.setCursor(Cursor.HAND));
        return streamControl;
    }


    @Override
    public void initialize(URL url, ResourceBundle rb){
        if(new File("save").exists()){
            List<String> load = null;
            try {
                load = Files.readAllLines(Paths.get("save"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(load != null){
                for (String stream : load){
                    String[] split =  stream.split(">");
                    String[] splitControl = stream.split("<");
                    CameraControl cameraControl = null;
                    if(splitControl.length>1){
                        cameraControl = new CameraControl(splitControl[1], splitControl[2], splitControl[3], splitControl[4], splitControl[5]);
                    }
                    StreamGet streamGet = new StreamGet(split[0], split[1], Boolean.parseBoolean(split[2]), Boolean.parseBoolean(split[3]), Boolean.parseBoolean(split[4]), Integer.parseInt(split[5]), cameraControl);
                    addNewStream(streamGet);
                }
            }
        }

        MenuItem showHide = new MenuItem("Show cameras with preview off"); //TODO
        context.getItems().add(showHide);
        showHide.setOnAction((ActionEvent actionEvent) ->{
            for(Map.Entry<StreamGet, Pair<VBox, VBox>> stream : streamNDisplay.entrySet()){
                if(!stream.getKey().getIsPreviewed()){
                    stream.getKey().setIsPreviewed(true);
                    Pair<VBox, VBox> newCameraNLabel = cameraNLabel(stream.getKey());
                    for(Node stack : newCameraNLabel.getKey().getChildren()){
                        if(stack instanceof StackPane){
                            for(Node display : ((StackPane) stack).getChildren()){
                                if(display instanceof ImageView){
                                    for(Node stack2 : newCameraNLabel.getValue().getChildren()){
                                        if(stack2 instanceof StackPane){
                                            for(Node display2 : ((StackPane)stack2).getChildren()){
                                                if(display2 instanceof ImageView) {
                                                    try{
                                                        stream.getKey().stopStream();
                                                        stream.getKey().runStream((ImageView) display, (ImageView)display2);
                                                        currentFrame.getChildren().add(newCameraNLabel.getKey());
                                                    }catch (Exception e){
                                                        Alert alert = new Alert(Alert.AlertType.ERROR);
                                                        alert.setTitle("Error!");
                                                        alert.setHeaderText("An error occurred: ");
                                                        alert.setContentText(e.toString());
                                                        alert.showAndWait();
                                                        return;
                                                    }
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    private void streamSettings(StreamGet stream, Pair<VBox,VBox> cameraNLabel){
        Dialog<String> dialog = new Dialog<>();
        dialog.setWidth(200);
        dialog.setTitle("Edit Dialog");
        dialog.setHeaderText("Edit camera");
        ButtonType addButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        TextField urlField = new TextField();
        TextField nameField = new TextField();
        CheckBox preview = new CheckBox("Enable preview ");
        CheckBox record = new CheckBox("Enable recording ");
        CheckBox detect = new CheckBox("Enable motion detection");
        nameField.setText(stream.getCameraName());
        urlField.setText(stream.getStreamPath());
        urlField.setDisable(true);
        preview.setSelected(stream.getIsPreviewed());
        record.setSelected(stream.getIsRecorded());
        detect.setSelected(stream.getIsMotionDetected());
        ChoiceBox<Integer> duration = new ChoiceBox<>(recordDuration);
        duration.setValue(stream.getRecordLoop());
        duration.setDisable(true);
        CheckBox control = new CheckBox("Enable camera move control");
        TextField controlUrl = new TextField(stream.getCameraControl().getControlUrl());
        controlUrl.setDisable(true);
        TextField upControl = new TextField(stream.getCameraControl().getUpControl());
        upControl.setDisable(true);
        TextField downControl = new TextField(stream.getCameraControl().getDownControl());
        downControl.setDisable(true);
        TextField leftControl = new TextField(stream.getCameraControl().getLeftControl());
        leftControl.setDisable(true);
        TextField rightControl = new TextField(stream.getCameraControl().getRightControl());
        rightControl.setDisable(true);
        control.setOnMouseClicked(handler -> {
            if(control.isSelected()){
                controlUrl.setDisable(false);
                upControl.setDisable(false);
                downControl.setDisable(false);
                leftControl.setDisable(false);
                rightControl.setDisable(false);
            }
            else{
                controlUrl.setDisable(true);
                upControl.setDisable(true);
                downControl.setDisable(true);
                leftControl.setDisable(true);
                rightControl.setDisable(true);
            }
        });

        //popUpConfiguration(urlField, nameField, preview, record, detect, duration, dialog.getDialogPane());
        popUpConfiguration(urlField, nameField, preview, record, detect, duration, dialog.getDialogPane(), controlUrl, upControl, downControl, leftControl, rightControl, control);


        dialog.setResultConverter(button -> {
            if(button == addButtonType){
                return nameField.getText();
            }
            return null;
        });

        Optional <String> result = dialog.showAndWait();

        result.ifPresent(editStream ->{
            if(!stream.getCameraName().equals(nameField.getText())){
                stream.setCameraName(nameField.getText());
                for(Node cameraName : cameraNLabel.getKey().getChildren()){
                    if(cameraName instanceof Label){
                        ((Label) cameraName).setText(nameField.getText());
                        break;
                    }
                }
                for(Node cameraName : cameraNLabel.getValue().getChildren()){
                    if(cameraName instanceof Label){
                        ((Label) cameraName).setText(nameField.getText());
                        break;
                    }
                    //break;
                }
            }
            stream.getCameraControl().setControlUrl(controlUrl.getText());
            stream.getCameraControl().setUpControl(upControl.getText());
            stream.getCameraControl().setDownControl(downControl.getText());
            stream.getCameraControl().setLeftControl(leftControl.getText());
            stream.getCameraControl().setRightControl(rightControl.getText());
            if(stream.getIsPreviewed() != preview.isSelected()){
                if(!preview.isSelected()){
                    currentFrame.getChildren().remove(cameraNLabel.getKey());
                    stream.stopStream();
                    try {
                        stream.runStream(null, null);
                    } catch (Exception e) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error!");
                        alert.setHeaderText("An error occurred: ");
                        alert.setContentText(e.toString());
                        alert.showAndWait();
                        return;
                    }
                }
                    stream.setIsPreviewed(preview.isSelected());
            }
            try {
                stream.setIsMotionDetected(detect.isSelected());
                stream.setIsRecorded(record.isSelected());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void popUpConfiguration(TextField urlField, TextField nameField, CheckBox preview, CheckBox record,
                                           CheckBox detect, ChoiceBox<Integer> duration, DialogPane dialogPane,
                                           TextField controlUrl, TextField upControl, TextField downControl,
                                           TextField leftControl, TextField rightControl, CheckBox control) {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.add(new Label("Name: "), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("URL: "), 0, 1);
        grid.add(urlField, 1, 1);
        grid.add(preview, 0, 2);
        grid.add(record, 0, 3);
        grid.add(detect, 0, 4);
        grid.add(control, 0, 5);
        grid.add(new Label("Record loop duration: (min)"), 0, 6);
        grid.add(duration, 1, 6);
        grid.add(new Label("URL for GET request: "), 0, 7);
        grid.add(controlUrl, 1, 7);
        grid.add(new Label("GET request-go up: "), 0, 8);
        grid.add(upControl, 1, 8);
        grid.add(new Label("GET request-go down: "), 0, 9);
        grid.add(downControl, 1, 9);
        grid.add(new Label("GET request-go left: "), 0, 10);
        grid.add(leftControl, 1, 10);
        grid.add(new Label("GET request-go right: "), 0, 11);
        grid.add(rightControl, 1, 11);
        dialogPane.setContent(grid);
    }

    public void newStreamPopUp() {

        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Add Dialog");
        dialog.setHeaderText("Add new camera to monitor");
        ButtonType addButtonType = new ButtonType("Start", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        TextField urlField = new TextField();
        TextField nameField = new TextField();
        nameField.promptTextProperty().set("Camera name");
        urlField.promptTextProperty().set("Camera URL");
        CheckBox preview = new CheckBox("Enable preview ");
        CheckBox record = new CheckBox("Enable recording ");
        CheckBox detect = new CheckBox("Enable motion detection");
        CheckBox control = new CheckBox("Enable camera move control");
        TextField controlUrl = new TextField();
        controlUrl.setDisable(true);
        TextField upControl = new TextField();
        upControl.setDisable(true);
        TextField downControl = new TextField();
        downControl.setDisable(true);
        TextField leftControl = new TextField();
        leftControl.setDisable(true);
        TextField rightControl = new TextField();
        rightControl.setDisable(true);
        ChoiceBox<Integer> duration = new ChoiceBox<>(recordDuration);
        duration.setValue(5);
        control.setOnMouseClicked(handler -> {
            if(control.isSelected()){
                controlUrl.setDisable(false);
                upControl.setDisable(false);
                downControl.setDisable(false);
                leftControl.setDisable(false);
                rightControl.setDisable(false);
            }
            else{
                controlUrl.setDisable(true);
                upControl.setDisable(true);
                downControl.setDisable(true);
                leftControl.setDisable(true);
                rightControl.setDisable(true);
            }
        });
        popUpConfiguration(urlField, nameField, preview, record, detect, duration, dialog.getDialogPane(), controlUrl, upControl, downControl, leftControl, rightControl, control);

        dialog.setResultConverter(button -> {
            if(button == addButtonType){
                if(urlField.getText().equals("") || nameField.getText().equals("") || nameField.getText().contains("/") ||
                nameField.getText().contains("\\") || nameField.getText().contains(":") || nameField.getText().contains("*") ||
                nameField.getText().contains("?") || nameField.getText().contains("<") || nameField.getText().contains(">") ||
                nameField.getText().contains("|")){
                    toAdd = false;
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Warning!");
                    alert.setHeaderText("Provide valid camera name and url!");
                    alert.setContentText("Camera name or stream url is invalid or contain forbidden characters");
                    alert.showAndWait();
                    return null;
                }
                return new Pair<>(urlField.getText(), nameField.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(newStream ->{
            if(!preview.isSelected()&&!record.isSelected()&&!detect.isSelected()) toAdd=false;
            for (Map.Entry<StreamGet, Pair<VBox,VBox>> stream : streamNDisplay.entrySet()) {
                if(stream.getKey().getStreamPath().equals(urlField.getText())){
                    toAdd = false;
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Warning!");
                    alert.setHeaderText("This camera is already here!");
                    alert.setContentText("Camera name with provided URL: " + stream.getKey().getCameraName());
                    alert.showAndWait();
                    newStreamPopUp();
                }
            }
            if(toAdd) {
                CameraControl cameraControl;
                if(controlUrl.getText().equals("")){
                    cameraControl = null;
                }
                else cameraControl = new CameraControl(controlUrl.getText(), upControl.getText(), downControl.getText(), leftControl.getText(), rightControl.getText());
                StreamGet streamGet = new StreamGet(nameField.getText(), urlField.getText(), preview.isSelected(), record.isSelected(), detect.isSelected(), duration.getValue(), cameraControl);
                try {
                    addNewStream(streamGet);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void addHoverOn() {
        addButton.setImage(SwingFXUtils.toFXImage(addButtonOn, null));
        addButton.setCursor(Cursor.HAND);
    }

    public void addHoverOff() {
        addButton.setImage(SwingFXUtils.toFXImage(addButtonOff, null));
    }

    public void hideShowNotPreviewed(ContextMenuEvent contextMenuEvent) {
        context.show(currentFrame, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
    }
}
