package com.example.lucyzhao.votingapp;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;

import static com.example.lucyzhao.votingapp.Utils.COMM_BLOCKID;
import static com.example.lucyzhao.votingapp.Utils.COMM_BLOCK_CUMU_CRYPT;
import static com.example.lucyzhao.votingapp.Utils.COMM_BLOCK_HASH;
import static com.example.lucyzhao.votingapp.Utils.COMM_BLOCK_PREV_HASH;
import static com.example.lucyzhao.votingapp.Utils.COMM_BLOCK_RESPONSE;
import static com.example.lucyzhao.votingapp.Utils.COMM_BLOCK_TOTAL_NUM;
import static com.example.lucyzhao.votingapp.Utils.COMM_BLOCK_VALID;
import static com.example.lucyzhao.votingapp.Utils.COMM_NFC_ID;
import static com.example.lucyzhao.votingapp.Utils.COMM_REQ_CHAIN;
import static com.example.lucyzhao.votingapp.Utils.COMM_RESPONSE_TYPE;
import static com.example.lucyzhao.votingapp.Utils.VOTING_URL;
import static java.lang.Thread.sleep;

/**
 * Created by LucyZhao on 2018/4/2.
 */

public class JSONReq {
    private static final String TAG = JSONReq.class.getSimpleName();
    private static final int MAX_RETRY = 5;

    static Block getBlock(Context context, String nfcID, String blockID) {
        Block block = new Block();

        /* ------------ prepare url ----------------*/
        String url = VOTING_URL
                + "REQUESTTYPE=" + COMM_REQ_CHAIN + "&"
                + COMM_NFC_ID + "=" + nfcID + "&"
                + COMM_BLOCKID + "=" + blockID;

        String cleanurl = VOTING_URL + COMM_NFC_ID + "=" + "1234";
        Log.v(TAG, "url is: " + url);

        JsonObject json = null;

        int retryCounter = 0;

        try {
            /* ----------issue initial req --------------*/
            json = Ion.with(context.getApplicationContext())
                    .load(url)
                    .asJsonObject()
                    .get();

            Log.v(TAG, "output is: " + json.toString());

            /* -----------retry if fails --------------*/
            while (json.get(COMM_RESPONSE_TYPE).toString().equals("\"NULL\"")) {
                sleep(100);
                json = Ion.with(context.getApplicationContext())
                        .load(cleanurl)
                        .asJsonObject()
                        .get();

                Log.v(TAG, "output is: " + json.toString());

                retryCounter++;
                Log.v(TAG, "count:" + retryCounter);

                if (retryCounter > MAX_RETRY) {
                    Log.v(TAG, "Retried 5 times!");
                    throw new RuntimeException();
                }
            }
        } catch (Exception e) {
            Toast.makeText(context.getApplicationContext(), "Error getting response from server!", Toast.LENGTH_SHORT).show();
        }

        /* ----------- process result ---------------*/
        if (json != null) {
            try {
                String valid = json.get(COMM_BLOCK_VALID).toString();
                Log.v(TAG, "valid is: " + valid);

                if(valid.equals("\"T\"")) {
                    block.setValid("True");
                }
                else {
                    block.setValid("False");
                }

                String hash = json.get(COMM_BLOCK_HASH).toString();
                block.setHash(hash);
                Log.v(TAG, "hash is " + hash);
                String prevHash = json.get(COMM_BLOCK_PREV_HASH).toString();
                block.setPrevHash(prevHash);
                String crypt = json.get(COMM_BLOCK_CUMU_CRYPT).toString();
                block.setCrypt(crypt);
                String numBlocks = json.get(COMM_BLOCK_TOTAL_NUM).toString();
                int num = Integer.parseInt(numBlocks);
                block.setNumBlocks(num);
                //todo does BLOCKID mean the sender's block ID?

            } catch (Exception e) {
                Toast.makeText(context.getApplicationContext(), R.string.json_err, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context.getApplicationContext(), R.string.json_err, Toast.LENGTH_SHORT).show();
        }
        return block;

    }
}
