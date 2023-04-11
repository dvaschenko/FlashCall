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
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/FCAdmin")
public class FlashCallAdminController {

    @Autowired
    private final DataManager dataManager;

    private final ObjectMapper mapper = new ObjectMapper();

    public FlashCallAdminController(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    //TODO Override exception handling as you need

    @PutMapping("/addFCAccount")
    public ResponseEntity<Object> addFlashCallAccount(@RequestBody String requestBody) throws URISyntaxException, IOException, InterruptedException {
        JsonNode jsonNode = mapper.readTree(requestBody);

        HttpRequest request = HttpRequest.newBuilder(new URI("https://restapi.plusofon.ru/api/v1/flash-call"))
                .timeout(Duration.of(5, SECONDS))
                .POST(HttpRequest.BodyPublishers.ofString(String.valueOf(jsonNode)))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Client", "10553")
                .header("Authorization", "Bearer " + dataManager.getPlusofonAPIToken())
                .build();


        HttpResponse<String> response = HttpClient
                .newBuilder()
                .proxy(ProxySelector.getDefault())
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());
        //todo remove
        System.out.println("response.body() = " + response.body());

        if (response.statusCode() == 200) {
            return ResponseHandler.generateResponse("Новый аккаунт с ID " + DataExtractor.extractCreatedAccountId(response.body()) + " успешно добавлен", HttpStatus.valueOf(response.statusCode()), null);
        }

        return ResponseHandler.generateResponse("Ошибка при создании аккаунта", HttpStatus.valueOf(response.statusCode()), DataExtractor.extractPlusofonErrorMessage(response.body()));

    }

    @GetMapping("/listFCAccounts")
    public ResponseEntity<Object> listFlashCallAccounts() throws URISyntaxException, IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder(new URI("https://restapi.plusofon.ru/api/v1/flash-call"))
                .timeout(Duration.of(5, SECONDS))
                .GET()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Client", "10553")
                .header("Authorization", "Bearer " + dataManager.getPlusofonAPIToken())
                .build();


