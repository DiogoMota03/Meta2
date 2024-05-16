package org.example.meta2;

import googol.client.Client;
import googol.client.IClient;
import googol.gateway.IGateway;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.HtmlUtils;

import java.rmi.Naming;
import java.rmi.RemoteException;


@Controller
public class MessagingController {
    private IGateway h;
    private Client c;

    @GetMapping("/")
    public String redirect() {
        try {
            h = (IGateway) Naming.lookup("XPTO");
            c = new Client();
            h.subscribe("jonas", (IClient) c);
        }catch (Exception ex){
            System.out.println("Error: " + ex.getMessage());
        }

        return "index.html";
    }

    @MessageMapping("/message")
    @SendTo("/topic/messages")
    public Message onMessage(Message message) {
        System.out.println("Message received " + message);
        try {
            Thread.sleep(1000); // simulated delay
        } catch (InterruptedException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return new Message(HtmlUtils.htmlEscape(message.content().toUpperCase()));
    }

    @GetMapping("/search")
    public String search(@RequestParam("s") String message) {
        System.out.println("Search text: " + message);
        try {
            h.searchWords("jonas", message, 0, true);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        return "search";
    }

    @GetMapping("/lucky")
    public String luckySearch() {
        System.out.println("Lucky Search Requested");
        return "lucky";
    }
}

