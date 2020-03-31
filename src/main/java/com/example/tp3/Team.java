package com.example.tp3;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;


public class Team implements Parcelable {

    public static final String TAG = Team.class.getSimpleName();

    private long id; // used for the _id colum of the db helper

    private String name;
    private long idTeam; // used for web service
    private String league;
    private long idLeague;
    private String stadium;
    private String stadiumLocation;
    private String teamBadge;
    private int totalPoints;
    private int ranking;
    private Match lastEvent;
    private String lastUpdate;

    public Team(String name) {
        this.name = name;
    }

    public Team(String name, String league) {
        this.name = name;
        this.league= league;
    }

    public Team(long id, String name, long idTeam, String league, long idLeague, String stadium, String stadiumLocation, String teamBadge, int totalPoints, int ranking, Match lastEvent, String lastUpdate) {
        this.id = id;
        this.name = name;
        this.idTeam = idTeam;
        this.league = league;
        this.idLeague = idLeague;
        this.stadium = stadium;
        this.stadiumLocation = stadiumLocation;
        this.teamBadge = teamBadge;
        this.totalPoints = totalPoints;
        this.ranking = ranking;
        this.lastEvent = lastEvent;
        this.lastUpdate = lastUpdate;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getIdTeam() {
        return idTeam;
    }


    public String getLeague() {
        return league;
    }

    public long getIdLeague() { return idLeague; }

    public Match getLastEvent() {
        return lastEvent;
    }

    public String getStadium() { return stadium;}

    public String getStadiumLocation() {
        return stadiumLocation;
    }

    public String getTeamBadge() {
        return teamBadge;
    }
    public int getTotalPoints() {
        return totalPoints;
    }

    public int getRanking() {
        return ranking;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setId(long id) { this.id = id;}

    public void setName(String name) {
        this.name = name;
    }

    public void setIdTeam(long idTeam) {
        this.idTeam = idTeam;
    }

    public void setLeague(String league) {
        this.league = league;
    }

    public void setIdLeague(long idLeague) { this.idLeague = idLeague; }

    public void setLastEvent(Match lastEvent) {
        this.lastEvent = lastEvent;
        setLastUpdate();
    }

    public void setStadium(String stadium) {
        this.stadium = stadium;
    }

    public void setStadiumLocation(String stadiumLocation) {
        this.stadiumLocation = stadiumLocation;
    }

    public void setTeamBadge(String teamBadge) {
        this.teamBadge = teamBadge;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
        setLastUpdate();
    }

    public void setRanking(int ranking) {
        this.ranking = ranking;
        setLastUpdate();
    }

    private void setLastUpdate() {
        Date currentTime = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss");
        this.lastUpdate = dateFormat.format(currentTime);
    }

    @Override
    public String toString() {
        return this.name+"("+this.league +"): "+this.lastEvent +"°C";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeLong(idTeam);
        dest.writeString(league);
        dest.writeLong(idLeague);
        dest.writeString(stadium);
        dest.writeString(stadiumLocation);
        dest.writeString(teamBadge);
        dest.writeInt(totalPoints);
        dest.writeInt(ranking);
        if (lastEvent != null)
            lastEvent.writeToParcel(dest,flags);
        dest.writeString(lastUpdate);
    }


    public static final Creator<Team> CREATOR = new Creator<Team>()
    {
        @Override
        public Team createFromParcel(Parcel source)
        {
            return new Team(source);
        }

        @Override
        public Team[] newArray(int size)
        {
            return new Team[size];
        }
    };

    public Team(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
        this.idTeam = in.readLong();
        this.league = in.readString();
        this.idLeague = in.readLong();
        this.stadium = in.readString();
        this.stadiumLocation = in.readString();
        this.teamBadge = in.readString();
        this.totalPoints = in.readInt();
        this.ranking = in.readInt();
        this.lastEvent = Match.CREATOR.createFromParcel(in);
        this.lastUpdate = in.readString();
    }


}
