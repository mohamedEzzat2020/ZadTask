package Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.mohamedezzat.zadtask.R;

import java.util.ArrayList;
import java.util.List;

import DataModels.Repository;
import Utils.PaginationAdapterCallback;
import Utils.SharedFuncations;

public class RepositoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM = 0;
    private static final int LOADING = 1;

    private List<Repository> repositoryResults;
    private Context context;

    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;

    private PaginationAdapterCallback mCallback;

    private String errorMsg;


    public RepositoryAdapter(Context context) {
        this.context = context;
        this.mCallback = (PaginationAdapterCallback) context;
        repositoryResults = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case ITEM:
                View viewItem = inflater.inflate(R.layout.item_list, parent, false);
                viewHolder = new RepositoryViewHolder(viewItem);
                break;
            case LOADING:
                View viewLoading = inflater.inflate(R.layout.item_progress, parent, false);
                viewHolder = new LoadingViewHolder(viewLoading);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Repository current = repositoryResults.get(position);

        switch (getItemViewType(position)) {

            case ITEM:
                final RepositoryViewHolder repositoryVH = (RepositoryViewHolder) holder;

                if (current.getFork()) {
                    repositoryVH.card_item_list_layout_cardview.setCardBackgroundColor(context.getResources().getColor(R.color.colorAccent));
                    repositoryVH.txt_item_list_layout_repo_name.setTextColor(Color.WHITE);
                    repositoryVH.txt_item_list_layout_owner_name.setTextColor(Color.parseColor("#F5F5F5"));
                    repositoryVH.txt_item_list_layout_repo_desc.setTextColor(Color.parseColor("#FAFAFA"));
                } else {
                    repositoryVH.card_item_list_layout_cardview.setCardBackgroundColor(Color.WHITE);
                    repositoryVH.txt_item_list_layout_repo_name.setTextColor(context.getResources().getColor(R.color.colorAccent));
                    repositoryVH.txt_item_list_layout_owner_name.setTextColor(Color.parseColor("#de000000"));
                    repositoryVH.txt_item_list_layout_repo_desc.setTextColor(Color.parseColor("#8a000000"));
                }

                repositoryVH.txt_item_list_layout_repo_name.setText(current.getFullName());
                repositoryVH.txt_item_list_layout_owner_name.setText(context.getString(R.string.repository_adapter_owner_name) + current.getOwner().getLogin());
                repositoryVH.txt_item_list_layout_repo_desc.setText(current.getDescription());

                break;

            case LOADING:
                LoadingViewHolder loadingVH = (LoadingViewHolder) holder;

                if (retryPageLoad) {
                    loadingVH.linear_item_progress_layout_errorlayout.setVisibility(View.VISIBLE);
                    loadingVH.progress_item_progress_layout_loadmore.setVisibility(View.GONE);
                    loadingVH.txt_item_progress_layout_loadmore_errortxt.setText(errorMsg != null ? errorMsg : context.getString(R.string.all_app_unexpected_error));

                } else {
                    loadingVH.linear_item_progress_layout_errorlayout.setVisibility(View.GONE);
                    loadingVH.progress_item_progress_layout_loadmore.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        return repositoryResults == null ? 0 : repositoryResults.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (position == repositoryResults.size() - 1 && isLoadingAdded) ? LOADING : ITEM;
    }

    public void add(Repository r) {
        repositoryResults.add(r);
        notifyItemInserted(repositoryResults.size() - 1);
    }

    public void addAll(List<Repository> repositoryResults) {

        for (Repository result : repositoryResults) {
            add(result);
        }
        SharedFuncations.SaveReposToShared(this.repositoryResults, context);
    }

    public void remove(Repository r) {
        int position = repositoryResults.indexOf(r);
        if (position > -1) {
            repositoryResults.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear() {
        isLoadingAdded = false;
        while (getItemCount() > 0) {
            remove(getItem(0));
        }
    }


    public void addLoadingFooter() {
        isLoadingAdded = true;
        add(new Repository());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = repositoryResults.size() - 1;
        Repository result = getItem(position);

        if (result != null) {
            repositoryResults.remove(position);
            notifyItemRemoved(position);
        }
    }

    public Repository getItem(int position) {
        return repositoryResults.get(position);
    }

    public void showRetry(boolean show, String errorMsg) {
        retryPageLoad = show;
        notifyItemChanged(repositoryResults.size() - 1);

        if (errorMsg != null) this.errorMsg = errorMsg;
    }

    protected class RepositoryViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {

        private CardView card_item_list_layout_cardview;
        private TextView txt_item_list_layout_repo_name;
        private TextView txt_item_list_layout_owner_name;
        private TextView txt_item_list_layout_repo_desc;
        private RelativeLayout reli_item_list_layout_container;

        public RepositoryViewHolder(View itemView) {
            super(itemView);

            card_item_list_layout_cardview = (CardView) itemView.findViewById(R.id.card_item_list_layout_cardview);
            reli_item_list_layout_container = (RelativeLayout) itemView.findViewById(R.id.reli_item_list_layout_container);
            txt_item_list_layout_repo_name = (TextView) itemView.findViewById(R.id.txt_item_list_layout_repo_name);
            txt_item_list_layout_repo_desc = (TextView) itemView.findViewById(R.id.txt_item_list_layout_repo_desc);
            txt_item_list_layout_owner_name = (TextView) itemView.findViewById(R.id.txt_item_list_layout_owner_name);

            card_item_list_layout_cardview.setOnLongClickListener(this);
            reli_item_list_layout_container.setOnLongClickListener(this);
            txt_item_list_layout_repo_name.setOnLongClickListener(this);
            txt_item_list_layout_repo_desc.setOnLongClickListener(this);
            txt_item_list_layout_owner_name.setOnLongClickListener(this);


        }


        @Override
        public boolean onLongClick(View view) {

            String[] arr = context.getResources().getStringArray(R.array.repository_adapter_longclick_options);
            new MaterialDialog.Builder(context)
                    .title(context.getString(R.string.repository_adapter_dialog_browse))
                    .items(arr)
                    .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                        @Override
                        public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {

                            String url;
                            if (which == 0)
                                url = repositoryResults.get(getAdapterPosition()).getHtmlUrl();
                            else
                                url = repositoryResults.get(getAdapterPosition()).getOwner().getHtmlUrl();

                            if (!url.startsWith("http://") && !url.startsWith("https://"))
                                url = "http://" + url;

                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("" + url));
                            context.startActivity(browserIntent);
                            return true;
                        }
                    })
                    .positiveText(context.getString(R.string.repository_adapter_dialog_go))
                    .show();
            return false;
        }
    }


    protected class LoadingViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ProgressBar progress_item_progress_layout_loadmore;
        private ImageButton imgbtn_item_progress_layout_loadmore_retry;
        private TextView txt_item_progress_layout_loadmore_errortxt;
        private LinearLayout linear_item_progress_layout_errorlayout;

        public LoadingViewHolder(View itemView) {
            super(itemView);

            progress_item_progress_layout_loadmore = (ProgressBar) itemView.findViewById(R.id.progress_item_progress_layout_loadmore);
            imgbtn_item_progress_layout_loadmore_retry = (ImageButton) itemView.findViewById(R.id.imgbtn_item_progress_layout_loadmore_retry);
            txt_item_progress_layout_loadmore_errortxt = (TextView) itemView.findViewById(R.id.txt_item_progress_layout_loadmore_errortxt);
            linear_item_progress_layout_errorlayout = (LinearLayout) itemView.findViewById(R.id.linear_item_progress_layout_errorlayout);

            imgbtn_item_progress_layout_loadmore_retry.setOnClickListener(this);
            linear_item_progress_layout_errorlayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.imgbtn_item_progress_layout_loadmore_retry:
                case R.id.linear_item_progress_layout_errorlayout:

                    showRetry(false, null);
                    mCallback.retryPageLoad();
                    break;
            }
        }
    }

}
