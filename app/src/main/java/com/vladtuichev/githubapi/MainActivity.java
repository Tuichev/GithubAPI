package com.vladtuichev.githubapi;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.vladtuichev.githubapi.Model.Repos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.R.attr.button;

public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_MESSAGE = "getExtra";
    private List<Repos> repositories;
    private UserLoginTask mAuthTask = null;
    private EditText etMail;
    private EditText etPassword;
    private Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etMail = (EditText)findViewById(R.id.eMailField);
        etPassword = (EditText)findViewById(R.id.pswdField);

        Button button = (Button) findViewById(R.id.LogIn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
            });
    }

    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }
            String userName=etMail.getText().toString();
            String userPswd=etPassword.getText().toString();

            mAuthTask = new UserLoginTask(userName, userPswd);
            mAuthTask.execute((Void) null);
    }

    public void LoginSuccessful(String message)
    {
        Intent startReposActivity= new Intent(MainActivity.this, ReposActivity.class);
        startReposActivity.putExtra(EXTRA_MESSAGE,message);
        startActivity(startReposActivity);
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            GitAPI service = retrofit.create(GitAPI.class);
            
            String base = mEmail +":" +mPassword;

            String authHeader = "Basic "+ Base64.encodeToString(base.getBytes(),Base64.NO_WRAP);
            Call<List<Repos>> call= service.getRepos(authHeader);
            try {
                Response<List<Repos>> response = call.execute();

                if(response.isSuccessful())
                {
                    repositories = new ArrayList<>();
                    repositories.addAll(response.body());
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
                Toast.makeText(MainActivity.this,"successful login! ",Toast.LENGTH_SHORT).show();

                String message="";
                for(int i=0; i<repositories.size(); i++)
                {
                    message+=repositories.get(i).getName()+"\n";
                    message+="language: "+repositories.get(i).getLanguage()+"\n";
                    message+="Watchers count: "+repositories.get(i).getWatchersCount()+"\n\n";
                }

                LoginSuccessful(message);

            } else {
               Toast.makeText(getApplicationContext(),"Error! Please enter correct data.",Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }
}
