package com.example.lucyzhao.votingapp;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;


import net.sf.scuba.smartcards.CardService;
import net.sf.scuba.smartcards.CardServiceException;

import org.jmrtd.BACKey;
import org.jmrtd.PassportService;
import org.jmrtd.lds.icao.DG1File;
import org.jmrtd.lds.icao.DG2File;
import org.jmrtd.lds.iso19794.FaceImageInfo;
import org.jmrtd.lds.iso19794.FaceInfo;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class NFCActivity extends AppCompatActivity {
    private static final String TAG = NFCActivity.class.getSimpleName();
    private static final String ISODEP_TECH_STRING = "android.nfc.tech.IsoDep";
    private NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);
        Log.v(TAG, "in on create");
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        readPassport(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                0);
        IntentFilter isodep = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);

        nfcAdapter.enableForegroundDispatch(this,
                pendingIntent,
                new IntentFilter[] {isodep},
                new String[][] { new String[] { IsoDep.class.getName() } });
    }

    @Override
    protected void onStop() {
        super.onStop();
        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.v(TAG, "in on new intent");
        readPassport(intent);
    }

    private void readPassport(Intent intent) {
        if (intent != null && NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            List<String> techArr = Arrays.asList(tag.getTechList());
            if(techArr.contains(ISODEP_TECH_STRING)) {
                IsoDep isoTag = IsoDep.get(tag);
                CardService cardService = CardService.getInstance(isoTag);
                try {
                    //todo add ui for this
                    BACKey bacKey = new BACKey("E28426441", "970109", "240821");
                    PassportService passportService = new PassportService(cardService, 256, 224, false, true);
                    passportService.open();
                    passportService.sendSelectApplet(false);
                    passportService.doBAC(bacKey);

                    InputStream dg1Is = passportService.getInputStream(PassportService.EF_DG1);
                    DG1File dg1File = new DG1File(dg1Is);
                    String birthdate = dg1File.getMRZInfo().getDateOfBirth();
                    Toast.makeText(this, "birthdate is" + birthdate, Toast.LENGTH_LONG).show();
                    ((TextView) findViewById(R.id.birthdate)).setText(birthdate);
                    ((TextView) findViewById(R.id.gender)).setText(dg1File.getMRZInfo().getGender().toString());
                    ((TextView) findViewById(R.id.nationality)).setText(dg1File.getMRZInfo().getNationality());
                    dg1Is.close();

//                    InputStream dg2Is = passportService.getInputStream(PassportService.EF_DG2);
//                    DG2File dg2File = new DG2File(dg2Is);
//
//                    List<FaceImageInfo> allFaceImageInfos = new ArrayList<>();
//                    List<FaceInfo> faceInfos = dg2File.getFaceInfos();
//                    for (FaceInfo faceInfo : faceInfos) {
//                        allFaceImageInfos.addAll(faceInfo.getFaceImageInfos());
//                    }
//
//                    if(allFaceImageInfos.size() > 0) {
//                        Log.v(TAG, "images are found");
//                        FaceImageInfo faceImageInfo = allFaceImageInfos.get(0);
//                        Log.v(TAG, Integer.toString(faceImageInfo.getHairColor()));
//                        Log.v(TAG, faceImageInfo.getEyeColor().name());
//                    }
//                    else {
//                        Log.v(TAG, "no images found");
//                    }

                } catch (CardServiceException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            else {
                Toast.makeText(this, "please scan a passport", Toast.LENGTH_SHORT).show();
            }

        }
    }

}
