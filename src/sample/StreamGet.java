package sample;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;



public class StreamGet {

    StreamGet(String Name, String Path, boolean IsPreviewed, boolean IsRecorded, boolean IsMotionDetected, int RecordLoop, CameraControl CAmeraControl){
        this.cameraName = Name;
        this.streamPath = Path;
        this.isMotionDetected = IsMotionDetected;
        this.isPreviewed = IsPreviewed;
        this.isRecorded = IsRecorded;
        this.recordLoop = RecordLoop;
        this.cameraControl = CAmeraControl;
    }
    private CameraControl cameraControl;
    public CameraControl getCameraControl(){return cameraControl;}
    public void setCameraControl(CameraControl control){this.cameraControl = control;}

    private final int recordLoop;
    public int getRecordLoop(){return recordLoop; }

    private boolean isPreviewed;
    public boolean getIsPreviewed(){return isPreviewed;}
    public void setIsPreviewed(boolean value) {
        this.isPreviewed = value;
    }

    private boolean isRecorded;
    public boolean getIsRecorded(){return isRecorded;}
    public void setIsRecorded(boolean value) throws Exception {
        if(this.isRecorded != value){
            this.isRecorded = value;
            if(value){
                recordStream();
            }
            else stopRecording();
        }
    }

    private boolean isMotionDetected;
    public boolean getIsMotionDetected(){return isMotionDetected;}
    public void setIsMotionDetected(boolean value) {
        if(this.isMotionDetected != value){
            this.isMotionDetected = value;
            if(value){
                motionDetect();
            }
            else stopDetecting();
        }
    }

    private String cameraName;
    public String getCameraName(){
        return cameraName;
    }
    public void setCameraName(String value){
        this.cameraName = value;
    }

    private final String streamPath;
    public String getStreamPath(){
        return streamPath;
    }

    private Size frameSize;
    private LocalDateTime today = LocalDateTime.now();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    //-------------CAPTURING-----------------
    private ScheduledExecutorService captureTimer;
    private final VideoCapture capture = new VideoCapture();
    private final Mat frame = new Mat();
    private int counterCapture = 0;
    private Image imageToShow = null;
    private final MatOfByte matOfByte = new MatOfByte();
    //-------------RECORDING-----------------
    private final VideoWriter videoWriter = new VideoWriter();
    private String fileName;
    private ScheduledExecutorService recordTimer;
    private ScheduledExecutorService cutTimer;
    //-------------MOTION DETECT-------------
    private ScheduledExecutorService detectTimer;
    private ScheduledExecutorService detectRecordTimer;
    private final VideoWriter detectWriter = new VideoWriter();
    private String detectFileName;
    private boolean recordON = false;
    private final Size blurSize = new Size(8,8);
    private final Mat workImg = new Mat();
    private Mat movingAvgImg = null;
    private final Mat scaleImg = new Mat();
    private final Mat gray = new Mat();
    private final Mat diffImg = new Mat();
    private double motionPercent = 0.0;


    public void runStream(ImageView currentFrame, ImageView currentFrameCopy) throws Exception {
        this.capture.open(streamPath);

        if (this.capture.isOpened()) {
            this.frameSize = new Size((int) capture.get(Videoio.CAP_PROP_FRAME_WIDTH), (int) this.capture.get(Videoio.CAP_PROP_FRAME_HEIGHT));

            Runnable frameGrabber = () -> {
                this.capture.read(frame);
                if(frame.width() == 0 && frame.height() == 0){
                    counterCapture++;
                    if(counterCapture > 10) {
                        this.capture.release();
                        this.capture.open(streamPath);
                        counterCapture = 0;
                    }
                }
                else {
                    if(currentFrame != null && currentFrameCopy != null){
                        imageToShow = matToImage(frame);
                        updateImageView(currentFrame,currentFrameCopy, imageToShow);
                    }
                }
            };
            this.captureTimer = Executors.newSingleThreadScheduledExecutor();
            this.captureTimer.scheduleAtFixedRate(frameGrabber, 0, 40, TimeUnit.MILLISECONDS);
        }
        else {
            // log the error
            throw new Exception("Cannot open the camera connection...");
        }
    }

    public void recordStream() throws Exception {
        today = LocalDateTime.now();
        fileName = today.format(formatter) + "_" + cameraName + ".avi";
        this.videoWriter.open(fileName, VideoWriter.fourcc('D','I','V','X'), 25, this.frameSize, true);

        if(this.videoWriter.isOpened()){

            Runnable frameRecorder = () -> {

                try{
                    if(!this.videoWriter.isOpened()) {
                        today = LocalDateTime.now();
                        fileName = today.format(formatter) + "_" + cameraName + ".avi";
                        this.videoWriter.open(fileName, VideoWriter.fourcc('D','I','V','X'), 25, this.frameSize, true);
                    }
                    this.videoWriter.write(frame);
                }
                catch (Exception e) {
                    System.err.println("Error occurred while recording: " + e);
                }
            };

            this.cutTimer = Executors.newSingleThreadScheduledExecutor();
            this.recordTimer = Executors.newSingleThreadScheduledExecutor();

            this.cutTimer.scheduleAtFixedRate(() -> {
                this.videoWriter.release();
                try {
                    Utils.moveFile(cameraName);
                } catch (IOException e) {
                    e.printStackTrace();
                } }, recordLoop, recordLoop, TimeUnit.MINUTES);
            this.recordTimer.scheduleAtFixedRate(frameRecorder, 0, 40, TimeUnit.MILLISECONDS);
        }
        else {
            // log the error
            throw new Exception("Cannot record camera feed...");
        }
    }

