package com.example.demo.checker;



import com.example.demo.entity.check_results.Check;
import com.example.demo.entity.check_results.Result;
import com.example.demo.entity.check_results.State;
import com.example.demo.entity.task.Task;
import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class GitlabChecker {
    private Task task;
    private String ip;
    private String domain;
    private String projectName;
    private String branchName;
    private SshExecutor sshExecutor;
    private boolean httpsConfigured;
    private boolean domainConfigured;
    private Check check;
    public GitlabChecker(Task task, String ip, String domain, String projectName, String branchName, String username, Check check) {
        this.task = task;
        this.ip = ip;
        this.domain = domain;
        this.projectName = projectName;
        this.branchName = branchName;
        this.check = check;
        this.sshExecutor = new SshExecutor(ip, username, 22, "C:\\Users\\xeo\\Desktop\\id_rsa");
    }



    public List<Result> makeCheck() throws Exception {
        var results = new ArrayList<Result>();
        var fileExists = !sshExecutor.getResult("find  /etc/gitlab -maxdepth 1 -name gitlab.rb").isBlank();
        if (!fileExists) {
            results.add(new Result(task, "Не найден конфиг Gitlab (/etc/gitlab/gitlab.rb)", State.Wrong, check));
            return results;
        }
        var config = sshExecutor.getResult("sudo cat /etc/gitlab/gitlab.rb");
        var lines = config.lines().toList();
        var external_url_string = String.format("external_url 'http://%s'", domain);
        var external_url_https_string = String.format("external_url 'https://%s'", domain);
        if (lines.contains(external_url_https_string)) {
            domainConfigured = true;
            httpsConfigured = true;
        }
        else if (!lines.contains(external_url_string)) {
            var description = String.format("Домен %s не привязан к Gitlab", domain);
            results.add(new Result(task, description, State.Wrong, check));
        }
        else {
            domainConfigured = true;
        }
        if (externalNginxEnabled(lines))
        {
            results.addAll(getNginxCheckResults());
        }
        else if (!letsEncryptEnabled(lines))
        {
            //httpsConfigured = false;
            results.add(new Result(task, "LetsEncrypt не включен руками", State.Wrong, check));
        }
        results.addAll(checkApiResults());
        return results;

    }

    private List<Result> getNginxCheckResults() throws Exception {
        var results = new ArrayList<Result>();
        var nginxInstallCheck = checkNginxinstall();
        var nginxConfigCheck = checkNginxConfig();
        if (!nginxInstallCheck.equals("ok"))
            results.add(new Result(task, nginxInstallCheck, State.Wrong, check));
        if (!nginxConfigCheck.equals("ok"))
            results.add(new Result(task, nginxConfigCheck, State.Wrong, check));
        return results;
    }

    private List<Result> checkApiResults() {
        var results = new ArrayList<Result>();
        var protocol = httpsConfigured ? "https":"http";
        var ipOrDomain = domainConfigured ? domain:ip;
        var apiExecutor = new GitlabApiExecutor(protocol, ipOrDomain);
        try {
            var projectIndex = getProjectIndex(apiExecutor.getProjects());
            if (projectIndex==-1) {
                results.add(new Result(task, String.format("Нет необходимого проекта %s", projectName), State.Warning, check));
                return results;
            }
            if (!isTaskBranchWithCommitInProject(apiExecutor.getBranches(projectIndex+1)))
                results.add(new Result(task, String.format("Нет необходимой ветки %s", branchName), State.Warning, check));
        } catch (IOException | InterruptedException | RuntimeException e) {
            results.add(new Result(task, e.getMessage(), State.Wrong, check));

        }
        return results;
    }

    private boolean isTaskBranchWithCommitInProject(JSONArray branches) {
        for (int i=0; i<branches.length(); i++)
        {
            var branch = branches.getJSONObject(i);
            if (branch.getString("name").equals(branchName))
                return true;
        }
        return false;
    }

    private int getProjectIndex(JSONArray projects) {


        for (int i = 0; i < projects.length(); i++) {
            var project = projects.getJSONObject(i);
            if (project.getString("name").equals(projectName)) {
                return i;
            }
        }
        return -1;
    }

    private boolean letsEncryptEnabled(List<String> configLines) throws Exception {
        return configLines.contains("letsencrypt['enable'] = true");
    }

    private boolean externalNginxEnabled(List<String> configLines) throws Exception {
        return configLines.contains("nginx['enable'] = false");
    }
    private String checkNginxinstall() throws Exception {
        String nginxInstalled = sshExecutor.getResult("ls /etc/nginx | grep \"nginx.conf\"");
        if (nginxInstalled.isBlank())
            return "Вероятно, nginx не установлен (нет файла nginx.conf)";
        String nginxStarted = sshExecutor.getResult("sudo service nginx status | grep running");
        if (nginxStarted.isBlank())
            return "Вероятно, nginx не поднят (служба не находится в статусе running)";
        return "ok";
    }
    private String checkNginxConfig() throws Exception {
        String grepDomain = String.format("cat /etc/nginx/sites-available/default | grep %s", domain);
        String viewDomain = sshExecutor.getResult(grepDomain);
        if (viewDomain.isBlank())
            return "Домен не настроен на внешнем nginx";
        String listen443Port = sshExecutor.getResult("cat /etc/nginx/sites-available/default | grep listen 443");
        String sslCertificateString =
                sshExecutor.getResult("cat /etc/nginx/sites-available/default | grep ssl_certificate");
        String sslOn = sshExecutor.getResult("cat /etc/nginx/sites-available/default | grep ssl_on");
        if (listen443Port.isBlank() || sslCertificateString.isBlank() || sslOn.isBlank() )
        {
            httpsConfigured = false;
        }
        var checkConfig = sshExecutor.getResult("sudo nginx -t 2>&1");

        if (checkConfig.contains("failed"))
        {
            return String.format("Встроенный чекер nginx обнаружил проблемы\n %s", checkConfig);
        }
        return "ok";

    }

}
