package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class GrafanaInfo {
    @Id
    @GeneratedValue
    private long id;
    private long taskId;
    private String grafanaPassword;
    private String clickhousePassword;
    private String zabbixDataSourceName;
    private String mySqlDataSourceName;
    private String clickhouseDataSourceName;

    public GrafanaInfo(long taskId,
                       String grafanaPassword,
                       String clickhousePassword, String zabbixDataSourceName, String mySqlDataSourceName, String clickhouseDataSourceName) {
        this.taskId = taskId;
        this.grafanaPassword = grafanaPassword;
        this.clickhousePassword = clickhousePassword;
        this.zabbixDataSourceName = zabbixDataSourceName;
        this.mySqlDataSourceName = mySqlDataSourceName;
        this.clickhouseDataSourceName = clickhouseDataSourceName;
    }
}
