package googol.barrel;

import googol.gateway.Gateway;
import googol.gateway.IGateway;
import googol.queue.URLData;

import java.io.*;
import java.net.*;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

/**
 * Class that represents the implementation of the {@link IISBs} interface
 * It provides methods for managing the inverted index and handling client requests
 */
public class ISBs extends UnicastRemoteObject implements IISBs {
    /**
     * Inverted index containing mapping of the every word and the links where they were found
     */
    private HashMap<String, ArrayList<URLData>> storage = new HashMap<>();

    /**
     * List of already indexed URLs
     */
    private ArrayList<URLData> savedURLs = new ArrayList<>();

    /**
     * Map to store the URLs that redirect to the URL in question
     */
    private HashMap<String, ArrayList<String>> associatedURLs = new HashMap<>();

    /**
     * Map to store how many times each search has been made
     */
    private HashMap<String, Integer> numSearches = new HashMap<>();

    /**
     * List of seconds that each search took
     */
    private ArrayList<Double> searchTime = new ArrayList<>();

    /**
     * Type of repartition of the Barrel: 0 represents words A-M, and 1 represents N-Z
     */
    private int type;

    /**
     * Barrel's unique ID
     */
    private int id;

    /**
     * Multicast address for receiving URL data
     */
    private final String multicastAdd = "224.3.2.1";

    /**
     * Multicast port to establish the connection
     */
    private final int port = 4321;

    /**
     * Primary constructor for this class
     * @throws RemoteException If any communication error occurs during the remote call
     */
    public ISBs() throws RemoteException {
        super();
    }

    /**
     * Setter for list of already indexed URLs
     * @param savedURLs List of already indexed URLs
     */
    @Override
    public void setSavedURLs(ArrayList<URLData> savedURLs) {
        this.savedURLs = savedURLs;
    }

    /**
     * Setter for Map with the URLs that redirect to the URL in question
     * @param associatedURLs the map to set
     */
    @Override
    public void setAssociatedURLs(HashMap<String, ArrayList<String>> associatedURLs) {
        this.associatedURLs = associatedURLs;
    }

    /**
     * Retrieves the type of repartition for the barrel
     * @return Type of repartition
     */
    @Override
    public int getType() {
        return type;
    }

    /**
     * Defines the type of repartition for the barrel
     * @param type Type of repartition
     */
    @Override
    public void setType(int type) {
        this.type = type;
    }

    /**
     * Retrieves the Barrel's unique ID
     * @return Barrel's ID
     */
    @Override
    public int getId() {
        return id;
    }

    /**
     * Defines the Barrel unique ID
     * @param id Barrel's ID
     */
    @Override
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Retrieves the Barrel's data
     * @return BarrelData object containing the Barrel's data
     */
    @Override
    public BarrelData getBarrelData() {
        return new BarrelData(storage, savedURLs, associatedURLs, numSearches);
    }

    /**
     * Sets the Barrel's data
     * @param bd BarrelData object containing the Barrel's data
     */
    @Override
    public void setBarrelData(BarrelData bd) {
        this.storage = bd.getStorage();
        this.savedURLs = bd.getSavedURLs();
        this.associatedURLs = bd.getAssociatedURLs();
    }

    /**
     * Method to get the associated URLs to the URL passed as a param
     * @param url URL to be searched
     * @return List of URLs associated
     */
    @Override
    public ArrayList<String> getAssociatedURLs(String url) {
        //System.out.println(url);
        //System.out.println(associatedURLs.size());

        if(associatedURLs.get(url) == null)
            System.out.println("URL not found");

        return associatedURLs.get(url);
    }

    /**
     * Getter for the number of searches HashMap
     * @param numSearches Number of searches
     */
    @Override
    public void setNumSearches(HashMap<String, Integer> numSearches) {
        this.numSearches = numSearches;
    }


    /**
     * Inserts a new word and corresponding URL to the inverted index
     * @param key Word to be indexed
     * @param value URL where word was found
     */
    public void insert(String key, URLData value) {
        if (!storage.containsKey(key)) {
            storage.put(key, new ArrayList<>());
        }
        storage.get(key).add(value);
    }

