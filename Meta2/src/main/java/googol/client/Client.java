package googol.client;

import java.net.UnknownHostException;
import java.rmi.*;
import java.rmi.ConnectException;
import java.rmi.server.*;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Scanner;
import java.net.*;

import googol.gateway.Gateway;
import googol.gateway.IGateway;
import googol.queue.URLData;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;

import javax.net.ssl.SSLHandshakeException;

/**
 * Represents the implementation of the {@link IClient} interface.
 */
public class Client extends UnicastRemoteObject implements IClient {
    /**
     * Name of the registry
     */
    private static String name;

    /**
     * Stores the last result of search
     */
    private static List<URLData> currentPageURLs;

    /**
     * Primary constructor for this class
     */
    public Client() throws RemoteException {
        super();
    }

    /**
     * Setter for the result of the search
     * @param currentPageURLs List of URLs to be set
     */
    @Override
    public void setCurrentPageURLs(List<URLData> currentPageURLs) throws RemoteException {
        Client.currentPageURLs = currentPageURLs;
    }

    /**
     * Getter for the result of the search
     * @return currentPageURLs List of URLs to be returned
     */
    @Override
    public List<URLData> getCurrentPageURLs() throws RemoteException {
        return currentPageURLs;
    }


    /**
     * Method to show a message in the requested client's console
     * @param s Message to be shown
     */
    @Override
    public void print_on_client(String s) throws RemoteException {
        System.out.println("> " + s);
    }


    /**
     * Main method of the Client: Attempts to connect and subscribe to the Gateway, allowing
     * the user to insert URLs, search for words in the indexed URLs, check the status of the
     * server and exit the program. The Client uses the Gateway's methods to perform his operations
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        System.getProperties().put("java.security.policy", "policy.all");
        System.setProperty("java.rmi.server.hostname", Gateway.GATEWAY_IP);
        String a;

        final String ANSI_RED = "\u001B[31m";
        final String ANSI_RESET = "\u001B[0m";

        // usage: java HelloClient username
		/*
		System.getProperties().put("java.security.policy", "policy.all");
		System.setSecurityManager(new RMISecurityManager());
		*/
        int success = 0, printed = 0;

