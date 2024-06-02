package com.example.demo.checker;

import org.json.JSONArray;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GitlabApiExecutor {
    private final String url;

    public GitlabApiExecutor(String protocol, String ipOrDomain) {
       url = getGitlabUrl(protocol, ipOrDomain);
    }
    public JSONArray getProjects() throws IOException, InterruptedException, RuntimeException {
        var requestUrl = String.format("%s/projects/", url);
        return getObjectFromRequest(requestUrl);
    }
    public JSONArray getBranches(int projectId) throws IOException, InterruptedException, RuntimeException {
        var branchesRequestUrl = String.format("%s/projects/%s/repository/branches", url, projectId);
        return getObjectFromRequest(branchesRequestUrl);
    }
    private  JSONArray getObjectFromRequest(String requestUrl) throws IOException, InterruptedException, RuntimeException {
        var builder = HttpRequest.newBuilder(URI.create(requestUrl)).GET().build();
        var client = HttpClient.newHttpClient();
        try {
            var response = client.send(builder, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 502)
                throw new RuntimeException("Gitlab упал(");
            else if (response.statusCode() != 200) {
                var message = String.format
                        ("При попытке GET запроса на %s сервер вернул ошибку %s с кодом %s, " +
                                        "а должен был вернуть ответ с api Gitlab",
                                requestUrl, response.body(), response.statusCode());
                throw new RuntimeException(message);
            }
            return new JSONArray(response.body());
        } catch (ConnectException exception) {
            throw new ConnectException("Сервер не позволяет установить соединение");
        }

    }

    private String getGitlabUrl(String protocol, String ipOrDomain){
    return String.format("%s://%s/api/v4", protocol, ipOrDomain);
    }
}
