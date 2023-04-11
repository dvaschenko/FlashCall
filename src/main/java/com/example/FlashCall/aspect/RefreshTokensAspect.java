package com.example.FlashCall.aspect;

import com.example.FlashCall.utils.DataExtractor;
import com.example.FlashCall.utils.DataManager;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

import static java.time.temporal.ChronoUnit.SECONDS;

@Aspect
@Component
public class RefreshTokensAspect {
    @Autowired
    private final DataManager dataManager;

    public RefreshTokensAspect(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Pointcut("execution(public * com.example.FlashCall.controller.FlashCallAdminController.*(..))")
    public void callAtMyAdminControllerPublic() { }

    @Pointcut("execution(public * com.example.FlashCall.controller.FlashCallController.*(..))")
    public void callAtMyControllerPublic() { }

    @Before("callAtMyControllerPublic() || callAtMyAdminControllerPublic()")
    public void beforeCallAtAnyMethod(JoinPoint jp) throws URISyntaxException, IOException, InterruptedException {
        if(dataManager.isEmpty()){
            throw new IllegalStateException();
        }
        checkTokens();
    }

    private void checkTokens() throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request =  HttpRequest.newBuilder(new URI("https://restapi.plusofon.ru/api/v1/flash-call"))
                .timeout(Duration.of(5, SECONDS))
                .GET()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Client", "10553")
                .header("Authorization", "Bearer " + dataManager.getPlusofonAPIToken())  //try current token
                .build();


        HttpResponse<String> response = HttpClient
                .newBuilder()
                .proxy(ProxySelector.getDefault())
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());

        if(response.statusCode() == 401){  //if 401 - refresh with refresh_token
            refreshPlusofonToken();
        }

        if(dataManager.getAccessToken().isEmpty()){
            dataManager.setAccessToken(DataExtractor.extractAccessTokenFromMainAccount(response.body()));
        }
    }

    private void refreshPlusofonToken() throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request =  HttpRequest.newBuilder(new URI("https://restapi.plusofon.ru/api/v1/refresh"))
                .timeout(Duration.of(5, SECONDS))
                .GET()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Client", "10553")
                .header("Authorization", "Bearer " + dataManager.getPlusofonAPIRefreshToken())
                .build();


        HttpResponse<String> response = HttpClient
                .newBuilder()
                .proxy(ProxySelector.getDefault())
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());
//todo remove
        System.out.println("response.body() = " + response.body());

        if (response.statusCode() == 200) {
            List<String> tokens = DataExtractor.extractPlusofonTokens(response.body());

            if(!tokens.isEmpty()){
                dataManager.setPlusofonAPIToken(tokens.get(0));
                dataManager.setPlusofonAPIRefreshToken(tokens.get(1));
            }
        }

    }
}
