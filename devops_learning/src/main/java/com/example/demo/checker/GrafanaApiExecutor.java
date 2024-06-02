package com.example.demo.checker;

import org.apache.commons.lang3.NotImplementedException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

public class GrafanaApiExecutor {
   private final String url;
   private final String credentials;

    public GrafanaApiExecutor(String username, String password, String ip) {
       url = getUrl(username, password, ip);
       credentials = String.format("%s:%s", username, password);
    }
    public JSONArray getDataSources() throws IOException, InterruptedException, URISyntaxException {
        var requestUrl = url+"datasources";
        return getArrayFrom(getBody(requestUrl));
    }
    public JSONObject getDataSource(String name) throws IOException, URISyntaxException, InterruptedException {
        var requestUrl = url+"datasources/name/"+name;
        return getObjectFrom(getBody(requestUrl));
    }
    public JSONArray getSearchDashboardResults() throws IOException, URISyntaxException, InterruptedException {
        var requestUrl= url+"search";
        return getArrayFrom(getBody(requestUrl));
    }
    public JSONObject getDashboardBy(String uid) throws IOException, URISyntaxException, InterruptedException {
        var requestUrl = String.format("%sdashboards/uid/%s", url, uid);
        return getObjectFrom(getBody(requestUrl));
    }

    private String getBody(String requestUrl) throws IOException, InterruptedException, URISyntaxException {
        var builder = HttpRequest.newBuilder(new URI(requestUrl)).GET().header("Content-type", "application/json")
                .header("Authorization",
                        String.format("Basic %s", Base64.getEncoder().encodeToString(credentials.getBytes())))
                .build();
        var client = HttpClient.newHttpClient();

        try {
            var response = client.send(builder, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode()==404)
                throw new RuntimeException(String.format("Не найден объект (дашборд/источник) по %s", requestUrl));
            if (response.statusCode()==401)
                throw new RuntimeException("Не удалось авторизоваться в Grafana под указанными в условии логином/паролем");
            if (response.statusCode() == 502)
                throw new RuntimeException("Gitlab упал(");
            else if (response.statusCode() != 200) {
                var message = String.format
                        ("При попытке GET запроса на %s сервер вернул ошибку %s с кодом %s, " +
                                        "а должен был вернуть ответ с api Grafana",
                                requestUrl, response.body(), response.statusCode());
                throw new RuntimeException(message);
            }
            return response.body();
        } catch (ConnectException exception) {
            throw new ConnectException("Сервер не позволяет установить соединение");
        }
    }

    private String getUrl(String username, String password, String ip) {
        return String.format("http://%s:3000/api/", ip);
    }


    private JSONArray getArrayFrom(String body)
    {
        return new JSONArray(body);
    }
    private JSONObject getObjectFrom(String body)
    {
        return new JSONObject(body);
    }
}
