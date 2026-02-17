package com.example.demo.controller;

import com.example.demo.dto.TerraformCreatedDto;
import com.example.demo.dto.TerraformDto;
import com.example.demo.service.TerraformService;
import lombok.AllArgsConstructor;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@AllArgsConstructor
public class TerraformController {
    private TerraformService terraformService;

    @PostMapping(consumes = MULTIPART_FORM_DATA_VALUE, path = "/terraform/create")
    public TerraformCreatedDto create(@RequestParam String filename, @RequestPart("file")  MultipartFile file) {
    var tfConfig = terraformService.save(filename, file);
    return new TerraformCreatedDto(tfConfig.getId(), tfConfig.getName());
    }
    @GetMapping("/terraform/init/{id}")
    public TerraformCreatedDto init(@PathVariable long id) {
        var inited = terraformService.init(id);
        return new TerraformCreatedDto(inited.getId(), inited.getName());
    }
    @GetMapping("/terraform/apply/{id}")
    public TerraformDto apply( @PathVariable long id) {
        var applied = terraformService.apply(id);
        return new TerraformDto(applied.getName(), applied.getFilePath(), applied.getName());
    }
    @GetMapping("/terraform/json/{id}")
    public JSONObject getJson(@PathVariable long id) {
        try {
            var result = terraformService.getOutput(id);
            return  result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
