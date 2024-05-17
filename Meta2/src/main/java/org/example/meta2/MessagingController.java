package org.example.meta2;

import googol.client.Client;
import googol.client.IClient;
import googol.gateway.Gateway;
import googol.gateway.IGateway;
import org.springframework.beans.factory.annotation.Autowired;
import googol.queue.URLData;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.HtmlUtils;

import javax.swing.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;


@Controller
public class MessagingController {
    private IGateway h;
    private Client c;
    private String name;

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

    @MessageMapping("/message")
    @SendTo("/topic/messages")
    public Message onMessage(String message) {
        System.out.println("Message received " + message);
        try {
            Thread.sleep(1000); // simulated delay
        } catch (InterruptedException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return new Message(HtmlUtils.htmlEscape(message));
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

    @PostMapping("/search")
    public ModelAndView searchPage(@RequestParam String text) {
        System.out.println("Search text: " + text);
        ModelAndView mav = new ModelAndView("search");
        try {
            int r = h.searchWords(name, text, 0, true);
            if (r == -1) {
                //TODO: decidir como fazer quando não encontrar nada
            } else{
                List<URLData> results = h.getResult();
                mav.addObject("results", results);
            }
        } catch (Exception e) {
            // handle exception
        }
        return mav;
    }

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

    @PostMapping("/status")
    public String statusPage() {
        System.out.println("Received text: ");
        return "status";
    }
}
