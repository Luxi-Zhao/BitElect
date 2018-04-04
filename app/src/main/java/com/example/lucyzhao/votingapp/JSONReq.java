package com.example.lucyzhao.votingapp;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
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
import static com.example.lucyzhao.votingapp.Utils.COMM_CAND1_FN;
import static com.example.lucyzhao.votingapp.Utils.COMM_CAND1_LN;
import static com.example.lucyzhao.votingapp.Utils.COMM_CAND2_FN;
import static com.example.lucyzhao.votingapp.Utils.COMM_CAND2_LN;
import static com.example.lucyzhao.votingapp.Utils.COMM_KEY_KEY;
import static com.example.lucyzhao.votingapp.Utils.COMM_KEY_MSG;
import static com.example.lucyzhao.votingapp.Utils.COMM_KEY_TYPE;
import static com.example.lucyzhao.votingapp.Utils.COMM_KEY_VALID;
import static com.example.lucyzhao.votingapp.Utils.COMM_NFC_ID;
import static com.example.lucyzhao.votingapp.Utils.COMM_REQ_CHAIN;
import static com.example.lucyzhao.votingapp.Utils.COMM_REQ_KEY;
import static com.example.lucyzhao.votingapp.Utils.COMM_REQ_RESULT;
import static com.example.lucyzhao.votingapp.Utils.COMM_RESPONSE_TYPE;
import static com.example.lucyzhao.votingapp.Utils.COMM_RESULT_CAND1V;
import static com.example.lucyzhao.votingapp.Utils.COMM_RESULT_CAND2V;
import static com.example.lucyzhao.votingapp.Utils.COMM_RESULT_VALID;
import static com.example.lucyzhao.votingapp.Utils.VOTING_URL;
import static java.lang.Thread.sleep;

/**
 * Created by LucyZhao on 2018/4/2.
 */

public class JSONReq {
    private static final String TAG = JSONReq.class.getSimpleName();
    private static final int MAX_RETRY = 5;

    /**
     *
     * @param context
     * @param nfcID doc num
     * @param blockID
     * @return null if an exception happened
     *         empty block if other problems happened
     *         block with data under normal conditions
     */
    static Block getBlock(Context context, String nfcID, String blockID) {
        Block block = new Block();

        /* ------------ prepare url ----------------*/
        String url = VOTING_URL
                + "REQUESTTYPE=" + COMM_REQ_CHAIN + "&"
                + COMM_NFC_ID + "=" + nfcID + "&"
                + COMM_BLOCKID + "=" + blockID;

        String cleanurl = VOTING_URL + COMM_NFC_ID + "=" + nfcID;
        Log.v(TAG, "url is: " + url);

        JsonObject json = getJSON(context, url, cleanurl);

        /* ----------- process result ---------------*/
        if (json != null) {
            try {
                String valid = json.get(COMM_BLOCK_VALID).toString();
                Log.v(TAG, "valid is: " + valid);

                if(valid.equals("\"T\"")) {
                    block.setValid(Utils.TRUE);
                }
                else {
                    block.setValid(Utils.FALSE);
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

                block.setHashColor(Color.RED);
                block.setPrevHashColor(Color.GREEN);

            } catch (Exception e) {
               return null;
            }
        } else {
            return null;
        }
        return block;
    }

    /**
     *
     * @param context
     * @param nfcID doc num
     * @return valid, cand1fn, cand1ln, cand2fn, cand2ln, cand1votes, cand2votes
     */
    static String[] getPollResult(Context context, String nfcID) {
        String[] ret = new String[7];

        /* ------------ prepare url ----------------*/
        String url = VOTING_URL
                + "REQUESTTYPE=" + COMM_REQ_RESULT + "&"
                + COMM_NFC_ID + "=" + nfcID;

        String cleanurl = VOTING_URL + COMM_NFC_ID + "=" + nfcID;
        Log.v(TAG, "url is: " + url);

        JsonObject json = getJSON(context, url, cleanurl);

        /* ----------- process result ---------------*/
        if (json != null) {
            try {
                String valid = json.get(COMM_RESULT_VALID).toString();
                Log.v(TAG, "result valid is: " + valid);

                if(valid.equals("\"T\"")) {
                    ret[0] = Utils.TRUE;
                }
                else {
                    ret[0] = Utils.FALSE;
                }

                String cand1fn = json.get(COMM_CAND1_FN).toString();
                String cand1ln = json.get(COMM_CAND1_LN).toString();
                String cand2fn = json.get(COMM_CAND2_FN).toString();
                String cand2ln = json.get(COMM_CAND2_LN).toString();

                String cand1Votes = json.get(COMM_RESULT_CAND1V).toString();
                String cand2Votes = json.get(COMM_RESULT_CAND2V).toString();

                ret[1] = cand1fn;
                ret[2] = cand1ln;
                ret[3] = cand2fn;
                ret[4] = cand2ln;
                ret[5] = cand1Votes;
                ret[6] = cand2Votes;

            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
        return ret;
    }

    /**
     *
     * @param context
     * @param nfcID doc num
     * @return valid,
     */
    static String[] sendKey(Context context, String nfcID, String key, String keyType) {
        String[] ret = new String[2];

        /* ------------ prepare url ----------------*/
        String url = VOTING_URL
                + "REQUESTTYPE=" + COMM_REQ_KEY + "&"
                + COMM_NFC_ID + "=" + nfcID + "&"
                + COMM_KEY_KEY + "=" + key + "&"
                + COMM_KEY_TYPE + "=" + keyType;

        String cleanurl = VOTING_URL + COMM_NFC_ID + "=" + nfcID;
        Log.v(TAG, "url is: " + url);

        JsonObject json = getJSON(context, url, cleanurl);

        /* ----------- process result ---------------*/
        if (json != null) {
            try {
                String valid = json.get(COMM_KEY_VALID).toString();
                Log.v(TAG, "result valid is: " + valid);

                if(valid.equals("\"T\"")) {
                    ret[0] = Utils.TRUE;
                }
                else {
                    ret[0] = Utils.FALSE;
                    ret[1] = json.get(COMM_KEY_MSG).toString();
                }

            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
        return ret;
    }

    private static JsonObject getJSON(Context context, String url, String cleanurl) {
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
            return null;
        }
        return json;
    }

}
