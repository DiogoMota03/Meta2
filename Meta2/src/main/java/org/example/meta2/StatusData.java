package org.example.meta2;

import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;

import java.io.Serializable;
import java.util.ArrayList;

public class StatusData implements Serializable {

    /**
     * Active barrels
     */
    private ArrayList<BarrelInfoData> barrels = new ArrayList<>();

    /**
     * Top 10 searches
     */
    private String top10;

    /**
     * Constructor for the StatusData class
     * @param
     */
    public StatusData(ArrayList<BarrelInfoData> barrels, String top10) {
        this.barrels = barrels;
        this.top10 = top10;
    }

    //set getters and setters with javadoc

    /**
     * Getter for the barrels
     * @return The barrels
     */
    public ArrayList<BarrelInfoData> getBarrels() {
        return barrels;
    }

    /**
     * Setter for the barrels
     * @param barrels The barrels to set
     */
    public void setBarrels(ArrayList<BarrelInfoData> barrels) {
        this.barrels = barrels;
    }

    /**
     * Getter for the top 10 searches
     * @return The top 10 searches
     */
    public String getTop10() {
        return top10;
    }

    /**
     * Setter for the top 10 searches
     * @param top10 The top 10 searches to set
     */
    public void setTop10(String top10) {
        this.top10 = top10;
    }
}
