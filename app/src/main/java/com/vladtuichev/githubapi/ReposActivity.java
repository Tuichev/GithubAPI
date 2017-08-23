package com.vladtuichev.githubapi;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.vladtuichev.githubapi.Model.Item;
import com.vladtuichev.githubapi.Model.Repos;
import com.vladtuichev.githubapi.Model.SearchModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import static com.vladtuichev.githubapi.R.id.repositories;

/**
 * Created by Vlad Tuichev on 23.08.2017.
 */

public class ReposActivity extends AppCompatActivity {
    private TextView tvMyRepos;
    private TextView tvSearchResult;
    private EditText etSearch;
    private SearchRepos mAuthTask = null;
    private List<Item> reposList;
    private Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repos);

        TabHost tabHost=(TabHost)findViewById(android.R.id.tabhost);
        tabHost.setup();

        TabHost.TabSpec tabSpec;
        tabSpec = tabHost.newTabSpec("tag1");

        tabSpec.setIndicator("My repositories");
        tabSpec.setContent(repositories);
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("tag2");

        tabSpec.setIndicator("Search");
        tabSpec.setContent(R.id.Search);
        tabHost.addTab(tabSpec);

        tabHost.setCurrentTabByTag("tag1");

        tvMyRepos = (TextView) findViewById(R.id.repos_view);
        tvSearchResult = (TextView) findViewById(R.id.SearchResult);
        etSearch = (EditText) findViewById(R.id.searchField);

        loadRepos();
    }

    private void loadRepos()
    {
        Intent intent = getIntent();
        String reposInfo=intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        tvMyRepos.setMovementMethod(new ScrollingMovementMethod());
        tvMyRepos.setText(reposInfo);
    }

    public void searchReposBtn(View view)
    {
        if (mAuthTask != null) {
            return;
        }

        String name= "{"+ etSearch.getText().toString()+"}";
        mAuthTask = new SearchRepos(name);
        mAuthTask.execute((Void) null);
    }

    public class SearchRepos extends AsyncTask<Void, Void, Boolean> {

        private final String repName;

        SearchRepos(String name) {
            repName = name;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            GitAPI service = retrofit.create(GitAPI.class);
            Call<SearchModel> call= service.getSearchResult(repName);

            try {
                Response<SearchModel> response = call.execute();

                if(response.isSuccessful())
                {
                    reposList = new ArrayList<>();
                    SearchModel result= response.body();
                    reposList.addAll(result.getItems());

                    return true;
                }
            }catch (IOException e)
            {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            if (success) {
                String message="";
                for(int i=0; i<reposList.size(); i++)
                {
                    message+=reposList.get(i).getName()+"\n";
                    message+="Full name: "+reposList.get(i).getFullName()+"\n";
                    message+="Private: "+reposList.get(i).getPrivate()+"\n";
                    message+="language: "+reposList.get(i).getLanguage()+"\n";
                    message+="Watchers count: "+reposList.get(i).getWatchersCount()+"\n\n";
                }

                tvSearchResult.setMovementMethod(new ScrollingMovementMethod());
                tvSearchResult.setText(message);

            } else {
                Toast.makeText(getApplicationContext(),"Fail!",Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }

}
