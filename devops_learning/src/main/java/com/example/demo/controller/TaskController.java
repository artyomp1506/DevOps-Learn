package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.entity.GitlabTaskInfo;
import com.example.demo.entity.GrafanaInfo;
import com.example.demo.entity.check_results.Check;
import com.example.demo.entity.check_results.Result;
import com.example.demo.entity.task.Task;
import com.example.demo.entity.task.TaskTemplate;
import com.example.demo.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.json.simple.JSONObject;
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
   // @ExceptionHandler(RuntimeException.class)
    //public ExceptionDto handleException(RuntimeException e) {
        //return new ExceptionDto(e.getMessage());
   // }
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
    @PostMapping("/tasks/moodle")
    public TaskResponse createTaskForMoodle(@RequestBody TaskDto taskDto) {

        var task = taskService.saveMoodle(taskDto.getTemplateId());
        var id = task.getId();
        var status = task.getStatus();

        return new TaskResponse(id, status);
    }
    @GetMapping("/yandex/{templateId}")
    public List<InputParameterDto> getYandexInputParameters( @PathVariable long templateId) throws IOException, ParseException {
    return taskService.getInputParameters(templateId);
    }
    @PreAuthorize("hasRole('Student')")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/tasks/local")
    public TaskResponse createLocalTask(Principal principal, @RequestBody LocalTaskDTO taskDto) {
        var userId = userService.getUserFromPrincipal(principal).getId();
        var task = taskService.saveLocal(userId, taskDto.getTemplateId(), taskDto.getIpAddresses());
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

    @PreAuthorize("hasRole('Student') or hasRole('Teacher')")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/tasks/{id}/info")
    public List<TaskInfoDto> getTaskInfo(Principal principal, @PathVariable long id) throws IOException, ParseException {
    var outputInfo = new ArrayList<TaskInfoDto>();
    var info = taskService.getInfo(id, principal.getName());
    for (var element:info)
        outputInfo.add(new TaskInfoDto(element.getTitle(), element.getValue()));
    return outputInfo;
    }
    @Operation(summary = "Проверить задачу")
    @GetMapping("/tasks/{taskId}/check")
    public CheckDto checkTask(@PathVariable long taskId) throws FileNotFoundException {
        var check = taskService.check(taskId);
        var outputDate = new SimpleDateFormat("dd.MM.yyyy hh:mm").format(check.getDate());
        return  new CheckDto(check.getId(), outputDate);
    }





    @Operation(summary = "Получить все результаты по id задачи")
    @GetMapping("/task/results/{taskId}")
    public List<Result> getTaskResultById(@PathVariable long taskId)
    {
        return taskService.getResultsByTaskId(taskId);
    }
    @Operation(summary = "Получить все результаты по id задачи")
    @GetMapping("/task/checks/{taskId}")
    public List<CheckDto> getCheckIds(@PathVariable  long taskId) {
        var results = new ArrayList<CheckDto>();
        var checks = taskService.getChecksByTaskId(taskId);
        for (var check:checks)
            results.add(new CheckDto(check.getId(), new SimpleDateFormat("dd.MM.yyyy hh:mm").format(check.getDate())));
        return results;
    }
    @Operation(summary = "Получить все результаты по id проверки")
    @GetMapping("/results/{checkId}")
    public List<Result> getTaskResultByCheckId(@PathVariable long checkId)
    {
        return resultService.getByCheckId(checkId);
    }
    @Operation(summary = "Добавить шаблон")
    @PostMapping("/add-template")
    public void addTemplate(@RequestBody TemplateAddDto newTemplate) throws IOException, RuntimeException {
        templateService.addTemplate(newTemplate.getName(), newTemplate.getConfigBody().toJSONString().replace("\\/", "/"));
    }
    @Operation(summary = "Удалить шаблон")
    @DeleteMapping("/delete-template/{id}")
    public void deleteTemplate(@PathVariable long id)
    {
        templateService.deleteTemplate(id);
    }
    @Operation(summary = "Посмотреть содержимое шаблона по id")
    @GetMapping("/read-template/{id}")
    public JSONObject readTemplate(@PathVariable long id) throws RuntimeException {
        return templateService.readTemplate(id);
    }
    @Operation(summary = "Редактировать шаблон по id")
    @PostMapping("/edit-template/{id}")
    public void editTemplate(@RequestBody TemplateEditDto editRequest, @PathVariable long id)
    {
        templateService.editTemplate(id, editRequest.getConfigBody().toJSONString().replace("\\/", "/" ));
    }
    @Operation(summary = "Проверить задачи из Яндекса")
    @PostMapping("/yandex/check/{id}")
    public CheckDto checkYandex(@PathVariable long id, @RequestBody YandexCheckDto checkObject) {
        var check = taskService.checkYandex(id, checkObject.getYcToken(), checkObject.getYcFolderId(), checkObject.getInputParameters());
        return new CheckDto(check.getId(), new SimpleDateFormat("dd.MM.yyyy hh:mm").format(check.getDate()));
    
    }
}
