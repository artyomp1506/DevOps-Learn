package com.example.demo.cloud;

import java.util.List;

public interface ICloudService {
    CloudResult create(String vmName, String userName, String sshKey, String imageId, int cores, int memory,
                       List<Long> diskSizes, String groups) throws Exception;
    CloudResult start(String vmId) throws InterruptedException;
    void stop(String vmId) throws InterruptedException;
    void delete(String vmId) throws InterruptedException;

}
