package googol.queue;

import googol.gateway.Gateway;
import googol.gateway.IGateway;

import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

/**
 * Represents a queue of URLs to be processed, with RMI connection availability
 */
public class URL_Queue extends UnicastRemoteObject implements IURL_Queue {
    /**
     * IP of the Queue
     */
    public static String URL_QUEUE_IP = "127.0.0.1";
    //public static String URL_QUEUE_IP = "192.168.167.51";

    /**
     * Queue of URLs that require processing
     */
    private Queue<String> urlQueue;
    /**
     * Unique URL_Queue instance
     */
    private static URL_Queue instance;
    /**
     * List of already indexed URLs
     */
    private static ArrayList<String> savedURLs;
    private static ArrayList<String> inProcessURLs;

    // Ao criar uma queue fazer: URL_Queue name = URL_Queue.getInstance();

    /**
     * Returns the instance of URL_Queue. If no instance exists, creates a new one.
     * @return instance of URL_Queue
     */
    public static URL_Queue getInstance() {
        if (instance == null) {
            try {
                instance = new URL_Queue();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
        return instance;
    }

    /**
     * Primary constructor for this class
     * @throws RemoteException If any communication error occurs during the remote call
     */
    public URL_Queue() throws RemoteException {
       this.urlQueue = new LinkedList<>();
    }

    /**
     * Adds a given URL to the Queue if it isn't already there or saved as an already indexed URL
     * @param url New URL to be added
     */
    @Override
    public void addURL(String url) {
        try {
            if (!urlQueue.contains(url) && !savedURLs.contains(url) && !inProcessURLs.contains(url))
                urlQueue.add(url);
            else
                System.out.println("URL already indexed or in queue!");
        } catch (Exception e) {
            System.out.println("Failed to add URL to the queue");
        }
    }

    /**
     * Shows the current Queue composition (helper function)
     */
    @Override
    public void showURLs() {
        if (!urlQueue.isEmpty()) {
            System.out.print("[ ");
            for (String url : urlQueue)
                System.out.print(url + " ");
            System.out.println(" ]");
        }
    }

    /**
     * Retrieves and removes the first URL in the queue for processing
     * @return First URL of the Queue
     */
    @Override
    public String requestNextURL() {
        if (urlQueue.isEmpty())
            return null;

        String nextURL = urlQueue.peek();

        while (savedURLs.contains(nextURL) || inProcessURLs.contains(nextURL)) {
            //System.out.println("Found: " + nextURL);
            urlQueue.remove();
            nextURL = urlQueue.peek();
            if (urlQueue.isEmpty())
                return null;
        }

        inProcessURLs.add(nextURL);
        urlQueue.remove();

        return nextURL;
    }

    /**
     * Notifies the Queue that a Downloader finished processing a URL
     * @param url URL that has finished processing
     */
    @Override
    public void finishedURLProcessing(String url) {
        inProcessURLs.remove(url);
        savedURLs.add(url);
    }

    /**
     * Adds all the URLs found by a downloader to the Queue
     * @param newURLs ArrayList of all the new-found URLs
     */
    @Override
    public void addAll(ArrayList<String> newURLs) {
        urlQueue.addAll(newURLs);
    }

    /**
     * Main method of the Queue: Creates/Retrieves the Queue instance, create a local ArrayList
     * to save indexed URLs, sets up an RMI connection and initializes data
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            URL_QUEUE_IP = args[0];
        }

        System.getProperties().put("java.security.policy", "policy.all");
        System.setProperty("java.rmi.server.hostname", Gateway.GATEWAY_IP);

        final String ANSI_GREEN = "\u001B[32m";
        final String ANSI_RED = "\u001B[31m";
        final String ANSI_RESET = "\u001B[0m";

        int success = 0, printed = 0;
        Scanner scanner = new Scanner(System.in);

        while(success == 0) {
            // Main code besides SIGINT catch
            try {
                URL_Queue queue = getInstance();

                LocateRegistry.createRegistry(1098);
                Naming.rebind("//" + URL_QUEUE_IP + ":1098/Queue", queue);
                IGateway g = (IGateway) Naming.lookup("rmi://" + Gateway.GATEWAY_IP + ":1099/XPTO");

                savedURLs = g.requestSavedURLs();

                if (savedURLs == null)
                    savedURLs = new ArrayList<>();

                inProcessURLs = new ArrayList<>();

                // Cria e vincula o registro RMI
                // LocateRegistry.createRegistry(1098);

                //queue.addURL("http://www.uc.pt");
                //queue.showURLs();

                System.out.println(ANSI_GREEN + "Queue is running..." + ANSI_RESET);
                success = 1;
                printed = 0;

                String a = scanner.next();
                if (a.equalsIgnoreCase("exit")) {
                    System.exit(0);
                }

            } catch (ConnectException e) {
                if (printed == 0)
                    System.out.println(ANSI_RED + "Gateway not reachable..." + ANSI_RESET);
                printed = 1;
                success = 0;

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            } catch (Exception e) {
                System.out.println("Error: " + e);
            }
        }

        /*
        try {
            Thread.sleep(5000); // Sleep for 5 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        */

    }
}
