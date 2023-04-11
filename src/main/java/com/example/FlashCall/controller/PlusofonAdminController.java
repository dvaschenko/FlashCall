package com.example.FlashCall.controller;

import com.example.FlashCall.response.ResponseHandler;
import com.example.FlashCall.utils.DataExtractor;
import com.example.FlashCall.utils.DataManager;
import com.example.FlashCall.utils.InMemoryDataManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.temporal.ChronoUnit.SECONDS;

@RestController
@RequestMapping("/plusofonAdmin")
public class PlusofonAdminController {

    @Autowired
    private DataManager dataManager;

    {
        new InMemoryDataManager();
    }

    @Autowired
    private final FlashCallAdminController FCcontroller = new FlashCallAdminController(dataManager);

    private final ObjectMapper mapper = new ObjectMapper();

    public PlusofonAdminController(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    /*
    Method for receiving Plusofon Flash Call API Token that is required for call requests. For more information see:
    https://help.plusofon.ru/API/v0/Plusofon_API
    After call we are going to store Token in the TempDataSaver field
    */

    //TODO Override exception handling as you need

    @PostMapping("/plusofonLogin")
    public ResponseEntity<Object> plusofonLogin(@RequestBody String requestBody) throws IOException, URISyntaxException, InterruptedException {

        JsonNode jsonNode;
        try {
            jsonNode = mapper.readTree(requestBody);
        } catch (JsonProcessingException e) {
            return ResponseHandler.generateResponse("Error while read data", HttpStatus.BAD_REQUEST, e.getOriginalMessage());
        }

        HttpRequest request = HttpRequest.newBuilder(new URI("https://restapi.plusofon.ru/api/v1/login"))
                .timeout(Duration.of(5, SECONDS))
                .POST(HttpRequest.BodyPublishers.ofString(String.valueOf(jsonNode)))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Client", "10553")
                .build();

        HttpResponse<String> response = HttpClient
                .newBuilder()
                .proxy(ProxySelector.getDefault())
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            List<String> tokens = DataExtractor.extractPlusofonTokens(response.body());

            if (!tokens.isEmpty()) {
                dataManager.setPlusofonAPIToken(tokens.get(0));
                dataManager.setPlusofonAPIRefreshToken(tokens.get(1));
            }

            manageAccountExistence();
            //todo remove
            System.out.println("PlusofonAPIToken() = " + dataManager.getPlusofonAPIToken());
            System.out.println("Plusofon refresh token = " + dataManager.getPlusofonAPIRefreshToken());
            System.out.println("access token = " + dataManager.getAccessToken());
            System.out.println("Main account ID = " + dataManager.getMainAccountID());


            return ResponseHandler.generateResponse("Токен успешно получен", HttpStatus.valueOf(response.statusCode()), null);
        } else {
            return ResponseHandler.generateResponse("Проблема при авторизации", HttpStatus.valueOf(response.statusCode()), DataExtractor.extractPlusofonErrorMessage(response.body()));
        }
    }


    private void manageAccountExistence() {
        try {
            FCcontroller.listFlashCallAccounts();

            if (dataManager.getMainAccountID() == 0) {
                Map<String, String> newAccountBody = new HashMap<>();
                newAccountBody.put("name", "mainAccount");
                JsonNode node = mapper.valueToTree(newAccountBody);
                FCcontroller.addFlashCallAccount(String.valueOf(node));
            }


        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

}
