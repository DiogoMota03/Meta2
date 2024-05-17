package googol.gateway;

import googol.barrel.BarrelData;
import googol.barrel.IISBs;
import googol.client.IClient;
import googol.queue.IURL_Queue;
import googol.queue.URLData;
import googol.queue.URL_Queue;
import org.example.meta2.HackerNewsController;
import org.example.meta2.HackerNewsItemRecord;
import org.springframework.stereotype.Service;

import java.net.*;
import java.net.UnknownHostException;
import java.rmi.*;
import java.rmi.ConnectException;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.time.Duration;
import java.util.*;
import java.util.HashMap;
import java.time.Instant;
import java.util.UUID;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Represents the main server component implementing the {@link IGateway} interface.
 * Acts as a central hub for communication with connected clients and barrels
 */
public class Gateway extends UnicastRemoteObject implements IGateway {
    /**
     * IP of the Gateway
     */
    public static String GATEWAY_IP = "127.0.0.1";
    //public static String GATEWAY_IP = "192.168.167.51";

    /**
     * Variable to control the number of times the URL_Queue was not printed
     */
    private int notPrinted = 1;
    //public static URL_Queue queue;
    /**
     * Map to store the clients that are connected to the server
     */
    private static HashMap<String, IClient> clients = new HashMap<>();

    /**
     * ArrayList with all the barrels that are connected to the server
     */
    private static ArrayList<IISBs> barrels = new ArrayList<>();

    /**
     * ArrayList to store the status of each barrel
     */
    private static ArrayList<Boolean> barrelIsAlive = new ArrayList<>();

    /**
     * Last barrel, with distribution a-m, that was used for a search
     * */
    private int lastBarrelEven = -1;

    /**
     * Last barrel, with distribution n-z, that was used for a search
     * */
    private int lastBarrelOdd = -1;

    private static List<URLData> result = new ArrayList<>();

    /**
     * Primary constructor for this class
     * @throws RemoteException If any communication error occurs during the remote call
     */
    public Gateway() throws RemoteException {
        super();
    }

    public String requestId() throws RemoteException {
        return UUID.randomUUID().toString();
    }

    /**
     * Method to subscribe a client to the server
     * @param name Client's name
     * @param c Client to be subscribed
     * @return 1 if the subscription was successful, 0 otherwise
     * @throws RemoteException If any communication error occurs during the remote call
     */
    @Override
    public int subscribe(String name, IClient c) throws RemoteException {
        //System.out.println("Subscribing " + name);
        if(clients.get(name) != null){
            System.out.println("> Subscription failed");
            c.print_on_client("Client " + name + " already exists");
            return 0;
        }else {
            clients.put(name, c);
            System.out.println("> Subscribed " + name);
        }

        return 1;
    }

    /**
     * Method to get the maximum index of the page
     * @param size Size of the result of the search
     * @return The maximum index of the server
     */
    @Override
    public int getMaxIndex(int size) {
        if(size % 10 == 0){
            return size/10 -1;
        }else {
            return size/10;
        }
    }

    /**
     * Method to check if the barrels are still alive
     */
    public void isAlive() {
        if (!barrels.isEmpty()) {
            //System.out.println("Barrels connected: " + getBarrelsAlive());
            int i = 0;
            for (IISBs barrel : barrels) {
                if (!barrelIsAlive.get(i)){
                    //System.out.println("Barrel " + i + " is alive: " + barrelIsAlive.get(i));
                    i++;
                    continue;
                }
                try {
                    barrel.areYouAlive();
                    //System.out.println("Barrel " + i + " is alive");
                    barrelIsAlive.set(i, true);
                } catch (ConnectException e) {
                    //System.out.println("Barrel " + i + " is dead");
                    barrelIsAlive.set(i, false);
                } catch (Exception e) {
                    //System.out.println("An error occurred while calling for barrel...");
                    barrelIsAlive.set(i, false);
                }
                //System.out.println("Barrel " + i + " is alive: " + barrelIsAlive.get(i));
                i++;
            }
        }
    }

    /**
     * Method to get the number of barrels that are still alive
     * @return The number of alive barrels
     */
    public int getBarrelsAlive() {
        int counter = 0;
        for (boolean b : barrelIsAlive) {
            if (b) {
                counter++;
            }
        }
        return counter;
    }

