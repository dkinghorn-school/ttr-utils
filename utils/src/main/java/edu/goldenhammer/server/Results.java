package edu.goldenhammer.server;

public class Results {
    private String message;
    private int responseCode;

    public String getMessage() {
        return message;
    }

    /**
     * Serializes whatever message is passed in and stores it as the message
     * @param message
     */
    public void setAndSerializeMessage(String message) {
        this.message = "{\"message\":\"" + message + "\"}";
    }

    /**
     * Sets the message to whatever string literal is input
     * @param serializedMessage
     */
    public void setMessage(String serializedMessage) {
        this.message = serializedMessage;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }


}