package sample;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class CameraControl {
    public CameraControl(String ControlUrl, String UpControl, String DownControl, String LeftControl, String RightControl){
        this.controlUrl = ControlUrl;
        this.downControl = DownControl;
        this.upControl = UpControl;
        this.leftControl = LeftControl;
        this.rightControl = RightControl;
    }
    public CameraControl(String CameraUrl){
        this.controlUrl = CameraUrl.replace("/mjpg", "/control");
        this.downControl = "?pan=plus";
        this.upControl = "?pan=minus";
        this.leftControl = "?tilt=plus";
        this.rightControl = "?tilt=minus";
    }

    public enum direction{
        up,
        down,
        left,
        right
    }

    private String controlUrl;
    public String getControlUrl(){return controlUrl;}
    public void setControlUrl(String ControlUrl){controlUrl = ControlUrl;}
    private String upControl;
    public String getUpControl(){return upControl;}
    public void setUpControl(String UpControl){upControl = UpControl;}
    private String downControl;
    public String getDownControl(){return downControl;}
    public void setDownControl(String DownControl){downControl = DownControl;}
    private String leftControl;
    public String getLeftControl(){return leftControl;}
    public void setLeftControl(String LeftControl){leftControl = LeftControl;}
    private String rightControl;
    public String getRightControl(){return rightControl;}
    public void setRightControl(String RightControl){rightControl = RightControl;}

    void turnCamera(direction direction) throws IOException {
        String url = "";
        switch (direction){
            case up:
                url = controlUrl + upControl;
                break;
            case down:
                url = controlUrl + downControl;
                break;
            case left:
                url = controlUrl + leftControl;
                break;
            case right:
                url = controlUrl + rightControl;
                break;
        }
        URL newUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) newUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.getInputStream();
        connection.disconnect();
    }
}
