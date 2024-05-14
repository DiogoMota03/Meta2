package googol.queue;

import java.io.Serializable;

/**
 * Class to save the information that comes from a URL after being processed.
 * This class is Serializable, allowing instances to be serialized for storage or transmission
 */
public class URLData implements Serializable {
    /**
     * Link of the URL
     */
    private final String url;
    /**
     * Title of the page
     */
    private final String title;
    /**
     * Array of strings that saves some of the content associated with the URL
     */
    private final String[] content;

    /**
     * Number of associated URLs
     */
    private int numAssociatedURLs;

    /**
     * Main constructor of the class URLData
     * @param url URL of the object
     * @param title Title of the object's page
     * @param content 100 first words of the content of the page
     */
    public URLData(String url, String title, String[] content) {
        this.url = url;
        this.title = title;
        this.content = content;
        this.numAssociatedURLs = 0;
    }

    /**
     * Getter for the URL string
     * @return URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Getter for the title of the URL
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Getter for the content associated with the URL
     * @return content
     */
    public String[] getContent() {
        return content;
    }

    /**
     * Getter for the number of associated URLs
     * @return numAssociatedURLs
     */
    public int getNumAssociatedURLs() {
        return numAssociatedURLs;
    }

    /**
     * Setter for the number of associated URLs
     * @param numAssociatedURLs Number of associated URLs
     */
    public void setNumAssociatedURLs(int numAssociatedURLs) {
        this.numAssociatedURLs = numAssociatedURLs;
    }
}
