package com.mohamedezzat.zadtask;

import android.content.Context;
import android.net.ConnectivityManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.TimeoutException;

import API.RepositoryApi;
import API.RepositoryService;
import Adapters.RepositoryAdapter;
import DataModels.Repository;
import Utils.PaginationAdapterCallback;
import Utils.PaginationScrollListener;
import Utils.SharedFuncations;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements PaginationAdapterCallback {


    RepositoryAdapter adapter;
    LinearLayoutManager linearLayoutManager;

    RecyclerView rec_main_layout_recycler;
    ProgressBar progress_main_layout_loading;
    LinearLayout linear_error_layout_container;
    Button btn_error_layout_retry;
    TextView txt_error_layout_cause;
    SwipeRefreshLayout mSwipeToRefresh;

    private static final int PAGE_START = 1;
    private int TOTAL_PAGES = 21;
    private int currentPage = PAGE_START;

    private boolean isLoading = false;
    private boolean isLastPage = false;

    private RepositoryService repositoryService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedFuncations.InitSharedPreferance(this);
        setContentView(R.layout.activity_main);

        InitViews();
        ViewsListners();

        repositoryService = RepositoryApi.getClient().create(RepositoryService.class);
        loadFirstPage();

    }


    protected void InitViews() {

        rec_main_layout_recycler = (RecyclerView) findViewById(R.id.rec_main_layout_recycler);
        progress_main_layout_loading = (ProgressBar) findViewById(R.id.progress_main_layout_loading);
        linear_error_layout_container = (LinearLayout) findViewById(R.id.linear_error_layout_container);
        btn_error_layout_retry = (Button) findViewById(R.id.btn_error_layout_retry);
        txt_error_layout_cause = (TextView) findViewById(R.id.txt_error_layout_cause);

        adapter = new RepositoryAdapter(this);
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rec_main_layout_recycler.setLayoutManager(linearLayoutManager);
        rec_main_layout_recycler.setItemAnimator(new DefaultItemAnimator());
        rec_main_layout_recycler.setAdapter(adapter);


        mSwipeToRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        OnRefreshListener();
        mSwipeToRefresh.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorPrimaryDark,
                R.color.colorAccent);

    }


    private void ViewsListners() {

        rec_main_layout_recycler.addOnScrollListener(new PaginationScrollListener(linearLayoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage += 1;

                loadNextPage();
            }

            @Override
            public int getTotalPageCount() {
                return TOTAL_PAGES;
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });


        btn_error_layout_retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadFirstPage();
            }
        });


    }


    private void OnRefreshListener() {

        mSwipeToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (!SharedFuncations.isNetworkConnected(MainActivity.this)) {

                    Toast.makeText(getApplicationContext(), getString(R.string.all_app_no_internet_connection), Toast.LENGTH_LONG).show();
                    mSwipeToRefresh.setRefreshing(false);
                } else {
                    adapter.clear();
                    SharedFuncations.ClearSavedData(MainActivity.this);
                    currentPage = PAGE_START;
                    adapter.clear();
                    loadFirstPage();
                }
                mSwipeToRefresh.setRefreshing(false);

            }
        });
    }


    private void loadFirstPage() {

        hideErrorView();

        callRepositoriesApi().enqueue(new Callback<List<Repository>>() {
            @Override
            public void onResponse(Call<List<Repository>> call, Response<List<Repository>> response) {


                try {
                    hideErrorView();

                    List<Repository> results = response.body();
                    progress_main_layout_loading.setVisibility(View.GONE);
                    adapter.addAll(results);
                } catch (Exception e) {
                    showErrorView(e);
                    return;
                }


                if (currentPage <= TOTAL_PAGES) adapter.addLoadingFooter();
                else isLastPage = true;
            }

            @Override
            public void onFailure(Call<List<Repository>> call, Throwable t) {
                t.printStackTrace();
                showErrorView(t);
            }
        });
    }


    private void loadNextPage() {

        callRepositoriesApi().enqueue(new Callback<List<Repository>>() {
            @Override
            public void onResponse(Call<List<Repository>> call, Response<List<Repository>> response) {

                try {
                    adapter.removeLoadingFooter();
                    isLoading = false;

                    List<Repository> results = response.body();
                    adapter.addAll(results);
                } catch (Exception e) {
                    adapter.showRetry(true, fetchErrorMessage(e));
                }

                if (currentPage != TOTAL_PAGES) adapter.addLoadingFooter();
                else isLastPage = true;
            }

            @Override
            public void onFailure(Call<List<Repository>> call, Throwable t) {
                t.printStackTrace();
                adapter.showRetry(true, fetchErrorMessage(t));
            }
        });
    }


    private Call<List<Repository>> callRepositoriesApi() {

        return repositoryService.getRepositories(currentPage, 10);
    }


    @Override
    public void retryPageLoad() {
        loadNextPage();
    }


    private void showErrorView(Throwable throwable) {

        if (linear_error_layout_container.getVisibility() == View.GONE) {

            linear_error_layout_container.setVisibility(View.VISIBLE);
            progress_main_layout_loading.setVisibility(View.GONE);
            txt_error_layout_cause.setText(fetchErrorMessage(throwable));
        }
    }


    private String fetchErrorMessage(Throwable throwable) {

        String errorMsg = getString(R.string.all_app_unexpected_error);
        if (!SharedFuncations.isNetworkConnected(MainActivity.this)) {
            errorMsg = getString(R.string.all_app_no_internet_connection);
        } else if (throwable instanceof TimeoutException) {
            errorMsg = getString(R.string.all_app_connection_timeout_error);
        }

        return errorMsg;
    }


    private void hideErrorView() {

        if (linear_error_layout_container.getVisibility() == View.VISIBLE) {
            linear_error_layout_container.setVisibility(View.GONE);
            progress_main_layout_loading.setVisibility(View.VISIBLE);
        }
    }


}
