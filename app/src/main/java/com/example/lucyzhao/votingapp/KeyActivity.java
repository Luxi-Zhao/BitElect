package com.example.lucyzhao.votingapp;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class KeyActivity extends NavActivity {

    private RadioGroup rg;
    private EditText key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.onCreateDrawer(R.layout.activity_key);
        Utils.setTypeFace(this, R.id.key_title_txt);

        key = findViewById(R.id.key_private_4hex);
        rg = findViewById(R.id.key_type_group);

        key.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    getIntStr();
                }
                return false;
            }
        });

    }

    private String getIntStr() {
        String keyHex = key.getText().toString();
        if(keyHex.equals("")) {
            return "";
        }
        else if(!keyHex.matches("-?[0-9a-fA-F]+")) {
            Toast.makeText(this, R.string.key_format_wrong, Toast.LENGTH_SHORT).show();
            key.setText("");
            return "";
        }
        int keyInt = Integer.parseInt(keyHex, 16);
        TextView keyIntTxt = findViewById(R.id.key_private_int);
        String intStr = Integer.toString(keyInt);
        keyIntTxt.setText(intStr);
        return intStr;
    }

    private String getSelectedCandidateID() {
        String keyType = "";
        int id = rg.getCheckedRadioButtonId();
        // Check which radio button was clicked
        switch (id) {
            case R.id.key_type_L:
                keyType = "L";
                break;

            case R.id.key_type_M:
                keyType = "M";
                break;
        }
        return keyType;
    }

    public void sendKey(View view) {
        String keyType = getSelectedCandidateID();
        String keyInt = getIntStr();

        if (keyType.equals("") || keyInt.equals("")) {
            Toast.makeText(getApplicationContext(), R.string.empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        String nfcID = Utils.getDocNum(this);
        if (nfcID.equals("")) {
            Toast.makeText(getApplicationContext(), R.string.navigation_drawer_docnum_none, Toast.LENGTH_SHORT).show();
            return;
        }

        String[] ret = JSONReq.sendKey(this, nfcID, keyInt, keyType);
        if (ret == null) {
            Toast.makeText(getApplicationContext(), R.string.key_sending_err, Toast.LENGTH_LONG).show();
            return;
        }
        if (ret[0].equals(Utils.TRUE)) {
            Toast.makeText(getApplicationContext(), R.string.key_accepted, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), R.string.key_rejected, Toast.LENGTH_LONG).show();
        }
    }
}
