package com.nwhite.advisors.advisor;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoggingController {
    private final ChatClient chatClient;

    public LoggingController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/advisor")
    public String advisor() {
        return chatClient.prompt()
                .advisors(SimpleLoggingAdvisor.builder().build())
                .user("How long would it take for me to drive from Moscow to St.-Petersburg (Russia)?")
                .call()
                .content();
    }
}
