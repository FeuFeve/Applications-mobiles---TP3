package com.example.tp3;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    SportDbHelper dbHelper;
    SimpleCursorAdapter adapter;
    ListView sportsTeamList;
    static SwipeRefreshLayout SRF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Sports database helper
        dbHelper = new SportDbHelper(this);
        dbHelper.populate();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Cursor
        adapter = new SimpleCursorAdapter(this,
            android.R.layout.simple_list_item_2,
            dbHelper.fetchAllTeams(),
            new String[] { SportDbHelper.COLUMN_TEAM_NAME, SportDbHelper.COLUMN_LEAGUE_NAME },
            new int[] { android.R.id.text1, android.R.id.text2 });

        // Sports team list
        sportsTeamList = findViewById(R.id.sportsTeamList);
        sportsTeamList.setAdapter(adapter);
        db.close();

        // When clicking on a team
        sportsTeamList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor currentTeamCursor = (Cursor) parent.getItemAtPosition(position);

                Team currentTeam = SportDbHelper.cursorToTeam(currentTeamCursor);

                Intent intent = new Intent(MainActivity.this, TeamActivity.class);
                intent.putExtra(Team.TAG, currentTeam);
                intent.putExtra("isNewTeam", false);
                startActivityForResult(intent, 1);
            }
        });

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 'Add a new sports team' button
        FloatingActionButton addNewTeamButton = findViewById(R.id.addNewTeamButton);
        addNewTeamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NewTeamActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        // Delete a wine
        registerForContextMenu(sportsTeamList);
        sportsTeamList.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                MainActivity.super.onCreateContextMenu(menu, v, menuInfo);
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.delete_sport_team, menu);
            }
        });

        // Refresh when swiping
        SRF = findViewById(R.id.swiperefresh);
        SRF.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    AsyncTask<?, ?, ?> backgroundTask;

                    @Override
                    public void onRefresh() {
                        Toast.makeText(getApplicationContext(), "Refreshing...", Toast.LENGTH_LONG).show();

                        backgroundTask = new AsyncTask<Object, Void, String>() {
                            @Override
                            protected String doInBackground(Object... params) {
                                try {
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), "Updating...", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                    List<Team> teams = dbHelper.getAllTeams();
                                    for (Team team : teams) {
                                        // Update the basic information (name, league, stadium name, badge...)
                                        TeamActivity.updateTeamBasics(team);

                                        // Update the ranking of the team
                                        TeamActivity.updateTeamRanking(team);

                                        // Update the last match section with the score
                                        TeamActivity.updateTeamLastMatch(team);

                                        // Apply the changes to the database
                                        dbHelper.updateTeam(team);
                                        System.out.println("Updated " + team.getName());
                                    }
                                } catch (MalformedURLException e) {
                                    Log.d("MainActivity", "MalformedURLException");
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), "URL error", Toast.LENGTH_SHORT);
                                        }
                                    });
                                } catch (IOException e) {
                                    Log.d("MainActivity", "IOException");
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
                                // Update the list
                                updateListAndCursor("Updated");

                                // Stop the refreshing action
                                SRF.setRefreshing(false);
                            }
                        };

                        backgroundTask.execute();
                    }
                }
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {

            boolean updated = data.getExtras().getBoolean("updated");

            // Case it's a team update
            if (updated) {
                Team team = data.getParcelableExtra(Team.TAG);
                int res = dbHelper.updateTeam(team);

                if (res == -1) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                    alertDialogBuilder.setTitle("Erreur");
                    alertDialogBuilder.setMessage("Un problème est survenu pendant la mise à jour de la base de données.");
                    alertDialogBuilder.create();
                    alertDialogBuilder.show();
                }
                else {
                    updateListAndCursor("Modifications enregistrées");
                }
            }

            // Case it's a new team
            else {
                Team team = data.getParcelableExtra(Team.TAG);
                boolean inserted = dbHelper.addTeam(team);

                if (inserted) {
                    updateListAndCursor("Une nouvelle équipe a été ajoutée.");
                }
                else {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                    alertDialogBuilder.setTitle("Ajout impossible");
                    alertDialogBuilder.setMessage("Un équipe de même nom et étant dans la même liste existe déjà dans la base de données.");
                    alertDialogBuilder.create();
                    alertDialogBuilder.show();
                }
            }
        }
    }

    void updateListAndCursor(String toastMessage) {
        // Update the cursor
        adapter.changeCursor(dbHelper.getReadableDatabase().query(SportDbHelper.TABLE_NAME, null, null, null, null, null, null));

        // Refresh the list of wines
        adapter.notifyDataSetChanged();

        // Toast (little message) to inform the user
        if (!toastMessage.isEmpty()) {
            Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        if (item.getItemId() == R.id.delete) {
            Cursor cursor = (Cursor) sportsTeamList.getItemAtPosition(info.position);
            Team team = SportDbHelper.cursorToTeam(cursor);
            dbHelper.deleteTeam(team.getId());

            updateListAndCursor("L'équipe a été supprimée");

            return true;
        }
        return super.onContextItemSelected(item);
    }
}
