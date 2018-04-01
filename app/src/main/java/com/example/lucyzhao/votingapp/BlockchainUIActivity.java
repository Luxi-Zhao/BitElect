package com.example.lucyzhao.votingapp;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlockchainUIActivity extends NavActivity {
    RecyclerView recyclerView;
    BlockchainAdapter blockchainAdapter;
    List<Block> blockchain = new ArrayList<>();
    LinearLayoutManager llm;
    private static final String TAG = BlockchainUIActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.onCreateDrawer(R.layout.activity_blockchain_ui);

        recyclerView = findViewById(R.id.blockchain_recycler);
        llm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llm);
        blockchain.add(new Block());
        blockchain.add(new Block());
        blockchain.add(new Block());
        blockchain.add(new Block());

        this.blockchainAdapter = new BlockchainAdapter(blockchain);

        recyclerView.setAdapter(this.blockchainAdapter);
    }

    public class Block {
        private String data;
        private String hash, prevHash;
        private String nonce;
    }

    /**
     * Cannot update list for now
     */
    public class BlockchainAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        List<Object> heteroList = new ArrayList<>();
        private static final String CHAIN_STR = "chain";
        private static final int BLOCK = 1, CHAIN = 0;

        public BlockchainAdapter(List<Block> list) {
            for (Block block : list) {
                heteroList.add(block);
                heteroList.add(CHAIN_STR);
            }
            heteroList.remove(heteroList.size() - 1);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder viewHolder = null;
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            switch (viewType) {
                case BLOCK:
                    View v1 = inflater.inflate(R.layout.block_item, parent, false);
                    viewHolder = new BlockViewHolder(v1);
                    break;
                case CHAIN:
                    View v2 = inflater.inflate(R.layout.chain_item, parent, false);
                    viewHolder = new ChainViewHolder(v2);
                    break;
                default:
                    View v3 = inflater.inflate(R.layout.chain_item, parent, false);
                    viewHolder = new ChainViewHolder(v3);
                    break;
            }
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case BLOCK:
                    BlockViewHolder vh1 = (BlockViewHolder) holder;
                    configureBlockViewHolder(vh1, position);
                    break;
                case CHAIN:
                    // do nothing for now
                    break;
                default:
                    // do nothing for now
                    break;
            }
        }

        private void configureBlockViewHolder(BlockViewHolder vh, int position) {
            Random rnd = new Random();
            int r = rnd.nextInt(256);
            int g = rnd.nextInt(256);
            int b = rnd.nextInt(256);
            int color = Color.rgb(r, g, b);
            int borderColor = Color.rgb(r - 40, g - 40, b - 40);
            setHashDrawable(vh.hash, color, borderColor);

            if(position == 0) {
                setPrevHashDrawable(vh.prevHash, Color.RED, Color.BLUE);
                return;
            }
            BlockViewHolder prevView = (BlockViewHolder)recyclerView.findViewHolderForAdapterPosition(position - 2);

            if(prevView == null) {
                Log.v(TAG, "prev view null, return");
                return;
            }
            GradientDrawable drawable = (GradientDrawable)prevView.prevHash.getBackground();
            if(drawable == null) {
                Log.v(TAG, "drawable null, return");
                return;
            }
            int prevColor = drawable.getColors()[0];
            setPrevHashDrawable(vh.prevHash, prevColor, Color.BLUE);
        }

        private void setHashDrawable(View hashView, int bgColor, int borderColor) {
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.RECTANGLE);
            shape.setCornerRadii(new float[]{0, 0, 0, 0, 16, 16, 0, 0});
            shape.setColor(bgColor);
            shape.setStroke(5, borderColor);
            hashView.setBackground(shape);
        }

        private void setPrevHashDrawable(View hashView, int bgColor, int borderColor) {
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.RECTANGLE);
            shape.setCornerRadii(new float[]{8, 8, 0, 0, 0, 0, 0, 0});
            shape.setColor(bgColor);
            shape.setStroke(5, borderColor);
            hashView.setBackground(shape);
        }

        private int getBlockIndex(int heteroListPos) {
            return heteroListPos / 2;
        }

        @Override
        public int getItemCount() {
            return heteroList.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (position % 2 == 0) return BLOCK;
            else return CHAIN;
        }

        private class BlockViewHolder extends RecyclerView.ViewHolder {
            TextView hash, prevHash;

            private BlockViewHolder(View itemView) {
                super(itemView);
                hash = itemView.findViewById(R.id.block_hash);
                prevHash = itemView.findViewById(R.id.block_prev_hash);
            }
        }

        private class ChainViewHolder extends RecyclerView.ViewHolder {
            private ChainViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
}
