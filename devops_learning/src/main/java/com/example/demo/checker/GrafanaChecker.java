package com.example.demo.checker;

import com.example.demo.entity.check_results.Check;
import com.example.demo.entity.check_results.Result;
import com.example.demo.entity.check_results.State;
import com.example.demo.entity.task.Task;
import org.json.JSONArray;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class GrafanaChecker {
    private Task task;
    private String ip;
    private String username;
    private String password;
    private HashMap<String, String> dataSourceIds;
    private SshExecutor sshExecutor;
    private GrafanaApiExecutor grafanaApiExecutor;
    private Check check;
    private boolean clickHouseChecked;
    private boolean zabbixChecked;
    private boolean mysqlChecked;
    private String clickhousePassword;

    public GrafanaChecker(Task task, String ip, String username, String password, String sshUsername, String sshKeyPath,
                          HashMap<String, String> dataSourceIds, String clickhousePassword,  Check check ) {
        this.task = task;
        this.ip = ip;
        this.username = username;
        this.password = password;
        this.clickhousePassword = clickhousePassword;
        this.sshExecutor = new SshExecutor(ip, sshUsername, 22, sshKeyPath);
        this.grafanaApiExecutor = new GrafanaApiExecutor(username, password, ip);
        this.dataSourceIds = dataSourceIds;

        this.check = check;
    }

    ;
    public List<Result> check() throws Exception {
        var results = new ArrayList<Result>();
        try
        {
            if (!isGrafanaInstalled()) {
                results.add(new Result(task, "Grafana не установлена либо установлена некорректно", State.Wrong, check));
                return results;
            }
            if (!isClickhouseInstalled())
                results.add(new Result(task, "Не установлен Clickhouse", State.Wrong, check));

            else {
                var clickhouseCheckResult = checkClickhouseConfigure();
                if (!clickhouseCheckResult.equals("ok"))
                    results.add(new Result(task, clickhouseCheckResult,
                            State.Wrong, check));
            }

            results.addAll(checkGrafanaConfiguration());

        }
        catch (Exception exception)
        {
            results.add(new Result(task, exception.getMessage(), State.Wrong, check));
        }
        return results;
    }

    private String checkClickhouseConfigure() throws Exception {
        var command = String.format(
                "clickhouse-client --password=%s --query=\"select pickup_ntaname from trips limit 5;\" 2>&1",
                clickhousePassword);
        var result = sshExecutor.getResult(command);
        if (result.contains("NETWORK_ERROR"))
            return "Не удалось подключиться к Clickhouse";
        if (result.contains("UNKNOWN_TABLE"))
            return "Таблица trips не создана";
        if (!result.contains("Airport"))
            return "Таблица не содержит необходимых данных";
        return "ok";


    }

    private boolean isClickhouseInstalled() throws Exception {
        return !sshExecutor.getResult("which clickhouse").isBlank() &&
                !sshExecutor.getResult("which clickhouse-client").isBlank();
    }

    private List<Result> checkGrafanaConfiguration() throws IOException, InterruptedException, URISyntaxException {
        var results = new ArrayList<Result>();


        var notFoundTypes = new ArrayList<String>();
        for (var source:dataSourceIds.keySet())
        {
            try
            {
                var information = grafanaApiExecutor.getDataSource(source);
                var type = information.getString("type");
                var expectedType = dataSourceIds.get(source);
                if (!type.equals(expectedType))
                    results.add(new Result(task, String.format("Тип данных %s - %s, а должен быть %s",
                            source, type, expectedType), State.Wrong, check));
            }
            catch (RuntimeException e) {
                if (e.getMessage().contains("Не найден")) {
                    notFoundTypes.add(dataSourceIds.get(source));
                    results.add(new Result(task, String.format("Не найден источник %s", source), State.Warning, check));
                }
            }
        }
        if (!notFoundTypes.isEmpty())
            results.addAll(checkDataSources(grafanaApiExecutor.getDataSources(), notFoundTypes));

        var searchDashBoardResults = grafanaApiExecutor.getSearchDashboardResults();
        results.addAll(checkDashBoards(searchDashBoardResults));
        return results;
    }

    private List<Result> checkDashBoards(JSONArray searchDashBoardResults) throws IOException, URISyntaxException, InterruptedException {
        var results = new ArrayList<Result>();
        if (searchDashBoardResults.isEmpty())
        {
            results.add(new Result(task, "Не создано ни одного дашборда", State.Wrong, check));
            return results;
        }
        for (int i=0;i<searchDashBoardResults.length(); i++)
        {
            if (clickHouseChecked && zabbixChecked && mysqlChecked)
                break;
            var dashboardObject = searchDashBoardResults.getJSONObject(i);
            var title = dashboardObject.getString("title");
            var uid = dashboardObject.getString("uid");
            if (title.contains("clickhouse"))
            {
                clickHouseChecked=true;
                results.addAll(checkClickhouseDashBoard(uid));
            }
            else if (title.contains("mysql")) {
                mysqlChecked = true;
                results.addAll(checkMysqlDashBoard(uid));
            }
            else if (title.contains("zabbix"))
            {
                zabbixChecked=true;
                results.addAll(checkZabbixDashBoard(uid));
            }
        }
        if (results.isEmpty())
        {
            results.add(checkSuccsessOrFailWhenResultsEmpty());
        }
        return results;
    }

    private Result checkSuccsessOrFailWhenResultsEmpty() {
        if (mysqlChecked && clickHouseChecked && zabbixChecked)
            return new Result(task, "Всё настроено верно", State.Wrong, check);
        else
        {
            var message = "Имеются не все необходимые дашборды (проверьте, чтобы названия содержали источники данных)";
            return new Result(task, message, State.Wrong, check);
        }
    }

    private List<Result> checkZabbixDashBoard(String uid) throws IOException, URISyntaxException, InterruptedException {
        var results = new ArrayList<Result>();
        var dashboard = grafanaApiExecutor.getDashboardBy(uid).getJSONObject("dashboard");
        var panel = dashboard.getJSONArray("panels").getJSONObject(0);
        var target = panel.getJSONArray("targets").getJSONObject(0);
        var filter = target.getJSONObject("item").getString("filter");
        if (!filter.equals("Linux: CPU utilization"))
        {
            var message = String.format("Неверный фильтр %s, а должен быть Linux: CPU utilization ", filter);
            results.add(new Result(task, message, State.Wrong, check));
        }
        zabbixChecked = true;
        return results;
    }

    private List<Result> checkMysqlDashBoard(String uid) throws IOException, URISyntaxException, InterruptedException {
        var results = new ArrayList<Result>();
        var dashboard = grafanaApiExecutor.getDashboardBy(uid).getJSONObject("dashboard");
        var panel = dashboard.getJSONArray("panels").getJSONObject(0);
        var target = panel
                .getJSONArray("targets").
                getJSONObject(0);
        var columns = target.
                getJSONObject("sql").
                getJSONArray("columns");
        if (columns.length()!=2)
            results.add(new Result(task,
                    "В выборке не должно быть других столбцов кроме country и creditlimit (чувствительно к регистру)",
                    State.Wrong, check));
        for (int i=0; i<columns.length(); i++)
        {
            var name = columns.getJSONObject(i).getJSONArray("parameters").getJSONObject(0).getString("name").toLowerCase();
            if (!(name.equals("country") || name.equals("creditlimit")))
                results.add(new Result(task, String.format("Столбца %s не должно быть в параметрах выборки", name),
                        State.Wrong, check));
        }
        if (!target.getString("rawSql").toLowerCase().contains("limit 7"))
            results.add(new Result(task, "Не установлен лимит 7", State.Wrong, check));
        return results;
    }

    private List<Result> checkClickhouseDashBoard(String uid) throws IOException, URISyntaxException, InterruptedException {
        var results = new ArrayList<Result>();
        var dashboard = grafanaApiExecutor.getDashboardBy(uid).getJSONObject("dashboard");
        var panel = dashboard.getJSONArray("panels").getJSONObject(0);
        var columns = panel.getJSONArray("targets").getJSONObject(0).getJSONObject("builderOptions")
                .getJSONArray("columns");
        if (columns.length()!=2)
            results.add(new Result(task,
                    "В выборке должны участвовать ровно два поля trip_distance и pickup_ntaname",
                    State.Wrong, check));
        for (int i=0; i<columns.length(); i++)
        {
            var name = columns.getJSONObject(i).getString("name");
            if (!(name.equals("trip_distance") || name.equals("pickup_ntaname")))
                results.add(new Result(task, String.format("Cреди столбцов обнаружен %s, его быть не должно", name), State.Wrong, check));

        }
        var type = panel.getString("type");
        if (!type.equals("barchart"))
            results.add(new Result(task, String.format("У графика c Clickhouse тип %s, а должен быть barchart", type),
                    State.Wrong, check));
        clickHouseChecked=true;
        return results;


    }

    private List<Result> checkDataSources(JSONArray dataSources, List<String> expectedTypes)
            throws IOException, URISyntaxException, InterruptedException {

        var results = new ArrayList<Result>();
        for (var type : expectedTypes) {
            var found = false;
            for (int i = 0; i < dataSources.length(); i++) {
                var source = dataSources.getJSONObject(i);
                var typeName = source.getString("type");
                if (typeName.equals(type)) {
                    found = true;
                    break;
                }
            }
            if (!found)
                results.add(new Result(task, String.format("Не найден источник данных %s", type), State.Wrong, check));

        }
        return results;
    }

    private boolean isGrafanaStarted() throws Exception {
        return !sshExecutor.getResult("sudo systemctl status grafana-server | grep active").isBlank();
    }

    private boolean isGrafanaInstalled() throws Exception {
        var serverUtilPath = sshExecutor.getResult("which grafana-server");
        var clientUtilPath = sshExecutor.getResult("which grafana-cli");
        return !serverUtilPath.isBlank() || !clientUtilPath.isBlank();
    }

}


