package com.example.demo.cloud;

public class CloudResult {
    private String externalIP;
    private String internalIP;
    private String id;




    public CloudResult(String externalIP, String internalIP, String id)
    {

        this.externalIP = externalIP;
        this.internalIP = internalIP;
        this.id = id;
    }

    public String getExternalIP() {
        return externalIP;
    }

    public String getInternalIP() {
        return internalIP;
    }
    public String getId() {
        return id;
    }
    @Override
    public String toString() {
        return String.format("Внешний ip: %s, внутренний ip: %s", getExternalIP(), getInternalIP());
    }


}
