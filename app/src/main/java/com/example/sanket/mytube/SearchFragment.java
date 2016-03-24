package com.example.sanket.mytube;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.sanket.connectionutil.YouTubeConnector;
import com.example.sanket.model.File;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sanket on 10/4/15.
 */
public class SearchFragment extends Fragment {

    SearchFragmentListener searchFragmentListener;

    public interface  SearchFragmentListener {

        public void didSelectSearchResult(String videoId);
        public void didAddVideoToFavorites();
    }


    View rootView;
    private ArrayList<File> searchResults = new ArrayList<File>();
    String addToFavoritesResponseCode = "-1";
    String removeFromFavoritesResponseCode = "-1";
    int selectedIndex;


    @Override
    public void onAttach(Context context) {

        super.onAttach(context);

        searchFragmentListener = (SearchFragmentListener)context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {

        super.onStart();
    }

    @Override
    public void onStop() {

        super.onStop();
        hideSoftKeyboard(getActivity(), this.getView());
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);
    }

    public static void hideSoftKeyboard (Activity activity, View view) {

        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        System.out.println("onCreateView SearchFragment");
        rootView = inflater.inflate(R.layout.fragment_search, container, false);

        addTextChangeListener();
        addClickListener();

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    private void addTextChangeListener() {

        EditText searchEditText = (EditText) rootView.findViewById(R.id.search_input);

        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                EditText searchEditText = (EditText) rootView.findViewById(R.id.search_input);
                String searchQuery = searchEditText.getText().toString();

                if (actionId == EditorInfo.IME_ACTION_SEND ||
                        actionId == EditorInfo.IME_ACTION_GO ||
                        actionId == EditorInfo.IME_ACTION_DONE) {

                    hideSoftKeyboard(getActivity(), rootView);
                    searchOnYoutube(searchQuery);
                    return true;
                }
                return false;
            }
        });
    }

    private void addClickListener(){

        ListView searchvideos = (ListView)rootView.findViewById(R.id.search_videos);
        searchvideos.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos,
                                    long id) {

                System.out.println("onItemClick Adapter View");
                String videoId = searchResults.get(pos).getId();

                searchFragmentListener.didSelectSearchResult(videoId);
            }

        });
    }

    private void searchOnYoutube(final String keywords) {

        new SearchTask().execute(keywords);
    }

    private void updateVideosFound(List <File> videoList) {

        ArrayAdapter<File> adapter = new ArrayAdapter<File>(getActivity().getApplicationContext(), R.layout.search_item, videoList) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                if(convertView == null) {

                    convertView = getActivity().getLayoutInflater().inflate(R.layout.search_item, parent, false);
                }

                final File searchResult = searchResults.get(position);

                ImageView thumbnail = (ImageView)convertView.findViewById(R.id.video_thumbnail);
                TextView title = (TextView)convertView.findViewById(R.id.video_title);
                TextView publishedDate = (TextView)convertView.findViewById(R.id.publishedDate);
                TextView numberOfViews = (TextView)convertView.findViewById(R.id.numberOfViews);
                Button starButton = (Button)convertView.findViewById(R.id.star);
                CheckBox checkBox = (CheckBox)convertView.findViewById(R.id.selectionCheckBox);
                checkBox.setVisibility(View.INVISIBLE);
                starButton.setTag(position);

                if (searchResult.isFavorite()) {

                    starButton.setBackgroundResource(android.R.drawable.star_on);
                } else {

                    starButton.setBackgroundResource(android.R.drawable.star_off);
                }

                starButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        selectedIndex = (int)v.getTag();

                        File selectedVideo = searchResults.get(selectedIndex);

                        if (!selectedVideo.isFavorite()) {

                            addToFavoritesResponseCode = "-1";
                            new AddToFavoritesTask().execute(selectedVideo);
                        } else {

                            removeFromFavoritesResponseCode = "-1";
                            new RemoveFromFavoritesTask().execute(selectedVideo.getPlaylistId());
                        }
                    }
                });

                Picasso.with(getActivity().getApplicationContext()).load(searchResult.getThumbnailURL()).into(thumbnail);
                title.setText(searchResult.getTitle());
                publishedDate.setText(searchResult.getPublishedDate());
                numberOfViews.setText(searchResult.getNumberOfViews());

                return convertView;
            }
        };

        ListView searchvideos = (ListView)rootView.findViewById(R.id.search_videos);
        searchvideos.setAdapter(adapter);
    }

    private void updateVideoInSearchResults(Boolean isFavorite) {

        File selectedVideo = searchResults.get(selectedIndex);

        selectedVideo.setFavorite(isFavorite);
        searchResults.set(selectedIndex, selectedVideo);

        updateVideosFound(searchResults);
    }



    private class SearchTask extends AsyncTask <String, String, ArrayList<File>> {


        @Override
        protected ArrayList<File> doInBackground(String... keyword) {

            try {

                searchResults = YouTubeConnector.searchVideoWithKeywords(keyword[0]);
            } catch (Exception e) {

                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<File> items) {

            if (searchResults != null && searchResults.size() != 0) {

                updateVideosFound(searchResults);
            }
        }
    }



    private class AddToFavoritesTask extends AsyncTask <File, Void, String> {

        @Override
        protected String doInBackground(File... file) {

            try {

                addToFavoritesResponseCode = YouTubeConnector.insertIntoFavorites(file[0]);
            } catch (Exception e) {

                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String responseCode) {

            if (Integer.parseInt(addToFavoritesResponseCode) == 200) {

                updateVideoInSearchResults(true);
            }
        }
    }



    private class RemoveFromFavoritesTask extends AsyncTask <String , Void, String> {

        @Override
        protected String doInBackground(String... videoId) {

            try {

                removeFromFavoritesResponseCode = YouTubeConnector.removeFromFavorites(videoId[0]);
            } catch (Exception e) {

                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String responseCode) {

            if (Integer.parseInt(removeFromFavoritesResponseCode) == 204) {

                updateVideoInSearchResults(false);
            }
        }
    }
}