    public void motionDetect(){
        final double totalPixels = frameSize.area();
        if(capture.isOpened()){
            Runnable detect = () -> {
                try{
                    // Generate work image by blurring
                    Imgproc.blur(frame, workImg, blurSize);
                    // Generate moving average image if needed
                    if (movingAvgImg == null) {
                        movingAvgImg = new Mat();
                        workImg.convertTo(movingAvgImg, CvType.CV_32F);
                    }
                    // Generate moving average image
                    Imgproc.accumulateWeighted(workImg, movingAvgImg, .03);
                    // Convert the scale of the moving average
                    Core.convertScaleAbs(movingAvgImg, scaleImg);
                    // Subtract the work image frame from the scaled image average
                    Core.absdiff(workImg, scaleImg, diffImg);
                    // Convert the image to grayscale
                    Imgproc.cvtColor(diffImg, gray, Imgproc.COLOR_BGR2GRAY);
                    // Convert to BW
                    Imgproc.threshold(gray, gray, 25, 255, Imgproc.THRESH_BINARY);
                    // Total number of changed motion pixels
                    motionPercent = 100.0 * Core.countNonZero(gray) / totalPixels;
                    // Detect if camera is adjusting and reset reference if more than 25%
                    if (motionPercent > 25.0) {
                        workImg.convertTo(movingAvgImg, CvType.CV_32F);
                    }
                    // Threshold trigger motion
                    if (motionPercent > 0.75 && !recordON) {
                        recordON = true;
                        detectRecordTimer.schedule(() -> {
                            recordON = false;
                            detectWriter.release();
                            try {
                                Utils.moveFile(cameraName + "MD");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }, 30, TimeUnit.SECONDS);

                        today = LocalDateTime.now();
                        detectFileName = today.format(formatter) + "_" + cameraName + "MD.avi";
                        detectWriter.open(detectFileName, VideoWriter.fourcc('D','I','V','X'), 25, frameSize, true);
                    }
                    if(recordON){
                        detectWriter.write(frame);
                    }
                }
                catch (Exception e){
                    System.err.println("Cannot perform motion detection..." + e);
                }
            };
            this.detectRecordTimer = Executors.newSingleThreadScheduledExecutor();
            this.detectTimer = Executors.newSingleThreadScheduledExecutor();
            this.detectTimer.scheduleAtFixedRate(detect, 0, 40, TimeUnit.MILLISECONDS);
        }
    }

    public void stopStream() {
        if (!this.captureTimer.isShutdown()) {
            try {
                // stop the timer
                this.captureTimer.shutdown();
                this.captureTimer.awaitTermination(40, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e) {
                // log any exception
                System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
            }
        }

        if (this.capture.isOpened()) {
            // release the camera
            this.capture.release();
        }
    }

    public void stopDetecting(){
        if (this.detectTimer!=null && !this.detectTimer.isShutdown()) {
            try {
                // stop the timer
                this.detectTimer.shutdown();
                this.detectTimer.awaitTermination(1, TimeUnit.SECONDS);
            }
            catch (InterruptedException e) {
                // log any exception
                System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
            }
        }
        if(this.detectRecordTimer!=null && !this.detectRecordTimer.isShutdown()){
            // stop the timer
            this.detectRecordTimer.shutdown();
        }

        if(this.detectWriter.isOpened()){
            this.detectWriter.release();
        }
    }

    public void stopRecording(){
        if (this.recordTimer!=null && !this.recordTimer.isShutdown()) {
            try {
                // stop the timer
                this.cutTimer.shutdown();
                this.recordTimer.shutdown();
                this.recordTimer.awaitTermination(40, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e) {
                // log any exception
                System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
            }
        }

        if (this.videoWriter.isOpened()) {
            // release the camera
            this.videoWriter.release();
            try {
                Utils.moveFile(cameraName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateImageView(ImageView view, ImageView view2, Image image) {
        Utils.onFXThread(view.imageProperty(), image);
        Utils.onFXThread(view2.imageProperty(), image);
    }

    private Image matToImage(Mat frame) {
            try {
                return SwingFXUtils.toFXImage(matToBufferedImage(frame), null);
            }
            catch (Exception e) {
                System.err.println("Cannot convert the Mat object: " + e);
                return null;
            }
    }

    private BufferedImage matToBufferedImage(Mat frame) {
        Imgcodecs.imencode(".jpg", frame, matOfByte);
        byte[] bytes = matOfByte.toArray();
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bufferedImage;
    }
}


