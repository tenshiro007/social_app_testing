package com.example.socialtestingapp.model;

public class ModelChatlist {
    String id; //we will need id to get chat list,sender/receiver uid

    public ModelChatlist(String id) {
        this.id = id;
    }

    public ModelChatlist() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

