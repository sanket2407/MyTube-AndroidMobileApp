package com.example.sanket.constants;

import com.google.android.gms.common.Scopes;
import com.google.api.services.youtube.YouTubeScopes;

/**
 * Created by sanket on 10/17/15.
 */
public class Constants {

    public static final String[] SCOPES = {"oauth2:"+ Scopes.PROFILE, "oauth2:"+ YouTubeScopes.YOUTUBE};

    public static final String PLAYLIST_NAME = "SJSU-CMPE-277";

    public static final String BASE_URL = "https://www.googleapis.com/youtube/v3/";

    public static final String SEARCH_PATH = "search";
    public static final String GET_VIDEO = "videos";
    public static final String PLAYLISTS = "playlists";
    public static final String PLAYLISTITEMS = "playlistItems";


    public static final String ID = "id";
    public static final String PART = "part";
    public static final String MINE = "mine";
    public static final String SNIPPET = "snippet";
    public static final String STATISTICS = "statistics";
    public static final String MAX_RESULTS = "maxResults";
    public static final String KEYWORD = "q";
    public static final String TYPE = "type";
}