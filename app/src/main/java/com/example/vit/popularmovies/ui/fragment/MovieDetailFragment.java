package com.example.vit.popularmovies.ui.fragment;

import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.vit.popularmovies.MovieApplication;
import com.example.vit.popularmovies.R;
import com.example.vit.popularmovies.communication.BusProvider;
import com.example.vit.popularmovies.communication.Event;
import com.example.vit.popularmovies.rest.model.DetailedMovie;
import com.example.vit.popularmovies.rest.model.Movie;
import com.example.vit.popularmovies.rest.model.Trailer;
import com.example.vit.popularmovies.ui.RecyclerItemClickListener;
import com.example.vit.popularmovies.ui.adapter.TrailersAdapter;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.util.List;


public class MovieDetailFragment extends Fragment implements RecyclerItemClickListener.OnItemClickListener{

    static final String CLASS = MovieDetailFragment.class.getSimpleName() + ": ";
    Bus bus = BusProvider.getInstance();

    TextView tvTitle;
    TextView tvYear;
    TextView tvRuntime;
    TextView tvRating;
    TextView tvOverview;
    ImageView ivPoster;
    RecyclerView rvTrailers;
    LinearLayout llTrailers;
    LinearLayout llContent;
    ProgressBar pbLoading;

    TrailersAdapter adapter;

    DetailedMovie detailedMovie;
    List<Trailer> trailerList;

    public static MovieDetailFragment newInstance(int movieId) {
        MovieDetailFragment fragment = new MovieDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("id", movieId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log.d(MovieApplication.TAG, CLASS + "onCreate()");

        if (savedInstanceState != null) {
            this.detailedMovie = Parcels.
                    unwrap(savedInstanceState.getParcelable(DetailedMovie.class.getSimpleName()));
            this.trailerList = Parcels.
                    unwrap(savedInstanceState.getParcelable(Trailer.class.getSimpleName()));
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //Log.d(MovieApplication.TAG, CLASS + "onActivityCreated()");

        //this is the case when  data are not yet loaded any time
        if (this.detailedMovie == null && trailerList == null) {
            hideContent();
            bus.post(new Event.LoadDetailedMovieEvent(getArguments().getInt("id")));
            bus.post(new Event.LoadVideosEvent(getArguments().getInt("id")));
        }
        // if detailedMovie is not null than orientation was changed and we'll not load data again
        else {
            showContent();
            setData();
            if(!trailerList.isEmpty()) {
                setupTrailersList();
            }
        }



    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Log.d(MovieApplication.TAG, CLASS + "onCreateView()");
        View view = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        tvTitle = (TextView) view.findViewById(R.id.tvDetailTitle);
        tvYear = (TextView) view.findViewById(R.id.tvDetailYear);
        tvRuntime = (TextView) view.findViewById(R.id.tvDetailRuntime);
        tvRating = (TextView) view.findViewById(R.id.tvDetailRating);
        tvOverview = (TextView) view.findViewById(R.id.tvDetailOverview);
        ivPoster = (ImageView) view.findViewById(R.id.ivDetailPoster);

        rvTrailers = (RecyclerView) view.findViewById(R.id.rvTrailersList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getBaseContext(),
                LinearLayoutManager.HORIZONTAL, false);
        rvTrailers.setLayoutManager(layoutManager);
        rvTrailers.addOnItemTouchListener(new RecyclerItemClickListener(getActivity().getBaseContext(), this));
        llTrailers = (LinearLayout) view.findViewById(R.id.llTrailers);
        llContent = (LinearLayout) view.findViewById(R.id.llContent);
        pbLoading = (ProgressBar) view.findViewById(R.id.pbLoading);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        bus.register(this);
        //Log.d(MovieApplication.TAG, CLASS + "onStart()");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Log.d(MovieApplication.TAG, CLASS + "onSaveInstanceState()");
        outState.putParcelable(DetailedMovie.class.getSimpleName(), Parcels.wrap(this.detailedMovie));
        //Log.d(MovieApplication.TAG, CLASS + "trailerlist size = " + trailerList.size());
        outState.putParcelable(Trailer.class.getSimpleName(), Parcels.wrap(this.trailerList));
    }

    @Override
    public void onStop() {
        super.onStop();
        bus.unregister(this);
        //Log.d(MovieApplication.TAG, CLASS + "onStop()");
    }

    @Subscribe
    public void onLoadedDetailedMovieEvent(Event.LoadedDetailedMovieEvent event) {
        this.detailedMovie = event.getDetailedMovie();
        Log.d(MovieApplication.TAG, CLASS + "onLoadedDetailedMovieEvent()");
        setData();
    }

    @Subscribe
    public void onLoadedVideosEvent(Event.LoadedVideosEvent event) {
        Log.d(MovieApplication.TAG, CLASS + "onLoadedVideosEvent()");
        this.trailerList = event.getTrailerList();
        if (!trailerList.isEmpty()) {
            setupTrailersList();
        }
        showContent();
    }

    @Override
    public void onItemClick(View view, int position) {
        //Log.d(MovieApplication.TAG, CLASS + "onItemClick() pos = " + position);
        if(trailerList.get(position).getSite().toLowerCase().equals("youtube")) {
            watchYoutubeVideo(trailerList.get(position).getKey());
        }
    }

    private void setData() {
        tvTitle.setText(detailedMovie.getTitle());
        // getReleaseDate() returns date. Example 2013-10-08
        // so show only year
        tvYear.setText(detailedMovie.getReleaseDate().substring(0, 4));
        tvRuntime.setText(getString(R.string.runtime, String.valueOf(detailedMovie.getRuntime())));
        tvRating.setText(getString(R.string.rating, String.valueOf(detailedMovie.getVoteAverage())));
        tvOverview.setText(detailedMovie.getOverview());

        Picasso.with(getActivity().getBaseContext()).load(buildUrl(detailedMovie.getPosterPath()))
                .error(R.drawable.placeholder)
                .placeholder(R.drawable.placeholder)
                .into(ivPoster);
    }

    public String buildUrl(String posterPath) {
        final String size = "w185";
        return "http://image.tmdb.org/t/p/" + size + "/" + posterPath;
    }

    public void watchYoutubeVideo(String id){
        try{
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
            startActivity(intent);
        }catch (ActivityNotFoundException ex){
            Intent intent=new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://www.youtube.com/watch?v="+id));
            startActivity(intent);
        }
    }

    private void setupTrailersList(){
        if(adapter == null){
            adapter = new TrailersAdapter(getActivity().getBaseContext(), trailerList);
            rvTrailers.setAdapter(adapter);
        } else {
            adapter.setData(trailerList);
        }
        llTrailers.setVisibility(View.VISIBLE);
    }

    private boolean isAllDataLoaded(){
        return (detailedMovie != null) && (trailerList != null);
    }

    private void showContent(){
        llContent.setVisibility(View.VISIBLE);
        pbLoading.setVisibility(View.GONE);

    }

    private void hideContent(){
        llContent.setVisibility(View.GONE);
        pbLoading.setVisibility(View.VISIBLE);
    }
}
