package com.example.demo.cloud;

import com.example.demo.entity.check_results.Check;
import com.example.demo.entity.check_results.Result;
import com.example.demo.entity.check_results.State;
import com.example.demo.entity.task.Task;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import yandex.cloud.api.vpc.v1.SecurityGroupOuterClass;

import java.util.ArrayList;
import java.util.List;

enum Direction {
    INGRESS,
    EGRESS,
}
public class YandexVPCChecker extends YandexChecker {

    public YandexVPCChecker(String ycToken, String ycFolderId, JSONObject inputParameters, Task task, Check check) {
        super(ycToken, ycFolderId, inputParameters, task, check);
    }
    public List<Result> checkNetwork(JSONObject checkObject) {
        var networks = getCloudService().getNetworks(getYcFolderId());
        var results = new ArrayList<Result>();
        var network = networks.stream().filter(currentNetwork -> currentNetwork.getId().equals(getValueFromInput((String) checkObject.get("id")))).findFirst();
        if (network.isEmpty()) {
            results.add(new Result(getTask(), "Данной сети не существует", State.Wrong, getCheck()));
            return results;
        }
        var id = network.get().getId();
        var subnets = getCloudService().getSubnets(getYcFolderId());
        var subnet = subnets.stream().filter(currentSubnet->currentSubnet.getNetworkId().equals(id)).findFirst();
        if (subnet.isEmpty())
        {
            results.add(new Result(getTask(), "Данной подсети не существует", State.Wrong, getCheck() ));
            return results;
        }
        var actualCIDR = subnet.get().getV4CidrBlocksList();
       var expectedCIDR = (String) checkObject.get("cidr");
        if (!actualCIDR.contains(expectedCIDR))
            results.add(new Result(getTask(), String.format("Подсети с адресацией %s не существует", expectedCIDR), State.Wrong, getCheck()));
        return results;
    }
    public List<Result> checkSecurityGroup(JSONObject checkObject) {
        var results = new ArrayList<Result>();
        var id = getValueFromInput((String) checkObject.get("id"));
        var groups = getCloudService().getSecurityGroups(getYcFolderId());
        var group = groups.stream().filter(currentGroup->currentGroup.getId().equals(id)).findFirst();
        if (group.isEmpty()) {
            results.add(new Result(getTask(), "Данной группы не найдено", State.Wrong, getCheck()));
        }
        else {
            results.addAll(checkRules(checkObject, group.get(), Direction.EGRESS));
            results.addAll(checkRules(checkObject, group.get(), Direction.INGRESS));
        }
        if (results.isEmpty())
            results.add(new Result(getTask(), String.format("Группа безопасности с id = %s корректна", id), State.Correct, getCheck()));
        return results;
    }

    private List<Result> checkRules(JSONObject checkObject, SecurityGroupOuterClass.SecurityGroup group, Direction direction) {
        var results = new ArrayList<Result>();
        var rules = (JSONObject) checkObject.get(String.valueOf(direction).toLowerCase());
        var expectedCidr = (String) rules.get("cidr");
        if (expectedCidr!=null) {
            var ruleWithCidr = group.getRulesList().stream().filter(rule->rule.getCidrBlocks().getV4CidrBlocksList().
                    contains(expectedCidr)).toList();
            if (ruleWithCidr.isEmpty())
                results.add(new Result(getTask(), "Нет правил, удовлетворяющих условиям (cidr)", State.Wrong, getCheck()));
            var ports = (JSONArray) checkObject.get("ports");
            for (var port:ports) {
                if (ruleWithCidr.stream().noneMatch(rule->rule.getPorts().getFromPort()==(long) port &&
                        rule.getPorts().getToPort()==(long) port))
                    results.add(new Result(getTask(), String.format("Нет правил для порта %d", (long) port), State.Wrong, getCheck()));

            }
        }

        return results;
    }
}
