package com.example.demo.checker;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.List;

public class ApiExecutor {
    private String path;
    private List<Map<String, String>> headers;
    public ApiExecutor(String path, List<Map<String, String>> headers)
    {
    this.path = path;
    this.headers = headers;
    }
    public String sendGetResponse() throws IOException, InterruptedException {
        var builder = getBuilderWithHeaders().GET();
        return getResponse(builder);

    }



    public String sendPostRequest(String body) throws IOException, InterruptedException {
        var builder = getBuilderWithHeaders().POST(HttpRequest.BodyPublishers.ofString(body));
        return getResponse(builder);
    }
    private HttpRequest.Builder getBuilderWithHeaders()
    {
    var builder = HttpRequest.newBuilder(URI.create(path));
    for (var header:headers)
        builder.header(header.get("key"), header.get("value"));
    return builder;

    }
    private  String getResponse(HttpRequest.Builder builder) throws IOException, InterruptedException {
        var request = builder.build();
        var client = HttpClient.newHttpClient();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

}
