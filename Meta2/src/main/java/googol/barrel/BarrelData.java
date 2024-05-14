package googol.barrel;

import googol.queue.URLData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class that represents the data structure for the Barrel, containing everything that requires a save in case a barrel shuts down
 */
public class BarrelData implements Serializable {
    /**
     * Inverted index containing mapping of the every word and the links where they were found
     */
    private HashMap<String, ArrayList<URLData>> storage;

    /**
     * List of already indexed URLs
     */
    private ArrayList<URLData> savedURLs;

    /**
     * Map to store the URLs that redirect to the URL in question
     */
    private HashMap<String, ArrayList<String>> associatedURLs;

    /**
     * Number of searches for each set of words
     */
    private HashMap<String, Integer> numSearches;

    /**
     * Primary constructor for this class
     * @param storage Inverted index containing mapping of the every word and the links where they were found
     * @param savedURLs List of already indexed URLs
     * @param associatedURLs Map to store the URLs that redirect to the URL in question
     */
    public BarrelData(HashMap<String, ArrayList<URLData>> storage, ArrayList<URLData> savedURLs, HashMap<String, ArrayList<String>> associatedURLs, HashMap<String, Integer> numSearches) {
        super();
        this.storage = storage;
        this.savedURLs = savedURLs;
        this.associatedURLs = associatedURLs;
        this.numSearches = numSearches;
    }

    /**
     * Getter for the storage HashMap
     * @return The storage
     */
    public HashMap<String, ArrayList<URLData>> getStorage() {
        return storage;
    }

    /**
     * Setter for the storage HashMap
     * @param storage The storage to set
     */
    public void setStorage(HashMap<String, ArrayList<URLData>> storage) {
        this.storage = storage;
    }

    /**
     * Getter for the associated URLs
     * @return The associated URLs
     */
    public ArrayList<URLData> getSavedURLs() {
        return savedURLs;
    }

    /**
     * Setter for the saved URLs
     * @param savedURLs The saved URLs to set
     */
    public void setSavedURLs(ArrayList<URLData> savedURLs) {
        this.savedURLs = savedURLs;
    }

    /**
     * Getter for HashMap of associated URLs of every URL
     * @return The HashMap of associated URLs
     */
    public HashMap<String, ArrayList<String>> getAssociatedURLs() {
        return associatedURLs;
    }

    /**
     * Setter for HashMap of associated URLs of every URL
     * @param associatedURLs The HashMap of associated URLs to set
     */
    public void setAssociatedURLs(HashMap<String, ArrayList<String>> associatedURLs) {
        this.associatedURLs = associatedURLs;
    }

    /**
     * Setter for the number of searches for each set of words
     * @param numSearches The number of searches
     */
    public void setNumSearches(HashMap<String, Integer> numSearches) {
        this.numSearches = numSearches;
    }

    /**
     * Getter for the number of searches for each set of words
     * @return The number of searches
     */
    public HashMap<String, Integer> getNumSearches() {
        return numSearches;
    }
}
