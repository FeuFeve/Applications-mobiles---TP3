package com.example.tp3;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class TeamActivity extends AppCompatActivity {

    private static final String TAG = TeamActivity.class.getSimpleName();
    private TextView textTeamName, textLeague, textManager, textStadium, textStadiumLocation, textTotalScore, textRanking, textLastMatch, textLastUpdate;

    private int totalPoints;
    private int ranking;
    private Match lastEvent;
    private String lastUpdate;

    private ImageView imageBadge;
    private Team team;

    public static Team currentTeam;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team);

        // Get intent
        Intent intent = getIntent();
        team = intent.getParcelableExtra(Team.TAG);
        assert team != null;
        final boolean isNewTeam = intent.getExtras().getBoolean("isNewTeam");

        System.out.println("##### CLICKED ON:");
        System.out.println(team.getName());

        // Get layout elements
        textTeamName = findViewById(R.id.nameTeam);
        textLeague = findViewById(R.id.league);
        textStadium = findViewById(R.id.editStadium);
        textStadiumLocation = findViewById(R.id.editStadiumLocation);
        textTotalScore = findViewById(R.id.editTotalScore);
        textRanking = findViewById(R.id.editRanking);
        textLastMatch = findViewById(R.id.editLastMatch);
        textLastUpdate = findViewById(R.id.editLastUpdate);

        imageBadge = findViewById(R.id.imageView);

        updateView();

        final Button updateButton = findViewById(R.id.updateButton);
        updateButton.setOnClickListener(new View.OnClickListener() {

            AsyncTask<?, ?, ?> backgroundTask;

            @Override
            public void onClick(View v) {
                final String teamName = team.getName();
                currentTeam = team;

                if (backgroundTask != null) {
                    backgroundTask.cancel(true);
                }

                backgroundTask = new AsyncTask<Object, Void, String>() {
                    @Override
                    protected String doInBackground(Object... params) {
                        try {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Updating...", Toast.LENGTH_SHORT).show();
                                }
                            });

                            // Update the basic information (name, league, stadium name, badge...)
                            updateTeamBasics(team);

                            // Update the ranking of the team
                            updateTeamRanking(team);

                            // Update the last match section with the score
                            updateTeamLastMatch(team);

                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (MalformedURLException e) {
                            Log.d(TAG, "MalformedURLException");
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "URL error", Toast.LENGTH_SHORT);
                                }
                            });
                        } catch (IOException e) {
                            Log.d(TAG, "IOException");
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "An error occurred", Toast.LENGTH_SHORT);
                                }
                            });
                        }

                        return null;
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        // Update the visual information on the Team Activity
                        updateView();

                        // Send a new intent back to update the database
                        Intent intent = new Intent(TeamActivity.this, MainActivity.class);
                        intent.putExtra(Team.TAG, team);

                        // Send the "isNewWine" boolean back to differentiate the need to call an "insert" or an "update" call to the database
                        intent.putExtra("updated", true);

                        setResult(1, intent);

                        // Uncomment 'finish()' if you want to go back to the list after clicked on "Mettre à jour"
                        // finish();

                        currentTeam = null;
                    }
                };

                backgroundTask.execute();
            }
        });

    }

    @Override
    public void onBackPressed() {
        // TODO : prepare result for the main activity
        // → Done in the onPostExecute above
        super.onBackPressed();
    }

    private void updateView() {

        textTeamName.setText(team.getName());
        textLeague.setText(team.getLeague());
        textStadium.setText(team.getStadium());
        textStadiumLocation.setText(team.getStadiumLocation());
        textTotalScore.setText(Integer.toString(team.getTotalPoints()));
        textRanking.setText(Integer.toString(team.getRanking()));
        textLastMatch.setText(team.getLastEvent().toString());
        textLastUpdate.setText(team.getLastUpdate());

        // show The Image in a ImageView
        new DownloadImageTask(imageBadge).execute(team.getTeamBadge());

    }

    static void updateTeamBasics(Team team) throws IOException {
        // Make the url
        final URL url = WebServiceUrl.buildSearchTeam(team.getName());
        System.out.println("Basic information URL = " + url);

        // Send the request and update the team information
        sendRequestAndUpdate(url, team);
    }

    static void updateTeamRanking(Team team) throws IOException {
        // Make the url
        final URL url = WebServiceUrl.buildGetRanking(team.getIdLeague());
        System.out.println("Ranking URL = " + url);

        // Send the request and update the team information
        sendRequestAndUpdate(url, team);
    }

    static void updateTeamLastMatch(Team team) throws IOException {
        // Make the url
        final URL url = WebServiceUrl.buildSearchLastEvents(team.getIdTeam());
        System.out.println("Last match URL = " + url);

        // Send the request and update the team information
        sendRequestAndUpdate(url, team);
    }

    static void sendRequestAndUpdate(URL url, Team team) throws IOException {
        // Send the request
        final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        // Get the response
        InputStream response = urlConnection.getInputStream();

        // Read the response and update the team information
        TheSportsDbJSONResponseHandler jsonResponseHandler = new TheSportsDbJSONResponseHandler(team);
        jsonResponseHandler.readJsonStream(response);
    }


    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
