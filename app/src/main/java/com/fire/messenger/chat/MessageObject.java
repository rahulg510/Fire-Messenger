package com.fire.messenger.chat;

public class MessageObject {

    String messageId, message, senderId;

    public MessageObject(String messageId, String senderId, String message){
        this.message = message;
        this.messageId = messageId;
        this.senderId= senderId;

    }

    public String getMessageId(){
        return this.messageId;
    }

    public String getMessage() {
        return message;
    }

    public String getSenderId() {
        return senderId;
    }
}
