package com.example.FlashCall.controller;

import com.example.FlashCall.response.ResponseHandler;
import com.example.FlashCall.utils.DataExtractor;
import com.example.FlashCall.utils.DataManager;
import com.example.FlashCall.utils.InMemoryDataManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static java.time.temporal.ChronoUnit.SECONDS;

@RestController
@RequestMapping("/flashCall")
public class FlashCallController {
    @Autowired
    private final DataManager dataManager;

    private final ObjectMapper mapper = new ObjectMapper();

    public FlashCallController(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @PostMapping("/sendCall")
    public ResponseEntity<Object> sendCall(@RequestBody String requestBody) throws URISyntaxException, IOException, InterruptedException {
        JsonNode jsonNode = mapper.readTree(requestBody);

        String phone = jsonNode.get("phone").asText("");
        String phoneRegEx = "^7[0-9]{10}$";
        if (!phone.matches(phoneRegEx)) {
            return ResponseHandler.generateResponse("Некорректный формат номера телефона! Пожалуйста, используйте формат 71231234567", HttpStatus.BAD_REQUEST, null);
        }

        HttpRequest request = HttpRequest.newBuilder(new URI("https://restapi.plusofon.ru/api/v1/flash-call/send"))
                .timeout(Duration.of(10, SECONDS))
                .POST(HttpRequest.BodyPublishers.ofString(String.valueOf(jsonNode)))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Client", "10553")
                .header("Authorization", "Bearer " + dataManager.getAccessToken())
                .build();


        HttpResponse<String> response = HttpClient
                .newBuilder()
                .proxy(ProxySelector.getDefault())
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());
        //todo remove
        System.out.println("response.body() = " + response.body());

        if (response.statusCode() == 200) {
            dataManager.addKeyData(phone, DataExtractor.extractKey(response.body()));
            dataManager.addCreationMomentData(phone, System.currentTimeMillis());
            return ResponseHandler.generateResponse("Выполнен вызов на номер: " + phone, HttpStatus.valueOf(response.statusCode()), null);
        }

        return ResponseHandler.generateResponse("Ошибка при запросе звонка!", HttpStatus.valueOf(response.statusCode()), DataExtractor.extractPlusofonErrorMessage(response.body()));

    }

    @PostMapping("/check")
    public ResponseEntity<Object> check(@RequestBody String requestBody) throws URISyntaxException, IOException, InterruptedException {
        ObjectNode objectNode = mapper.readValue(requestBody, ObjectNode.class);
        String phone = objectNode.get("phone").asText();
        String relatedKey = dataManager.getKeyByPhone(phone);
        objectNode.remove("phone");
        objectNode.put("key", relatedKey);

        HttpRequest request = HttpRequest.newBuilder(new URI("https://restapi.plusofon.ru/api/v1/flash-call/check"))
                .timeout(Duration.of(10, SECONDS))
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(objectNode)))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Client", "10553")
                .header("Authorization", "Bearer " + dataManager.getAccessToken())
                .build();


        HttpResponse<String> response = HttpClient
                .newBuilder()
                .proxy(ProxySelector.getDefault())
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());

        //todo remove
        System.out.println("response.body() = " + response.body());

        if (response.statusCode() == 200) {
            dataManager.cleanData(phone);
            return ResponseHandler.generateResponse("Проверка пин-кода прошла успешно!", HttpStatus.valueOf(response.statusCode()), null);
        }

        return ResponseHandler.generateResponse("Ошибка при проверке пин-кода!", HttpStatus.valueOf(response.statusCode()), DataExtractor.extractPlusofonErrorMessage(response.body()));

    }

}
