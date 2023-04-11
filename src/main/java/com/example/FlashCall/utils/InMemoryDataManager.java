package com.example.FlashCall.utils;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryDataManager extends DataManager{
    private Map<String, String> codeRestoreData;
    private Map<String, Long> entryCreationMomentData;
    private String plusofonAPIToken;
    private String plusofonAPIRefreshToken;
    private String accessToken;
    private int mainAccountID;

    @PostConstruct
    public void initialize() {
        codeRestoreData = new ConcurrentHashMap<>();
        entryCreationMomentData = new ConcurrentHashMap<>();
        plusofonAPIToken = "";
        accessToken = "";
        plusofonAPIRefreshToken = "";
        mainAccountID = 0;
    }

    @Override
    public void addKeyData(String phone, String key){
        this.getCodeRestoreData().put(phone, key);
    }

    @Override
    public void addCreationMomentData(String phone, Long creationMillis){
        this.getEntryCreationMomentData().put(phone, creationMillis);
    }

    @Override
    public String getKeyByPhone (String phone){
        return this.codeRestoreData.get(phone);
    }

    @Override
    public void cleanData(String phone){
        this.codeRestoreData.remove(phone);
        this.entryCreationMomentData.remove(phone);
    }

    private Map<String, String> getCodeRestoreData() {
        return codeRestoreData;
    }

    private Map<String, Long> getEntryCreationMomentData() {
        return entryCreationMomentData;
    }

    @Override
    public String getPlusofonAPIToken() {
        return plusofonAPIToken;
    }

    @Override
    public void setPlusofonAPIToken(String plusofonAPIToken) {
        this.plusofonAPIToken = plusofonAPIToken;
    }

    @Override
    public String getPlusofonAPIRefreshToken() {
        return plusofonAPIRefreshToken;
    }

    @Override
    public void setPlusofonAPIRefreshToken(String plusofonAPIRefreshToken) {
        this.plusofonAPIRefreshToken = plusofonAPIRefreshToken;
    }

    @Override
    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public boolean isEmpty() {
        return (this.getCodeRestoreData().isEmpty() && this.plusofonAPIToken.isEmpty() && this.accessToken.isEmpty()
                && this.plusofonAPIRefreshToken.isEmpty() && this.getMainAccountID() == 0);
    }

    @Override
    public boolean userDataIsEmpty (){
        return (this.codeRestoreData.isEmpty() && this.entryCreationMomentData.isEmpty());
    }

    @Override
    public int getMainAccountID() {
        return mainAccountID;
    }

    @Override
    public void setMainAccountID(int mainAccountID) {
        this.mainAccountID = mainAccountID;
    }

    @Override
    public void cleanExpiredEntries() {
        long currentMoment = System.currentTimeMillis();
        List<String> keysToRemove = new ArrayList<>();
        entryCreationMomentData.forEach((key, value) -> {
            if (currentMoment >= value + 900000) {
                keysToRemove.add(key);
            }
        });

        keysToRemove.forEach(entryCreationMomentData.keySet()::remove);
        keysToRemove.forEach(codeRestoreData.keySet()::remove);

    }
}
