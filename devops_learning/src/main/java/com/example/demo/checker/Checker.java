package com.example.demo.checker;





import com.example.demo.entity.check_results.Check;
import com.example.demo.entity.check_results.Result;
import com.example.demo.entity.check_results.State;
import com.example.demo.entity.task.Task;
import org.bouncycastle.util.encoders.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Map;
import java.util.regex.Pattern;

public class Checker {

    private SshExecutor executor;
    private JSONObject checkers;
    private HashMap<String, String> variables;
    private Check check;
    private Task task;

    public Checker(SshExecutor executor, JSONObject checkers, HashMap<String, String> variables, Check check, Task task) {
        this.executor = executor;
        this.checkers = checkers;
        this.variables = variables;
        this.check = check;
        this.task = task;
    }
    public List<Result> makeCheck() {
        var results = new ArrayList<Result>();
        try {
            results.addAll(checkSSH());
            results.addAll(checkApi());
        }
        catch (Exception exception)
        {
            results.add(new Result(task, exception.getLocalizedMessage(), State.Wrong, check));
            exception.printStackTrace();
        }
        return results;
    }
    private List<Result> checkSSH() throws Exception {
        var sshChecker = (JSONObject) checkers.get("ssh_checker");
        var checks = (JSONArray) sshChecker.get("check");
        var results = new ArrayList<Result>();
        for (var check:checks)
        {

            var checkInJson = (JSONObject) check;
            var notValidCommand = (String) checkInJson.get("command");
            var inputCommand = convertToValid(notValidCommand);
            if (inputCommand!=null)
            {
                var timeout = (Long) checkInJson.get("timeout");

                var actual = executor.getResult(inputCommand).replace("\n","");
                System.out.println(actual);
                var ignoreSpaces = checkInJson.get("ignore_spaces");
                if (ignoreSpaces!=null && (boolean) ignoreSpaces)
                    actual = actual.replace(" ", "");
                var expectedContains = checkInJson.get("contains");

                if (expectedContains!=null)
                {
                    if (expectedContains instanceof JSONArray)
                        results.addAll(checkArrayContains(inputCommand, actual, (JSONArray) expectedContains));
                    else {
                        var expectedOutput = convertToValid((String) expectedContains);
                        if (!actual.contains(expectedOutput)) {
                            results.add(makeCheckResult("Результат команды %s - %s не содержит %s",
                                    inputCommand, actual, expectedOutput, (String) checkInJson.get("fail_message")));
                        }

                    }


                }
                var equalsExpected = convertToValid((String) checkInJson.get("equals"));
                if (equalsExpected!=null && !actual.equals(equalsExpected))
                {
                    results.add(makeCheckResult("Результат команды %s - %s не соответствует %s", inputCommand, actual, equalsExpected,
                            (String) checkInJson.get("fail_message")));

                }

            }
            else throw new InvalidObjectException("Объект не имеет ключа command");
        }
        return results;
    }

    private Result makeCheckResult(String messageFormat, String inputCommand, String actual, String equalsExpected, String failMessage) {
        var defaultMessage = String.format(messageFormat, inputCommand, actual,
                equalsExpected);

        return new Result(task,
                convertToValid((String) failMessage==null?defaultMessage:failMessage), State.Wrong, check);
    }

    private List<Result> checkArrayContains(String inputCommand, String actual, JSONArray expectedContains) {
        var results = new ArrayList<Result>();
        for (var commandObject:expectedContains)
        {
            System.out.println(actual);
            var objValue = (JSONObject) commandObject;
            var value = convertToValid((String) objValue.get("value"));
            var negative = objValue.get("not")!=null ?(Boolean) objValue.get("not"): false;
            if (negative && actual.contains(value))
                results.add(makeCheckResult("Результат команды %s - %s не должен содержать %s", inputCommand, actual, value, (String) objValue.get("fail_message")));
            else if (!actual.contains(value))
            {
                results.add(makeCheckResult("Результат команды %s - %s не содержит %s",inputCommand, actual,
                        value,(String) objValue.get("fail_message")));
            }
        }
        return results;
    }



