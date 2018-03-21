package com.example.lucyzhao.votingapp;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by LucyZhao on 2018/3/8.
 */

public class Utils {
    public static final String QR_RESULT = "qr_result";
    public static final String NFC_RESULT = "nfc_result";

    public static final String COMM_NFC_ID = "NFCID";
    public static final String COMM_QR_CODE = "QRCODE";
    public static final String COMM_CANDIDATE_ID = "CANDIDATEID";

    public static final String AVATAR_URL = "avatar_url";
    public static final String NAME = "name";
    public static final String BIO = "bio";

    public static final String PASSPORT_BIRTHDATE_STR = "Birthdate";
    public static final String PASSPORT_NATIONALITY_STR = "Nationality";
    public static final String ALLOWABLE_NATIONALITY = "CHN";

    public static final int ALLOWABLE_AGE = 18;

    public static boolean checkNationalityEligiblity(String nationality) {
        return nationality.equals(ALLOWABLE_NATIONALITY);
    }

    /**
     * Birthday in the format yymmdd
     * @param birthDate
     * @return
     */
    public static boolean checkAgeEligibility(String birthDate) {

        int year = Integer.parseInt(birthDate.substring(0, 2));
        int month = Integer.parseInt(birthDate.substring(2, 4));
        int date = Integer.parseInt(birthDate.substring(4, 6));

        Calendar bDay = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        bDay.set(year, month, date);
        int age = today.get(Calendar.YEAR) - bDay.get(Calendar.YEAR);
        if(today.get(Calendar.DAY_OF_YEAR) < bDay.get(Calendar.DAY_OF_YEAR)) age--;

        return age >= ALLOWABLE_AGE;
    }

}