    /**
     * Method to get the top 10 searches from the barrels
     * @return The top 10 searches
     */
    public String getTop10Searches() {
        for (int i = 0; i < barrels.size(); i++) {
            if (barrelIsAlive.get(i)) {
                IISBs barrel = barrels.get(i);

                try {
                    return barrel.getTop10();
                } catch (RemoteException | NullPointerException e) {
                    System.out.println("Error while getting TOP10 searches");
                } catch (Exception e) {
                    System.out.println("Error in getTop10Searches()...");
                }
            }
        }

        return null;
    }

    /**
     * Requesting all status information of the server
     * @param name Client's name
     * @throws RemoteException If any communication error occurs during the remote call
     */
    @Override
    public void getStatus(String name) throws RemoteException {
        //System.out.println("Getting Status");

        String top10Searches;

        StringBuilder info = new StringBuilder("Active Barrels: " + getBarrelsAlive() + "\n");
        for (int i = 0; i < barrelIsAlive.size(); i++) {
            if (barrelIsAlive.get(i)) {
                IISBs barrel = barrels.get(i);
                double avgSearchTime = barrel.getAvgSearchTime();
                String format = String.format("%.2f", avgSearchTime);

                info.append("\t" + "Barrel ").append(i).append("\t").append(format).append("ms\n");
            }
        }

        top10Searches = getTop10Searches();

        if (top10Searches != null) {
            info.append("\n> TOP10 Searches:\n").append(top10Searches);
            if (top10Searches.isEmpty()) {
                info.append("\tNothing to be shown");
            }
        }

        clients.get(name).print_on_client(info.toString());
    }

    /**
     * Method to insert a new URL into the Queue
     * @param name Client's name
     * @param s URL to be inserted
     */
    @Override
    public void insertURL(String name, String s) {
        System.out.println("> Inserting: " + s);
        try {
            IURL_Queue queue = (IURL_Queue) Naming.lookup("rmi://" + URL_Queue.URL_QUEUE_IP + ":1098/Queue");
            queue.addURL(s);
            //+
            // queue.showURLs();
        } catch (NotBoundException e) {
            System.out.println("URL_Queue not reachable...");
        } catch (MalformedURLException e) {
            System.out.println("Invalid URL was presented!");
        } catch (RemoteException e) {
            System.out.println("Error while adding URL to the queue");
        } catch (Exception e) {
            System.out.println("Error in insertURL()...");
        }

        //queue.addURL(s);
        //clients.get(name).print_on_client("inserting");
    }

