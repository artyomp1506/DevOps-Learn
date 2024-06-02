package com.example.demo.dns;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.net.http.HttpRequest.Builder;

@Service
public class CloudflareService {
    @Value("${cf.zone_id}")
    private  String zoneId;
    @Value("${cf.token}")
    private  String token;
    private Builder builder;


    public void editorAddZone(String zone, String ip) {
        var requestBody = getBody(zone, ip);
        var zones = getAllZones();
        var client = HttpClient.newHttpClient();
        if (!zones.contains(zone))
            builder = builder.POST(HttpRequest.BodyPublishers.ofString(requestBody));
        else builder = getEditZoneBuilder(zones, zone, requestBody);
        try {
            client.send(builder.build(), HttpResponse.BodyHandlers.discarding());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private Builder getEditZoneBuilder(String records, String zone, String body) {
        var jsonRecordsResult = new JSONObject(records);
        var recordsArray = jsonRecordsResult.getJSONArray("result");
        for (int i=0; i< recordsArray.length(); i++){
            var record = recordsArray.getJSONObject(i);
            if (record.getString("name").equals(zone)) {
                var id = record.getString("id");
                var url = getZoneUrl(id);
                return getNewBuilderWihHeader(url).PUT(HttpRequest.BodyPublishers.ofString(body));
            }
        }
        throw new RuntimeException("No such record");
    }


    private String getAllZones()
    {
        var client = HttpClient.newHttpClient();
        builder = getNewBuilderWihHeader(getUrl()).GET();
        var request = builder.build();
        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    private String getUrl()
    {
        return String.format("https://api.cloudflare.com/client/v4/zones/%s/dns_records", zoneId);
    }
    private String getZoneUrl(String currentRecordId)
    {
        return String.format("https://api.cloudflare.com/client/v4/zones/%s/dns_records/%s", zoneId, currentRecordId);
    }
    private String getBody(String zone, String ip)
    {
        var jsonObject = new JSONObject();
        jsonObject.put("content", ip);
        jsonObject.put("name", zone);
        jsonObject.put("proxied", false);
        jsonObject.put("type", "A");
        return jsonObject.toString();
    }
    private Builder getNewBuilderWihHeader(String url)
    {
        return HttpRequest.newBuilder(URI.create(url))
                .header("Content-type", "application/json")
                .header("Authorization", String.format("Bearer %s", token));
    }
}

