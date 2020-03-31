package com.example.tp3;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class NewTeamActivity extends AppCompatActivity {

    private EditText textTeam, textLeague;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_team);

        textTeam = findViewById(R.id.editNewName);
        textLeague = findViewById(R.id.editNewLeague);

        final Button saveButton = findViewById(R.id.saveButton);

        saveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String teamName = textTeam.getText().toString();

                if (teamName.isEmpty()) {
                    new AlertDialog.Builder(NewTeamActivity.this)
                            .setTitle("Sauvegarde impossible")
                            .setMessage("Le nom de l'équipe doit être non vide.")
                            .show();
                }
                else {
                    Intent intent = new Intent();

                    String teamLeague = textLeague.getText().toString();
                    Team team = new Team(teamName, teamLeague);

                    intent.putExtra(Team.TAG, team);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
    }


}
