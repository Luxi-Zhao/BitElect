package com.example.lucyzhao.votingapp;

import android.app.DialogFragment;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.example.lucyzhao.votingapp.Utils.getDocNum;

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

        this.blockchainAdapter = new BlockchainAdapter(blockchain);

        recyclerView.setAdapter(this.blockchainAdapter);

        String nfcID = getDocNum(this);
        if (nfcID.equals("")) {
            Toast.makeText(this, R.string.navigation_drawer_docnum_none, Toast.LENGTH_LONG).show();
        } else {
            // populate the list with data got from wifi
            new GetBlocksTask(this).execute(nfcID);
        }

    }

    /**
     * For testing only
     */
    private void initList() {
        for (int i = 0; i < 10; i++) {
            int bgColor = generateRandColor();
            int prevColor;
            if (i == 0) prevColor = INIT_COLOR;
            else prevColor = blockchain.get(i - 1).getHashColor();
            Block b = new Block(bgColor, prevColor);
            b.setHash("asdfadsf");
            b.setPrevHash("asdfasdfasdf");
            b.setBlockID(Integer.toString(i));
            blockchain.add(b);
        }
    }


    private static int generateRandColor() {
        Random rnd = new Random();
        int r = rnd.nextInt(200);
        int g = rnd.nextInt(200);
        int b = rnd.nextInt(200);
        int color = Color.rgb(r, g, b);
        return color;
    }


    private static class GetBlocksTask extends AsyncTask<String, Void, List<Block>> {
        WeakReference<BlockchainUIActivity> activityRef;

        GetBlocksTask(BlockchainUIActivity activity) {
            this.activityRef = new WeakReference<>(activity);
        }

        /**
         * @param params nfcID
         * @return null if can't get blockchain size
         * otherwise, return a list of blocks of blockchain size
         * if individual block retrieval failed, uses empty blocks as
         * placeholders
         */
        protected List<Block> doInBackground(String... params) {
            BlockchainUIActivity activity = activityRef.get();
            if (activity == null || activity.isFinishing()) return null;

            List<Block> list = new ArrayList<>();
            Context context = activity.getApplicationContext();
            String nfcID = params[0];

            // get blockchain size
            Block b = JSONReq.getBlock(context, nfcID, "0");
            if (b == null) {
                return null;
            }
            list.add(0, b);
            int size = b.getNumBlocks();

            // get other blocks
            for (int i = 1; i < size; i++) {
                Block block = JSONReq.getBlock(context, nfcID, Integer.toString(i));
                if (block != null) {
                    list.add(i, block);
                } else {
                    list.add(i, new Block());
                }

            }
//            for (int i = 0; i < 10; i++) {
//                int bgColor = generateRandColor();
//                int prevColor;
//                if (i == 0) prevColor = INIT_COLOR;
//                else prevColor = list.get(i - 1).getHashColor();
//                Block b = new Block(bgColor, prevColor);
//                b.setHash("asdfadsf");
//                b.setPrevHash("asdfasdfasdf");
//                b.setBlockID(Integer.toString(i));
//                list.add(b);
//            }
            return list;
        }


        protected void onPostExecute(List<Block> blocks) {
            BlockchainUIActivity activity = activityRef.get();
            if (activity == null || activity.isFinishing()) return;
            if (blocks == null) {
                Toast.makeText(activity, R.string.block_getting_err, Toast.LENGTH_SHORT).show();
                return;
            }

            activity.blockchain = blocks;
            activity.blockchainAdapter.updateInnerList(activity.blockchain);
        }
    }

    //////////////////////////////////////////////////////
    //////////////////RECYCLER VIEW UI////////////////////
    //////////////////////////////////////////////////////

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
            this.heteroList = createHeteroList(list);
        }

        private List<Object> createHeteroList(List<Block> list) {
            List<Object> heteroList = new ArrayList<>();
            for (Block block : list) {
                heteroList.add(block);
                heteroList.add(CHAIN_STR);
            }
            if (!heteroList.isEmpty()) {
                heteroList.remove(heteroList.size() - 1);
            }

            return heteroList;
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
            int color = b.getHashColor();
            int prevColor = b.getPrevHashColor();
            setCubeColor(vh.hash, color);
            setCubeColor(vh.prevHash, prevColor);
            vh.blockItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BlockInfoFragment.newInstance(b).show(getFragmentManager(), "blockInfoFrag");
                }
            });

            // show prev hash and hash hints for genesis block
            if (getBlockIndex(position) == 0) {
                vh.prevHash.setText(R.string.block_prevhash_hint);
                vh.hash.setText(R.string.block_hash_hint);
            } else {
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

        public void updateInnerList(List<Block> newBlocks) {
            this.heteroList = createHeteroList(newBlocks);
            this.notifyDataSetChanged();
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
            args.putString("hash", block.getHash());
            args.putString("prevHash", block.getPrevHash());
            args.putString("blockID", block.getBlockID());

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
