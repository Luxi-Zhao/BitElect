package com.example.lucyzhao.votingapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import static com.example.lucyzhao.votingapp.Utils.YOUR_FACE_ID;


/**
 * Created by LucyZhao on 2018/3/19.
 */

public class CustomFaceDetector extends Detector<Face> {
    private static final String TAG = CustomFaceDetector.class.getSimpleName();
    private Detector<Face> detector;
    private Context context;
    public Bitmap publicBitmap;
    private int imgNum = 0;
    private int timeout = 30;

    private int firstWidth;
    private int firstHeight;


    public CustomFaceDetector(Detector<Face> detector, Context context) {
        this.detector = detector;
        this.context = context;
    }

    private void dumpData(Face face, Frame frame, Bitmap bitmap) {
        if(face == null) return;
        float facex = face.getPosition().x;
        float facey = face.getPosition().y;
        float facew = face.getWidth();
        float faceh = face.getHeight();

        float frameW = frame.getMetadata().getWidth();
        float frameH = frame.getMetadata().getHeight();

        float bw = bitmap.getWidth();
        float bh = bitmap.getHeight();
        Log.v(TAG, "facex " + facex + " facey " + facey + " facew " + facew + " faceh " + faceh);
        Log.v(TAG, "face left " + facex + " top " + facey + " right " + (facex+facew) + " bot " + (facey + faceh));
        Log.v(TAG, "frameW " + frameW + " frameH " + frameH);
        Log.v(TAG, "bitmap w " + bw + " bitmap h" + bh);
    }

    //todo slow down face detection rate
    @Override
    public SparseArray<Face> detect(Frame frame) {
        SparseArray<Face> detectedFaces = detector.detect(frame);
        if(detectedFaces.size() > 0 && imgNum < 4 && timeout <= 0) {
            Face face = detectedFaces.get(detectedFaces.keyAt(0));
            Log.v(TAG, "face detected");

            int width = frame.getMetadata().getWidth();
            int height = frame.getMetadata().getHeight();
            int rotation = frame.getMetadata().getRotation();
            int rotDegree = (rotation % 4) * 90;

            // convert captured frame to bitmap
            ByteBuffer buf = frame.getGrayscaleImageData();
            byte[] nv21Bytes = new byte[buf.remaining()];
            buf.get(nv21Bytes);

            YuvImage yuvImage = new YuvImage(nv21Bytes, ImageFormat.NV21, width, height, null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 20, baos);
            byte[] imgBytes = baos.toByteArray();
            Bitmap bitmap = BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.length);

            dumpData(face, frame, bitmap);

            //rotate
            Matrix matrix = new Matrix();
            matrix.postRotate(rotDegree);
            Bitmap rotatedB = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            Log.v(TAG, "rotated get width:" + rotatedB.getWidth() + " rotated get height" + rotatedB.getHeight());
            Log.v(TAG, "rotated get size" + rotatedB.getByteCount());
            try {
                Log.v(TAG, "saved ");
                Utils.saveImg(YOUR_FACE_ID, Integer.toString(imgNum), rotatedB, context);

            } catch (IOException e) {
                e.printStackTrace();
            }

            imgNum++;

        }
        timeout--;
        return detectedFaces;
    }


    @Override
    public boolean isOperational() {
        return detector.isOperational();
    }

    @Override
    public boolean setFocus(int id) {
        return detector.setFocus(id);
    }

    /**
     *
     * @param face
     * @param width frame width
     * @param height frame height
     * @return
     */
    private Bitmap cropOutFace(Face face, int width, int height, Bitmap origB) {
        //crop out face
        float facex = face.getPosition().x >= 0 ? face.getPosition().x : 0;
        float facey = face.getPosition().y >= 0 ? face.getPosition().y : 0;
        float facew = face.getWidth() + facex < width ? face.getWidth() : width - facex;
        float faceh = face.getHeight() + facey < height ? face.getHeight() : height - facey;


        return Bitmap.createBitmap(origB, (int)facex, (int)facey, (int)facew, (int)faceh);
        //Log.v(TAG, "cropped bitmap x " + facex + " y " + facey + " w " + facew + " h " + faceh);
    }

}