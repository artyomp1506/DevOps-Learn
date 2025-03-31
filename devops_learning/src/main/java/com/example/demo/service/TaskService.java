package com.example.demo.service;

import com.example.demo.checker.Checker;
import com.example.demo.checker.InfoGenerator;
import com.example.demo.checker.SshExecutor;
import com.example.demo.cloud.*;
import com.example.demo.dto.InputParameterDto;
import com.example.demo.entity.check_results.Check;
import com.example.demo.entity.check_results.Result;
import com.example.demo.entity.check_results.State;
import com.example.demo.entity.task.*;
import com.example.demo.entity.user.User;
import com.example.demo.repository.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class TaskService {
    private ITaskRepository taskRepository;
    private IResultRepository resultRepository;
    private ImageService imageService;
    private TemplateRepository templateRepository;
    private InfoRepository infoRepository;
    private CheckerRepository checkerRepository;
    private IUserRepository userRepository;
    private VirtualMachineRepository machineRepository;
    private ICloudService cloudService;
    private final long MOODLE_USERS_ID = -1;
    @Value("${vm_ip:localhost}")
    private String checkCommandVmIp;
    @Value("${key_path:null}")
    private String keyPath;

    @Autowired
    public TaskService(ITaskRepository taskRepository, IResultRepository resultRepository, ImageService imageService, TemplateRepository templateRepository, InfoRepository infoRepository, CheckerRepository checkerRepository, IUserRepository userRepository, VirtualMachineRepository machineRepository, ICloudService cloudService) {
        this.taskRepository = taskRepository;
        this.resultRepository = resultRepository;
        this.imageService = imageService;
        this.templateRepository = templateRepository;
        this.infoRepository = infoRepository;
        this.checkerRepository = checkerRepository;
        this.userRepository = userRepository;
        this.machineRepository = machineRepository;
        this.cloudService = cloudService;
       // init();
    }
    public void init() {
        var tasks = taskRepository.findAll();
        for (var task:tasks)
        {
            var results = getResultsByTaskId(task.getId());
            for (var result:results)
            {
                var check = result.getCheck();
                if (check.taskId==0) {
                    check.taskId = task.getId();
                    checkerRepository.save(check);
                }
            }
        }
    }
    public long save(User user, String typeName)  {
        var task = new Task(user, TaskType.valueOf(typeName));
        taskRepository.save(task);
        return task.getId();
    }
    public Task save(long userId, long templateId)
    {
        var task = new Task(userRepository.findById(userId).get(), templateRepository.findById(templateId).get());
        task.setStatus(Status.NoVm);
        taskRepository.save(task);
        return task;
    }
    public Task saveMoodle(long templateId)
    {
        var task = new Task(templateRepository.findById(templateId).get());
        task.setStatus(Status.NoVm);
        taskRepository.save(task);
        return task;
    }
    public Task saveLocal(long userId, long templateId, String[] ipAddresses) {
        var task = new Task(userRepository.findById(userId).get(), templateRepository.findById(templateId).get());
        taskRepository.save(task);
        var machines = new ArrayList<VirtualMachine>();
        for (var ipAddress:ipAddresses)
            machines.add(new VirtualMachine(null, ipAddress, null, task.getId()));
        machineRepository.saveAll(machines);
        taskRepository.save(task);
        return  task;
    }
    public Task saveCloud(long userId, long templateId) {
        var task = new Task(userRepository.findById(userId).get(), templateRepository.findById(templateId).get());
        taskRepository.save(task);
        return task;
    }
    public List<Task> getAllBy(long userId) {
        return taskRepository.findAllByUserId(userId);
    }
    public Task getById(long taskId) {
        return taskRepository.findById(taskId).orElseThrow();
    }

    public void runTask(long id, String username, String sshKey) {
        new Thread(()->{
            var task = taskRepository.findById(id).get();
            var template = task.getTemplate();
            var filePath = template.getFilePath();
            try {
                var fileObj = new JSONParser().parse(new FileReader(filePath));
                var taskObject = (JSONObject) fileObj;
                var virtualMachines = (JSONArray) taskObject.get("virtual_machines");
                var machinesForTask = getMachinesForTask(id, username, sshKey, virtualMachines);
                machineRepository.saveAll(machinesForTask);
                task.setMachines(machinesForTask);
                task.setStatus(Status.Running);
                taskRepository.save(task);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }).start();

    }

    private ArrayList<VirtualMachine> getMachinesForTask(long id, String username, String sshKey, JSONArray virtualMachines) throws Exception {
        var machinesForTask = new ArrayList<VirtualMachine>();
        for (int i = 0; i< virtualMachines.size(); i++)
        {
            var machine = (JSONObject) virtualMachines.get(i);
            var disks = convertToDiskList((JSONArray) machine.get("disks"));
            var cores = Integer.parseInt(machine.get("cores").toString());
            var memoryInGb = Integer.parseInt(machine.get("memory").toString());
            var imageId = (String) machine.get("image");
            var groups = getAdditionalGroupsParameter((JSONArray) machine.get("groups"));
            var vmDetails = runVM(i, id, username, sshKey, imageId, cores,
                    memoryInGb, disks, groups);
            machinesForTask.add(new VirtualMachine(vmDetails.getId(), vmDetails.getExternalIP(), vmDetails.getInternalIP(), id));
        }
        return machinesForTask;
    }

    private List<Long> convertToDiskList(JSONArray disks) {
        var resultsSizes = new ArrayList<Long>();
        if (disks==null)
            return resultsSizes;
        for (var size:disks)
            resultsSizes.add((Long) size);
        return resultsSizes;


    }

    private CloudResult runVM(int machineNumber, long id, String username, String sshKey, String imageId, int cores, int memory, List<Long> diskSizes, String groups) throws Exception {

        var name = String.format("vm%d-%d", id, machineNumber);
        var vm = cloudService.create(name, username, sshKey, imageId, cores, memory, diskSizes, groups);
        return vm;
    }
    public void startVm(long taskId)
    {

        var task = taskRepository.findById(taskId).get();
        new Thread(()->{
            try {
                for (var machine:task.getMachines())
                {
                    var vmInfo = cloudService.start(machine.getServiceId());
                    machine.setExternalIp(vmInfo.getExternalIP());
                    machineRepository.save(machine);
                }

                task.setStatus(Status.Running);
                taskRepository.save(task);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
    public void stopVm(long taskId)
    {
        var task = taskRepository.findById(taskId).get();
        new Thread(()->{
            try {
                for (var machine:task.getMachines())
                {
                   cloudService.stop(machine.getServiceId());
                    machine.setExternalIp(null);
                    machineRepository.save(machine);
                }
                task.setStatus(Status.Stopped);
                taskRepository.save(task);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
    public void deleteVM(long taskId) throws InterruptedException {
        new Thread(()-> {
            try {
                var task = taskRepository.findById(taskId).get();

                for (var machine:task.getMachines())
                {
                    cloudService.delete(machine.getServiceId());
                    machineRepository.delete(machine);
                }
                task.setStatus(Status.NoVm);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
        }

    public List<Result> getResultsByTaskId(long taskId)  {
        return resultRepository.getResultByTaskId(taskId);
    }
    public List<TaskInfo> getInfo(long id, String username) throws IOException, ParseException {
        var task = taskRepository.findById(id).get();
        var taskId = task.getId();
        var info = task.getInfoList();
        var template = task.getTemplate();
        if (!info.isEmpty())
            return info;
            else {
                var infoGenerator = new InfoGenerator(template.getFilePath(), username, taskId);
                var newInfo = infoGenerator.generateInfo();
                infoRepository.saveAll(newInfo);
                task.setInfoList(newInfo);
                taskRepository.save(task);
                return newInfo;
        }
    }
    public List<Check> getChecksByTaskId(long taskId) {
        return checkerRepository.getByTaskId(taskId);
    }
    public Check check(long taskId) throws FileNotFoundException {
        var check = new Check(taskId);
        checkerRepository.save(check);
        var checkId = check.getId();
        var task = taskRepository.findById(taskId).get();
        var templateInfo = task.getTemplate();
        var checkFilePath = templateInfo.getFilePath();
        var variables = new HashMap<String, String>();

        for (var info:infoRepository.findByTaskId(taskId))
            variables.put(info.getName(), info.getValue());
        JSONObject checkObject = null;
        try {
            checkObject = (JSONObject) new JSONParser().parse(new FileReader(checkFilePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        var checks = (JSONArray) checkObject.get("checks");
        new Thread(()->{
            try {
                for (var currentCheck:checks) {
                    var chkeobj = (JSONObject) currentCheck;
                    var vmIndex = (Long) chkeobj.get("vm_index");
                    for (int i=0; i<task.getMachines().size(); i++)
                    {
                        var machineIp = task.getMachines().get(i).getExternalIp();
                        var machineInternalIp = task.getMachines().get(i).getInternalIp();
                        variables.put(String.format("ip%d", i), machineIp);
                        variables.put(String.format("int_ip%d", i), machineInternalIp);
                    }
                    var sshPassword = variables.getOrDefault("ssh_password", null);
                    var sshExecutor = new SshExecutor(variables.get(String.format("ip%d", vmIndex)), "checker", sshPassword, 22, "C:\\Users\\xeo\\Desktop\\id_rsa");
                    var checker = new Checker(sshExecutor, chkeobj, variables, check, task);
                    var results = checker.makeCheck();
                    resultRepository.saveAll(results);
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
        return check;
    }
    public Check checkYandex(long taskId, String ycToken, String ycFolderId,  JSONObject inputParameters) {
        var task = taskRepository.findById(taskId).get();
        var taskCheck = new Check(taskId);
        checkerRepository.save(taskCheck);
        var template = task.getTemplate();
        var filePath = template.getFilePath();
        JSONObject checkObject = null;
        try {
            checkObject = (JSONObject) new JSONParser().parse(new FileReader(filePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        var checks = (JSONArray) checkObject.get("checks");
        var results = new ArrayList<Result>();
        new Thread(()-> {
            try {
                for (var currentCheck : checks) {
                    var chkeobj = (JSONObject) currentCheck;
                    var yandex = (JSONObject) chkeobj.get("yandex");
                    var yandexCheck = (JSONObject) yandex.get("check");

                    if (yandexCheck.containsKey("compute")) {
                        results.addAll(new YandexComputeChecker(ycToken, ycFolderId, inputParameters, task, taskCheck).checkMachine((JSONObject) yandexCheck.get("compute"), (String) inputParameters.get("vm_id")));
                    }
                    if (yandexCheck.containsKey("networks"))
                    {
                        var checker = new YandexVPCChecker(ycToken, ycFolderId, inputParameters, task, taskCheck);
                        var networks = (JSONObject) yandexCheck.get("networks");
                        var security = (JSONObject) yandexCheck.getOrDefault("security", null);
                        if (networks!=null)
                            results.addAll(checker.checkNetwork(networks));
                        else if (security!=null) {
                            results.addAll(checker.checkSecurityGroup(security));
                        }
                    }
                    if (yandexCheck.containsKey("snapshot"))
                    {
                        var snapshotChecker = new YandexSnapshotChecker(ycToken, ycFolderId, inputParameters, task, taskCheck);
                        results.add(snapshotChecker.checkSnapshot((JSONObject) yandexCheck.get("snapshot")));
                    }
                    if (yandexCheck.containsKey("load_balancer"))
                        results.addAll(new YandexLoadBalancerChecker(ycToken, ycFolderId, inputParameters, task, taskCheck).
                                checkLoadBalancer((JSONObject) yandexCheck.get("load_balancer") ));
                        

                    if (yandexCheck.containsKey("ssh")) {
                        var executor = new SshExecutor(checkCommandVmIp, "back", null, 22, keyPath);
                        results.addAll(new Checker(inputParameters, executor, null, null, taskCheck, task).
                                checkSSH((JSONArray) yandexCheck.get("ssh")));
                        if (results.isEmpty())
                            results.add(new Result(task, "Проверки консольными командами пройдены", State.Correct, taskCheck));
                    }

                }

            } catch (Exception e) {
                results.add(new Result(task, e.getMessage(), State.Wrong, taskCheck));

            }
            resultRepository.saveAll(results);
        }).start();
        return taskCheck;
    }
    public List<InputParameterDto> getInputParameters(long id) throws IOException, ParseException {
        var template = templateRepository.findById(id).get();
        var path = template.getFilePath();
        var taskObject = (JSONObject) new JSONParser().parse(new FileReader(path));
       var checks = (JSONArray) taskObject.get("checks");
       var current = (JSONObject) checks.get(0);
       return new YandexInputHandler().getInputFields(current);

    }
    private String getAdditionalGroupsParameter(JSONArray groups)
    {
        if (groups==null)
            return "";
        var builder = new StringBuilder();
        for (var groupObject:groups)
        {
            var group = (String) groupObject;
            builder.append(", ");
            builder.append(group);

        }
        return builder.toString();
    }

}
