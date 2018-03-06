package com.example.lucyzhao.votingapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {
    TextView myTextView;
    ImageView myImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myTextView = findViewById(R.id.qr_code_txt);

        myImageView = findViewById(R.id.qr_code_img);



    }

    public void vote(View view) {
        Toast.makeText(this, "you voted", Toast.LENGTH_SHORT).show();
    }

    public void scan(View view) {

        Bitmap myBitmap = BitmapFactory.decodeResource(
                getApplicationContext().getResources(),
                R.drawable.puppy);
        myImageView.setImageBitmap(myBitmap);

        BarcodeDetector detector =
                new BarcodeDetector.Builder(getApplicationContext())
                        .setBarcodeFormats(Barcode.QR_CODE)
                        .build();
        if(!detector.isOperational()){
            myTextView.setText("Could not set up the detector!");

        }
        else {
            Frame frame = new Frame.Builder().setBitmap(myBitmap).build();
            SparseArray<Barcode> barcodes = detector.detect(frame);
            Barcode thisCode = barcodes.valueAt(0);
            myTextView.setText(thisCode.rawValue);
        }
    }
}
