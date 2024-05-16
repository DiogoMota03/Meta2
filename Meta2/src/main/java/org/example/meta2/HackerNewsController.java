package org.example.meta2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.ArrayList;

// Search: http://localhost:8080/topstories?search=word1%20word2%20word3
//    %20 -> space
// List all: http://localhost:8080/topstories


@RestController
public class HackerNewsController {
    private static final Logger logger = LoggerFactory.getLogger(HackerNewsController.class);


    //@GetMapping("/topstories")
    //@ResponseBody
    public List<HackerNewsItemRecord> hackerNewsTopStories(String searchWord) {
        // TODO: Get IDs of top stories

        List<HackerNewsItemRecord> resultado = new ArrayList<>();

        String topStoriesEndpoint = "https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty";

        RestTemplate restTemplate = new RestTemplate();
        List hackerNewsNewTopStories = restTemplate.getForObject(topStoriesEndpoint, List.class);

        String[] searchWordsArray = searchWord.split(" "); // divide a string de pesquisa em várias palavras

        int counter = 0;
        for (Object storyId :
                hackerNewsNewTopStories) {
            String storyURL = "https://hacker-news.firebaseio.com/v0/item/" + storyId + ".json?print=pretty";

            HackerNewsItemRecord oneStory = restTemplate.getForObject(storyURL, HackerNewsItemRecord.class);

            if(!searchWord.isEmpty()) {
                if (oneStory != null && oneStory.text() != null) {
                    if (Arrays.stream(searchWordsArray).allMatch(word -> oneStory.text().toLowerCase().contains(word.toLowerCase()))/*oneStory.text().toLowerCase().contains(searchWord.toLowerCase())*/) {
                        resultado.add(oneStory);
                    }
                }
            } else {
                resultado.add(oneStory);
            }

            counter+=1;
            if(counter>40)
                break;
        }

        return resultado;
    }
}