    /**
     * Method to request the next URL from the Queue to be processed
     * @return The next URL to be processed
     * @throws RemoteException If any communication error occurs during the remote call
     */
    @Override
    public String requestNext() throws RemoteException {
        try {
            IURL_Queue q = (IURL_Queue) Naming.lookup("rmi://" + URL_Queue.URL_QUEUE_IP + ":1098/Queue");
            notPrinted = 1;
            return q.requestNextURL();

        } catch (MalformedURLException | NotBoundException e) {
            if (notPrinted == 1) {
                System.out.println("URL_Queue not reachable...");
                notPrinted = 0;
            }
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Method to show the URLs in the Queue
     */
    @Override
    public void showURLs() {
        try {
            IURL_Queue q = (IURL_Queue) Naming.lookup("rmi://" + URL_Queue.URL_QUEUE_IP + ":1098/Queue");
            q.showURLs();

        } catch (MalformedURLException e) {
            System.out.println("Invalid URL format");
        } catch (NotBoundException e) {
            System.out.println("URL_Queue not reachable...");
        } catch (RemoteException e) {
            System.out.println("Error while showing URLs");
        } catch (Exception e) {
            System.out.println("Error in showURLs()...");
        }
    }

    /**
     * Method to add new URLs to the Queue
     * @param newURLs List of URLs to be added
     */
    @Override
    public void addURLs(ArrayList<String> newURLs) {
        try {
            IURL_Queue q = (IURL_Queue) Naming.lookup("rmi://" + URL_Queue.URL_QUEUE_IP + ":1098/Queue");
            q.addAll(newURLs);

        } catch (MalformedURLException e) {
            System.out.println("Invalid URL format");
        } catch (NotBoundException e) {
            System.out.println("URL_Queue not reachable...");
        } catch (RemoteException e) {
            System.out.println("Error while adding URLs to the queue");
        } catch (Exception e) {
            System.out.println("Error in addURLs()...");
        }
    }

    /**
     * Method to mark a URL has indexed
     * @param url URL that finished processing
     */
    @Override
    public void finishProcess(String url) {
        try {
            IURL_Queue q = (IURL_Queue) Naming.lookup("rmi://" + URL_Queue.URL_QUEUE_IP + ":1098/Queue");
            q.finishedURLProcessing(url);

        } catch (MalformedURLException e) {
            System.out.println("Invalid URL format");
        } catch (NotBoundException e) {
            System.out.println("URL_Queue not reachable...");
        } catch (RemoteException e) {
            System.out.println("Error while finishing URL processing");
        } catch (Exception e) {
            System.out.println("Error in finishProcess()...");
        }
    }

    /**
     * Method to display a 10 result page to the client
     * @param name Client's name
     * @param index Page's index
     * @param result List of URLs to be displayed
     * @throws RemoteException If any communication error occurs during the remote call
     * */
    public void showPage(String name, int index, List<URLData> result) throws RemoteException {

        //System.out.println("Showing page: " + index);

        List<URLData> show = new ArrayList<>(result.subList(index * 10, Math.min((index * 10) + 10, result.size())));

        clients.get(name).setCurrentPageURLs(show);

        clients.get(name).print_on_client("\n-----------------------------------\n");

        for (URLData page : show) {
            clients.get(name).print_on_client(page.getUrl());
            clients.get(name).print_on_client(page.getTitle());
            StringBuilder b = new StringBuilder();

            for (int i = 0; i < Math.min(30, page.getContent().length); i++) {
                b.append(page.getContent()[i]);
                if (i < 29)
                    b.append(" ");
                if (i % 11 == 0 && i != 0)
                    b.append("\n");
            }
            clients.get(name).print_on_client(b + "...\n");
            //clients.get(name).print_on_client( "associate " + page.getNumAssociatedURLs() + "\n");
        }

        clients.get(name).print_on_client("-----------------------------------\n");

        clients.get(name).print_on_client("Showing page: " + (index + 1) + "/" + (getMaxIndex(result.size()) + 1));

        //System.out.println("Showing page: " + index);
    }

    /**
     * Method to search for a word
     * @param name Client's name
     * @param s Word to be searched
     * @param index Page's index
     * @param flag Original search
     * @return The maximum index of the server
     * @throws RemoteException If any communication error occurs during the remote call
     * */
    @Override
    public int searchWords(String name, String s, int index, boolean flag) throws RemoteException {
        System.out.println("Searching for: " + s);

        // call heackernews search
        HackerNewsController hackerNewsController = new HackerNewsController();
        List<HackerNewsItemRecord> hackerNewsResults = hackerNewsController.hackerNewsTopStories(s);

        /* Appearance: TODO formatar, realizar esta pesquisa sem urls nos barrels ou sem resultados oara a pesquisa nos barrels

    > https://github.com/quarylabs/quary
    > Show HN: Open-source BI and analytics for engineers
    > We are building Quary (), an engineer-first BI/analytics product. You can fi-
    nd our repo at and our website at . There’s a demo video here: As engineers
     who have worked on data at startups and Amazon, we were frustrated by sel...
 */
        clients.get(name).print_on_client("\n########### Hacker news results #########\n");

        ArrayList<String> hackerNewsUrls = new ArrayList<>();

        System.out.println("HackerNews Results: " + hackerNewsResults.size());
        for (HackerNewsItemRecord hackerNewsResult : hackerNewsResults) {
            clients.get(name).print_on_client(hackerNewsResult.url());
            hackerNewsUrls.add(hackerNewsResult.url());
            clients.get(name).print_on_client(hackerNewsResult.title());

            // Remove all links from the text (looked bad)
            String text = hackerNewsResult.text();
            Document doc = Jsoup.parse(text);
            Elements links = doc.select("a");

            for (Element link : links) {
                link.remove();
            }

            StringBuilder b = new StringBuilder();

            for (int i = 0; i < Math.min(225, doc.text().length()); i++) {
                b.append(doc.text().charAt(i));

                if (i % 75 == 0 && i != 0) {
                    if (doc.text().charAt(i+1) != ' ') {
                        b.append("-");
                    }
                    b.append("\n");
                }
            }
            clients.get(name).print_on_client(b + "...\n");

        }

        clients.get(name).print_on_client("#######################################");

        hackerNewsUrls.forEach(url -> { // TODO copilot -> didnt check
            try {
                addURLs(hackerNewsUrls);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        List<URLData> hackerNewsResultsURLData = new ArrayList<>();
        for (HackerNewsItemRecord hackerNewsResult : hackerNewsResults) {
            URLData l = new URLData(hackerNewsResult.url(), hackerNewsResult.title(), new String[]{hackerNewsResult.text()});
            hackerNewsResultsURLData.add(l);

        }


        //System.out.println("index: " + index);

        if (flag) {
            for (int i = 0; i < barrels.size(); i++) {
                if (barrelIsAlive.get(i)) {
                    IISBs barrel = barrels.get(i);

                    try {
                        barrel.saveSearch(s);

                    } catch (RemoteException e) {
                        System.out.println("Error while saving search");
                    }
                }
            }
        }

        //Separate the words in the string
        String[] words = s.split(" ");
        //ArrayList to store the words that start with a-m
        ArrayList<String> evens = new ArrayList<>();
        //ArrayList to store the words that start with n-z
        ArrayList<String> odds = new ArrayList<>();

        //Separate the words in the two lists
        for (String word : words) {
            if (word.isEmpty())
                continue;

            if (word.charAt(0) >= 'a' && word.charAt(0) <= 'm') {
                evens.add(word);
            } else if (word.charAt(0) >= 'n' && word.charAt(0) <= 'z') {
                odds.add(word);
            }
        }

        //If both arrays are empty, there is no word to search + hackerNews has no results
        if(evens.isEmpty() && odds.isEmpty() && hackerNewsUrls.isEmpty()){ // TODO check
            clients.get(name).print_on_client("No words to search");
            return -1;
        }

        //Convert the lists to arrays
        String[] even = new String[0];
        String[] odd = new String[0];

        //Variables to control if the word was sent to the barrel
        boolean sendEven = false;
        boolean sendOdd = false;

        //No words to search in this barrel, no need to send to it
        if (evens.isEmpty()) {
            sendEven = true;
        } else {
            even = evens.toArray(new String[0]);
        }
        if (odds.isEmpty()) {
            sendOdd = true;
        } else {
            odd = odds.toArray(new String[0]);
        }

        //Lists that will store the results of the search
        List<URLData> resultEven = new ArrayList<>();
        List<URLData> resultOdd = new ArrayList<>();

        //Iterate over the barrels to send the words to an available barrel
        for (IISBs barrel : barrels) {
            if (barrel.getType() == 0 && barrel.getId() != lastBarrelEven && !sendEven && barrelIsAlive.get(barrel.getId())) {
                lastBarrelEven = barrel.getId();
                // saves the time that the search required
                Instant start = Instant.now();
                resultEven = barrel.clientRequest(even);
                Instant end = Instant.now();
                double execTime = Duration.between(start, end).toMillis();
                barrel.saveSearchTime(execTime);
                sendEven = true;
            } else if (barrel.getType() == 1 && barrel.getId() != lastBarrelOdd && !sendOdd && barrelIsAlive.get(barrel.getId())) {
                lastBarrelOdd = barrel.getId();
                // saves the time that the search required
                Instant start = Instant.now();
                resultOdd = barrel.clientRequest(odd);
                Instant end = Instant.now();
                double execTime = Duration.between(start, end).toMillis();
                barrel.saveSearchTime(execTime);
                sendOdd = true;
            }
        }

        //If it wasn't possible to send the word to a different barrel, send it to the last barrel
        if (!sendEven) {
            if (barrelIsAlive.get(lastBarrelEven)) {
                Instant start = Instant.now();
                resultEven = barrels.get(lastBarrelEven).clientRequest(even);
                Instant end = Instant.now();
                double execTime = Duration.between(start, end).toMillis();
                barrels.get(lastBarrelEven).saveSearchTime(execTime);
                sendEven = true;
            }
        }

        if (!sendOdd) {
            if (barrelIsAlive.get(lastBarrelOdd)) {
                Instant start = Instant.now();
                resultOdd = barrels.get(lastBarrelOdd).clientRequest(odd);
                Instant end = Instant.now();
                double execTime = Duration.between(start, end).toMillis();
                barrels.get(lastBarrelOdd).saveSearchTime(execTime);
                sendOdd = true;
            }
        }

        //If it wasn't possible to get the result from any barrel, send an error message to the client
        if (!sendOdd || !sendEven) {
            clients.get(name).print_on_client("Error on server, please try again");
            return -1;
        }

        if ((resultEven == null || resultOdd == null) && hackerNewsResults.isEmpty()) { // TODO check
            clients.get(name).print_on_client("No results found!");
            return -1;
        }

        //List to store the final result
        result = hackerNewsResultsURLData;

        //If one of the lists is empty, the result is the other list
        if (even.length == 0) {
            if (result.isEmpty())
                result = resultOdd;
            else
                if (resultOdd != null)
                    result.addAll(resultOdd);
        } else if (odd.length == 0) {
            if (result.isEmpty())
                result = resultEven;
            else
                if (resultEven != null)
                    result.addAll(resultEven);
        } else { //If both lists have words, the result is the intersection of the two lists
            if (!hackerNewsResultsURLData.isEmpty()) {
                resultEven.addAll(hackerNewsResultsURLData);
                resultOdd.addAll(hackerNewsResultsURLData);
            }
            result = resultEven;

            for (URLData first : result) {
                for (URLData second : resultOdd) {
                    if (first.getUrl().equals(second.getUrl())) {
                        result.remove(second);
                    }
                }
            }
        }


        if((result == null || result.isEmpty()) && hackerNewsResults.isEmpty()){ // TODO check
            clients.get(name).print_on_client("No results found");
            return -1;
        }

        //Print the result on the client
        showPage(name, index, result);

        return getMaxIndex(result.size());
    }

    @Override
    public List<URLData> getResult() throws RemoteException {
        return result;
    }

    /**
     * Method to show the menu of associated URLs
     * @param name Client's name
     * @throws RemoteException If any communication error occurs during the remote call
     */
    @Override
    public void showMenuAssociated(String name) throws RemoteException {
        //System.out.println("Showing associated URLs");

        List<URLData> result = clients.get(name).getCurrentPageURLs();
        //System.out.println(result.size());

        int index = 0;
        for (URLData page : result) {
            clients.get(name).print_on_client(index++ + " - " + page.getUrl());
        }

        clients.get(name).print_on_client("Select an URL to show associated URLs");

        //System.out.println("Showing URLs");
    }

    /**
     * Method to show the associated URLs of a given URL
     * @param name Client's name
     * @param url URL to show the associated URLs
     * @return 1 if the operation was successful, 0 otherwise
     * @throws RemoteException If any communication error occurs during the remote call
     */
    @Override
    public int showAssociatedURLs (String name, String url) throws RemoteException {
        //System.out.println("Showing associated URLs");

        ArrayList<String> result = null;

        for (int i = 0; i < barrelIsAlive.size(); i++) {
            if (barrelIsAlive.get(i)) {
                //System.out.println("Barrel " + i + " is alive and sending request " + url);
                result = barrels.get(i).getAssociatedURLs(url);
                break;
            }
        }

        if (result == null) {
            clients.get(name).print_on_client("Option not available, please try again later");
            return -1;
        }

        for (String page : result) {
            clients.get(name).print_on_client(page);
        }

        //System.out.println("Showing associated URLs");

        return 0;
    }

    /**
     * Method to delete a client from the server
     * @param name Client's name
     */
    @Override
    public void deleteClient(String name) {
        clients.remove(name);
        if (name == null)
            name = "<unnamed>";
        System.out.println("> Client " + name + " deleted");
    }

    /**
     * Method to request the saved URLs from any alive barrel
     * @return List of saved URLs
     */
    @Override
    public ArrayList<String> requestSavedURLs() {
        for(int i = 0; i < barrels.size(); i++){
            if (barrelIsAlive.get(i)){
                IISBs barrel = barrels.get(i);
                try {
                    return barrel.getSavedURLs();
                } catch (RemoteException e) {
                    System.out.println("Error while getting saved URLs");
                } catch (Exception e) {
                    System.out.println("Error in requestSavedURLs()...");
                }
            }
        }
        return null;
    }

    /**
     * Method to update the barrels with an already registered barrel
     * @param start Start index to search for a barrel
     * @param index Index of the barrel that is being registered
     * @throws RemoteException If any communication error occurs during the remote call
     */
    private void updateBarrels(int start, int index) throws RemoteException {
        for (int i = start; i < barrelIsAlive.size(); i += 2) {
            if (barrelIsAlive.get(i) && i != index) {
                //System.out.println("Updating barrel");
                barrels.get(index).setBarrelData(barrels.get(i).getBarrelData());
                System.out.println("Barrel updated");
                return;
            }
        }

        if (start == 0) {
            start = 1;
        } else {
            start = 0;
        }

        for (int i = start; i < barrelIsAlive.size(); i += 2) {
            if (barrelIsAlive.get(i)) {
                //System.out.println("Updating barrel");
                BarrelData bd = barrels.get(i).getBarrelData();

                barrels.get(index).setSavedURLs(bd.getSavedURLs());
                barrels.get(index).setAssociatedURLs(bd.getAssociatedURLs());
                barrels.get(index).setNumSearches(bd.getNumSearches());

                barrels.get(index).setBarrelData(barrels.get(i).getBarrelData());
                System.out.println("Barrel updated by different type");
                return;
            }
        }
        //System.out.println("Barrel is unique!");
    }

    /**
     * Method to register a barrel to the server
     * @param barrel Barrel to be registered
     * @return 1 if the registration was successful, 0 otherwise
     * @throws RemoteException If any communication error occurs during the remote call
     */
    @Override
    public void barrelRegistration(IISBs barrel) throws RemoteException {
        boolean created = false;

        for (int i = 0; i < barrelIsAlive.size(); i++) {
            if (!barrelIsAlive.get(i)) {
                barrel.setType(i);
                //indice par: A-M, indice ímpar: N-Z
                barrel.setType(i % 2);
                barrels.set(i, barrel);
                barrelIsAlive.set(i, true);
                System.out.println("Barrel " + barrel.getId() + " registered");
                created = true;
                break;
            }
        }

        if (!created) {
            barrel.setId(barrels.size());
            //indice par: A-M, indice ímpar: N-Z
            barrel.setType((barrels.size()) % 2);
            barrels.add(barrel);
            barrelIsAlive.add(true);
            System.out.println("Barrel " + barrels.size() + " registered");
        }

        updateBarrels(barrel.getType(), barrel.getId());
    }

    public void printOnServer(String s) throws RemoteException {
        System.out.println(s);
    }



    /**
     * Main method the program server, finds the current IP of the machine, creates a new server object and binds it to the RMI registry while
     * constantly checking if the barrels are still alive in a parallel thread
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        System.getProperties().put("java.security.policy", "policy.all");
        System.setProperty("java.rmi.server.hostname", GATEWAY_IP);

        if (args.length > 0) {
            GATEWAY_IP = args[0];
        }

        final String ANSI_GREEN = "\u001B[32m";
        final String ANSI_RED = "\u001B[31m";
        final String ANSI_RESET = "\u001B[0m";

        try {
            Gateway h = new Gateway();
            LocateRegistry.createRegistry(1099);
            Naming.rebind("XPTO", h);
            System.out.println(ANSI_GREEN + "Hello Server ready..." + ANSI_RESET);

            Thread exitRequest = new Thread(() -> {
                Scanner sc = new Scanner(System.in);
                String exitString = sc.next();

                if (exitString.equalsIgnoreCase("exit")) {
                    System.exit(0);
                }
            });
            exitRequest.start();

            while (true) {
                h.isAlive();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

        } catch (Exception e) {
            System.out.println(ANSI_RED + "Hello Server failed: " + e + ANSI_RESET);
        }
    }
}
