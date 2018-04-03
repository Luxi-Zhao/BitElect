package com.example.lucyzhao.votingapp;

/**
 * Created by LucyZhao on 2018/4/3.
 */

public class Block {
    private String hash, prevHash;
    private String blockID;
    private String crypt;
    private String valid;
    private int numBlocks;

    // UI colors
    private int hashColor, prevHashColor;

    Block() {

    }
    Block(int hashColor, int prevHashColor) {
        this.hashColor = hashColor;
        this.prevHashColor = prevHashColor;
    }

    public String getValid() {
        return this.valid;
    }

    public void setValid(String valid) {
        this.valid = valid;
    }

    public String getHash() {
        return this.hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getPrevHash() {
        return this.prevHash;
    }

    public void setPrevHash(String prevhash) {
        this.prevHash = prevhash;
    }

    public String getBlockID() {
        return this.blockID;
    }

    public void setBlockID(String blockID) {
        this.blockID = blockID;
    }

    public String getCrypt() {
        return this.crypt;
    }

    public void setCrypt(String crypt) {
        this.crypt = crypt;
    }

    public int getHashColor() {
        return this.hashColor;
    }

    public void setHashColor(int hashColor) {
        this.hashColor = hashColor;
    }

    public int getPrevHashColor() {
        return this.prevHashColor;
    }

    public void setPrevHashColor(int prevHashColor) {
        this.prevHashColor = prevHashColor;
    }

    public int getNumBlocks() {
        return this.numBlocks;
    }

    public void setNumBlocks(int numBlocks) {
        this.numBlocks = numBlocks;
    }
}
