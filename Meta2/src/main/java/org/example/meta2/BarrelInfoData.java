package org.example.meta2;

import java.io.Serializable;

public class BarrelInfoData implements Serializable {
    /**
            * Barrel id
     */
    private String id;

    /**
     * Time of response of the barrel
     */
    private String time;

    /**
     * Type of repartition of the barrel
     */
    private String repartition;

    /**
     * Constructor for the StatusData class
     * @param id Barrel id
     * @param time Time of response of the barrel
     * @param repartition Type of repartition of the barrel
     */
    public BarrelInfoData(String id, String time, String repartition) {
        this.id = id;
        this.time = time;
        this.repartition = repartition;
    }


    /**
     * Getter for the id
     * @return The id
     */
    public String getId() {
        return id;
    }

    /**
     * Setter for the id
     * @param id The id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Getter for the time
     * @return The time
     */
    public String getTime() {
        return time;
    }

    /**
     * Setter for the time
     * @param time The time to set
     */
    public void setTime(String time) {
        this.time = time;
    }

    /**
     * Getter for the repartition
     * @return The repartition
     */
    public String getRepartition() {
        return repartition;
    }

    /**
     * Setter for the repartition
     * @param repartition The repartition to set
     */
    public void setRepartition(String repartition) {
        this.repartition = repartition;
    }
}
