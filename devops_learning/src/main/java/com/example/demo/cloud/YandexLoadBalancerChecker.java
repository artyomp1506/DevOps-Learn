package com.example.demo.cloud;

import com.example.demo.entity.check_results.Check;
import com.example.demo.entity.task.Task;
import org.json.simple.JSONObject;
import com.example.demo.entity.check_results.Check;
import com.example.demo.entity.check_results.Result;
import com.example.demo.entity.check_results.State;

import java.util.ArrayList;
import java.util.List;

public class YandexLoadBalancerChecker extends YandexChecker {
    public YandexLoadBalancerChecker(String ycToken, String ycFolderId, JSONObject inputParameters, Task task, Check check) {
        super(ycToken, ycFolderId, inputParameters, task, check);
    }

    public List<Result> checkLoadBalancer(JSONObject checkObject) {
        var results = new ArrayList<Result>();
        var id = getValueFromInput((String) checkObject.get("id"));
       var loadBalancer = getCloudService().getLoadBalancers(getYcFolderId()).stream().
               filter(balancer->balancer.getId().equals(id)).findFirst();
       if (loadBalancer.isEmpty())
           results.add(new Result(getTask(), "Балансировщика с данным id нет", State.Wrong, getCheck()));
       var group = loadBalancer.get().getAttachedTargetGroupsList().stream().findFirst();
       if (group.get().getHealthChecksList().stream().noneMatch(healthCheck ->
               String.valueOf(healthCheck.getHttpOptions().getPort()).
                       equals(getValueFromInput((String) checkObject.get("port")))))
           results.add(new Result(getTask(), "Не задан необходимый порт", State.Wrong, getCheck()));
       var listener = loadBalancer.get().getListenersList().stream().filter(currentListener -> String.valueOf(currentListener.getPort()).equals((String) checkObject.get("listener_port"))).findFirst();
       if (listener.isEmpty())
           results.add(new Result(getTask(), "Некорректно настроен обработчик", State.Wrong, getCheck()));
       if (results.isEmpty())
           results.add(new Result(getTask(), "Балансировщик корректен", State.Correct, getCheck()));
       return results;


    }
}
