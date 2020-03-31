package com.example.tp3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE;

public class SportDbHelper extends SQLiteOpenHelper {

    private static final String TAG = SportDbHelper.class.getSimpleName();

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "sport.db";

    public static final String TABLE_NAME = "sport";

    public static final String _ID = "_id";
    public static final String COLUMN_TEAM_NAME = "team";
    public static final String COLUMN_TEAM_ID = "idTeam";
    public static final String COLUMN_LEAGUE_NAME = "league";
    public static final String COLUMN_LEAGUE_ID = "idLeague";
    public static final String COLUMN_STADIUM = "stadium";
    public static final String COLUMN_STADIUM_LOCATION = "stadiumLocation";
    public static final String COLUMN_TEAM_BADGE = "teamBadge";
    public static final String COLUMN_TOTAL_POINTS = "totalPoints";
    public static final String COLUMN_RANKING = "ranking";
    public static final String COLUMN_LAST_MATCH_ID = "idMatch";
    public static final String COLUMN_LAST_MATCH_LABEL = "labelMatch";
    public static final String COLUMN_LAST_MATCH_HOME_TEAM = "homeTeam";
    public static final String COLUMN_LAST_MATCH_AWAY_TEAM = "awayTeam";
    public static final String COLUMN_LAST_MATCH_SCORE_HOME = "homeScore";
    public static final String COLUMN_LAST_MATCH_SCORE_AWAY = "awayScore";
    public static final String COLUMN_LAST_UPDATE = "lastupdate";

    public SportDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_BOOK_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COLUMN_TEAM_NAME + " TEXT NOT NULL, " +
                COLUMN_TEAM_ID + " INTEGER, " +
                COLUMN_LEAGUE_NAME + " TEXT, " +
                COLUMN_LEAGUE_ID + " INTEGER, " +
                COLUMN_STADIUM + " TEXT, " +
                COLUMN_STADIUM_LOCATION + " TEXT, " +
                COLUMN_TEAM_BADGE + " TEXT, " +
                COLUMN_TOTAL_POINTS + " INTEGER, " +
                COLUMN_RANKING + " INTEGER, " +
                COLUMN_LAST_MATCH_ID + " INTEGER, " +
                COLUMN_LAST_MATCH_LABEL + " TEXT, " +
                COLUMN_LAST_MATCH_HOME_TEAM + " TEXT, " +
                COLUMN_LAST_MATCH_AWAY_TEAM + " TEXT, " +
                COLUMN_LAST_MATCH_SCORE_HOME + " INTEGER, " +
                COLUMN_LAST_MATCH_SCORE_AWAY + " INTEGER, " +
                COLUMN_LAST_UPDATE+ " TEXT, " +

                // To assure the application have just one team entry per
                // team name and league, it's created a UNIQUE
                " UNIQUE (" + COLUMN_TEAM_NAME + ", " +
                COLUMN_LEAGUE_NAME + ") ON CONFLICT ROLLBACK);";

