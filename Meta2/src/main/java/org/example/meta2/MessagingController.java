package org.example.meta2;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.util.HtmlUtils;

@Controller
public class MessagingController {
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
    public String search(@Payload String message) {
        System.out.println("Search text: " + message);
        return "search.html";
    }

    @GetMapping("/lucky")
    public String luckySearch() {
        System.out.println("Lucky Search Requested");
        return "lucky.html";
    }
}

