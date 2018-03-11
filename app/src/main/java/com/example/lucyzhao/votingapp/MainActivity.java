package com.example.lucyzhao.votingapp;

import android.Manifest;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Slide;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int TAKE_PICTURE = 23;
    TextView myTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myTextView = findViewById(R.id.qr_code_txt);

        Intent intent = this.getIntent();
        String qr_result = intent.getStringExtra(Utils.QR_RESULT);
        if(qr_result != null) {
            Log.v(TAG, "result if qr scanning is:" + qr_result);
            myTextView.setText(qr_result);
        }
        else {
            Log.v(TAG, "qr scanning returned null!");
        }
    }

    public void vote(View view) {
        Toast.makeText(this, "you voted", Toast.LENGTH_SHORT).show();
    }

    public void scan(View view) {
        scanQRCode();
    }

    public void scanPassport(View view) {
        //startActivity(new Intent(this, NFCActivity.class));
        new PassportInfoFragment().show(getFragmentManager(),"enterPassportInfo");
    }

    private void scanQRCode() {
        //requestCameraPermission();
        Intent intent = new Intent(this, QRCodeActivity.class);
        startActivity(intent);
    }

//    /**
//     * set the input fragment's switching behavior
//     * @param fragment
//     */
//    private void createFragment(Fragment fragment) {
//        fragment.setEnterTransition(new Slide(Gravity.END));
//        fragment.setExitTransition(new Slide(Gravity.START));
//        FragmentTransaction transaction = getFragmentManager().beginTransaction();
//
//        // Replace whatever is in the fragment_container view with this fragment,
//        // and add the transaction to the back stack if needed
//        transaction.replace(R.id.activity_main, fragment);
//        transaction.commit();
//    }

    public static class PassportInfoFragment extends DialogFragment implements View.OnClickListener{
//        private DatePicker birthDate;
//        private DatePicker expiryDate;
        private EditText birthDate;
        private EditText expiryDate;
        private EditText docNum;
        private Button okBtn;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState){
            View fragment = inflater.inflate(R.layout.fragment_passport_info, container, false);
            birthDate = fragment.findViewById(R.id.birthdate_edit_text);
            expiryDate = fragment.findViewById(R.id.expirydate_edit_text);
            docNum = fragment.findViewById(R.id.passport_num_edit_text);
            okBtn = fragment.findViewById(R.id.save_info_ok_btn);
            okBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String birthDateStr = birthDate.getText().toString();
                    String expiryDateStr = expiryDate.getText().toString();
                    String docNumStr = docNum.getText().toString().toUpperCase();
                    if(birthDateStr.length() != 6 || expiryDateStr.length() != 6 || docNumStr.isEmpty()) {
                        Toast.makeText(getContext(), "Incorrect Info", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    SharedPreferences sharedPref = getActivity()
                            .getSharedPreferences(getString(R.string.pref_file_key), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(getString(R.string.shared_pref_doc_num), docNumStr);
                    editor.putString(getString(R.string.shared_pref_birthdate), birthDateStr);
                    editor.putString(getString(R.string.shared_pref_expirydate), expiryDateStr);
                    editor.apply(); //asynchronously save to pref
                    Log.v(TAG,"saved to pref");
                    dismiss();
                }
            });

            return fragment;
        }

        //todo check for NULL
        private String getDateStrFromPicker(DatePicker dp) {
            SimpleDateFormat format = new SimpleDateFormat("yyMMdd");

            Calendar calendar = Calendar.getInstance();
            calendar.set(dp.getYear(), dp.getMonth(), dp.getDayOfMonth());
            Date date = calendar.getTime();
            return format.format(date);
        }

        @Override
        public void onClick(View view) {
            dismiss();
        }
    }


}