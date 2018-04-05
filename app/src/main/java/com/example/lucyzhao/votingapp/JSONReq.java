package com.example.lucyzhao.votingapp;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;

import static com.example.lucyzhao.votingapp.Utils.COMM_BLOCKID;
import static com.example.lucyzhao.votingapp.Utils.COMM_BLOCK_CUMU_CRYPT;
import static com.example.lucyzhao.votingapp.Utils.COMM_BLOCK_HASH;
import static com.example.lucyzhao.votingapp.Utils.COMM_BLOCK_PREV_HASH;
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
 *
 * Class that handles communication with the WIFI module
 */

public class JSONReq {
    private static final String TAG = JSONReq.class.getSimpleName();
    private static final int MAX_RETRY = 5;

    /**
     * @param nfcID   doc num
     * @return null if an exception happened
     * empty block if other problems happened
     * block with data under normal conditions
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

                if (valid.equals("\"T\"")) {
                    block.setValid(Utils.TRUE);
                } else {
                    block.setValid(Utils.FALSE);
                }

                // hash
                JsonElement hashJ = json.get(COMM_BLOCK_HASH);
                String hash = "0";
                if(hashJ != null) {
                    hash = hashJ.toString().replaceAll("[\"]", "");
                }
                block.setHash(hash);
                Log.v(TAG, "hash is " + hash);
                int hashInt = Integer.parseInt(hash);

                // prev hash
                JsonElement prevHJ = json.get(COMM_BLOCK_PREV_HASH);
                String prevHash = "0";
                if(prevHJ != null) {
                    prevHash = prevHJ.toString().replaceAll("[\"]", "");
                }
                block.setPrevHash(prevHash);
                int prevHashInt = Integer.parseInt(prevHash);

                // cumulative crypt
                JsonElement cryptJ = json.get(COMM_BLOCK_CUMU_CRYPT);
                String crypt = "0";
                if(cryptJ != null) {
                    crypt = cryptJ.toString().replaceAll("[\"]", "");
                }
                block.setCrypt(crypt);

                String numBlocks = json.get(COMM_BLOCK_TOTAL_NUM).toString();
                int num = Integer.parseInt(numBlocks);
                block.setNumBlocks(num);

                block.setBlockID(blockID);

                int[] colors = getColorsFromHash(hashInt, prevHashInt);
                block.setHashColor(colors[0]);
                block.setPrevHashColor(colors[1]);

            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
        return block;
    }

    /**
     * Convert hash and prevHash values to colors to display
     * on the blockchain UI
     * @return hashColor, prevHashColor
     */
    private static int[] getColorsFromHash(int hash, int prevHash) {
        int[] ret = new int[2];
        int hashR = hash % 256;
        int hashG = (hash * 13) % 256;
        int hashB = (hash * 21) % 256;

        int hashC = Color.rgb(hashR, hashG, hashB);

        int prevR = prevHash % 256;
        int prevG = (prevHash * 13) % 256;
        int prevB = (prevHash * 21) % 256;

        int prevHC = Color.rgb(prevR, prevG, prevB);
        ret[0] = hashC;
        ret[1] = prevHC;
        return ret;
    }

    /**
     * @param nfcID   doc num
     * @return valid, cand1fn, cand1ln, cand2fn, cand2ln,
     *         cand1votes, cand2votes are optionally returned
     *         when they are not returned, their values are set to
     *         0
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

                if (valid.equals("\"T\"")) {
                    ret[0] = Utils.TRUE;
                } else {
                    ret[0] = Utils.FALSE;
                }

                String cand1fn = json.get(COMM_CAND1_FN).toString().replaceAll("[\"]", "");
                String cand1ln = json.get(COMM_CAND1_LN).toString().replaceAll("[\"]", "");
                String cand2fn = json.get(COMM_CAND2_FN).toString().replaceAll("[\"]", "");
                String cand2ln = json.get(COMM_CAND2_LN).toString().replaceAll("[\"]", "");

                // cand1 votes and cand2 votes might not be returned
                JsonElement cand1v = json.get(COMM_RESULT_CAND1V);
                JsonElement cand2v = json.get(COMM_RESULT_CAND2V);
                String cand1Votes, cand2Votes;
                if (cand1v != null && cand2v != null) {
                    cand1Votes = cand1v.toString();
                    cand2Votes = cand2v.toString();
                } else {
                    cand1Votes = "0";
                    cand2Votes = "0";
                }

                ret[1] = cand1fn;
                ret[2] = cand1ln;
                ret[3] = cand2fn;
                ret[4] = cand2ln;
                ret[5] = cand1Votes;
                ret[6] = cand2Votes;

            } catch (Exception e) {
                Log.v(TAG, "in exception");
                Log.v(TAG, e.toString());
                return null;
            }
        } else {
            Log.v(TAG, "json is null!!!");
            return null;
        }
        return ret;
    }

    /**
     * @param nfcID   doc num
     * @return valid
     *         rejection msg if key is not valid
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

                if (valid.equals("\"T\"")) {
                    ret[0] = Utils.TRUE;
                } else {
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

    /**
     * Makes a synchronous request to the WIFI module
     * @return JSON object containing the response
     */
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
