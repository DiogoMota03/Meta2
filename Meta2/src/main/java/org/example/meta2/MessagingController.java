package org.example.meta2;

import googol.client.Client;
import googol.client.IClient;
import googol.gateway.Gateway;
import googol.gateway.IGateway;
import org.springframework.beans.factory.annotation.Autowired;
import googol.queue.URLData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.HtmlUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.swing.*;
import javax.xml.transform.Source;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@Controller
public class MessagingController {
    /**
     * Template to render the view
     */
    private IGateway h;
    /**
     * Client to receive the messages
     */
    private Client c;
    /**
     * Name of the client
     */
    private String name;

    /**
     * Thymeleaf view resolver to render the templates
     */
    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;

    @PostMapping("/showAssociatedURLs")
    public ResponseEntity<List<String>> showConnections(@RequestParam String url) {
        List<String> associatedUrls = null; // Substitua 'service' pelo nome do seu serviço
        try {
            associatedUrls = h.showAssociatedURLsResults(name, url);
        } catch (RemoteException e) {
            System.out.println("Error showing associated urls: " + e.getMessage());
        } catch (NullPointerException e) {
            System.out.println("Error: Gateway isn't connected -> " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error in show associated urls: " + e.getMessage());
        }
        return ResponseEntity.ok(associatedUrls);
    }

    /**
     * Redirects to the index page
     * @return index page
     */
    @GetMapping("/")
    public String redirect() {
        try {
            h = (IGateway) Naming.lookup("XPTO");
            c = new Client();
            if(name == null)
                name = h.requestId();
            h.subscribe(name, (IClient) c);

        }catch (Exception ex){
            System.out.println("Error: " + ex.getMessage());
        }

        return "index";
    }

/*
    @GetMapping("/search")
    public String search(@RequestParam("s") String message) {
        System.out.println("Search text: " + message);
        try {
            int r = h.searchWords(name, message, 0, true);
            if (r == -1) {
                //TODO: decidir como fazer quando não encontrar nada
            } else{
                //TODO: imprimir resultados na página
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        return "search";
    }

    @GetMapping("/insert")
    public String insertURL(@RequestParam("s") String message) {
        System.out.println("Insert link: " + message);

        try {
            h.insertURL("jonas", message);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        return "status";
    }

    @GetMapping("/status")
    public String statusPage() {
        System.out.println("Lucky Search Requested");
        return "status";
    }*/

    /* todo
    @PostMapping("/search")
    public String searchPage(@RequestParam String text) {
        System.out.println("Search text: " + text);
        try {
            int r = h.searchWords(name, text, 0, true);
            if (r == -1) {
                //TODO: decidir como fazer quando não encontrar nada
            } else{
                List<URLData> results = h.getResult();
                for (URLData result : results) {
                    System.out.println("------> " + result.getUrl() + result.getTitle() + Arrays.toString(result.getContent()));
                }
            }
        } catch (RemoteException e) {
            System.out.println("Error searching: " + e.getMessage());
        } catch (NullPointerException e) {
            System.out.println("Error: Gateway isn't connected -> " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error in search: " + e.getMessage());
        }
        return "search";
    } */

    /**
     * Endpoint called to search for a word
     * @param text word to search
     * @param page page to show - changed with next/orevious buttons
     * @return ResponseEntity with the search page
     */
    @PostMapping("/search")
    public ResponseEntity<String> searchPage(@RequestParam String text, @RequestParam(defaultValue = "0") int page) {
        System.out.println("Search text: " + text);
        try {
            int r = h.searchWords(name, text, 0, true);
            if (r == -1) {

                List<URLData> results = new ArrayList<>();

                // Add the wikipedia top result
                WikipediaSearch ws = new WikipediaSearch();
                URLData wikiResult = ws.topWikiSearch(text);
                if (wikiResult != null)
                    results.add(wikiResult);
                else
                    results = Collections.emptyList();

                Context context = new Context();
                context.setVariable("results", results);
                String htmlContent = thymeleafViewResolver.getTemplateEngine().process("search", context);
                return ResponseEntity.ok(htmlContent);
            } else{
                List<URLData> results = h.getResult(text);
                Context context = new Context();
                context.setVariable("results", results.subList(page * 10, Math.min((page + 1) * 10, results.size())));
                String htmlContent = thymeleafViewResolver.getTemplateEngine().process("search", context);
                return ResponseEntity.ok(htmlContent);
            }
        } catch (Exception e) {
            // handle exception-
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred: " + e.getMessage());
        }
    }


    /**
     * Endpoint called to insert an URL
     * @param text URL to insert
     * @return ResponseEntity with the URL inserted
     */
    @PostMapping("/")
    public ResponseEntity<String> insertURL(@RequestParam String text) {
        System.out.println("Insert text: " + text);
        try {
            h.insertURL("jonas", text);
        } catch (RemoteException e) {
            System.out.println("Error inserting url: " + e.getMessage());
        } catch (NullPointerException e) {
            System.out.println("Error: Gateway isn't connected -> " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error in insert: " + e.getMessage());
        }
        return ResponseEntity.ok(text);
    }

    /**
     * Endpoint called to show the status page
     * @return status page
     */
    @PostMapping("/status")
    public String status(Model model) {
        // Call the getStatus() method
        getStatus();

        // Return the name of the view
        return "status";
    }

    /**
     * Method to get the status of the gateway
     */
    private void getStatus() {
        // Your implementation here
        try {
            h.getStatus(name);
        } catch (RemoteException e) {
            System.out.println("Error getting status " + e.getMessage());
        } catch (NullPointerException e) {
            System.out.println("Error: Gateway isn't connected -> " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error in status: " + e.getMessage());
        }
    }


}
