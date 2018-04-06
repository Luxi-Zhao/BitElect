package com.example.lucyzhao.votingapp.nfc;

import android.app.FragmentTransaction;
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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lucyzhao.votingapp.MainActivity;
import com.example.lucyzhao.votingapp.PulsingButton;
import com.example.lucyzhao.votingapp.R;
import com.example.lucyzhao.votingapp.Utils;

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
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Scans passport and sends NFCResult back to MainActivity
 * NFCResult is set to docnum
 */
public class NFCActivity extends AppCompatActivity {
    private static final String TAG = NFCActivity.class.getSimpleName();
    // NFC
    private static final String ISODEP_TECH_STRING = "android.nfc.tech.IsoDep";
    private NfcAdapter nfcAdapter;
    private String nfcResult = "";

    // UI
    private LinearLayout nfcInfoLayout;
    private InfoAdapter infoAdapter;
    private List<BioInfo> infoList = new ArrayList<>();
    private PulsingButton goVoteBtn;
    private ImageView NFCImg;

    private RelativeLayout nfcProgressLayout;
    private ProgressBar nfcProgressBar;
    private StepperFragment stepper;


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

        goVoteBtn = findViewById(R.id.go_vote_btn);
        goVoteBtn.setVisibility(View.INVISIBLE);
        NFCImg = findViewById(R.id.NFCImage);

        nfcInfoLayout = findViewById(R.id.nfc_mrz_info);
        nfcInfoLayout.setVisibility(View.GONE);