        HttpResponse<String> response = HttpClient
                .newBuilder()
                .proxy(ProxySelector.getDefault())
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            if (dataManager.getMainAccountID() == 0) {
                dataManager.setMainAccountID(DataExtractor.extractMainAccountId(response.body()));
            }
            return ResponseHandler.generateResponse("Список существующих аккаунтов", HttpStatus.valueOf(response.statusCode()), DataExtractor.extractData(response.body()));
        }

        return ResponseHandler.generateResponse("Ошибка при запросе списка аккаунтов", HttpStatus.valueOf(response.statusCode()), DataExtractor.extractPlusofonErrorMessage(response.body()));

    }

    @DeleteMapping("/deleteFCAccount")
    public ResponseEntity<Object> deleteFlashCallAccount(@RequestBody String requestBody) throws URISyntaxException, IOException, InterruptedException {

        JsonNode jsonNode = mapper.readTree(requestBody);
        int accountId = jsonNode.get("accountId").asInt();

        HttpRequest request = HttpRequest.newBuilder(new URI("https://restapi.plusofon.ru/api/v1/flash-call/" + accountId))
                .timeout(Duration.of(5, SECONDS))
                .DELETE()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Client", "10553")
                .header("Authorization", "Bearer " + dataManager.getPlusofonAPIToken())
                .build();


        HttpResponse<String> response = HttpClient
                .newBuilder()
                .proxy(ProxySelector.getDefault())
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());
//todo remove
        System.out.println("response.body() = " + response.body());

        if (response.statusCode() == 200) {
            return ResponseHandler.generateResponse("Аккаунт с ID " + accountId + " успешно удалён", HttpStatus.valueOf(response.statusCode()), null);
        }

        return ResponseHandler.generateResponse("Ошибка при удалении аккаунта", HttpStatus.valueOf(response.statusCode()), DataExtractor.extractPlusofonErrorMessage(response.body()));

    }

    @PostMapping("/FCSettingsSet")
    public ResponseEntity<Object> FCSettingsSet(@RequestBody String requestBody) throws URISyntaxException, IOException, InterruptedException {
        JsonNode jsonNode = mapper.readTree(requestBody);

        HttpRequest request = HttpRequest.newBuilder(new URI("https://restapi.plusofon.ru/api/v1/flash-call/settings"))
                .timeout(Duration.of(10, SECONDS))
                .POST(HttpRequest.BodyPublishers.ofString(String.valueOf(jsonNode)))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Client", "10553")
                .header("Authorization", "Bearer " + dataManager.getPlusofonAPIToken())
                .build();


        HttpResponse<String> response = HttpClient
                .newBuilder()
                .proxy(ProxySelector.getDefault())
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());
//todo remove
        System.out.println("response.body() = " + response.body());

        if (response.statusCode() == 200) {
            return ResponseHandler.generateResponse("Глобальные настройки аккаунта успешно изменены", HttpStatus.valueOf(response.statusCode()), null);
        }

        return ResponseHandler.generateResponse("Ошибка при попытке изменения глобальных настроек!", HttpStatus.valueOf(response.statusCode()), DataExtractor.extractPlusofonErrorMessage(response.body()));

    }

    @GetMapping("/FCSettingsCurrent")
    public ResponseEntity<Object> FCSettings() throws URISyntaxException, IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder(new URI("https://restapi.plusofon.ru/api/v1/flash-call/settings"))
                .timeout(Duration.of(5, SECONDS))
                .GET()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Client", "10553")
                .header("Authorization", "Bearer " + dataManager.getPlusofonAPIToken())
                .build();


        HttpResponse<String> response = HttpClient
                .newBuilder()
                .proxy(ProxySelector.getDefault())
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());
//todo remove
        System.out.println("response.body() = " + response.body());

        if (response.statusCode() == 200) {
            return ResponseHandler.generateResponse("Текущие настройки аккаунта", HttpStatus.valueOf(response.statusCode()), DataExtractor.extractData(response.body()));
        }

        return ResponseHandler.generateResponse("Ошибка при запросе текущих настроек", HttpStatus.valueOf(response.statusCode()), DataExtractor.extractPlusofonErrorMessage(response.body()));

    }

    @GetMapping("/FCProposals")
    public ResponseEntity<Object> FCProposals() throws URISyntaxException, IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder(new URI("https://restapi.plusofon.ru/api/v1/flash-call/proposals"))
                .timeout(Duration.of(5, SECONDS))
                .GET()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Client", "10553")
                .header("Authorization", "Bearer " + dataManager.getPlusofonAPIToken())
                .build();


        HttpResponse<String> response = HttpClient
                .newBuilder()
                .proxy(ProxySelector.getDefault())
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return ResponseHandler.generateResponse("Доступные для подключения пакеты", HttpStatus.valueOf(response.statusCode()), DataExtractor.extractData(response.body()));
        }

        return ResponseHandler.generateResponse("Ошибка при запросе доступных пакетов", HttpStatus.valueOf(response.statusCode()), DataExtractor.extractPlusofonErrorMessage(response.body()));

    }

    @GetMapping("/currentMainAccountProposal")
    public ResponseEntity<Object> currentMainAccountProposal() throws URISyntaxException, IOException, InterruptedException {
        int mainAccountId = dataManager.getMainAccountID();

        if (mainAccountId == 0) {
            return ResponseHandler.generateResponse("Требуется создать аккаунт для работы с API или войти в систему", HttpStatus.BAD_REQUEST, null);

        }

        HttpRequest request = HttpRequest.newBuilder(new URI("https://restapi.plusofon.ru/api/v1/flash-call/proposals/" + mainAccountId))
                .timeout(Duration.of(5, SECONDS))
                .GET()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Client", "10553")
                .header("Authorization", "Bearer " + dataManager.getPlusofonAPIToken())
                .build();


        HttpResponse<String> response = HttpClient
                .newBuilder()
                .proxy(ProxySelector.getDefault())
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());
        //todo remove
        System.out.println("response.body() = " + response.body());

        if (response.statusCode() == 200) {
            return ResponseHandler.generateResponse("Данные текущего пакета главного аккаунта", HttpStatus.valueOf(response.statusCode()), DataExtractor.extractData(response.body()));
        }

        return ResponseHandler.generateResponse("Ошибка при запросе текущего пакета главного аккаунта!", HttpStatus.valueOf(response.statusCode()), DataExtractor.extractPlusofonErrorMessage(response.body()));

    }

    @GetMapping("/accountProposal")
    public ResponseEntity<Object> accountProposal(@RequestParam("accountId") String accountId) throws URISyntaxException, IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder(new URI("https://restapi.plusofon.ru/api/v1/flash-call/proposals/" + accountId))
                .timeout(Duration.of(5, SECONDS))
                .GET()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Client", "10553")
                .header("Authorization", "Bearer " + dataManager.getPlusofonAPIToken())
                .build();


        HttpResponse<String> response = HttpClient
                .newBuilder()
                .proxy(ProxySelector.getDefault())
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());
        //todo remove
        System.out.println("response.body() = " + response.body());

        if (response.statusCode() == 200) {
            return ResponseHandler.generateResponse("Данные текущего пакета главного аккаунта", HttpStatus.valueOf(response.statusCode()), DataExtractor.extractData(response.body()));
        }

        return ResponseHandler.generateResponse("Ошибка при запросе текущего пакета главного аккаунта!", HttpStatus.valueOf(response.statusCode()), DataExtractor.extractPlusofonErrorMessage(response.body()));

    }

    @PutMapping("/addProposal")
    public ResponseEntity<Object> addProposal(@RequestParam("accountId") String accountId, @RequestBody String requestBody) throws URISyntaxException, IOException, InterruptedException {
        JsonNode jsonNode = mapper.readTree(requestBody);

        HttpRequest request = HttpRequest.newBuilder(new URI("https://restapi.plusofon.ru/api/v1/flash-call/proposals/" + accountId))
                .timeout(Duration.of(10, SECONDS))
                .PUT(HttpRequest.BodyPublishers.ofString(String.valueOf(jsonNode)))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Client", "10553")
                .header("Authorization", "Bearer " + dataManager.getPlusofonAPIToken())
                .build();


        HttpResponse<String> response = HttpClient
                .newBuilder()
                .proxy(ProxySelector.getDefault())
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());
//todo remove
        System.out.println("response.body() = " + response.body());

        if (response.statusCode() == 200) {
            return ResponseHandler.generateResponse("Пакет звонков успешно добавлен к аккаунту " + accountId, HttpStatus.valueOf(response.statusCode()), null);
        }

        return ResponseHandler.generateResponse("Ошибка при добавлении пакета звонков!", HttpStatus.valueOf(response.statusCode()), DataExtractor.extractPlusofonErrorMessage(response.body()));

    }

}