        db.execSQL(SQL_CREATE_BOOK_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * Fills ContentValues result from a Team object
     */
    private ContentValues fill(Team team) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TEAM_NAME, team.getName());
        values.put(COLUMN_TEAM_ID, team.getIdTeam());
        values.put(COLUMN_LEAGUE_NAME, team.getLeague());
        values.put(COLUMN_LEAGUE_ID, team.getIdLeague());
        values.put(COLUMN_STADIUM, team.getStadium());
        values.put(COLUMN_STADIUM_LOCATION, team.getStadiumLocation());
        values.put(COLUMN_TEAM_BADGE, team.getTeamBadge());
        values.put(COLUMN_TOTAL_POINTS, team.getTotalPoints());
        values.put(COLUMN_RANKING, team.getRanking());
        Match lastEvent = team.getLastEvent();
        if (lastEvent == null) {
            lastEvent = new Match();
        }
        values.put(COLUMN_LAST_MATCH_ID, lastEvent.getId());
        values.put(COLUMN_LAST_MATCH_LABEL, lastEvent.getLabel());
        values.put(COLUMN_LAST_MATCH_HOME_TEAM, lastEvent.getHomeTeam());
        values.put(COLUMN_LAST_MATCH_AWAY_TEAM, lastEvent.getAwayTeam());
        values.put(COLUMN_LAST_MATCH_SCORE_HOME, lastEvent.getHomeScore());
        values.put(COLUMN_LAST_MATCH_SCORE_AWAY, lastEvent.getAwayScore());
        values.put(COLUMN_LAST_UPDATE, team.getLastUpdate());
        return values;
    }

    /**
     * Adds a new team
     * @return  true if the team was added to the table ; false otherwise (case when the pair (team, championship) is
     * already in the data base)
     */
    public boolean addTeam(Team team) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = fill(team);

        Log.d(TAG, "adding: " + team.getName() + " with id=" + team.getId());

        // Inserting Row
        // The unique used for creating table ensures to have only one copy of each pair (team, championship)
        // If rowID = -1, an error occured
        long rowID = db.insertWithOnConflict(TABLE_NAME, null, values, CONFLICT_IGNORE);
        db.close(); // Closing database connection

        return (rowID != -1);
    }

    /**
     * Updates the information of a team inside the data base
     * @return the number of updated rows
     */
    public int updateTeam(Team team) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = fill(team);

        // updating row
        return db.updateWithOnConflict(TABLE_NAME, values, _ID + " = ?",
                new String[] { String.valueOf(team.getId()) }, CONFLICT_IGNORE);
    }

    /**
     * Returns a cursor on all the teams of the data base
     */
    public Cursor fetchAllTeams() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, COLUMN_TEAM_NAME + " ASC", null);

        Log.d(TAG, "call fetchAllTeams()");
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    /**
     * Returns a list on all the teams of the data base
     */
    public List<Team> getAllTeams() {
        List<Team> res = new ArrayList<>();

        for (Cursor cursor = fetchAllTeams(); !cursor.isAfterLast(); cursor.moveToNext()) {
            res.add(cursorToTeam(cursor));
        }

        return res;
    }

    public void deleteTeam(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, _ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
    }

    public void populate() {
        Log.d(TAG, "call populate()");
        addTeam(new Team("RC Toulonnais", "French Top 14"));
        addTeam(new Team("ASM Clermont Auvergne", "French Top 14"));
        addTeam(new Team("Stade Rochelais", "French Top 14"));
        addTeam(new Team("Bath Rugby","English Premiership Rugby"));
        addTeam(new Team("Edinburgh","Pro14"));
        addTeam(new Team("Stade Toulousain", "French Top 14"));
        addTeam(new Team("Wasps","English Premiership Rugby"));
        addTeam(new Team("Bristol Rugby","English Premiership Rugby"));
        addTeam(new Team("CA Brive","French Top 14"));

        SQLiteDatabase db = this.getReadableDatabase();
        long numRows = DatabaseUtils.longForQuery(db, "SELECT COUNT(*) FROM "+TABLE_NAME, null);
        Log.d(TAG, "nb of rows="+numRows);
        db.close();
    }

    public static Team cursorToTeam(Cursor cursor) {

        Match lastEvent = new Match(cursor.getLong(cursor.getColumnIndex(COLUMN_LAST_MATCH_ID)),
                cursor.getString(cursor.getColumnIndex(COLUMN_LAST_MATCH_LABEL)),
                cursor.getString(cursor.getColumnIndex(COLUMN_LAST_MATCH_HOME_TEAM)),
                cursor.getString(cursor.getColumnIndex(COLUMN_LAST_MATCH_AWAY_TEAM)),
                cursor.getInt(cursor.getColumnIndex(COLUMN_LAST_MATCH_SCORE_HOME)),
                cursor.getInt(cursor.getColumnIndex(COLUMN_LAST_MATCH_SCORE_AWAY)));

        Team team = new Team(cursor.getLong(cursor.getColumnIndex(_ID)),
                cursor.getString(cursor.getColumnIndex(COLUMN_TEAM_NAME)),
                cursor.getLong(cursor.getColumnIndex(COLUMN_TEAM_ID)),
                cursor.getString(cursor.getColumnIndex(COLUMN_LEAGUE_NAME)),
                cursor.getLong(cursor.getColumnIndex(COLUMN_LEAGUE_ID)),
                cursor.getString(cursor.getColumnIndex(COLUMN_STADIUM)),
                cursor.getString(cursor.getColumnIndex(COLUMN_STADIUM_LOCATION)),
                cursor.getString(cursor.getColumnIndex(COLUMN_TEAM_BADGE)),
                cursor.getInt(cursor.getColumnIndex(COLUMN_TOTAL_POINTS)),
                cursor.getInt(cursor.getColumnIndex(COLUMN_RANKING)),
                lastEvent,
                cursor.getString(cursor.getColumnIndex(COLUMN_LAST_UPDATE))
        );

        return team;
    }

    public Team getTeam(int id) {
        Team team = null;
	// TODO
	return team;
    }
}
