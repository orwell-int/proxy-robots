package orwell.proxy.robot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.proxy.config.IConfigCamera;

import java.net.MalformedURLException;
import java.net.URL;

public class IPWebcam implements ICamera {
    private final static Logger logback = LoggerFactory.getLogger(IPWebcam.class);
    private final static String DUMMY_URL = "http://goo.gl/y1QTB7";
    private URL url;

    public IPWebcam(final URL url) {
        this.url = url;
    }

    public IPWebcam(final IConfigCamera configCamera) {
        if (null == configCamera) {
            logback.error("Config of camera is missing");
        } else {
            try {
                if (null == configCamera.getResourcePath()) {
                    url = new URL("http", configCamera.getIp(), configCamera.getPort(), "");
                } else {
                    url = new URL("http", configCamera.getIp(), configCamera.getPort(), configCamera.getResourcePath());
                }
            } catch (final MalformedURLException e) {
                logback.error("Camera URL is not correct: " + e.getMessage());
            }
        }
    }

    @Override
    public String getUrl() {
        if (null == url) {
            return null;
        } else {
            return url.toString();
        }
    }

    @Override
    public void accept(final IRobotElementVisitor visitor) {
        visitor.visit(this);
    }

    public static IPWebcam getDummy() {
        try {
            final URL dummyUrl = new URL(DUMMY_URL);
            return new IPWebcam(dummyUrl);
        } catch (final MalformedURLException e) {
            logback.error(e.getMessage());
            return null;
        }
    }
}
