package com.example.FlashCall.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class DataExtractor {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<String> extractPlusofonTokens(String response) {
        List<String> extractedTokens = new ArrayList<>();
        JsonNode jsonNode;

        try{
            jsonNode = mapper.readTree(response);

        } catch (JsonProcessingException e) {
            //TODO Override exception handling as you need
            throw new RuntimeException(e);
        }
        extractedTokens.add(jsonNode.get("token").asText());
        extractedTokens.add(jsonNode.get("refresh_token").asText());
        return extractedTokens;
    }

    public static String extractPlusofonErrorMessage(String response) {
        JsonNode jsonNode;

        try{
            jsonNode = mapper.readTree(response);

        } catch (JsonProcessingException e) {
            //TODO Override exception handling as you need
            throw new RuntimeException(e);
        }

        return jsonNode.get("message").asText();
    }

    public static String extractAccessToken(String response) {
        JsonNode jsonNode;

        try{
            jsonNode = mapper.readTree(response);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return jsonNode.get("data").get("access_token").asText();
    }

    public static JsonNode extractData(String response) {
        JsonNode jsonNode;

        try{
            jsonNode = mapper.readTree(response);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return jsonNode.get("data");
    }

    public static int extractMainAccountId(String response) {
        JsonNode jsonNode;

        try{
            jsonNode = mapper.readTree(response);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return jsonNode.get("data").get(0).get("id").asInt(0);
    }

    public static int extractCreatedAccountId(String response) {
        JsonNode jsonNode;

        try{
            jsonNode = mapper.readTree(response);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return jsonNode.get("data").get("account_id").asInt(0);
    }

    public static String extractKey(String response) {
        JsonNode jsonNode;

        try{
            jsonNode = mapper.readTree(response);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return jsonNode.get("data").get("key").asText();
    }

    public static String extractAccessTokenFromMainAccount(String response) {
        JsonNode jsonNode;

        try{
            jsonNode = mapper.readTree(response);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return jsonNode.get("data").get(0).get("access_token").asText("");

    }


}
