package com.aarav.aaravchat;

public class UserChat {
    private String usernameWith;
    private String chatId;

    public UserChat(String usernameWith, String chatId) {
        this.usernameWith = usernameWith;
        this.chatId = chatId;
    }

    public String getUsernameWith() {
        return usernameWith;
    }

    public void setUsernameWith(String usernameWith) {
        this.usernameWith = usernameWith;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }
}
