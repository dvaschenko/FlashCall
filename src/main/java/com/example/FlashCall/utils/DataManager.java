package com.example.FlashCall.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class DataManager {

    // TODO: Move DB logic here to override in memory saving if needed

    public abstract void addKeyData(String phone, String key);

    public abstract void addCreationMomentData(String phone, Long creationMillis);

    public abstract String getKeyByPhone (String phone);

    public abstract void cleanData(String phone);

    public abstract String getPlusofonAPIToken();

    public abstract void setPlusofonAPIToken(String plusofonAPIToken);

    public abstract String getPlusofonAPIRefreshToken();

    public abstract void setPlusofonAPIRefreshToken(String plusofonAPIRefreshToken);

    public abstract String getAccessToken();

    public abstract void setAccessToken(String accessToken);

    public abstract boolean isEmpty();

    public abstract boolean userDataIsEmpty ();

    public abstract int getMainAccountID();

    public abstract void setMainAccountID(int mainAccountID);

    public abstract void cleanExpiredEntries();
}
