package com.example.android.newsapp1;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import java.util.List;

public class NewsLoader extends AsyncTaskLoader<List<News>> {

    //Tag for Log Messages
    private static final String LOG_TAG = NewsLoader.class.getName();

    //QueryUrl
    private String mUrl;

    /**
     * Constructs new {@link NewsLoader}
     *
     * @param context of the activity
     * @param url     to load data from
     */

    public NewsLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();

    }

    @Override
    public List<News> loadInBackground() {
        if (mUrl == null) {
            return null;
        }
            //Perform network Request, parse response, and extract list of News where word is your events word
            List<News> news = (List<News>) NewsUtils.fetchFireData ( mUrl );
            return news;
        }
    }

