package com.example.lucyzhao.votingapp;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.sf.scuba.smartcards.CardService;
import net.sf.scuba.smartcards.CardServiceException;

import org.jmrtd.BACKey;
import org.jmrtd.PassportService;
import org.jmrtd.lds.icao.DG1File;
import org.jmrtd.lds.icao.DG2File;
import org.jmrtd.lds.icao.MRZInfo;
import org.jmrtd.lds.iso19794.FaceImageInfo;
import org.jmrtd.lds.iso19794.FaceInfo;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class NFCActivity extends AppCompatActivity {
    private static final String TAG = NFCActivity.class.getSimpleName();
    // NFC
    private static final String ISODEP_TECH_STRING = "android.nfc.tech.IsoDep";
    private NfcAdapter nfcAdapter;
    private String nfcResult = "";

    // UI
    private InfoAdapter infoAdapter;
    private List<BioInfo> infoList = new ArrayList<>();
    private TextView nfcResultTxt;
    private Button goVoteBtn;
    private ImageView testNFCImg;


    //////////////////////////////////////////////////////////////
    ///////////////////////////CALLBACKS//////////////////////////
    //////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);

        // UI
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        infoAdapter = new InfoAdapter(infoList);
        RecyclerView infoRecycler = findViewById(R.id.info_recylerview);
        infoRecycler.setLayoutManager(layoutManager);
        infoRecycler.setAdapter(infoAdapter);

        nfcResultTxt = findViewById(R.id.nfc_result_txt);
        goVoteBtn = findViewById(R.id.go_vote_btn);
        goVoteBtn.setVisibility(View.INVISIBLE);
        testNFCImg = findViewById(R.id.testNFCImage);

        // read NFC
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
                new IntentFilter[]{isodep},
                new String[][]{new String[]{IsoDep.class.getName()}});
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.v(TAG, "in on new intent");
        readPassport(intent);
    }

    //////////////////////////////////////////////////////////////
    ////////////////////////////PASSPORT//////////////////////////
    //////////////////////////////////////////////////////////////

    private void readPassport(Intent intent) {
        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.pref_file_key), Context.MODE_PRIVATE);
        String birthDate = sharedPref.getString(getString(R.string.shared_pref_birthdate), "");
        String expDate = sharedPref.getString(getString(R.string.shared_pref_expirydate), "");
        String docNum = sharedPref.getString(getString(R.string.shared_pref_doc_num), "");

        Log.v(TAG, "opened shared prefs, birthdate " + birthDate + "exp date" + expDate);

        if (intent != null && NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            List<String> techArr = Arrays.asList(tag.getTechList());
            if (techArr.contains(ISODEP_TECH_STRING)) {
                //new ReadPassportTask(birthDate, expDate, docNum).execute(tag);
                Log.v(TAG, "executing read image task...");
                new ReadPassportImgTask(birthDate, expDate, docNum).execute(tag);
            } else {
                Toast.makeText(this, "please scan a passport", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private class ReadPassportTask extends AsyncTask<Tag, Integer, MRZInfo> {
        private String birthDate, expiryDate, docNum;

        ReadPassportTask(String birthdate, String expirydate, String docnum) {
            this.birthDate = birthdate;
            this.expiryDate = expirydate;
            this.docNum = docnum;
        }

        @Override
        protected MRZInfo doInBackground(Tag... params) {
            IsoDep isoTag = IsoDep.get(params[0]);
            CardService cardService = CardService.getInstance(isoTag);
            try {
                BACKey bacKey = new BACKey(docNum, birthDate, expiryDate);
                PassportService passportService = new PassportService(cardService, 256, 224, false, true);
                passportService.open();
                passportService.sendSelectApplet(false);
                passportService.doBAC(bacKey);

                InputStream dg1Is = passportService.getInputStream(PassportService.EF_DG1);
                DG1File dg1File = new DG1File(dg1Is);
                dg1Is.close();

//                InputStream dg2Is = passportService.getInputStream(PassportService.EF_DG2);
//                DG2File dg2File = new DG2File(dg2Is);
//
//                List<FaceImageInfo> allFaceImageInfos = new ArrayList<>();
//                List<FaceInfo> faceInfos = dg2File.getFaceInfos();
//                for (FaceInfo faceInfo : faceInfos) {
//                    allFaceImageInfos.addAll(faceInfo.getFaceImageInfos());
//                }
//
//                if (!allFaceImageInfos.isEmpty()) {
//                    FaceImageInfo faceImageInfo = allFaceImageInfos.get(0);
//                    InputStream imgIs = faceImageInfo.getImageInputStream();
//                    Log.v(TAG, imgIs.toString());
//                }
                return dg1File.getMRZInfo();
            } catch (CardServiceException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(MRZInfo mrzInfo) {
            if (mrzInfo == null) {
                nfcResultTxt.setText("Authentication Failed");
                return;
            }

            String lastName = mrzInfo.getPrimaryIdentifier().replaceAll("\\W", "");
            String firstName = mrzInfo.getSecondaryIdentifier().replaceAll("\\W", "");
            String nationality = mrzInfo.getNationality();
            String personalNumber = mrzInfo.getPersonalNumber();
            String docNumber = mrzInfo.getDocumentNumber();

            // update info list UI
            infoList.removeAll(infoList);
            infoList.add(new BioInfo("Name", lastName + ", " + firstName));
            infoList.add(new BioInfo("Nationality", nationality));

            SimpleDateFormat fromFormat = new SimpleDateFormat("yyMMdd");
            SimpleDateFormat toFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.CANADA);
            try {
                Date birthDate = fromFormat.parse(mrzInfo.getDateOfBirth());
                Date expiryDate = fromFormat.parse(mrzInfo.getDateOfExpiry());
                infoList.add(new BioInfo("Birthdate", toFormat.format(birthDate)));
                infoList.add(new BioInfo("Passport Expiry Date", toFormat.format(expiryDate)));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            infoList.add(new BioInfo("Personal Number", personalNumber));
            infoList.add(new BioInfo("Document Number", docNumber));
            infoAdapter.notifyDataSetChanged();

            enableVoting(String.valueOf(Math.abs(docNumber.hashCode())));
        }

        private void enableVoting(String nfcResultStr) {
            //todo add eligibility checks
            // if the person is eligible ...
            goVoteBtn.setVisibility(View.VISIBLE);
            nfcResult = nfcResultStr;
        }

    }


    private class ReadPassportImgTask extends AsyncTask<Tag, Integer, Bitmap> {
        private String birthDate, expiryDate, docNum;

        ReadPassportImgTask(String birthdate, String expirydate, String docnum) {
            this.birthDate = birthdate;
            this.expiryDate = expirydate;
            this.docNum = docnum;
        }

        @Override
        protected Bitmap doInBackground(Tag... params) {
            IsoDep isoTag = IsoDep.get(params[0]);
            CardService cardService = CardService.getInstance(isoTag);
            try {
                BACKey bacKey = new BACKey(docNum, birthDate, expiryDate);
                PassportService passportService = new PassportService(cardService, 256, 224, false, true);
                passportService.open();
                passportService.sendSelectApplet(false);
                passportService.doBAC(bacKey);

                InputStream dg2Is = passportService.getInputStream(PassportService.EF_DG2);
                DG2File dg2File = new DG2File(dg2Is);

                List<FaceImageInfo> allFaceImageInfos = new ArrayList<>();
                List<FaceInfo> faceInfos = dg2File.getFaceInfos();
                for (FaceInfo faceInfo : faceInfos) {
                    allFaceImageInfos.addAll(faceInfo.getFaceImageInfos());
                }

                if (!allFaceImageInfos.isEmpty()) {
                    FaceImageInfo faceImageInfo = allFaceImageInfos.get(0);
                    InputStream imgIs = faceImageInfo.getImageInputStream();
                    Log.v(TAG, imgIs.toString());
                    Bitmap b = ImgDecoder.decodeImage(getApplicationContext(), faceImageInfo.getMimeType(), imgIs);
                    return b;
                }
                return null;
            } catch (CardServiceException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Bitmap b) {
            if (b == null) {
                Log.v(TAG, "bitmap is null!!!!!!!");
                return;
            }
            testNFCImg.setImageBitmap(b);
        }
    }

    //////////////////////////////////////////////////////////////
    ////////////////////////////UI////////////////////////////////
    //////////////////////////////////////////////////////////////
    /**
     * Send scanned result back to MainActivity
     *
     * @param view
     */
    public void goVote(View view) {
        Log.v(TAG, "nfc shouldn't be empty, nfc result is: " + nfcResult);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra(Utils.NFC_RESULT, nfcResult);
        startActivity(intent);
        finish();
    }

    private class BioInfo {
        String tag;
        String value;

        BioInfo(String tag, String value) {
            this.tag = tag;
            this.value = value;
        }
    }

    public class InfoAdapter extends RecyclerView.Adapter<InfoAdapter.ViewHolder> {
        private List<BioInfo> passportInfo;

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tag, value;

            public ViewHolder(View view) {
                super(view);
                tag = view.findViewById(R.id.info_tag);
                value = view.findViewById(R.id.info_value);
            }
        }

        public InfoAdapter(List<BioInfo> passportInfo) {
            this.passportInfo = passportInfo;
        }

        @Override
        public InfoAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.passport_info_item, parent, false);

            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(InfoAdapter.ViewHolder holder, int position) {
            holder.tag.setText(passportInfo.get(position).tag);
            holder.value.setText(passportInfo.get(position).value);
        }

        @Override
        public int getItemCount() {
            return passportInfo.size();
        }


    }


}
