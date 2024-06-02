package com.example.demo.cloud;

import yandex.cloud.api.compute.v1.*;
import yandex.cloud.api.compute.v1.InstanceServiceGrpc.InstanceServiceBlockingStub;
import yandex.cloud.api.compute.v1.InstanceServiceOuterClass.AttachedDiskSpec;
import yandex.cloud.api.compute.v1.InstanceServiceOuterClass.AttachedDiskSpec.DiskSpec;
import yandex.cloud.api.compute.v1.InstanceServiceOuterClass.CreateInstanceMetadata;
import yandex.cloud.api.compute.v1.InstanceServiceOuterClass.CreateInstanceRequest;
import yandex.cloud.api.compute.v1.InstanceServiceOuterClass.DeleteInstanceRequest;
import yandex.cloud.api.compute.v1.InstanceServiceOuterClass.ListInstancesRequest;
import yandex.cloud.api.compute.v1.InstanceServiceOuterClass.NetworkInterfaceSpec;
import yandex.cloud.api.compute.v1.InstanceServiceOuterClass.PrimaryAddressSpec;
import yandex.cloud.api.compute.v1.InstanceServiceOuterClass.OneToOneNatSpec;
import yandex.cloud.api.compute.v1.InstanceServiceOuterClass.ResourcesSpec;
import yandex.cloud.api.compute.v1.DiskServiceGrpc.DiskServiceBlockingStub;
import yandex.cloud.api.operation.OperationOuterClass.Operation;
import yandex.cloud.api.operation.OperationServiceGrpc;
import yandex.cloud.api.operation.OperationServiceGrpc.OperationServiceBlockingStub;
import yandex.cloud.sdk.ServiceFactory;
import yandex.cloud.sdk.Zone;
import yandex.cloud.sdk.auth.Auth;
import yandex.cloud.sdk.utils.OperationUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloudService implements ICloudService {
    private  final String ycFolderId;
    private  final String  ycSubnetBId;
    private final InstanceServiceBlockingStub instanceService;
    private final OperationServiceBlockingStub operationService;
    private final DiskServiceBlockingStub diskService;
    public CloudService(String ycFolderId, String ycSubnetBId)
    {

        this.ycFolderId = ycFolderId;
        this.ycSubnetBId = ycSubnetBId;
        ServiceFactory factory = ServiceFactory.builder()
                .credentialProvider(Auth.oauthTokenBuilder().fromEnv("YC_TOKEN"))
                .requestTimeout(Duration.ofMinutes(1))
                .build();
        instanceService = factory.create(InstanceServiceBlockingStub.class, InstanceServiceGrpc::newBlockingStub);
        operationService = factory.create(OperationServiceBlockingStub.class, OperationServiceGrpc::newBlockingStub);
        diskService = factory.create(DiskServiceGrpc.DiskServiceBlockingStub.class, DiskServiceGrpc::newBlockingStub);
    }

    public  CloudResult create(String vmName, String userName, String sshKey, String imageId, int cores, int memory,
                               List<Long> diskSizes, String groups) throws Exception {
    var metadata = new HashMap<String, String>();
    var userData = String.format("#cloud-config\nusers:\n  - name: %s\n    groups: sudo%s\n    shell: /bin/bash\n" +
            "    sudo: 'ALL=(ALL) NOPASSWD:ALL'\n    ssh-authorized-keys:\n      %s", userName, groups,  sshKey);
    metadata.put("user-data", userData);
    metadata.put("hostname", vmName);
    metadata.put("local-hostname", vmName);

        var vmCores = (long) cores;
        var vmMemory = (long)memory*1024*1024*1024;
        Operation createOperation = instanceService.create(buildCreateInstanceRequest(imageId, vmName, metadata, vmCores,
                vmMemory, diskSizes));
        var createInstanceMetadata = createOperation.getMetadata().unpack(CreateInstanceMetadata.class);
        OperationUtils.wait(operationService, createOperation, Duration.ofMinutes(2));
        var id = createInstanceMetadata.getInstanceId();
        return getCloudResult(id);
    }



    public CloudResult start(String vmId) throws InterruptedException {
    Operation startOperation = instanceService.start(buildStartInstanceRequest(vmId));
    OperationUtils.wait(operationService, startOperation, Duration.ofMinutes(5));
    return getCloudResult(vmId);
}
public void stop(String vmId) throws InterruptedException {
    Operation stopOperation = instanceService.stop(buildStopInstanceRequest(vmId));
    OperationUtils.wait(operationService, stopOperation, Duration.ofMinutes(5));
}
    public void delete(String vmId) throws InterruptedException {
        var instance = instanceService.list(buildListInstancesRequest()).getInstancesList()
                .stream()
                .filter(currentInstance->currentInstance.getId().equals(vmId))
                .findFirst().get();
        var diskId = instance.getBootDisk().getDiskId();
        Operation deleteVmOperation = instanceService.delete(buildDeleteInstanceRequest(vmId));
        OperationUtils.wait(operationService, deleteVmOperation, Duration.ofMinutes(2));
        Operation deleteDiskOperation = diskService.delete(buildDiskDeleteRequest(diskId));
        OperationUtils.wait(operationService, deleteDiskOperation, Duration.ofMinutes(2));
    }
    



    private  CreateInstanceRequest buildCreateInstanceRequest(String imageId, String name, Map<String,String> metadata, long vmCores, long vmMemory, List<Long> diskSizes) {
        var request= CreateInstanceRequest.newBuilder()
                .setFolderId(ycFolderId)
                .setName(name)
                .setZoneId(Zone.RU_CENTRAL1_B.getId())
                .setPlatformId("standard-v3")
                .setResourcesSpec(ResourcesSpec.newBuilder().setCores(vmCores).setMemory(vmMemory))
                .putAllMetadata(metadata)
                .setBootDiskSpec(AttachedDiskSpec.newBuilder()
                        .setDiskSpec(DiskSpec.newBuilder()
                                .setTypeId("network-ssd")
                                .setImageId(imageId)
                                .setSize(20L * 1024 * 1024 * 1024)))

                .addNetworkInterfaceSpecs(NetworkInterfaceSpec.newBuilder()
                        .setSubnetId(ycSubnetBId)
                        .setPrimaryV4AddressSpec(PrimaryAddressSpec.getDefaultInstance())
                        .setPrimaryV4AddressSpec(PrimaryAddressSpec.newBuilder()
                                .setOneToOneNatSpec(
                                OneToOneNatSpec.newBuilder()
                                        .setIpVersion(InstanceOuterClass.IpVersion.IPV4)
                                        .build()
                        ).build())
                );
        for (var size:diskSizes)
            request.addSecondaryDiskSpecs(AttachedDiskSpec.newBuilder().setDiskSpec(DiskSpec.newBuilder()
                                    .setSize(size*1024*1024*1024)
                                    .setTypeId("network-hdd")
                    .build())
                    .build());
                return request.build();
    }

    private  ListInstancesRequest buildListInstancesRequest() {
        return ListInstancesRequest.newBuilder().setFolderId(ycFolderId).build();
    }

    private  DeleteInstanceRequest buildDeleteInstanceRequest(String instanceId) {
        return DeleteInstanceRequest.newBuilder().setInstanceId(instanceId).build();
    }
    private DiskServiceOuterClass.DeleteDiskRequest buildDiskDeleteRequest(String diskId) {
        return DiskServiceOuterClass.DeleteDiskRequest.newBuilder().setDiskId(diskId).build();
    }

    private InstanceServiceOuterClass.StopInstanceRequest buildStopInstanceRequest(String vmId)
    {
        return InstanceServiceOuterClass.StopInstanceRequest.newBuilder().setInstanceId(vmId).build();
    }
    private InstanceServiceOuterClass.StartInstanceRequest buildStartInstanceRequest(String vmId)
    {
        return InstanceServiceOuterClass.StartInstanceRequest.newBuilder().setInstanceId(vmId).build();
    }
    private CloudResult getCloudResult(String id) {
        var  ready_instance = instanceService.list(buildListInstancesRequest()).getInstancesList()
                .stream().filter(instance -> instance.getId().equals(id)).findFirst();
        var network_interface = ready_instance.get().getNetworkInterfacesList().stream().findFirst().get();
        var internal_ip = network_interface.getPrimaryV4Address().getAddress();
        var external_ip = network_interface.getPrimaryV4Address().getOneToOneNat().getAddress();
        return new CloudResult(external_ip, internal_ip, id);
    }
}