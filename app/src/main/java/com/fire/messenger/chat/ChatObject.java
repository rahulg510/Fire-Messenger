package com.fire.messenger.chat;

public class ChatObject {
    private String chatId;

    public ChatObject(String id)
    {
        chatId = id;
    }

    public String getChatId() {
        return chatId;
    }
}
