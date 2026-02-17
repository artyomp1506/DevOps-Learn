package com.example.demo.controller;

import com.example.demo.dto.AnsibleMakeHostsDto;
import com.example.demo.entity.AnsiblePlaybookEntity;
import com.example.demo.entity.check_results.Check;
import com.example.demo.entity.check_results.Result;
import com.example.demo.service.AnsibleCheckService;
import com.example.demo.service.AnsibleService;
import com.example.demo.service.AnsibleServiceOutput;
import com.example.demo.service.TerraformService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@AllArgsConstructor
public class AnsibleController {
    private AnsibleService ansibleService;
    private AnsibleCheckService ansibleCheckService;
    @PostMapping(consumes = MULTIPART_FORM_DATA_VALUE, path = "/ansible/upload")
    public AnsiblePlaybookEntity upload(@RequestParam String name, @RequestParam String hostFileName, @RequestParam String mainRoleName, @RequestPart("file") MultipartFile multipartFile) {
    return this.ansibleService.savePlaybook(name, hostFileName, mainRoleName, multipartFile);
    }
    @GetMapping("/ansible/unzip/{id}")
    public AnsiblePlaybookEntity unzip(@PathVariable long id) {
        return this.ansibleService.unzip(id);
    }
    @GetMapping("/ansible/run/{id}")
    public AnsiblePlaybookEntity run(@PathVariable long id, @RequestParam long taskId) {
        try {
            return this.ansibleService.runPlaybook(id, taskId).getEntity();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("/ansible/make-hosts")
    public AnsiblePlaybookEntity makeHostsFromTerraform(@RequestBody AnsibleMakeHostsDto dto) throws Exception {
        return this.ansibleService.makeHosts(dto.getAnsibleId(), dto.getTerraformId(), dto.getTaskId(), dto.getHostKeys());
    }
    @PostMapping("/ansible/check")
    public Check check(long playbookId, long taskId) throws IOException, InterruptedException {
        return this.ansibleCheckService.checkTask(playbookId, taskId);
    }
    @GetMapping("/ansible/list")
    public List<AnsiblePlaybookEntity> listPlaybooks() {
        return this.ansibleService.getAll();
    }

}
