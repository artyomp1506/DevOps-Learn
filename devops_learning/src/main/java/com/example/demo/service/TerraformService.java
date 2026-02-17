package com.example.demo.service;

import com.example.demo.dto.TerraformDto;
import com.example.demo.entity.TerraformConfiguration;
import com.example.demo.repository.ITerraformRepository;
import lombok.AllArgsConstructor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@AllArgsConstructor
public class TerraformService {
    public ITerraformRepository terraformRepository;

    public TerraformConfiguration save(String name, String filename, String config) {
        File file = new File(filename);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(config);
            var tfConfig = new TerraformConfiguration(name, filename);
            terraformRepository.save(tfConfig);
            return tfConfig;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public TerraformConfiguration save(String name, MultipartFile file) {

        var tfconfig = new TerraformConfiguration();
        terraformRepository.save(tfconfig);
        if (!Files.exists(Paths.get(String.format("%s/manifests/%d/", System.getProperty("user.dir"), tfconfig.getId()))))
            new File(String.format("%s/manifests/%d/", System.getProperty("user.dir"), tfconfig.getId())).mkdirs();
        Path destinationFile = Paths.get(String.format("%s/manifests/%d/%s", System.getProperty("user.dir"), tfconfig.getId(), name));
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            tfconfig.setName(name);
            tfconfig.setFilePath(destinationFile.toAbsolutePath().toString());
            terraformRepository.save(tfconfig);
            return tfconfig;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public TerraformConfiguration apply(long id) {
        var tfConfig = terraformRepository.findById(id);
        if (tfConfig.isPresent()) {

            try {

                var processApplyBuilder = new ProcessBuilder("terraform", "apply", "-auto-approve");
                processApplyBuilder.directory(Paths.get(tfConfig.get().getFilePath()).getParent().toFile());
                var applyProcess = processApplyBuilder.start();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(applyProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("[TERRAFORM] " + line);
                    }
                    applyProcess.waitFor();
                    return tfConfig.get();

                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        return null;
    }

    public TerraformConfiguration init(long id) {
        var tfConfig = terraformRepository.findById(id);
        if (tfConfig.isPresent()) {
            ProcessBuilder processBuilder = new ProcessBuilder("terraform", "init");
            try {

                processBuilder.directory(Paths.get(tfConfig.get().getFilePath()).getParent().toFile());
                System.out.println(processBuilder.directory());
                var initProcess = processBuilder.start();
                return tfConfig.get();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
    public JSONObject getOutput(long id) throws Exception {
        var output = new StringBuilder();
        var tfConfig = terraformRepository.findById(id);
        if (tfConfig.isPresent()) {
            var workingPath = Paths.get(tfConfig.get().getFilePath()).getParent().toFile();
            var outputProcessBuilder = new ProcessBuilder("terraform", "output", "-json");
            outputProcessBuilder.directory(workingPath);
            try {
                var outputProcess = outputProcessBuilder.start();
                BufferedReader br = new BufferedReader(new InputStreamReader(outputProcess.getInputStream()));
                while (true) {
                    String line = br.readLine();
                    if (line==null)
                        break;
                    output.append(line);
                    output.append("\n");
                }
                var result = output.toString();
                return (JSONObject) new JSONParser().parse(result);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        }
        throw new Exception("Unable to parse");
    }
}


