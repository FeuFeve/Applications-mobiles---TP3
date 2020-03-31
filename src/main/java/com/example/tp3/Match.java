package com.example.tp3;

import android.os.Parcel;
import android.os.Parcelable;

public class Match implements Parcelable {

    public static final String TAG = Match.class.getSimpleName();

    private long id;

    private String label;
    private String homeTeam;
    private String awayTeam;
    private int homeScore;
    private int awayScore;

    public Match(){}

    public Match(long id, String label, String homeTeam, String awayTeam, int homeScore, int awayScore) {
        this.id = id;
        this.label = label;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
    }

    protected Match(Parcel in) {
        id = in.readLong();
        label = in.readString();
        homeTeam = in.readString();
        awayTeam = in.readString();
        homeScore = in.readInt();
        awayScore = in.readInt();
    }

    public static final Creator<Match> CREATOR = new Creator<Match>() {
        @Override
        public Match createFromParcel(Parcel in) {
            return new Match(in);
        }

        @Override
        public Match[] newArray(int size) {
            return new Match[size];
        }
    };

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(String homeTeam) {
        this.homeTeam = homeTeam;
    }

    public String getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(String awayTeam) {
        this.awayTeam = awayTeam;
    }

    public int getHomeScore() {
        return homeScore;
    }

    public void setHomeScore(int homeScore) {
        this.homeScore = homeScore;
    }

    public int getAwayScore() {
        return awayScore;
    }

    public void setAwayScore(int awayScore) {
        this.awayScore = awayScore;
    }

    public String toString() {
        return label + " : "+homeScore+"-"+awayScore;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(label);
        dest.writeString(homeTeam);
        dest.writeString(awayTeam);
        dest.writeInt(homeScore);
        dest.writeInt(awayScore);
    }
}
