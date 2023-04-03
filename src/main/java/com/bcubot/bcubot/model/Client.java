package com.bcubot.bcubot.model;

public class Client {
    public String id;
    public String pass;

    public Client(String id, String pass) {
        this.id = id;
        this.pass = pass;
    }

    public Client() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }
}
