package com.example.android.thisisnotfakenews.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import com.example.android.thisisnotfakenews.model.Article;
import com.example.android.thisisnotfakenews.utilities.QueryUtils;

import java.util.List;

/**
 * Created by Reggie on 14/07/2017.
 * Custom loader for news articles.
 */

public class NewsLoader extends AsyncTaskLoader<List<Article>>{

    private static final String LOG_TAG = NewsLoader.class.getSimpleName();

    private String urlQuery;
    private Context context;

    public NewsLoader(Context context, String urlQuery) {
        super(context);
        this.urlQuery = urlQuery;
        this.context = context;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    // To load in a background thread
    @Override
    public List<Article> loadInBackground() {
        if (urlQuery == null) {
            return null;
        }
        Log.d(LOG_TAG, context.toString());

        // Return the {@link List<Book>} result of the http request.
        return QueryUtils.getArticleData(context, urlQuery);
    }
}