package com.vladtuichev.githubapi;

import com.vladtuichev.githubapi.Model.Repos;
import com.vladtuichev.githubapi.Model.SearchModel;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Vlad Tuichev on 21.08.2017.
 */

public interface GitAPI {
    @GET("user/repos")
    Call<List<Repos>> getRepos(@Header("Authorization") String authHeader);

    @GET("search/repositories")
    Call<SearchModel> getSearchResult(@Query("q") String query);
}
