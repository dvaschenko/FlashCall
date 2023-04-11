package com.example.FlashCall.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;

public class ResponseHandler {

    public static ResponseEntity<Object> generateResponse(String message, HttpStatus status, Object responseObj){
        var map = new HashMap<String, Object>();
        map.put("message", message);
        map.put("status", status.value());
        map.put("data", responseObj);

        return new ResponseEntity<>(map, status);
    }
}
