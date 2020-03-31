package com.example.tp3;

import android.util.JsonReader;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Process the response to a GET request to the Web service
 * https://www.thesportsdb.com/api/v1/json/1/searchteams.php?t=R
 * Responses must be provided in JSON.
 *
 */


public class TheSportsDbJSONResponseHandler {

    private static final String TAG = TheSportsDbJSONResponseHandler.class.getSimpleName();

    private Team team;


    public TheSportsDbJSONResponseHandler(Team team) {
        this.team = team;
    }

    /**
     * @param response done by the Web service
     * @return A Team with attributes filled with the collected information if response was
     * successfully analyzed
     */
    public void readJsonStream(InputStream response) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(response, "UTF-8"));
        try {
            readTeams(reader);
        } finally {
            reader.close();
        }
    }

    public void readTeams(JsonReader reader) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            String name;
            try {
                name = reader.nextName();
            } catch (IllegalStateException e) {
                Log.d(TAG, "IllegalStateException");
                return;
            }

            switch (name) {
                case "teams":
                    readArrayTeams(reader);
                    break;
                case "table":
                    readArrayRanking(reader);
                    break;
                case "results":
                    readArrayMatch(reader);
                    break;
                default:
                    reader.skipValue();
            }
        }
        reader.endObject();
    }

    private void readArrayTeams(JsonReader reader) throws IOException {
        try {
            reader.beginArray();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException");
            return;
        }
        int nb = 0; // only consider the first element of the array
        while (reader.hasNext() ) {
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (nb == 0) {
                    if (name.equals("idTeam")) {
                        team.setIdTeam(reader.nextLong());
                    } else if (name.equals("strTeam")) {
                        team.setName(reader.nextString());
                    } else if (name.equals("strLeague")) {
                        team.setLeague(reader.nextString());
                    } else if (name.equals("idLeague")) {
                        team.setIdLeague(reader.nextLong());
                    } else if (name.equals("strStadium")) {
                        team.setStadium(reader.nextString());
                    } else if (name.equals("strStadiumLocation")) {
                        team.setStadiumLocation(reader.nextString());
                    } else if (name.equals("strTeamBadge")) {
                        team.setTeamBadge(reader.nextString());
                    } else {
                        reader.skipValue();
                    }
                }  else {
                    reader.skipValue();
                }
            }
            reader.endObject();
            nb++;
        }
        reader.endArray();
    }

    private void readArrayRanking(JsonReader reader) throws IOException {
        try {
            reader.beginArray();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException");
            return;
        }

        int rank = 0; // Rank of the team we are currently on

        boolean isCurrent = false; // Set to true when current team is the one we are searching for, false otherwise

        while (reader.hasNext()) {
            reader.beginObject();
            rank++;
            while (reader.hasNext()) {
                String current = reader.nextName();

                if (current.equals("teamid")) {
                    long currentTeamId = reader.nextLong();
                    if (team.getIdTeam() == currentTeamId) {
                        isCurrent = true;
                        team.setRanking(rank);
                    }
                    else {
                        isCurrent = false;
                    }
                } else if (isCurrent) {
                    if (current.equals("total")) {
                        team.setTotalPoints(reader.nextInt());
                    }
                    else {
                        reader.skipValue();
                    }
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
        }
        reader.endArray();
    }

    private void readArrayMatch(JsonReader reader) throws IOException {
        try {
            reader.beginArray();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException");
            return;
        }

        int nb = 0; // only consider the first element of the array

        // Match info
        long id = -1;
        String label = "";
        String homeTeam = "";
        String awayTeam = "";
        int homeScore = -1;
        int awayScore = -1;

        while (reader.hasNext() ) {
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (nb == 0) {
                    if (name.equals("idEvent")) {
                        id = reader.nextLong();
                    } else if (name.equals("strEvent")) {
                        label = reader.nextString();
                    } else if (name.equals("strHomeTeam")) {
                        homeTeam = reader.nextString();
                    } else if (name.equals("strAwayTeam")) {
                        awayTeam = reader.nextString();
                    } else if (name.equals("intHomeScore")) {
                        try {
                            homeScore = reader.nextInt();
                        } catch (IllegalStateException e) {
                            // Happens when the match wasn't played and there is "NULL" instead of the score
                            homeScore = 0;
                            reader.skipValue();
                        }
                    } else if (name.equals("intAwayScore")) {
                        try {
                            awayScore = reader.nextInt();
                        } catch (IllegalStateException e) {
                            // Happens when the match wasn't played and there is "NULL" instead of the score
                            awayScore = 0;
                            reader.skipValue();
                        }
                    } else {
                        reader.skipValue();
                    }
                }  else {
                    reader.skipValue();
                }
            }
            reader.endObject();
            nb++;
        }
        reader.endArray();

        // Update the team's last match if any was found
        if (id != -1) {
            Match lastMatch = new Match(id, label, homeTeam, awayTeam, homeScore, awayScore);
            team.setLastEvent(lastMatch);
        }
    }
}
