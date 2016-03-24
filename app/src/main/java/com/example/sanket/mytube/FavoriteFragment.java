package com.example.sanket.mytube;

import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.sanket.connectionutil.YouTubeConnector;
import com.example.sanket.model.File;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by milind.mahajan on 10/4/15.
 */
public class FavoriteFragment extends Fragment {

    FavoriteFragmentListener favoriteFragmentListener;

    public interface  FavoriteFragmentListener {

        public void didSelectFavoriteResult(String videoId);
        public void didModifyFavorites();
    }


    View rootView;
    private ArrayList<File> searchResults = new ArrayList<File>();
    int selectedIndex;
    String removeFromFavoritesResponseCode = "-1";
    private ActionMode mActionMode;
    private SelectionAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_favorite, container, false);

        ListView favoritesList = (ListView)rootView.findViewById(R.id.favorite_videos);
        favoritesList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        addClickListener();

        return rootView;
    }

    private void addClickListener(){

        ListView favoriteVideos = (ListView)rootView.findViewById(R.id.favorite_videos);
        favoriteVideos.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos,
                                    long id) {

                System.out.println("onItemClick Adapter View Favorite fragment");
                String videoId = searchResults.get(pos).getId();

                favoriteFragmentListener.didSelectFavoriteResult(videoId);
            }

        });
    }

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);

        favoriteFragmentListener = (FavoriteFragmentListener)context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {

        super.onStart();

        getFavorites();
    }

    @Override
    public void onStop() {

        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);
    }

    private void getFavorites() {

        new FavoriteTask().execute();
    }

    private void updateVideosFound(List <File> videoList) {

        mAdapter = new SelectionAdapter(getContext(),
                R.layout.search_item, 0, videoList);
        ListView favoriteVideos = (ListView)rootView.findViewById(R.id.favorite_videos);
        favoriteVideos.setAdapter(mAdapter);
    }

    private void updateVideoInSearchResults(Boolean isFavorite) {

        searchResults.remove(selectedIndex);

        updateVideosFound(searchResults);
    }

    public class FavoriteTask extends AsyncTask<String, String, ArrayList<File>> {

        @Override
        protected ArrayList<File> doInBackground(String... keyword) {

            try {

                searchResults = YouTubeConnector.getFavorites();
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

            if (Integer.parseInt(removeFromFavoritesResponseCode) != -1) {

                updateVideoInSearchResults(false);
            }
        }
    }

    class ActionBarCallBack implements ActionMode.Callback {

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            System.out.println("onActionItemClicked");
            switch (item.getItemId()) {

                case R.id.item_delete:

                    ArrayList <File> toBeDeleted = mAdapter.getSelectedFiles();

                    for (int i = 0; i < toBeDeleted.size(); i++) {

                        new RemoveFromFavoritesTask().execute(toBeDeleted.get(i).getPlaylistId());
                    }
                    mode.finish();
            }            return false;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            System.out.println("onActionItemClicked");
            mode.getMenuInflater().inflate(R.menu.contextual_list_view, menu);
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            System.out.println("onActionItemClicked");

        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            System.out.println("onActionItemClicked");

            mode.setTitle("CheckBox is Checked");
            return false;
        }

    }


    private class SelectionAdapter extends ArrayAdapter<File> {

        ArrayList <File> videoList = new ArrayList<File>();
        ArrayList <File> selectedFiles = new ArrayList<File>();

        public SelectionAdapter(Context context, int resource, int textViewResourceId, List <File> objects) {

            super(context, resource, textViewResourceId, objects);
            videoList.addAll(objects);
        }

        public void setNewSelection(int position, boolean value) {

            selectedFiles.add(videoList.get(position));
            notifyDataSetChanged();
        }

        public void removeSelection(int position) {

            removeFromSelection(videoList.get(position));
            notifyDataSetChanged();
        }

        private void removeFromSelection (File file) {

            for (int i = 0; i < selectedFiles.size(); i++) {

                if (selectedFiles.get(i).getId().equals(file.getId())) {

                    selectedFiles.remove(i);
                    break;
                }
            }
        }

        public ArrayList <File> getSelectedFiles () {

            return this.selectedFiles;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if(convertView == null) {

                convertView = getActivity().getLayoutInflater().inflate(R.layout.search_item, parent, false);
            }

            File searchResult = videoList.get(position);

            ImageView thumbnail = (ImageView)convertView.findViewById(R.id.video_thumbnail);
            TextView title = (TextView)convertView.findViewById(R.id.video_title);
            TextView publishedDate = (TextView)convertView.findViewById(R.id.publishedDate);
            TextView numberOfViews = (TextView)convertView.findViewById(R.id.numberOfViews);
            Button starButton = (Button)convertView.findViewById(R.id.star);
            CheckBox checkBox = (CheckBox)convertView.findViewById(R.id.selectionCheckBox);
            checkBox.setVisibility(View.VISIBLE);

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if(isChecked) {

                        mAdapter.setNewSelection(position, isChecked);
                    }
                    else {

                        mAdapter.removeSelection(position);
                    }

                    if (mAdapter.getSelectedFiles().size() != 0) {

                        mActionMode = getActivity().startActionMode(new ActionBarCallBack());
                    } else {

                        mActionMode.finish();
                    }
                }
            });
            starButton.setTag(position);

            starButton.setBackgroundResource(android.R.drawable.star_on);

            starButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    selectedIndex = (int)v.getTag();

                    File selectedVideo = searchResults.get(selectedIndex);

                    removeFromFavoritesResponseCode = "-1";
                    new RemoveFromFavoritesTask().execute(selectedVideo.getPlaylistId());
                }
            });

            Picasso.with(getActivity().getApplicationContext()).load(searchResult.getThumbnailURL()).into(thumbnail);
            title.setText(searchResult.getTitle());
            publishedDate.setText(searchResult.getPublishedDate());
            numberOfViews.setText(searchResult.getNumberOfViews());

            return convertView;
        }
    }
}