    /**
     * Searches for a word in the inverted index
     * @param key Word to be searched
     * @return List of values (type: URLData class) associated to that word
     */
    public ArrayList<URLData> search(String key) {
        return storage.get(key);
    }


    /**
     * Save the current Barrel data on an object file
     */
    public void guardaDados() {
        File f = new File("BarrelData" + id + ".dat");
        try {
            FileOutputStream fos = new FileOutputStream(f);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            BarrelData bd = new BarrelData(storage, savedURLs, associatedURLs, numSearches);
            oos.writeObject(bd);
        } catch (FileNotFoundException ex) {
            System.err.println("Error creating file: " + ex.getMessage());
        } catch (IOException ex) {
            System.err.println("Error writing to the file: " + ex.getMessage());
        } catch (Exception ex) {
            System.err.println("Error: " + ex.getMessage());
        }
    }


    /**
     * Method to intersect the results of the search of each word
     * @param words Words to be searched
     * @return List of URLData class objects that contains all the words searched
     */
    private List<URLData> intersection(String[] words) {
        if (search(words[0]) == null) {
            return null;
        }

        List<URLData> result = new ArrayList<>(search(words[0]));

        if(words.length > 1) {
            // percorrer as restantes palavras
            for (int i = 1; i < words.length; i++) {
                if (search(words[i]) == null) {
                    return null;
                }

                // reter apenas os resultados que aparecem nas duas listas
                result.retainAll(search(words[i]));

                if (result.isEmpty()) {
                    return result;
                }
            }
        }

        return result;
    }

    /**
     * Process a client's request (word search) and returns the result
     * @param words Words to be searched
     * @return List of URLData class objects matching the search query
     */
    @Override
    public List<URLData> clientRequest(String[] words) {
        System.out.println("Search requested...");

        // guardar os resultados da primeira palavra
        List<URLData> result;

        result = intersection(words);

        if (result != null) {
            result.sort(Comparator.comparing(URLData::getNumAssociatedURLs).reversed());
        }

        return result;
    }


    /**
     * Store the new URL data and updates the associated storage
     * @param urlData URLData object, contains URL, page title and some words in the URL
     */
    private void saveURLData(URLData urlData) {
        // eliminar as palavras repetidas
        Set<String> words = new HashSet<>(Arrays.asList(urlData.getContent()));

        for (String word : words) {
            if (word.isEmpty())
                continue;

            if (type == 0 && word.charAt(0) >= 'a' && word.charAt(0) <= 'm') {
                insert(word, urlData);
            } else if (type == 1 && word.charAt(0) >= 'n' && word.charAt(0) <= 'z') {
                insert(word, urlData);
            }
        }

        savedURLs.add(urlData);
    }


    /**
     * Initiates the ISB's instance, starts listening for incoming requests, and processes them
     */
    public void start() {
        System.getProperties().put("java.security.policy", "policy.all");
        System.setProperty("java.rmi.server.hostname", Gateway.GATEWAY_IP);
        MulticastSocket socket = null;

        try {
            socket = new MulticastSocket(port);
            InetAddress group = InetAddress.getByName(multicastAdd);

            socket.joinGroup(new InetSocketAddress(group, 0), NetworkInterface.getByIndex(0));

            while (true) {
                byte[] buffer = new byte[65536];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String message = new String(packet.getData(), 0, packet.getLength());
                //System.out.println(message);

                String[] data = message.split("\\|");

                String[] content = data[2].split(" ");

                URLData urlData = new URLData(data[0], data[1], content);

                String[] urls = null;
                if (data.length > 3 && !data[3].isEmpty()) {
                    urls = data[3].split(" ");
                }
                /* else {
                    System.out.println("SIZE MENOR QUE 3");
                    for (String s : data) {
                        System.out.println(s);
                    }
                }*/

                if (urls != null) {
                    for (String url : urls) {
                        if (!associatedURLs.containsKey(url)) {
                            ArrayList<String> l = new ArrayList<>();
                            l.add(data[0]);
                            associatedURLs.put(url, l);

                            urlData.setNumAssociatedURLs(1);
                        } else {
                            if (!associatedURLs.get(url).contains(data[0])) {
                                associatedURLs.get(url).add(data[0]);
                                urlData.setNumAssociatedURLs(urlData.getNumAssociatedURLs() + 1);
                            }
                        }
                    }
                }

                saveURLData(urlData);

                System.out.println(urlData.getUrl() + " " + urlData.getTitle());

                String successMes = "SUCCESS: Barrel received URL";
                byte[] successbuf = successMes.getBytes();
                DatagramPacket successPack = new DatagramPacket(successbuf, successbuf.length, packet.getAddress(), packet.getPort());
                socket.send(successPack);
            }

        } catch (Exception e) {
            System.out.println("Error in barrel's start method: " + e);
        } finally {
            if (socket != null)
                socket.close();
        }
    }

