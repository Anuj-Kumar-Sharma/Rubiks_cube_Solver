package com.example.anujsharma.colors;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, View.OnTouchListener{


    private CameraBridgeViewBase cameraBridgeViewBase;
    private Mat mRgba;
    private TextView color, coordinates;
    private ImageView colorImage;
    double x = -1, y = -1;
    private Scalar mBlogColorHsv, mBlogColorRgba;

    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS :
                    cameraBridgeViewBase.enableView();
                    cameraBridgeViewBase.setOnTouchListener(MainActivity.this);
                    break;
                default:
                    super.onManagerConnected(status);

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        color = (TextView) findViewById(R.id.xtvcolor);
        coordinates = (TextView) findViewById(R.id.xtvcoordinates);
        colorImage = (ImageView) findViewById(R.id.xivColorImage);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        cameraBridgeViewBase = (CameraBridgeViewBase) findViewById(R.id.xsvSurfaceView);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(cameraBridgeViewBase!=null) {
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, MainActivity.this, baseLoaderCallback);
        }
        else {
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(cameraBridgeViewBase!=null) {
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat();
        mBlogColorHsv = new Scalar(255);
        mBlogColorRgba = new Scalar(255);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        return mRgba;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int cols = mRgba.cols();
        int rows = mRgba.rows();

        double yLow = cameraBridgeViewBase.getHeight()*0.2401961;
        double yHigh = cameraBridgeViewBase.getHeight()*0.7696078;

        double xScale = (double)cols/(double)cameraBridgeViewBase.getWidth();
        double yScale = (double)rows/(yHigh-yLow);

        x = event.getX();
        y = event.getY();

        x=x*xScale;
        y=y*yScale;

        /*x = event.getX();
        y = event.getY();*/

        if(x<0 || y<0 || x>cols || y>rows) return false;

        /*x = event.getX();
        y = event.getY();*/

        coordinates.setText("Coordinates X: "+Double.valueOf(x)+", Y: "+Double.valueOf(y));

        Rect touchedRect = new Rect();
        touchedRect.x = (int)x;
        touchedRect.y = (int)y;
        touchedRect.width = 8;
        touchedRect.height = 8;

        Mat touchedRegionRgba = mRgba.submat(touchedRect);
        Mat touchedRegionHsv = new Mat();


        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);
        mBlogColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = touchedRect.height*touchedRect.width;
        for (int i = 0; i < mBlogColorHsv.val.length; i++) {
            mBlogColorHsv.val[i] /= pointCount;
        }
        
        mBlogColorRgba = convertHsv2Rgba(mBlogColorHsv);
        color.setText("Color: #"+String.format("%02X", (int)mBlogColorRgba.val[0])
                +String.format("%02X", (int)mBlogColorRgba.val[1])
                +String.format("%02X", (int)mBlogColorRgba.val[2]));

        int colorValue = Color.rgb((int)mBlogColorRgba.val[0]
                ,(int)mBlogColorRgba.val[1]
                ,(int)mBlogColorRgba.val[2]);

        color.setTextColor(colorValue);
        coordinates.setTextColor(colorValue);
        colorImage.setBackgroundColor(colorValue);
        return false;
    }

    private Scalar convertHsv2Rgba(Scalar mBlogColorHsv) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, mBlogColorHsv);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);
        return new Scalar(pointMatRgba.get(0, 0));

    }

}
