package com.example.FlashCall.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
@Component
public class DataCleanScheduler {

    @Autowired
    private DataManager dataManager;

    //each hour checks TempDataSaver data and removes expired to prevent memory leaks
    @Scheduled(cron = "0 */1 * * *")
    public void clearExpiredPhoneData() {
        if(!dataManager.userDataIsEmpty()) {
            dataManager.cleanExpiredEntries();
        }
    }
}
