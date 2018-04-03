package com.example.lucyzhao.votingapp;

import android.app.DialogFragment;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlockchainUIActivity extends NavActivity {
    RecyclerView recyclerView;
    BlockchainAdapter blockchainAdapter;
    List<Block> blockchain = new ArrayList<>();
    LinearLayoutManager llm;
    private static final int INIT_COLOR = Color.RED;
    private static final String TAG = BlockchainUIActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.onCreateDrawer(R.layout.activity_blockchain_ui);

        recyclerView = findViewById(R.id.blockchain_recycler);
        llm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llm);
        for (int i = 0; i < 10; i++) {
            int bgColor = generateRandColor();
            int prevColor;
            if (i == 0) prevColor = INIT_COLOR;
            else prevColor = blockchain.get(i - 1).hashColor;
            Block b = new Block(bgColor, prevColor);
            b.hash = "asdfasdf";
            b.prevHash = "prefvpadsfasd";
            b.blockID = Integer.toString(i);
            blockchain.add(b);
        }
        this.blockchainAdapter = new BlockchainAdapter(blockchain);

        recyclerView.setAdapter(this.blockchainAdapter);
    }

    public class Block {
        private String data;
        private String hash, prevHash;
        private String blockID;

        // UI colors
        private int hashColor, prevHashColor;

        Block(int hashColor, int prevHashColor) {
            this.hashColor = hashColor;
            this.prevHashColor = prevHashColor;
        }
    }

    private int generateRandColor() {
        Random rnd = new Random();
        int r = rnd.nextInt(200);
        int g = rnd.nextInt(200);
        int b = rnd.nextInt(200);
        int color = Color.rgb(r, g, b);
        return color;
    }


    /**
     * Cannot update list for now
     */
    public class BlockchainAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        List<Object> heteroList = new ArrayList<>();
        private static final String CHAIN_STR = "chain";
        private static final int BLOCK = 1, CHAIN = 0;

        /**
         * @param list a list of data holders containing
         *             info of each block
         */
        public BlockchainAdapter(List<Block> list) {
            for (Block block : list) {
                heteroList.add(block);
                heteroList.add(CHAIN_STR);
            }
            heteroList.remove(heteroList.size() - 1);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder viewHolder;
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
                    // do nothing
                    break;
                default:
                    // do nothing
                    break;
            }
        }

        private void configureBlockViewHolder(BlockViewHolder vh, int position) {
            final Block b = blockchain.get(getBlockIndex(position));
            int color = b.hashColor;
            int prevColor = b.prevHashColor;
            setCubeColor(vh.hash, color);
            setCubeColor(vh.prevHash, prevColor);
            vh.blockItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BlockInfoFragment.newInstance(b).show(getFragmentManager(), "blockInfoFrag");
                }
            });

            // show prev hash and hash hints for genesis block
            if(getBlockIndex(position) == 0) {
                vh.prevHash.setText(R.string.block_prevhash_hint);
                vh.hash.setText(R.string.block_hash_hint);
            }
            else{
                vh.prevHash.setText("");
                vh.hash.setText("");
            }
        }

        private void setCubeColor(View hashView, int bgColor) {
            hashView.setBackgroundTintList(ColorStateList.valueOf(bgColor));
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
            RelativeLayout blockItem;

            private BlockViewHolder(View itemView) {
                super(itemView);
                hash = itemView.findViewById(R.id.block_hash);
                prevHash = itemView.findViewById(R.id.block_prev_hash);
                blockItem = itemView.findViewById(R.id.block_item_layout);
            }
        }

        private class ChainViewHolder extends RecyclerView.ViewHolder {
            private ChainViewHolder(View itemView) {
                super(itemView);
            }
        }
    }

    public static class BlockInfoFragment extends DialogFragment {
        String prevHashStr, hashStr, blockIDStr;
        TextView validity, prevHash, hash, blockID, cumuCrypt;

        public static BlockInfoFragment newInstance(Block block) {
            BlockInfoFragment f = new BlockInfoFragment();

            // Supply index input as an argument.
            Bundle args = new Bundle();
            args.putString("hash", block.hash);
            args.putString("prevHash", block.prevHash);
            args.putString("blockID", block.blockID);

            f.setArguments(args);

            return f;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Bundle bundle = getArguments();
            if (bundle == null) return;
            prevHashStr = bundle.getString("prevHash");
            hashStr = bundle.getString("hash");
            blockIDStr = bundle.getString("blockID");
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View fragment = inflater.inflate(R.layout.fragment_block_info, container, false);
            validity = fragment.findViewById(R.id.block_validity_txt);
            prevHash = fragment.findViewById(R.id.prev_hash_txt);
            hash = fragment.findViewById(R.id.hash_txt);
            blockID = fragment.findViewById(R.id.block_id_title_txt);
            cumuCrypt = fragment.findViewById(R.id.crypt_txt);

            Typeface typeface = getActivity().getResources().getFont(R.font.quicksand);
            blockID.setTypeface(typeface);

            prevHash.setText(prevHashStr);
            hash.setText(hashStr);
            String blockStr = "Block ID " + blockIDStr;
            blockID.setText(blockStr);
            return fragment;
        }
    }

}
