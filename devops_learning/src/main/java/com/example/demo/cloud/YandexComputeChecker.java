package com.example.demo.cloud;

import com.example.demo.entity.check_results.Check;
import com.example.demo.entity.check_results.Result;
import com.example.demo.entity.check_results.State;
import com.example.demo.entity.task.Task;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import yandex.cloud.api.compute.v1.*;

import java.util.ArrayList;
import  java.util.List;

public class YandexComputeChecker extends YandexChecker {


    public YandexComputeChecker(String ycToken, String ycFolderId, JSONObject inputParameters, Task task, Check check) {
        super(ycToken, ycFolderId, inputParameters, task, check);
    }

    public List<Result> checkMachine(JSONObject machine, String instanceId)
    {
        var result =  new ArrayList<Result>();

        var cpuValue = (JSONObject)machine.getOrDefault("cpu", null);
        var memoryValue = (JSONObject)machine.getOrDefault("memory", null);
        var instance = getCloudService().getInstance(instanceId);
        var resources = instance.getResources();
        var metadata = (JSONArray) machine.get("metadata");
        if (metadata!=null)
        result.addAll(checkMetadata(instance, metadata));
        var instanceMemory = (long) (resources.getMemory()/Math.pow(2, 30));
        if (cpuValue!=null && (long) cpuValue.get("value")!=resources.getCores())
            result.add(new Result(getTask(), (String)  cpuValue.get("fail_message"), State.Wrong, getCheck()));
        if (memoryValue!=null && (long) memoryValue.get("value")!=instanceMemory)
            result.add(new Result(getTask(), (String) memoryValue.get("fail_message"), State.Wrong, getCheck()));
        var diskValue = (JSONObject) machine.getOrDefault("disk", null);
        if (diskValue!=null) {
            var checkResult = checkDiskValue(instance, diskValue);
            if (checkResult.getState()!=State.Correct)
                result.add(checkResult);

        }
        if (result.isEmpty())
            result.add(new Result(getTask(), "ВМ корректна", State.Correct, getCheck()));
        return result;
    }
    private Result checkDiskValue(InstanceOuterClass.Instance instance, JSONObject diskValue) {
    var diskId = instance.getBootDisk().getDiskId();
    var disk = getCloudService().getDisks(this.getYcFolderId()).stream().filter(currentDisk->currentDisk.getId()==diskId).findFirst().get();
    var size = (long) (disk.getSize()/Math.pow(2, 30));
    var expectedSize = (long) diskValue.get("size");
    if (size!=expectedSize)
        return new Result(getTask(), (String) diskValue.get("fail_message"), State.Wrong, getCheck());
    return new Result(getTask(), null, State.Correct, getCheck());
    }
    private List<Result> checkMetadata(InstanceOuterClass.Instance instance, JSONArray metaData) {
        var results = new ArrayList<Result>();
        if (metaData!=null) {
            for (var checkObject: metaData)
            {
                var jsonObject = (JSONObject) checkObject;
                var value = instance.getMetadataOrDefault((String) jsonObject.get("key"), null);
                var expectedValue = jsonObject.get("value");
                var contains = (boolean) jsonObject.getOrDefault("contains", false);
                if ((contains && !value.contains((String) expectedValue)) || value.equals(expectedValue))
                    results.add(new Result(getTask(), (String) jsonObject.get("fail_message"), State.Wrong, getCheck()));
            }

        }
        if (results.isEmpty())
        results.add(new Result(getTask(), "Метаданные корректны", State.Correct, getCheck()));
        return results;
    }
}