        nfcProgressLayout = findViewById(R.id.nfc_progress_layout);
        nfcProgressBar = findViewById(R.id.nfc_progress_bar);
        stepper = new StepperFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.stepper_frag, stepper);
        transaction.addToBackStack(null);
        transaction.commit();

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
                new ReadPassportTask(birthDate, expDate, docNum, this).execute(tag);
            } else {
                Toast.makeText(this, "please scan a passport", Toast.LENGTH_SHORT).show();
            }

        }
    }



    private static class ReadPassportTask extends AsyncTask<Tag, Integer, ReadPassportTask.NFCData> {
        WeakReference<NFCActivity> activityRef;
        private String birthDate, expiryDate, docNum;

        class NFCData {
            MRZInfo mrzInfo = null;
            Bitmap passportPic = null;
        }

        ReadPassportTask(String birthdate, String expirydate, String docnum, NFCActivity nfcActivity) {
            this.birthDate = birthdate;
            this.expiryDate = expirydate;
            this.docNum = docnum;
            activityRef = new WeakReference<>(nfcActivity);
        }

        @Override
        protected NFCData doInBackground(Tag... params) {
            NFCActivity activity = activityRef.get();
            if(activity == null || activity.isFinishing()) return null;

            IsoDep isoTag = IsoDep.get(params[0]);
            CardService cardService = CardService.getInstance(isoTag);
            try {
                BACKey bacKey = new BACKey(docNum, birthDate, expiryDate);
                PassportService passportService = new PassportService(cardService, 256, 224, false, true);
                passportService.open();
                passportService.sendSelectApplet(false);
                passportService.doBAC(bacKey);

                publishProgress(1);
                InputStream dg1Is = passportService.getInputStream(PassportService.EF_DG1);
                InputStream dg2Is = passportService.getInputStream(PassportService.EF_DG2);

                DG1File dg1File = new DG1File(dg1Is);
                publishProgress(2);
                DG2File dg2File = new DG2File(dg2Is);
                publishProgress(3);

                NFCData nfcData = new NFCData();
                nfcData.mrzInfo = dg1File.getMRZInfo();
                Log.v(TAG, "obtained all mrz info");

                List<FaceImageInfo> allFaceImageInfos = new ArrayList<>();
                List<FaceInfo> faceInfos = dg2File.getFaceInfos();
                for (FaceInfo faceInfo : faceInfos) {
                    allFaceImageInfos.addAll(faceInfo.getFaceImageInfos());
                }

                if (!allFaceImageInfos.isEmpty()) {
                    FaceImageInfo faceImageInfo = allFaceImageInfos.get(0);
                    InputStream imgIs = faceImageInfo.getImageInputStream();
                    publishProgress(4);
                    Bitmap b = ImgDecoder.decodeImage(activity.getApplicationContext(), faceImageInfo.getMimeType(), imgIs);
                    nfcData.passportPic = b;
                }
                return nfcData;
            } catch (CardServiceException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            NFCActivity activity = activityRef.get();
            if(activity == null || activity.isFinishing()) return;

            activity.stepper.setStepCompleted(progress[0]);
        }

        protected void onPostExecute(NFCData nfcData) {
            NFCActivity activity = activityRef.get();
            if(activity == null || activity.isFinishing()) return;

            activity.nfcProgressBar.setVisibility(View.GONE);
            if (nfcData == null) {
                Log.v(TAG, "no data");
                activity.stepper.setTaskFailed(activity.getString(R.string.reading_failed));
                return;
            }
            MRZInfo mrzInfo = nfcData.mrzInfo;
            Bitmap b = nfcData.passportPic;

            if (mrzInfo == null) {
                activity.stepper.setTaskFailed(activity.getString(R.string.auth_failed));
                return;
            }

            activity.nfcProgressLayout.setVisibility(View.GONE);
            activity.nfcInfoLayout.setVisibility(View.VISIBLE);

            String lastName = mrzInfo.getPrimaryIdentifier().replaceAll("\\W", "");
            String firstName = mrzInfo.getSecondaryIdentifier().replaceAll("\\W", "");
            String nationality = mrzInfo.getNationality();
            String personalNumber = mrzInfo.getPersonalNumber();
            String docNumber = mrzInfo.getDocumentNumber();
            String gender = mrzInfo.getGender().name();

            // update info list UI
            enableVoting(String.valueOf(Math.abs(docNumber.hashCode())), mrzInfo);
            activity.infoList.removeAll(activity.infoList);

            activity.infoList.add(activity.new BioInfo("Name", lastName + ", " + firstName));
            activity.infoList.add(activity.new BioInfo(Utils.PASSPORT_NATIONALITY_STR, nationality));

            SimpleDateFormat fromFormat = new SimpleDateFormat("yyMMdd");
            SimpleDateFormat toFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.CANADA);
            try {
                Date birthDate = fromFormat.parse(mrzInfo.getDateOfBirth());
                Date expiryDate = fromFormat.parse(mrzInfo.getDateOfExpiry());
                activity.infoList.add(activity.new BioInfo(Utils.PASSPORT_BIRTHDATE_STR, toFormat.format(birthDate)));
                //activity.infoList.add(activity.new BioInfo("Passport Expiry Date", toFormat.format(expiryDate)));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            //activity.infoList.add(activity.new BioInfo("Personal Number", personalNumber));
            //activity.infoList.add(activity.new BioInfo("Document Number", docNumber));
            activity.infoList.add(activity.new BioInfo("Gender", gender));
            activity.infoAdapter.notifyDataSetChanged();

            // handle bitmap
            if (b == null) {
                Log.v(TAG, "bitmap is null");
            } else {
                activity.NFCImg.setImageBitmap(b);
            }
        }

        private void enableVoting(String nfcResultStr, MRZInfo mrzInfo) {
            NFCActivity activity = activityRef.get();
            if(activity == null || activity.isFinishing()) return;

            activity.goVoteBtn.setVisibility(View.VISIBLE);
            boolean ageEligible = Utils.checkAgeEligibility(mrzInfo.getDateOfBirth());
            boolean nationalityEligible = Utils.checkNationalityEligiblity(mrzInfo.getNationality());
            activity.infoAdapter.setAgeElibility(ageEligible);
            activity.infoAdapter.setNationalityElibility(nationalityEligible);

            if (ageEligible && nationalityEligible) {
                activity.goVoteBtn.enablePulsingButton();
                activity.nfcResult = nfcResultStr;
            } else {
                activity.goVoteBtn.setEnabled(false);
            }
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
        private boolean ageEligible = true;
        private boolean nationalityEligible = true;

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tag, value;
            ImageView checkMark;

            public ViewHolder(View view) {
                super(view);
                tag = view.findViewById(R.id.info_tag);
                value = view.findViewById(R.id.info_value);
                checkMark = view.findViewById(R.id.info_checkmark);
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
            BioInfo bioInfo = passportInfo.get(position);
            holder.tag.setText(bioInfo.tag);
            holder.value.setText(bioInfo.value);
            if (bioInfo.tag.equals(Utils.PASSPORT_BIRTHDATE_STR)) {
                if (ageEligible) {
                    holder.checkMark.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_action_check));
                } else {
                    holder.checkMark.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_action_err));
                }
            }
            if (bioInfo.tag.equals(Utils.PASSPORT_NATIONALITY_STR)) {
                if (nationalityEligible) {
                    holder.checkMark.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_action_check));
                } else {
                    holder.checkMark.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_action_err));
                }
            }
        }

        @Override
        public int getItemCount() {
            return passportInfo.size();
        }

        public void setAgeElibility(boolean eligible) {
            this.ageEligible = eligible;
        }

        public void setNationalityElibility(boolean eligible) {
            this.nationalityEligible = eligible;
        }

    }


}
