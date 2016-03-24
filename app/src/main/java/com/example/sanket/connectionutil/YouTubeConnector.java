package com.example.sanket.connectionutil;

import com.example.sanket.application_settings.ApplicationSettings;
import com.example.sanket.constants.Constants;
import com.example.sanket.model.File;
import com.google.api.client.util.Joiner;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.PlaylistSnippet;
import com.google.api.services.youtube.model.ResourceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Created by sanket on 10/10/15.
 */
public class YouTubeConnector {

    private static final long NUMBER_OF_VIDEOS_RETURNED = 25;

    public static ArrayList<File> searchVideoWithKeywords(String keywords) throws Exception {

        /**
         * https://www.googleapis.com/youtube/v3/search?part=id%2Csnippet&maxResults=1&q=Maroon+5+Sugar&type=video
         */
        String query = URLEncoder.encode(keywords, "utf-8");
        String searchVideoURL = Constants.BASE_URL+Constants.SEARCH_PATH;

        StringBuilder searchVideoURLBuilder = new StringBuilder();
        searchVideoURLBuilder.append(Constants.PART).append("="+"id,snippet");
        searchVideoURLBuilder.append("&").append(Constants.MAX_RESULTS).append("="+NUMBER_OF_VIDEOS_RETURNED);
        searchVideoURLBuilder.append("&").append(Constants.KEYWORD).append("=").append(query);
        searchVideoURLBuilder.append("&").append(Constants.TYPE).append("=").append("video");

        String searchRequestParams = searchVideoURLBuilder.toString();
        ArrayList <String> searchVideoResponse = ConnectionUtil
                .getResponse(searchVideoURL, searchRequestParams, ApplicationSettings.getSharedSettings().getAccessToken());

        JSONObject searchVideosJSON = new JSONObject(searchVideoResponse.get(0));

        Map<String, Object> searchVideoMap = toMap(searchVideosJSON);

        ArrayList <Object> searchedVideoItems = new ArrayList<Object>();
        searchedVideoItems.addAll((Collection<?>) searchVideoMap.get("items"));

        ArrayList<File> items = new ArrayList<File>();
        ArrayList <File> playlistItemList = getVideosInFavorites();

        for (int i = 0; i < searchedVideoItems.size(); i++) {

            File file = new File();

            HashMap idMap = ((HashMap)((HashMap)searchedVideoItems.get(i)).get("id"));

            Map<String, Object> videoMap = getVideoDetails((String) idMap.get("videoId"));

            ArrayList <Object> videoItems = new ArrayList<Object>();
            videoItems.addAll((Collection<?>) videoMap.get("items"));

            HashMap snippetMap = ((HashMap) ((HashMap) videoItems.get(0)).get("snippet"));
            HashMap statisticsMap = ((HashMap) ((HashMap) videoItems.get(0)).get("statistics"));

            file.setId((String) ((HashMap)videoItems.get(0)).get("id"));
            file.setPublishedDate((String) snippetMap.get("publishedAt"));
            file.setTitle((String) snippetMap.get("title"));
            file.setThumbnailURL((String) ((HashMap) (((HashMap) snippetMap.get("thumbnails"))).get("default")).get("url"));
            String playlistId = getPlaylistId(playlistItemList, file.getId());
            file.setPlaylistId(playlistId);
            file.setFavorite((playlistId != "0"));
            file.setNumberOfViews((String) statisticsMap.get("viewCount"));

            items.add(file);
        }

        return items;
    }

    private static Map<String, Object> getVideoDetails (String id) throws JSONException {

        /**
         * https://www.googleapis.com/youtube/v3/videos?part=snippet&id=yzTuBuRdAyA
         */
        List<String> videoIds = new ArrayList<String>();
        videoIds.add(id);
        Joiner stringJoiner = Joiner.on(',');
        String videoId = stringJoiner.join(videoIds);

        String videoURL = Constants.BASE_URL+Constants.GET_VIDEO;

        StringBuilder videoURLBuilder = new StringBuilder();
        videoURLBuilder.append(Constants.PART).append("="+Constants.SNIPPET+","+Constants.STATISTICS);
        videoURLBuilder.append("&").append(Constants.ID).append("="+videoId);

        String videoRequestParams = videoURLBuilder.toString();
        ArrayList <String> videoResponse = ConnectionUtil
                .getResponse(videoURL, videoRequestParams, ApplicationSettings.getSharedSettings().getAccessToken());

        JSONObject videoJSON = new JSONObject(videoResponse.get(0));

        Map<String, Object> videoMap = toMap(videoJSON);

        return videoMap;
    }