        while (success == 0){
            try {
                Scanner sc = new Scanner(System.in);
                IGateway g = (IGateway) Naming.lookup("rmi://" + Gateway.GATEWAY_IP + ":1099/XPTO");
                // Catch a program "crash" (shutdown)
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        g.deleteClient(name);

                    } catch (RemoteException e) {
                        System.out.println("Error deleting client: " + e);
                        System.exit(0);
                    } catch (Exception e) {
                        System.out.println("Error deleting client... :" + e);
                        System.exit(0);
                    }
                }));
                Client c = new Client();

                System.out.print("Enter your username: \n> ");
                name = sc.nextLine();
                int sub = g.subscribe(name, c);
                System.out.println("Client sent subscription to server");

                while (sub == 0) {
                    System.out.print("Enter your username: \n> ");
                    name = sc.nextLine();
                    sub = g.subscribe(name, c);
                    System.out.println("Client sent subscription to server");
                }

                success = 1;
                printed = 0;

                while (true) {
                    System.out.println("\nInsert command:");
                    System.out.print("> ");
                    a = sc.next();

                    switch (a) {
                        case "insert" -> {
                            a = sc.nextLine();
                            if (a.isEmpty()) {
                                System.out.print("Invalid command: insert <URL>\n> ");
                                break;
                            }

                            String[] temp = a.split(" ");

                            if(temp.length != 2) {
                                System.out.println("Too many arguments: insert <URL>\n> ");
                                break;
                            }

                            a = temp[1];

                            int retryInsert = 1;
                            while(retryInsert == 1) {
                                try {
                                    Jsoup.connect(a).get();

                                    g.insertURL(name, a);
                                    retryInsert = 0;
                                } catch (UnknownHostException e) {
                                    System.out.println("Page not found!");
                                    break;
                                } catch (
                                        SSLHandshakeException e) { // uma das páginas encontradas era unreachable, verifiquei e era mesmo erro da página e nao no código
                                    System.out.println("ERROR: URL unreachable, invalid SSL certificate");
                                    break;
                                } catch (IllegalArgumentException e) {
                                    System.out.println("Invalid URL, page not found!");
                                    break;
                                } catch (HttpStatusException e) {
                                    System.out.println("Error fetching URL!");
                                    break;
                                } catch (SocketTimeoutException e) {
                                    System.out.println("Bad Gateway: Page currently not reachable!");
                                    break;
                                } catch (ConcurrentModificationException e) {
                                    retryInsert = 0;
                                } catch (Exception e) {
                                    System.out.println("Error inserting URL: " + e);
                                    break;
                                }
                            }
                        }
                        case "search" -> {
                            a = sc.nextLine();

                            if (a.isEmpty()) {
                                System.out.println("Invalid command: search <word(s)>\n>");
                                break;
                            }

                            int index = 0;

                            String search = a;

                            //search for the word(s) in the URL, returns -1 if the word(s) is not found, otherwise returns the max index
                            int maxIndex = g.searchWords(name, search, index, true);

                            //System.out.println("Max Index " + maxIndex);

                            if (maxIndex == -1) {
                                break;
                            }

                            while (true) {
                                if (index == 0) {
                                    if (maxIndex == 0) {
                                        System.out.println("|Exit| |Connections|");
                                    } else {
                                        System.out.println("|Exit| |Next| |Connections|");
                                    }
                                } else if (index == maxIndex) {
                                    System.out.println("|Previous| |Exit| |Connections|");
                                } else {
                                    System.out.println("|Previous| |Exit| |Next| |Connections|");
                                }

                                System.out.print("> ");
                                a = sc.nextLine();

                                //System.out.println("------------------------\n" + a + "\n-------------------------------");

                                if (a.equalsIgnoreCase("exit")) {
                                    currentPageURLs.clear();
                                    break;
                                } else if (a.equalsIgnoreCase("next") && index != maxIndex) {
                                    maxIndex = g.searchWords(name, search, ++index, false);
                                } else if (a.equalsIgnoreCase("previous") && index != 0) {
                                    maxIndex = g.searchWords(name, search, --index, false);
                                } else if (a.equalsIgnoreCase("connections")) {
                                    g.showMenuAssociated(name);

                                    while (true) {
                                        System.out.print("> ");
                                        a = sc.nextLine();

                                        if (a.equalsIgnoreCase("exit")) {
                                            maxIndex = g.searchWords(name, search, index, false);
                                            break;
                                        } else {
                                            try {
                                                int i = Integer.parseInt(a);
                                                //System.out.println(a);

                                                if (i >= 0 && i < currentPageURLs.size()) {
                                                    //System.out.println("here");

                                                    int temp = g.showAssociatedURLs(name, currentPageURLs.get(i).getUrl());

                                                    if (temp != -1) {
                                                        System.out.println("Press enter to continue...");
                                                        sc.nextLine();
                                                    }

                                                    maxIndex = g.searchWords(name, search, index, false);
                                                    break;
                                                } else {
                                                    System.out.println("Number out of bounds. Try again!");
                                                }
                                            } catch (NumberFormatException e) {
                                                System.out.println("Invalid number. Try again!");
                                            } catch (Exception e) {
                                                System.out.println("Error: " + e);
                                            }
                                        }
                                    }
                                } else {
                                    System.out.println("Invalid option. Try again!");
                                }
                            }
                        }
                        case "status" ->
                            g.getStatus(name);
                        case "exit" -> {
                            // Vai ser apanhado pelo shutdown hook
                            System.exit(0);
                        }
                        default -> System.out.println("Invalid command. Try again!");
                    }
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
            } catch (Exception e) { // TODO-> triggers a null pointer exception if 0 results are returned in search
                System.out.println(ANSI_RED + "Exception in client main: " + e + ANSI_RESET);
            }
        }
    }
}
