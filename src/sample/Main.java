package sample;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

public class Main extends Application {
    private final BufferedImage icon = ImageIO.read(getClass().getResource("/resources/images/icon.png"));

    public Main() throws IOException {
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setTitle("IP Camera monitor");
        primaryStage.getIcons().add(SwingFXUtils.toFXImage(icon, null));
        primaryStage.setMaximized(true);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() throws IOException {
        for (Map.Entry<StreamGet, Pair<VBox, VBox>> stream : Controller.streamNDisplay.entrySet()) {
            stream.getKey().stopStream();
            stream.getKey().stopRecording();
            stream.getKey().stopDetecting();
        }
        Utils.saveConfiguration(Controller.streamNDisplay);
    }

    public static void main(String[] args) {
        Utils.loadLibrary();
        launch(args);
    }
}
