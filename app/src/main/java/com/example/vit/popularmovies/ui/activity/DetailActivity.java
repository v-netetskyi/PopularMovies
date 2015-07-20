package com.example.vit.popularmovies.ui.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.parceler.Parcels;

import com.example.vit.popularmovies.MovieApplication;
import com.example.vit.popularmovies.R;
import com.example.vit.popularmovies.rest.model.Movie;
import com.example.vit.popularmovies.ui.fragment.MovieDetailFragment;

/**
 * Created by Vit on 2015-07-17.
 */
public class DetailActivity extends AppCompatActivity {

    static final String CLASS = DetailActivity.class.getSimpleName() + ":";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Bundle args = getIntent().getBundleExtra("args");

        getFragmentManager().beginTransaction().
                replace(R.id.detail_container, MovieDetailFragment.newInstance(args)).commit();
    }
}
