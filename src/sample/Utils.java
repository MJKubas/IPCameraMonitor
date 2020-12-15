package sample;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public final class Utils
{
    public static void moveFile(String cameraName) throws IOException {
        File directory = new File(cameraName);
        String workingDir = System.getProperty("user.dir");
        if(!directory.exists())
        {
            directory.mkdir();
        }
        File f = new File(workingDir);
        String[] files = f.list();
        if(files != null){
            for (String file:files) {
                if(file.contains("_" + cameraName + ".avi")){
                    Files.move(Paths.get(file), Paths.get(cameraName + "\\" + file));
                }
            }
        }
    }

    public static <T> void onFXThread(final ObjectProperty<T> property, final T value)
    {
        Platform.runLater(() -> property.set(value));
    }

    public static void saveConfiguration(Map<StreamGet, Pair<VBox, VBox>> streamNDisplay) throws IOException {
        FileWriter save = new FileWriter("save", false);

        for(Map.Entry<StreamGet, Pair<VBox, VBox>> stream : streamNDisplay.entrySet()){
            save.write(stream.getKey().getCameraName() + ">" + stream.getKey().getStreamPath() + ">" +
                    stream.getKey().getIsPreviewed() + ">" + stream.getKey().getIsRecorded() + ">" +
                    stream.getKey().getIsMotionDetected() + ">" + stream.getKey().getRecordLoop());
            if(stream.getKey().getCameraControl() != null){
                save.write("><" + stream.getKey().getCameraControl().getControlUrl() + "<" + stream.getKey().getCameraControl().getUpControl() + "<" +
                stream.getKey().getCameraControl().getDownControl() + "<" + stream.getKey().getCameraControl().getLeftControl() + "<" +
                stream.getKey().getCameraControl().getRightControl());
            }
            save.write("\n");
        }
        save.close();
    }
    public static void loadLibrary() {
        try {
            System.load(System.getProperty("user.dir")+"/opencv_videoio_ffmpeg440_64.dll");
            System.load(System.getProperty("user.dir")+"/opencv_java440.dll");

        } catch (Exception e) {
            throw new RuntimeException("Failed to load opencv native library", e);
        }
    }

}
