package uk.co.epicuri.api.core.pojo;

/**
 * 28/08/2014
 */
public class Authentication {
    private int Id;
    private String Name, Username, AuthKey, Pin, Password;
    private boolean Manager;

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public String getAuthKey() {
        return AuthKey;
    }

    public void setAuthKey(String authKey) {
        AuthKey = authKey;
    }

    public String getPin() {
        return Pin;
    }

    public void setPin(String pin) {
        Pin = pin;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public boolean isManager() {
        return Manager;
    }

    public void setManager(boolean manager) {
        Manager = manager;
    }
}
