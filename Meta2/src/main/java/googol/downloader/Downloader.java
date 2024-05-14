package googol.downloader;

import googol.gateway.Gateway;
import googol.gateway.IGateway;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.net.ssl.SSLHandshakeException;
import java.net.*;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.ConnectException;
import java.util.*;

/**
 * Represents a web page downloader that retrieves content from a given URL and sends this information via multicast
 */
public class Downloader implements IDownloader {
    /**
     * Name of the multicast group
     */
    private final String multicastAdd = "224.3.2.1";

    /**
     * Port of the multicast group
     */
    private final int port = 4321;

    /**
     * Flag to indicate the availability of the downloader
     */
    private boolean isFree = true;

    /**
     * List of new URLs to index
     */
    private static ArrayList<String> newURLs = new ArrayList<>();

    /**
     * Retrieves data from a given URL and extracts links in it
     * @param url The URL to retrieve information
     * @return URLData object containing data from the URL
     */
    public String crawl(String url) {
        StringBuilder data = null;

        try {
            Document doc = Jsoup.connect(url).get();

            data = new StringBuilder();

            String title = doc.title();
            if (title.contains("|")) {
                title = title.replace("|", "");
            }

            data.append(url).append("|").append(title).append("|");

            StringBuilder content = new StringBuilder();
            StringTokenizer tokens = new StringTokenizer(doc.text());
            int countTokens = 0;
            while (tokens.hasMoreElements() && countTokens < 100) {
                content.append(tokens.nextToken().toLowerCase());
                if (countTokens < 99) {
                    content.append(" ");
                }
                countTokens++;
            }

            String contentStr = content.toString();
            if (contentStr.contains("|")) {
                contentStr = contentStr.replace("|", "");
            }

            data.append(contentStr).append("|");

            // extract all the links that are present in the current page
            Elements links = doc.select("a[href]");

            for (Element link : links) {
                // add newUrls in an ArrayList -> add in queue
                newURLs.add(link.attr("abs:href"));

                if (data.length() + link.attr("abs:href").length() > 65000) {
                    break;
                }
                data.append(link.attr("abs:href")).append(" ");
            }

            //System.out.println(newURLs);

        } catch (UnknownHostException e) {
            System.out.println("ERROR: Page not found: " + url);
        } catch (IllegalArgumentException e) {
            System.out.println("ERROR: Invalid URL, page not found: " + url);
        } catch (SSLHandshakeException e) { // uma das páginas encontradas era unreachable, verifiquei e era mesmo erro da página e nao no código
            System.out.println("ERROR: URL unreachable, invalid SSL certificate: " + url);
        } catch (UnsupportedMimeTypeException e) {
            System.out.println("ERROR: Unsupported MIME type, not a URL (downloadable content): " + url);
        } catch (HttpStatusException e) {
            System.out.println("ERROR: Fetching URL: " + url);
        } catch (SocketTimeoutException e) {
            System.out.println("Bad Gateway: Page currently not reachable: " + url);
        } catch (MalformedURLException e) {
            System.out.println("Invalid URL was presented: " + url);
        } catch (Exception e) {
            System.out.println("ERROR: " + e);
        }

        if (data != null)
            return data.toString();

        return null;
    }

    /**
     * Processes and sends URL's data using a multicast socket
     * @param url The URL to process
     */
    @Override
    public void run(String url) {
        MulticastSocket socket = null;
        //System.out.println("Downloader " + downloaderID + " running...");

        try {
            socket = new MulticastSocket();

            String message = crawl(url);
            //System.out.println(message);
            if (message != null) {
                byte[] buffer = message.getBytes();

                InetAddress group = InetAddress.getByName(multicastAdd);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
                socket.send(packet);

                byte[] successBuf = new byte[1024];
                DatagramPacket successPacket = new DatagramPacket(successBuf, successBuf.length);
                socket.receive(successPacket);
                //String successMessage = new String(successPacket.getData(), 0, successPacket.getLength());
                //System.out.println(successMessage);
            }

        } catch (SocketException e) { // Fixed
            System.out.println("Message was to long to be sent...");
        } catch (Exception e) {
            System.out.println("Multicast failed: " + e);
        } finally {
            if (socket != null)
                socket.close();
        }
    }

    /**
     * Main Method of the Downloader: Creates a new Downloader Object, establishes a new RMI connection
     * with the Gateway, requests the top from the Queue, through the Gateway, for processing and sends the new URLs found
     * @param args Command-line arguments
     * @throws InterruptedException If the sleep fails
     */
    public static void main(String[] args) throws InterruptedException {
        System.getProperties().put("java.security.policy", "policy.all");
        System.setProperty("java.rmi.server.hostname", Gateway.GATEWAY_IP);

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

        int printed = 0;
        while (true) {
            String url = null;

            try {
                //IURL_Queue q = (IURL_Queue) Naming.lookup("Queue");
                IGateway g = (IGateway) Naming.lookup("rmi://" + Gateway.GATEWAY_IP + ":1099/XPTO");

                Downloader downloader = new Downloader();

                int success; // Retry for sending more urls
                printed = 0;
                //ArrayList<String> newURLs = new ArrayList<>();

                System.out.println(ANSI_GREEN + "Downloader is running..." + ANSI_RESET);

                while(true) {
                    success = 0;
                    if (downloader.isFree) {
                        //String url = "https://www.google.com";
                        url = null;

                        while (url == null) {
                            try {
                                url = g.requestNext();
                            } catch (ConnectException | RuntimeException e) {
                                Thread.sleep(0);
                            }

                            // Se nao existir um URL, espera 1 segundo antes de perguntar de novo
                            if (url == null) Thread.sleep(1000);
                        }
                        System.out.println("downloader recebeu: " + url);

                        downloader.isFree = false;

                        downloader.run(url);
                        //System.out.println(newURLs);

                        while (success == 0) {
                            try {
                                // TODO: se ficar lento, remover
                                //newURLs.removeAll(savedURLs);
                                g.addURLs(newURLs);
                                g.finishProcess(url);
                                //g.showURLs();

                                success = 1;
                            } catch (ConcurrentModificationException e) {
                                //System.out.println(ANSI_YELLOW + "Retrying..." + ANSI_RESET);
                                //Thread.sleep(500);
                            } catch (RemoteException e) {
                                if(printed == 0)
                                    System.out.println(ANSI_RED + "Gateway not reachable..." + ANSI_RESET);
                                printed = 1;
                                Thread.sleep(3000);
                                g = (IGateway) Naming.lookup("rmi://" + Gateway.GATEWAY_IP + ":1099/XPTO");
                            } catch (Exception e) {
                                System.out.println(ANSI_RED + "Exception occurred..." + ANSI_RESET);
                                Thread.sleep(1000);
                            }
                        }
                        downloader.isFree = true;
                    }
                }

            } catch (MalformedURLException e) {
                System.out.println(ANSI_RED + "Invalid URL was presented: " + url + ANSI_RESET);
                //throw new RuntimeException(e);
            } catch (NotBoundException | ConnectException e) {
                if (printed == 0)
                    System.out.println(ANSI_RED + "Gateway not reachable..." + ANSI_RESET);
                printed = 1;
                Thread.sleep(5000);
            } catch (RemoteException e) {
                System.out.println(ANSI_RED + "Remote exception occurred..." + ANSI_RESET);
                Thread.sleep(1000);
                //throw new RuntimeException(e);
            } catch (InterruptedException e) {
                System.out.println("Sleep failed...");
                //throw new RuntimeException(e);
            } catch (Exception e) {
                System.out.println("Error: " + e);
                //throw new RuntimeException(e);
            }
        }
    }
}