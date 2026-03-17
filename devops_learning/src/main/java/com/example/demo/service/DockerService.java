package com.example.demo.service;

import java.io.IOException;

public class DockerService {
    private String baseImage;
    private String name;
    public void build() throws IOException {
        var pb = new ProcessBuilder("docker", "build", "-t", name, "-f", baseImage);
        pb.start();

    }
    public void push(String registry, String registryId) throws IOException {
        var pb = new ProcessBuilder("docker", "push", String.format("%s/%s%s", registry, registryId, baseImage));
        pb.start();
    }
}