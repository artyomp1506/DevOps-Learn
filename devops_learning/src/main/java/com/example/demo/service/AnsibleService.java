package com.example.demo.service;

import com.example.demo.entity.AnsiblePlaybookEntity;
import com.example.demo.entity.TerraformConfiguration;
import com.example.demo.entity.check_results.Check;
import com.example.demo.entity.check_results.Result;
import com.example.demo.entity.check_results.State;
import com.example.demo.repository.AnsibleRepository;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@AllArgsConstructor
public class AnsibleService {
    private AnsibleRepository ansibleRepository;
    private TerraformService terraformService;

    public AnsiblePlaybookEntity savePlaybook(String name, String hostFileName, String mainRoleName, MultipartFile file) {
        var ansConfig = new AnsiblePlaybookEntity();
        ansibleRepository.save(ansConfig);
        if (!Files.exists(Paths.get(String.format("%s/playbooks/%d/", System.getProperty("user.dir"), ansConfig.getId()))))
            new File(String.format("%s/playbooks/%d/", System.getProperty("user.dir"), ansConfig.getId())).mkdirs();
        Path destinationFile = Paths.get(String.format("%s/playbooks/%d/%s", System.getProperty("user.dir"), ansConfig.getId(), name));
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            ansConfig.setName(name);
            ansConfig.setMainRoleName(mainRoleName);
            ansConfig.setArchivePath(destinationFile.toAbsolutePath().toString());
            ansConfig.setHostFileName(hostFileName);
            ansibleRepository.save(ansConfig);
            return ansConfig;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public AnsiblePlaybookEntity unzip(long id) {
        var ansEntity = this.ansibleRepository.findById(id).get();
        File destDir = new File(String.format("./playbooks/%d", ansEntity.getId()));

        byte[] buffer = new byte[1024];

        try {
            var zis = new ZipInputStream(new FileInputStream(ansEntity.getArchivePath()));
            var zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = new File(destDir, zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }

                    // write file content
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                zipEntry = zis.getNextEntry();
            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return ansEntity;
    }

    public AnsibleServiceOutput runPlaybook(long id, long taskId) throws IOException, InterruptedException {
        var ansEntity = this.ansibleRepository.findById(id);
        if (ansEntity.isPresent()) {
            var configuration = ansEntity.get();
            var mainRolePath = configuration.getArchivePath();
            String[] ansibleCommandParts = {
                    "ansible-playbook",
                    "-i",  configuration.getHostFileName(),
                    "/mnt/" + configuration.getMainRoleName()
            };
            String currentDir = System.getProperty("user.dir");
            String keyPathOnHost = currentDir + "/students-run/" + taskId + "/id_rsa";
            File keyFile = new File(keyPathOnHost);

            if (keyFile.exists()) {
                // Для Windows и Unix-совместимости
                if (!System.getProperty("os.name").toLowerCase().contains("win")) {
                    // Unix/Linux/Mac
                    ProcessBuilder chmod = new ProcessBuilder("chmod", "600", keyPathOnHost);
                    chmod.start().waitFor();
                } else {
                    // Windows - устанавливаем минимальные права через Java
                    keyFile.setReadable(true, true);
                    keyFile.setWritable(true, true);
                    keyFile.setExecutable(false);
                }
            }
            String fullCommand = String.format(
                    "chmod 600 /students-run/%d/id_rsa && ansible-playbook -i /students-run/%d/hosts /mnt/%s", taskId, taskId,
                    configuration.getMainRoleName()
            );
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "docker", "run",
                    "-v", String.format("./playbooks/%d/:/mnt", configuration.getId()),
                    "-v", String.format("./students-run/%d/:/students-run/%d", taskId, taskId),
                    "my-ansible",
                    "sh", "-c", fullCommand
            );
            processBuilder.redirectErrorStream(true);

//             for (String part : ansibleCommandParts) {
//                processBuilder.command().add(part);
//            }
            StringBuilder outputBuilder = new StringBuilder();
            var applyProcess = processBuilder.start();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(applyProcess.getInputStream()))) {
                String line = reader.readLine();
                while (true) {
                    if (line == null)
                        break;
                    System.out.println(line);
                    line = reader.readLine();
                    outputBuilder.append(line);
                }
                applyProcess.waitFor();
                return new AnsibleServiceOutput(configuration, outputBuilder.toString());

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }


        }
        return null;
    }


    public AnsiblePlaybookEntity makeHosts(long ansibleId, long terraformId, long taskId,  JSONArray hostsKeys) throws Exception {
        var ansibleEntity = ansibleRepository.findById(ansibleId).get();
        var terraformResult = terraformService.getOutput(terraformId);
        var hostsBuilder = new StringBuilder();
        hostsBuilder.append("[task_servers]\n");
        for (int i=0; i<hostsKeys.size(); i++) {
            var outputObject = (JSONObject) terraformResult.get(hostsKeys.get(i));
            var value = outputObject.get("value");
            hostsBuilder.append(
                    String.format(
                            "server%d ansible_host=%s ansible_user=ubuntu ansible_ssh_private_key_file=./id_rsa ansible_ssh_common_args='-o StrictHostKeyChecking=no'\n",
                            i, value));
        }
        var hostsString = hostsBuilder.toString();
        var path = String.format("./students-run/%d/hosts", taskId);
        var file = new File(path);
        File parentDir = file.getParentFile();
        if (parentDir != null) {
            parentDir.mkdirs();
        }
        var writer = new FileWriter(path);
        writer.write(hostsString);
        writer.close();
        ansibleEntity.setHostFileName(String.format("/students-run/%d/hosts", taskId));
        ansibleRepository.save(ansibleEntity);
        return ansibleEntity;

    }

    public List<AnsiblePlaybookEntity> getAll() {
        var results = new ArrayList<AnsiblePlaybookEntity>();
       for (var result: ansibleRepository.findAll())
           results.add(result);
       return results;
    }
}

