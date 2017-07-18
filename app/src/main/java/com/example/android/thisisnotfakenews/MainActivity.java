package com.example.android.thisisnotfakenews;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.thisisnotfakenews.adapters.ArticleAdapter;
import com.example.android.thisisnotfakenews.loaders.NewsLoader;
import com.example.android.thisisnotfakenews.model.Article;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * The type Main activity.
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        LoaderManager.LoaderCallbacks<List<Article>> {

    public static final String APP_PACKAGE_NAME = "android.thisisnotfakenews.";

    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final int NEWS_LOADER_ID = 1;

    private static final String FULL_URL_KEY = "Full Request Url";

    // The base request URL.
    private static final String NEWS_REQUEST_URL_SEARCH =
            "http://content.guardianapis.com/search?order-by=newest&show-fields=all&q=";

    private static final String API_KEY = "api-key=0edca91a-77c5-47fb-aed7-936dcb456ff8";

    // Setting a default search term by which to display the latest articles
    private String searchTerm = "world";

    // The request URL which will be form by concatenating the NEWS_REQUEST_URL and the API Key along
    // with the search term.
    private String httpRequestUrl;

    // String to hold the subtitle of the toolbar.
    private String toolbarSubtitle;

    // Key for the subtitle for use just on save instance state.
    private static final String SUBTITLE_KEY = "Subtitle";

    private ArticleAdapter adapter;

    private List<Article> articleList = new ArrayList();

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.article_list) RecyclerView articleListView;

    // Views to be displayed if there are no articles or there is no network.
    @BindView(R.id.no_articles) TextView noArticleView;
    @BindView(R.id.no_network) TextView noNetworkView;

    // ProgressBar for displaying the loading progress.
    @BindView(R.id.progress)
    ProgressBar progressBar;

    // Loader manager for handling the loader(s)
    LoaderManager loaderManager;

    // For handling the RecyclerView layout.
    LinearLayoutManager layoutManager;

    // Key for saving the scroll position of the RecyclerView.
    public static final String BUNDLE_RECYCLER_LAYOUT_KEY = "RecyclerView Layout";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        loaderManager = getSupportLoaderManager();

        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        initNavDrawer();

        if(savedInstanceState != null){
            httpRequestUrl = savedInstanceState.getString(FULL_URL_KEY);
            initArticleAdapter();
            toolbarSubtitle = savedInstanceState.getString(SUBTITLE_KEY);
            Parcelable savedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT_KEY);
            layoutManager.onRestoreInstanceState(savedRecyclerLayoutState);
        }
        else{
            toolbarSubtitle = getString(R.string.action_bar_subtitle);

            initArticleAdapter();
        }

        loadArticles();

        getSupportActionBar().setSubtitle(toolbarSubtitle);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(FULL_URL_KEY, httpRequestUrl);
        outState.putString(SUBTITLE_KEY, toolbarSubtitle);
        outState.putParcelable(BUNDLE_RECYCLER_LAYOUT_KEY, layoutManager.onSaveInstanceState());
    }

    @Override
    public void onRestoreInstanceState(Bundle inState) {
        httpRequestUrl = inState.getString(FULL_URL_KEY);
        toolbarSubtitle = inState.getString(SUBTITLE_KEY);
        Parcelable savedRecyclerLayoutState = inState.getParcelable(BUNDLE_RECYCLER_LAYOUT_KEY);
        layoutManager.onRestoreInstanceState(savedRecyclerLayoutState);
    }

    /**
     * Check network connectivity
     */
    private void initNetworkConnectivityCheck() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        // Check network info and make sure there is one
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            noNetworkView.setVisibility(View.GONE);
            articleListView.setVisibility(View.VISIBLE);
            loaderManager.restartLoader(NEWS_LOADER_ID, null, this);
        } else {
            // If there is no network info, tell the user
            articleListView.setVisibility(View.GONE);
            noNetworkView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * A small method called when the search button is entered.
     */
    public void loadArticles() {
        adapter.clear();
        httpRequestUrl = prepareRequestUrl();
        initNetworkConnectivityCheck();
        initArticleAdapter();
    }

    /**
     * For the {@link RecyclerView}, set the {@link ArticleAdapter} and use a LinearLayoutManager
     * to set it to vertical.
     */
    private void initArticleAdapter() {
        adapter = new ArticleAdapter(this, articleList);
        articleListView.setLayoutManager(layoutManager);
        articleListView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(this).
                color(Color.LTGRAY).sizeResId(R.dimen.quiz_list_item_divider_width).
                marginResId(R.dimen.quiz_list_item_divider_margin, R.dimen.quiz_list_item_divider_margin).build());
        articleListView.setAdapter(adapter);
    }

    /**
     * This method prepares the request used to fetch data from Google API
     * @return search query
     */
    public String prepareRequestUrl() {
        String requestUrl = NEWS_REQUEST_URL_SEARCH;
        requestUrl += searchTerm;
        requestUrl += "&" + API_KEY;
        return (requestUrl);
    }

    //region Navigation Drawer Methods
    /**
     * A method to initiate the Navigation Drawer
     */
    private void initNavDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Convert the item title into the search term to parse into the url. Replace spaces with '%20'.
     * @param item      The selected item
     * @return          Return true on method execution
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        searchTerm = item.getTitle().toString().toLowerCase().replace(" ", "%20");
        toolbarSubtitle = item.getTitle().toString();
        getSupportActionBar().setSubtitle(toolbarSubtitle);
        loadArticles();

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    //endregion

    //region Loader methods
    @Override
    public Loader<List<Article>> onCreateLoader(int i, Bundle bundle) {
        articleListView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        return new NewsLoader(this, httpRequestUrl);
    }

    @Override
    public void onLoadFinished(Loader<List<Article>> loader, List<Article> articleList) {
        articleListView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        adapter.clear();
        // filling ArticleAdapter
        if (articleList != null && !articleList.isEmpty()) {
            adapter.reloadList(articleList);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Article>> loader) {
        adapter.clear();
        adapter.notifyDataSetChanged();
    }
    //endregion
}
