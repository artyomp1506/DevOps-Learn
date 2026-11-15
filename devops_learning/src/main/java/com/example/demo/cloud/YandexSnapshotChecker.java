package com.example.demo.cloud;

import com.example.demo.entity.check_results.Check;
import com.example.demo.entity.check_results.Result;
import com.example.demo.entity.check_results.State;
import com.example.demo.entity.task.Task;
import org.json.simple.JSONObject;
import java.util.NoSuchElementException;

public class YandexSnapshotChecker extends YandexChecker {


    public YandexSnapshotChecker(String ycToken, String ycFolderId, JSONObject inputParameters, Task task, Check check) {
        super(ycToken, ycFolderId, inputParameters, task, check);
    }

    public Result checkSnapshot(JSONObject snapShotObject) {
        var snapShotId = getValueFromInput((String) snapShotObject.getOrDefault("id", null));
        var vmId = getValueFromInput((String) snapShotObject.getOrDefault("vm_id", null));
        if (snapShotId!=null && vmId!=null) {
        var disks = getCloudService().getDisks(getYcFolderId());
        var instance = getCloudService().getInstances(getYcFolderId()).stream()
                .filter(in -> in.getId().equals(vmId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("ВМ не найдена с id: " + vmId));
        var selectedDiskId = instance.getBootDisk().getDiskId();
        var disk = disks.stream()
                .filter(currentDisk -> currentDisk.getId().equals(selectedDiskId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Диск не найден для id: " + selectedDiskId));
            var sourceSnapshotId = disk.getSourceSnapshotId();
            if (sourceSnapshotId!=snapShotId)
                return new Result(getTask(), (String) snapShotObject.get("fail_message"), State.Wrong, getCheck());


        }
        return new Result(getTask(),"Указаны неверные id", State.Wrong, getCheck());
    }
}