    public List<Result> checkApi() throws ParseException, IOException, InterruptedException {

        var apiChecker = (JSONObject) checkers.get("api_checker");
        if (apiChecker==null)
            return new ArrayList<Result>();
        var headers = (JSONArray) apiChecker.get("headers");
        var checks = (JSONArray) apiChecker.get("check");
        var mappedHeaders = getMappedHeaders(headers);
        var results = new ArrayList<Result>();
        for (var check : checks) {
            var checkObject = (JSONObject) check;
            var prepare = (JSONObject) checkObject.get("prepare");
            if (prepare!=null)
            {
                var response = "";
                var prepareHeaders = getMappedHeaders((JSONArray) prepare.get("headers"));
                var path = convertToValid((String) prepare.get("path"));
                var executor = new ApiExecutor(path, prepareHeaders);
                var post = prepare.get("post");
                if (post!=null)
                {
                    response=executor.sendPostRequest((String) post);
                }
                else response = executor.sendGetResponse();
                var result = new JSONParser().parse(response);
                if (result instanceof JSONArray)
                    for (var obj:(JSONArray)result)
                    {
                        savePrepareValues((JSONObject) obj, (JSONObject) prepare.get("save") );
                        results.addAll(checkApiTest((JSONObject) checkObject.get("test"), mappedHeaders));
                    }
                else if (result instanceof JSONObject) {
                    savePrepareValues((JSONObject) result, (JSONObject) prepare.get("save") );
                    results.addAll(checkApiTest((JSONObject) checkObject.get("test"), mappedHeaders));
                }
            }
            else {
                results.addAll(checkApiTest(checkObject, mappedHeaders));
            }

        }
        return results;
    }
    private List<Result> checkApiTest(JSONObject checkObject, List<Map<String,String>> mappedHeaders)
            throws IOException, InterruptedException {
        var results = new ArrayList<Result>();
        var response = "";
        System.out.println(variables.get("ip"));
        var path = convertToValid((String) checkObject.get("path"));
        var executor = new ApiExecutor(path, mappedHeaders);
        var postObject = checkObject.get("post");
        if (postObject != null) {
            response = executor.sendPostRequest(convertToValid((String) postObject));
        } else response = executor.sendGetResponse();
        var contains = checkObject.get("contains");
        if (contains != null) {
            var defaultMessageTemplate = String.format("%s не содержит %s", path, "%s");
            results.addAll(checkContains(response, (JSONArray) contains, (String) checkObject.get("fail_message"),
                    defaultMessageTemplate));
        }
        var expectedValues = (JSONObject) checkObject.get("expected");
        if (expectedValues != null) {
            try {
                results.addAll(checkValueFromObject(response, expectedValues));
            } catch (ParseException exception) {
                throw new RuntimeException(exception);
            }
        }
        return results;
    }




    private void savePrepareValues(JSONObject current, JSONObject save) throws IOException, InterruptedException, ParseException {
        variables.put((String) save.get("name"), (String) current.get(save.get("parameter")));
    }


    private List<Map<String, String>> getMappedHeaders(JSONArray headers) {
        var headersMaps = new ArrayList<Map<String, String>>();
        for (var header : headers) {
            var objHeader = (JSONObject) header;
            var dict = new HashMap<String, String>();
            dict.put("key", convertToValid((String) objHeader.get("key")));
            var value = objHeader.get("value");
            if (value instanceof JSONObject result)
            {
                var username = (String) result.get("username");
                var password = (String) result.get("password");
                dict.put("value", Base64.toBase64String(String.format("%s:%s", username, password).getBytes(StandardCharsets.UTF_8)));

            }
            else dict.put("value", (String) objHeader.get("value"));
            headersMaps.add(dict);
        }
        return headersMaps;
    }

    private String convertToValid(String parametersString) {
        System.out.println(parametersString);
        if (parametersString == null)
            return null;
        Pattern pattern = Pattern.compile("(\\$\\{(.*?)\\})",
                Pattern.MULTILINE);
        StringBuilder result = new StringBuilder(parametersString);
        var matcher = pattern.matcher(result);
        int startIndex=0;
        while (matcher.find(startIndex)) {
            System.out.println(matcher.group(2));
            var replace = (String) variables.get(matcher.group(2));
            result.replace(matcher.start(), matcher.end(), replace);
            System.out.println(replace);
            startIndex = matcher.start()+replace.length();
        }
        return result.toString();
    }


    private List<Result> checkContains(String response, JSONArray containsList, String failMessage, String defaultMessage) {
        var results = new ArrayList<Result>();
        for (var word : containsList) {
            var validWord = convertToValid((String) word);
            System.out.println(word);
            if (validWord!=null && !response.contains(validWord)) {
                if (failMessage != null) {
                    results.add(new Result(task, failMessage, State.Wrong, check));
                } else {
                    var message = String.format(defaultMessage, validWord);
                    results.add(new Result(task, message, State.Wrong, check));
                }
            }

        }
        return results;
    }

    private List<Result> checkValueFromObject(String response, JSONObject expected) throws IOException,
            ParseException, InterruptedException {
        var eachChecked = false;
        var currentElement = new JSONParser().parse(response);
        var results = new ArrayList<Result>();
        var pathTo = (String) expected.get("path_to");
        var elements = pathTo.split("/", 0);


        for (var element : elements) {
            if (currentElement instanceof JSONArray) {
                var currentArray = (JSONArray) currentElement;
                if (!Objects.equals(element, "each")) {
                    currentElement = currentArray.get(Integer.parseInt(element));
                } else {
                    for (var resultObject : currentArray) {
                        var currentObject = (JSONObject) resultObject;
                        results.addAll(checkExpectedValue(currentObject, (JSONObject) expected.get("values"),
                                false,
                                null));
                    }
                    eachChecked=true;
                }
            } else {
                var obj = (JSONObject) currentElement;
                currentElement = obj.get(element);
            }
        }
        if(!eachChecked)
            results.addAll(checkExpectedValue((JSONObject) currentElement, expected, false, null));
        return results;

    }

    private List<Result> checkExpectedValue(JSONObject currentObject, JSONObject expectedFields,
                                            boolean checkAllElements, String failMessage) {
        var results = new ArrayList<Result>();
        for (var key : expectedFields.keySet()) {
            var expectedValue = expectedFields.get(key);
            var actual = currentObject.get(key);
            if (!actual.equals(expectedValue)) {
                var message = failMessage != null ? failMessage :
                        String.format("Неверное значение для параметра %s - %s, а должно быть %s", key,
                                actual, expectedValue);
                results.add(new Result(task, message, State.Wrong, check));
                if (!checkAllElements)
                    return results;
            }
        }
        return results;
    }
}

