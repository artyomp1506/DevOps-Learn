package com.example.demo;

import com.example.demo.checker.GrafanaChecker;
import com.example.demo.checker.SshExecutor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

import java.util.HashMap;

@ComponentScan
@EnableJpaRepositories
@EnableRedisRepositories
@SpringBootApplication
@EntityScan
public class DevOpsLearningApplication {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(DevOpsLearningApplication.class, args);
		//System.out.println(System.getProperty("user.dir"));
//		var isSudoUser = true;
//		var groups = isSudoUser? "    groups: sudo\n":"";
//		var sudoSettings = isSudoUser? "    sudo: 'ALL=(ALL) NOPASSWD:ALL'\n":"";
//		var userData = String.format(new StringBuilder().append("#cloud-config\n")
//				.append("users:\n")
//				.append("  - name: %s\n")
//				.append("%s")
//				.append("    shell: /bin/bash\n")
//				.append("%s")
//				.append("    ssh-authorized-keys:\n")
//				.append("      %s")
//				.toString(),
//				"userName", groups, sudoSettings, "sshKey");



	}

}