    public static String createFavoritesPlaylist() throws JSONException {

        PlaylistSnippet playlistSnippet = new PlaylistSnippet();
        playlistSnippet.setTitle(Constants.PLAYLIST_NAME);

        Playlist playlist = new Playlist();
        playlist.setSnippet(playlistSnippet);

        JSONObject insertPlaylistRequestBody = new JSONObject(playlist);

        String insertPlayListURL = Constants.BASE_URL+Constants.PLAYLISTS;

        StringBuilder insertPlayListURLBuilder = new StringBuilder();
        insertPlayListURLBuilder.append(Constants.PART).append("="+"snippet");
        String insertPlaylistParams = insertPlayListURLBuilder.toString();

        String responseCode = ConnectionUtil.postRequest(insertPlayListURL, insertPlaylistParams,
                insertPlaylistRequestBody.toString(), ApplicationSettings.getSharedSettings().getAccessToken(), true);

        return responseCode;
    }

    public static ArrayList <File> getFavorites() throws JSONException {

        ArrayList <File> playlistItemList = getVideosInFavorites();

        for (int i = 0; i < playlistItemList.size(); i++) {

            File file = playlistItemList.get(i);

            Map<String, Object> videoMap = getVideoDetails(file.getId());

            ArrayList <Object> videoItems = new ArrayList<Object>();
            videoItems.addAll((Collection<?>) videoMap.get("items"));

            HashMap snippetMap = ((HashMap) ((HashMap) videoItems.get(0)).get(Constants.SNIPPET));
            HashMap statisticsMap = ((HashMap) ((HashMap) videoItems.get(0)).get(Constants.STATISTICS));

            file.setPublishedDate((String) snippetMap.get("publishedAt"));
            file.setTitle((String) snippetMap.get("title"));
            file.setThumbnailURL((String) ((HashMap) (((HashMap) snippetMap.get("thumbnails"))).get("default")).get("url"));
            file.setFavorite(true);
            file.setNumberOfViews((String) statisticsMap.get("viewCount"));

            playlistItemList.set(i, file);
        }

        return playlistItemList;
    }

    private static ArrayList <File> getVideosInFavorites () throws JSONException {

        /**
         * https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&maxResults=50&playlistId=PL-ATEPhTf2Qx11lhgZ52adMGDXxPVVT2P
         */
        String getPlayListItemsURL = Constants.BASE_URL+Constants.PLAYLISTITEMS;

        StringBuilder getPlayListItemsURLBuilder = new StringBuilder();
        getPlayListItemsURLBuilder.append(Constants.PART).append("="+"snippet");
        getPlayListItemsURLBuilder.append("&").append(Constants.MAX_RESULTS).append("=").append("40");
        getPlayListItemsURLBuilder.append("&").append("playlistId").append("="+ApplicationSettings.getSharedSettings().getFavoritePlaylistId());

        String playlistItemsParams = getPlayListItemsURLBuilder.toString();
        ArrayList <String> playlistItemsResponse = ConnectionUtil
                .getResponse(getPlayListItemsURL, playlistItemsParams, ApplicationSettings.getSharedSettings().getAccessToken());

        JSONObject playlistItemsJSON = new JSONObject(playlistItemsResponse.get(0));

        Map<String, Object> playlistItemsMap = toMap(playlistItemsJSON);

        ArrayList <Object> playlistVideoItems = new ArrayList<Object>();
        playlistVideoItems.addAll((Collection<?>) playlistItemsMap.get("items"));

        ArrayList <File> favoritePlayList = new ArrayList<File>();

        for (int i = 0; i < playlistVideoItems.size(); i++) {

            File file = new File();

            HashMap snippetMap = ((HashMap)((HashMap) playlistVideoItems.get(i)).get("snippet"));
            file.setId((String) ((HashMap) snippetMap.get("resourceId")).get("videoId"));
            file.setPlaylistId(((String)((HashMap)playlistVideoItems.get(i)).get("id")));

            favoritePlayList.add(file);
        }

        return favoritePlayList;
    }

