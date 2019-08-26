package com.baidu.face;

import com.baidu.aip.face.AipFace;
import com.baidu.aip.face.MatchRequest;
import com.baidu.aip.util.Base64Util;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamUtils;
import com.github.sarxos.webcam.util.ImageUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.HOGDescriptor;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.springframework.util.Base64Utils;
import org.xmlunit.util.Convert;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Dictionary;

public class CaptureBasic extends JPanel {

    private BufferedImage mImg;
    /**
     * 接口申请免费，请自行申请使用，如果学习使用可以用下
     */
    private static final String APP_ID = "11275267";
    private static final String API_KEY = "WC1wOLjGjSCVa0X7CDWkdZbz";
    private static final String SECRET_KEY = "dqMAkX80svGFomgBA4LqOcuet7LvaGBx";


    public static void main(String[] args) {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

            Mat capImg = new Mat();
            VideoCapture capture = new VideoCapture(0);
            int height = (int) capture.get(Videoio.CAP_PROP_FRAME_HEIGHT);
            int width = (int) capture.get(Videoio.CAP_PROP_FRAME_WIDTH);
            if (height == 0 || width == 0) {
                throw new Exception("camera not found!");
            }

            JFrame frame = new JFrame("camera");
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            CaptureBasic panel = new CaptureBasic();
            panel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent arg0) {
                    System.out.println("click");
                }

                @Override
                public void mouseMoved(MouseEvent arg0) {
                    System.out.println("move");

                }

                @Override
                public void mouseReleased(MouseEvent arg0) {
                    System.out.println("mouseReleased");
                }

                @Override
                public void mousePressed(MouseEvent arg0) {
                    System.out.println("mousePressed");
                }

                @Override
                public void mouseExited(MouseEvent arg0) {
                    System.out.println("mouseExited");
                    //System.out.println(arg0.toString());
                }

