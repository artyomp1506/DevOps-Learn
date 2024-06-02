package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.entity.GitlabTaskInfo;
import com.example.demo.entity.GrafanaInfo;
import com.example.demo.entity.check_results.Result;
import com.example.demo.entity.task.Task;
import com.example.demo.entity.task.TaskTemplate;
import com.example.demo.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@RestController
public class TaskController {
    private TaskService taskService;
    private ResultService resultService;

    private UserService userService;
    private TemplateService templateService;
@Autowired
    public TaskController(TaskService taskService, ResultService resultService,  UserService userService, TemplateService templateService) {
        this.taskService = taskService;
        this.resultService = resultService;

    this.userService = userService;
    this.templateService = templateService;
}
    @PreAuthorize("hasRole('Student')")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/tasks/")
    public TaskResponse createTask(Principal principal, @RequestBody TaskDto taskDto) {
    var userId = userService.getUserFromPrincipal(principal).getId();
     var task = taskService.save(userId, taskDto.getTemplateId());
     var id = task.getId();
     var status = task.getStatus();

    return new TaskResponse(id, status);
    }
    //@PreAuthorize("hasRole('Student')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Получить все задания пользователя по его id")
    @GetMapping("/tasks/user/{id}")
    public List<Task> getTasksByUserId(@RequestParam long id) {
        return taskService.getAllBy(id);
    }
    @GetMapping("/tasks/{taskId}")
    public TaskDetailResponse getTaskById(@PathVariable long taskId)
    {
        var ipAddresses = new ArrayList<String>();
        var task = taskService.getById(taskId);
        for (var machine:task.getMachines())
            ipAddresses.add(machine.getExternalIp());
        return new TaskDetailResponse(task.getStatus(), ipAddresses);
    }
    @PostMapping("/tasks/{id}/run")
    @ResponseStatus(value = HttpStatus.OK)
    public void runTask(@PathVariable long id, @RequestBody TaskRunDto taskRunDto) {
    taskService.runTask(id, taskRunDto.getUserName(), taskRunDto.getSshKey());
    }
    @Operation(summary = "Запустить виртуальную машину по id")
    @GetMapping("/tasks/{id}/start")
    @ResponseStatus(value = HttpStatus.OK)
    public void runTask(@PathVariable long id)
    {
        taskService.startVm(id);
    }
    @Operation(summary = "Остановить виртуальную машину по id")
    @GetMapping("/tasks/{id}/stop")
    public void stopTask(@PathVariable long id)
    {
        taskService.stopVm(id);

    }
    @Operation(summary = "Удалить виртуальную машину")
    @GetMapping("/tasks/{taskId}/delete-vm/")
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteVM(@PathVariable long taskId) throws InterruptedException {
        taskService.deleteVM(taskId);
    }

    @Operation(summary = "Обновить шаблоны задач")
    @GetMapping("/update-templates")
     public List<TaskTemplate> updateTemplates()
    {
        return templateService.updateRepository();
    }
    @Operation(summary = "Получить шаблоны задач")
    @GetMapping("/get-templates")
    public List<TemplateDto> getTemplates()
    {
        var templates = new ArrayList<TemplateDto>();
        var fullTemplates = templateService.getTemplates();
        for (var template:fullTemplates)
            templates.add(new TemplateDto(template.getId(), template.getTitle()));
        return templates;
    }
    @Operation (summary = "Получить шаблон по ID")
    @GetMapping("/template/{id}")
    public TemplateDto getTemplate(@PathVariable  long id)
    {
    var template = templateService.getTemplate(id);
    return new TemplateDto(id, template.getTitle());
    }

    @PreAuthorize("hasRole('Student')")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/tasks/{id}/info")
    public List<TaskInfoDto> getTaskInfo(Principal principal, @PathVariable long id) throws IOException, ParseException {
    var outputInfo = new ArrayList<TaskInfoDto>();
    var info = taskService.getInfo(id, principal.getName());
    for (var element:info)
        outputInfo.add(new TaskInfoDto(element.getTitle(), element.getValue()));
    return outputInfo;
    }
    @GetMapping("/tasks/{taskId}/check")
    public CheckDto checkTask(@PathVariable long taskId) throws FileNotFoundException {
        var check = taskService.check(taskId);
        var outputDate = new SimpleDateFormat("dd.MM.yyyy hh:mm").format(check.getDate());
        return  new CheckDto(check.getId(), outputDate);
    }





    @GetMapping("/task/results/{taskId}")
    public List<Result> getTaskResultById(@PathVariable long taskId)
    {
        return taskService.getResultsByTaskId(taskId);
    }
    @GetMapping("/results/{checkId}")
    public List<Result> getTaskResultByCheckId(@PathVariable long checkId)
    {
        return resultService.getByCheckId(checkId);
    }
}