    public static String getFavoritePlaylist(String playlistName) throws JSONException {

        /**
         * https://www.googleapis.com/youtube/v3/playlists?part=id%2Csnippet&mine=true
         */

        String getPlayListItemsURL = Constants.BASE_URL+Constants.PLAYLISTS;

        StringBuilder getPlayListURLBuilder = new StringBuilder();
        getPlayListURLBuilder.append(Constants.PART).append("="+Constants.ID);
        getPlayListURLBuilder.append(",").append("snippet");
        getPlayListURLBuilder.append("&").append(Constants.MINE).append("="+"true");

        String playlistParams = getPlayListURLBuilder.toString();
        ArrayList <String> playlistResponse = ConnectionUtil
                .getResponse(getPlayListItemsURL, playlistParams, ApplicationSettings.getSharedSettings().getAccessToken());

        JSONObject playlistJSON = new JSONObject(playlistResponse.get(0));

        Map<String, Object> playlistMap = toMap(playlistJSON);

        ArrayList <Object> playlistList = new ArrayList<Object>();
        playlistList.addAll((Collection<?>) playlistMap.get("items"));

        String playlistId = (String)((HashMap) playlistList.get(0)).get("id");
        return playlistId;
    }

    public static String removeFromFavorites(String videoId) {

        /**
         * https://www.googleapis.com/youtube/v3/playlistItems?id=PLi_rXDdOef2RhLGgX4nvVjnvJpHPAdX0sp1MDqaKUFDo
         */
        String deletePlayListItemsURL = Constants.BASE_URL+Constants.PLAYLISTITEMS;

        StringBuilder deletePlayListItemsURLBuilder = new StringBuilder();
        deletePlayListItemsURLBuilder.append(Constants.ID).append("="+videoId);
        String insertPlaylistItemsParams = deletePlayListItemsURLBuilder.toString();

        String responseCode = ConnectionUtil.postRequest(deletePlayListItemsURL, insertPlaylistItemsParams,
                "", ApplicationSettings.getSharedSettings().getAccessToken(), false);

        return responseCode;
    }

    public static String insertIntoFavorites(File file) {

        /**
         * https://www.googleapis.com/youtube/v3/playlistItems?part=snippet%2CcontentDetails
         */
        ResourceId resourceId = new ResourceId();
        resourceId.setKind("youtube#video");
        resourceId.setVideoId(file.getId());

        PlaylistItemSnippet playlistItemSnippet = new PlaylistItemSnippet();
        playlistItemSnippet.setTitle(file.getTitle());
        playlistItemSnippet.setPlaylistId(ApplicationSettings.getSharedSettings().getFavoritePlaylistId());
        playlistItemSnippet.setResourceId(resourceId);

        PlaylistItem playlistItem = new PlaylistItem();
        playlistItem.setSnippet(playlistItemSnippet);

        JSONObject insertPlaylistItemRequestBody = new JSONObject(playlistItem);

        String insertPlayListItemsURL = Constants.BASE_URL+Constants.PLAYLISTITEMS;

        StringBuilder insertPlayListItemsURLBuilder = new StringBuilder();
        insertPlayListItemsURLBuilder.append(Constants.PART).append("="+"snippet");
        insertPlayListItemsURLBuilder.append(",").append("contentDetails");
        String insertPlaylistItemsParams = insertPlayListItemsURLBuilder.toString();

        String responseCode = ConnectionUtil.postRequest(insertPlayListItemsURL, insertPlaylistItemsParams,
                insertPlaylistItemRequestBody.toString(), ApplicationSettings.getSharedSettings().getAccessToken(), true);

        return responseCode;
    }

    private static String getPlaylistId (ArrayList<File> playlistItemList, String videoId) {

        String playlistId = "0";
        for(File file:playlistItemList) {

            if (file.getId().equals(videoId)) {

                playlistId = file.getPlaylistId();
                break;
            }
        }

        return playlistId;
    }

    public static Map<String, Object> jsonToMap(JSONObject json) throws JSONException {
        Map<String, Object> retMap = new HashMap<String, Object>();

        if(json != JSONObject.NULL) {
            retMap = toMap(json);
        }
        return retMap;
    }

    public static Map<String, Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<String, Object>();

        Iterator<String> keysItr = object.keys();
        while(keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    public static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<Object>();
        for(int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }
}