    /**
     * Method called by the Gateway to ensure the Barrel receives the call and it's still alive.
     */
    @Override
    public void areYouAlive() {}


/**
     * Retrieves the list of saved URLs in the Barrel
     * @return List of saved URLs
     */
    @Override
    public ArrayList<String> getSavedURLs() {
        return savedURLs.stream().collect(ArrayList::new, (list, urlData) -> list.add(urlData.getUrl()), ArrayList::addAll);
    }

    /**
     * Method to save every search and how many times each one was made
     * @param s String search
     */
    @Override
    public void saveSearch(String s) {
        if (!numSearches.containsKey(s)) {
            numSearches.put(s, 1);
        } else {
            numSearches.put(s, numSearches.get(s) + 1);
        }
    }

    /**
     * Retrieves the top 10 most searched words
     * @return String with the top 10 most searched words
     */
    @Override
    public String getTop10() {
        List<Map.Entry<String, Integer>> sortedSearches = new ArrayList<>(numSearches.entrySet());
        //sortedSearches.sort(Comparator.comparing(Map.Entry::getValue, Comparator.reverseOrder()));
        sortedSearches.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        StringBuilder top10 = new StringBuilder();
        int counter = 0;

        for (Map.Entry<String, Integer> sorted : sortedSearches) {
            if (counter >= 10 || sortedSearches.size() == counter) {
                break;
            }

            top10.append("\t").append(sorted.getValue()).append("\t\t").append(sorted.getKey()).append("\n");
            counter++;
        }

        return top10.toString();
    }

    /**
     * Method to calculate the average search time
     * @return Average time
     */
    @Override
    public Double getAvgSearchTime() {
        return searchTime.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    /**
     * Method to add how long a search takes to happen
     * @param i Search's time
     */
    @Override
    public void saveSearchTime(double i) {
        this.searchTime.add(i);
    }

    /**
     * Main method for testing the ISBs class
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        ISBs isbs;

        final String ANSI_GREEN = "\u001B[32m";
        final String ANSI_RED = "\u001B[31m";
        final String ANSI_RESET = "\u001B[0m";

        Thread exitRequest = new Thread(() -> {
            Scanner sc = new Scanner(System.in);
            String exitString = sc.next();

            if (exitString.equalsIgnoreCase("exit")) {
                System.exit(0);
            }
        });
        exitRequest.start();

        int success = 0, printed = 0;
        while (success == 0) {
            try {
                IGateway g = (IGateway) Naming.lookup("rmi://" + Gateway.GATEWAY_IP + ":1099/XPTO");

                isbs = new ISBs();
                success = 1;
                printed = 0;

                g.barrelRegistration(isbs);

                //System.out.println(isbs.id);
                //System.out.println(isbs.type);

                System.out.println(ANSI_GREEN + "Barrel running..." + ANSI_RESET);

                isbs.start();

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

            }
            catch (Exception e) {
                System.out.println("Exception in main: " + e);
            }
        }
    }
}
