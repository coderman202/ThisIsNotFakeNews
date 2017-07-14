package com.example.android.thisisnotfakenews;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

    // The base request URL.
    private static final String NEWS_REQUEST_URL_SEARCH =
            "http://content.guardianapis.com/search?order-by=newest&show-fields=all&q=";

    private static final String API_KEY = "api-key=0edca91a-77c5-47fb-aed7-936dcb456ff8";

    // Setting a default search term by which to display the latest articles
    private String searchTerm = "world";

    // The request URL which will be form by concatenating the NEWS_REQUEST_URL and the API Key along
    // with the search term.
    private String httpRequestUrl;

    private ArticleAdapter adapter;

    private List<Article> articleList = new ArrayList();

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.article_list) RecyclerView articleListView;

    // Views to be displayed if there are no articles or there is no network.
    @BindView(R.id.no_articles) TextView noArticleView;
    @BindView(R.id.no_network) TextView noNetworkView;

    // The edit text for the user to enter a search term and find any articles matching that term.
    // And the button for completing the search.
    EditText articleSearchEditText;
    ImageView searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        final LayoutInflater factory = getLayoutInflater();
        final View navHeaderLayout = factory.inflate(R.layout.nav_header_main, null);

        articleSearchEditText = navHeaderLayout.findViewById(R.id.article_search);
        searchButton = navHeaderLayout.findViewById(R.id.search_button);

        setSupportActionBar(toolbar);
        getSupportActionBar().setSubtitle(getString(R.string.action_bar_subtitle));

        initNavDrawer();

        /*searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enterSearch();
            }
        });*/

        initArticleAdapter();

        loadArticles();
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
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(NEWS_LOADER_ID, null, this);
        } else {
            // If there is no network info, tell the user
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
        getLoaderManager().restartLoader(NEWS_LOADER_ID, null, this);
        initArticleAdapter();
        checkForEmptyList();
    }

    /**
     * For the {@link RecyclerView}, set the {@link ArticleAdapter} and use a LinearLayoutManager
     * to set it to vertical.
     */
    private void initArticleAdapter() {
        adapter = new ArticleAdapter(this, articleList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        articleListView.setLayoutManager(layoutManager);
        articleListView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(this).
                color(Color.LTGRAY).sizeResId(R.dimen.quiz_list_item_divider_width).
                marginResId(R.dimen.quiz_list_item_divider_margin, R.dimen.quiz_list_item_divider_margin).build());
        articleListView.setAdapter(adapter);
    }

    private void checkForEmptyList(){
        if(adapter.getItemCount() == 0){
            noArticleView.setVisibility(View.VISIBLE);
        }else{
            noArticleView.setVisibility(View.GONE);
        }
    }

    /**
     * Method to be called after the user has entered a search term
     */
    private void enterSearch() {
        searchTerm = articleSearchEditText.getText().toString();
        if(!searchTerm.equals("")){
            hideKeyboard(this);
            loadArticles();
        }
        else{
            Toast.makeText(this, R.string.empty_search, Toast.LENGTH_LONG).show();
        }
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
        getSupportActionBar().setSubtitle(item.getTitle());
        loadArticles();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    //endregion

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

    //region Loader methods
    @Override
    public Loader<List<Article>> onCreateLoader(int i, Bundle bundle) {
        return new NewsLoader(this, httpRequestUrl);
    }

    @Override
    public void onLoadFinished(Loader<List<Article>> loader, List<Article> articleList) {
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

    //region Methods to handle the hiding of the keyboard.
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View v = getCurrentFocus();

        if (v != null &&
                (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) &&
                v instanceof EditText &&
                !v.getClass().getName().startsWith(APP_PACKAGE_NAME)) {
            int scrcoords[] = new int[2];
            v.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + v.getLeft() - scrcoords[0];
            float y = ev.getRawY() + v.getTop() - scrcoords[1];

            if (x < v.getLeft() || x > v.getRight() || y < v.getTop() || y > v.getBottom())
                hideKeyboard(this);
        }
        return super.dispatchTouchEvent(ev);
    }

    public static void hideKeyboard(Activity activity) {
        if (activity != null && activity.getWindow() != null && activity.getWindow().getDecorView() != null) {
            InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
        }
    }
    //endregion
}