                @Override
                public void mouseDragged(MouseEvent arg0) {
                    System.out.println("mouseDragged");
                    //System.out.println(arg0.toString());
                }

            });
            frame.setContentPane(panel);
            frame.setVisible(true);
            frame.setSize(width + frame.getInsets().left + frame.getInsets().right,
                    height + frame.getInsets().top + frame.getInsets().bottom);
            int n = 0;
            Mat temp = new Mat();
            while (frame.isShowing() && n < 500) {
                //System.out.println("第"+n+"张");
                capture.read(capImg);
                Imgproc.cvtColor(capImg, temp, Imgproc.COLOR_RGB2GRAY);
                //Imgcodecs.imwrite("G:/opencv/lw/neg/back"+n+".png", temp);
                panel.mImg = panel.mat2BI(detectFace(capImg));
                panel.repaint();
                //n++;
                //break;
            }
            capture.release();
            frame.dispose();
        } catch (Exception e) {
            System.out.println("例外：" + e);
        } finally {
            System.out.println("--done--");
        }

    }

    /**
     * opencv实现人脸识别
     *
     * @param img
     */
    public static Mat detectFace(Mat img) throws Exception {

        System.out.println("Running DetectFace ... ");
        // 从配置文件lbpcascade_frontalface.xml中创建一个人脸识别器，该文件位于opencv安装目录中
        CascadeClassifier faceDetector = new CascadeClassifier("D:\\TDDOWNLOAD\\opencv\\sources\\data\\haarcascades\\haarcascade_frontalface_alt.xml");


        // 在图片中检测人脸
        MatOfRect faceDetections = new MatOfRect();

        faceDetector.detectMultiScale(img, faceDetections);

        //System.out.println(String.format("Detected %s faces", faceDetections.toArray().length));

        Rect[] rects = faceDetections.toArray();
        if (rects != null && rects.length >= 1) {
            for (Rect rect : rects) {
                Imgproc.rectangle(img, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                        new Scalar(0, 0, 255), 2);
            }
        }
        return img;
    }

    /**
     * opencv实现人型识别，hog默认的分类器。所以效果不好。
     *
     * @param img
     */
    public static Mat detectPeople(Mat img) {
        //System.out.println("detectPeople...");
        if (img.empty()) {
            System.out.println("image is exist");
        }
        HOGDescriptor hog = new HOGDescriptor();
        hog.setSVMDetector(HOGDescriptor.getDefaultPeopleDetector());
        System.out.println(HOGDescriptor.getDefaultPeopleDetector());
        //hog.setSVMDetector(HOGDescriptor.getDaimlerPeopleDetector());
        MatOfRect regions = new MatOfRect();
        MatOfDouble foundWeights = new MatOfDouble();
        //System.out.println(foundWeights.toString());
        hog.detectMultiScale(img, regions, foundWeights);
        for (Rect rect : regions.toArray()) {
            Imgproc.rectangle(img, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 0, 255), 2);
        }
        return img;
    }

    private BufferedImage mat2BI(Mat mat) {
        int dataSize = mat.cols() * mat.rows() * (int) mat.elemSize();
        byte[] data = new byte[dataSize];
        mat.get(0, 0, data);
        int type = mat.channels() == 1 ?
                BufferedImage.TYPE_BYTE_GRAY : BufferedImage.TYPE_3BYTE_BGR;

        if (type == BufferedImage.TYPE_3BYTE_BGR) {
            for (int i = 0; i < dataSize; i += 3) {
                byte blue = data[i + 0];
                data[i + 0] = data[i + 2];
                data[i + 2] = blue;
            }
        }
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        image.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), data);

        return image;
    }

    public void paintComponent(Graphics g) {
        if (mImg != null) {
            g.drawImage(mImg, 0, 0, mImg.getWidth(), mImg.getHeight(), this);
        }
    }

    @Test
    public void testface() throws IOException {
        InputStream inputStream = new FileInputStream("H:\\照片\\社保.jpg");
        InputStream inputStream1 = new FileInputStream("H:\\照片\\1寸照片.jpg");
        byte[] bytes = new byte[inputStream.available()];
        inputStream.read(bytes);
        byte[] bytes1 = new byte[inputStream1.available()];
        inputStream1.read(bytes1);
        double match = this.match(Base64Util.encode(bytes), Base64Util.encode(bytes1));
        if(match>=80){
            System.out.println("匹配成功");
        }else {
            System.out.println("匹配失败");
        }
        inputStream.close();
        inputStream1.close();
    }


    /**
     *
     * <p>Title: go</p>
     * <p>Description: </p>
     * @param baseA 图片a
     * @param baseB	图片b
     */
    public double match(String baseA,String baseB) {
        Double result=0.0;
        AipFace client = new AipFace(APP_ID, API_KEY, SECRET_KEY);
        ArrayList<MatchRequest> requests = new ArrayList<MatchRequest>();
        requests.add(new MatchRequest(baseA, "BASE64"));
        requests.add(new MatchRequest(baseB, "BASE64"));
        try {
            JSONObject res = client.match(requests);
            JSONObject jsonObj = new JSONObject(res.toString());
            System.out.println(jsonObj);
            JSONObject scoreObj = (JSONObject) jsonObj.get("result");
            Double score =(Double) scoreObj.get("score");
            System.out.println("照片匹配度:"+(double) Math.round(score * 100) / 100);
            result= (double) Math.round(score * 100) / 100;
        } catch (JSONException e) {
            System.out.println("认证失败!");
        }
        return result;
    }

    @Test
    public void test(){
        Webcam webcam = Webcam.getDefault();
        System.out.println(webcam);
        //初始化
        File file = new File("C://system");
        file.mkdir();
        //获取视屏流
        WebcamUtils.capture(webcam, file+"/system");

        byte[] bytes = WebcamUtils.getImageBytes(webcam, ImageUtils.FORMAT_PNG);
        System.out.println(bytes);
    }

}
