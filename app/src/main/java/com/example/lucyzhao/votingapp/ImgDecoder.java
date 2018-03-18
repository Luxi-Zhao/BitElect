package com.example.lucyzhao.votingapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import jj2000.j2k.decoder.Decoder;
import jj2000.j2k.util.ParameterList;

/**
 * Credits to https://github.com/tananaev/passport-reader
 */
public class ImgDecoder {
    private static final String TAG = ImgDecoder.class.getSimpleName();
    private static final String IMAGE_CACHE_DIR = "/cachedImg.jp2";
    private static final String PPM_DIR = "/img.ppm";

    public static Bitmap decodeImage(Context context, String mimeType, InputStream inputStream) throws IOException {
        if (mimeType.equalsIgnoreCase("image/jp2") || mimeType.equalsIgnoreCase("image/jpeg2000")) {

            String jp2Path = context.getCacheDir().getAbsolutePath() + IMAGE_CACHE_DIR;
            String ppmPath = context.getCacheDir().getAbsolutePath() + PPM_DIR;

            writeInputStreamToFile(jp2Path, inputStream);
            decodeJp2File(jp2Path, ppmPath);
            Log.v(TAG, "reading img");
            return readImageFromPPM(ppmPath);

        } else if (mimeType.equalsIgnoreCase("image/x-wsq")) {
            return null;
        } else {
            return BitmapFactory.decodeStream(inputStream);
        }

    }

    private static void writeInputStreamToFile(String path, InputStream is) {
        try {
            File file = new File(path);
            FileOutputStream fos = new FileOutputStream(file);
            try {
                byte[] buffer = new byte[1024]; // or other buffer size
                int len;

                while ((len = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.flush();
            } finally {
                fos.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * The decoder reads in a .jp2 file pointed to by fileInPath and outputs
     * a .ppm file pointed to by fileOutPath
     * @param fileInPath points to .jp2 file to be decoded
     * @param fileOutPath points to .ppm file
     */
    private static void decodeJp2File(String fileInPath, String fileOutPath) {
        ParameterList pl = new ParameterList();
        String[][] allParameters = Decoder.getAllParameters();

        for (int i = 0; i < allParameters.length; i++) {
            if (allParameters[i][3] != null) {
                pl.setProperty(allParameters[i][0], allParameters[i][3]);
            }
        }
        pl.setProperty("i", fileInPath);
        pl.setProperty("o", fileOutPath);

        ParameterList defParams = new ParameterList(pl);
        Decoder decoder = new Decoder(defParams);
        Log.v(TAG, "decoder running");
        decoder.run();
    }

    /**
     * Inflates a bitmap from the given PPM file pointed to by path
     * @param path path to the PPM file
     * @return bitmap
     */
    private static Bitmap readImageFromPPM(String path) {
        try {
            BufferedInputStream reader = new BufferedInputStream(new FileInputStream(new File(path)));
            if (!checkMagicID(reader)) {
                return null;
            }
            int width = readASCIIDecimal(reader);
            int height = readASCIIDecimal(reader);
            int maxColor = readASCIIDecimal(reader);
            Log.v(TAG, "width: " + width + " height: " + height + " max color: " + maxColor);

            if (!checkMaxColor(maxColor)) {
                return null;
            }

            int[] colors = new int[width * height];
            int numPixels = 0;
            for (int i = 0; i < width * height; i++) {
                int r = readUnsignedByte(reader);
                int g = readUnsignedByte(reader);
                int b = readUnsignedByte(reader);
                colors[numPixels] = Color.rgb(r, g, b);
                numPixels++;
            }

            return Bitmap.createBitmap(colors, width, height, Bitmap.Config.ARGB_8888);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Only PPM files in the format of P6 is supported
     * @param is input stream
     * @return whether this PPM file can be decoded
     * @throws IOException
     */
    private static boolean checkMagicID(InputStream is) throws IOException {
        byte[] magicID = new byte[2];
        int len;
        len = is.read(magicID);
        Log.v(TAG, "magic id is " + Character.toString((char) magicID[0]) + Character.toString((char) magicID[1]));
        if (len != 2) {
            return false;
        } else if (magicID[0] != 'P' || magicID[1] != '6') {
            return false;
        }
        is.read(); //eat whitespace
        return true;
    }

    /**
     * The decoder only supports RGB values ranging from 0 - 255
     * @param maxColor the max range
     * @return whether this range is supported
     */
    private static boolean checkMaxColor(int maxColor) {
        return maxColor == 255;
    }

    private static boolean isWhiteSpace(char c) {
        return c == ' ' || c == '\t' || c == '\r' || c == '\n';
    }

    /**
     * Reads a sequence of chars terminated by an empty character
     * @param is input stream
     * @return the integer represented by the sequence of chars
     * @throws IOException
     */
    private static int readASCIIDecimal(InputStream is) throws IOException {
        StringBuilder s = new StringBuilder();
        char c;
        while (!isWhiteSpace(c = (char) is.read())) {
            s.append(c);
        }
        return Integer.parseInt(s.toString().trim());
    }

    /**
     * Convert a byte read from input stream to unsigned integer
     * Bytes in Java are Two's Complements, ranging from -128 - 127,
     * but RGB values range from 0 - 255
     * @param is input stream
     * @return unsigned integer representation of the byte
     * @throws IOException
     */
    private static int readUnsignedByte(InputStream is) throws IOException {
        return is.read() & 0xff;
    }

}

