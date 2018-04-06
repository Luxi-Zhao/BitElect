package com.example.lucyzhao.votingapp.blockchain_ui;

/**
 * Created by LucyZhao on 2018/4/3.
 *
 * Data class
 * Represents a block in a blockchain
 */

public class Block {
    private String hash, prevHash;
    private String blockID; //location of the block in the chain
    private String crypt;   //cumulative encryption
    private String valid;   //whether the block is valid
    private int numBlocks;  //number of blocks in the chain

    // UI colors
    private int hashColor, prevHashColor;

    public Block() {

    }
    public Block(int hashColor, int prevHashColor) {
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
