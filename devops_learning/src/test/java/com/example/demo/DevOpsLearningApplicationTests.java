package com.example.demo;

import com.example.demo.cloud.ICloudService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;

@SpringBootTest
@ContextConfiguration(classes = com.example.demo.config.Config.class)
class DevOpsLearningApplicationTests {
	private ICloudService cloudService;
	@Value("${ssh_test}")
	private String publicSSHKey;
	@Autowired
	public DevOpsLearningApplicationTests(ICloudService cloudService) {
		this.cloudService = cloudService;
	}


	@Test
	void contextLoads() {
	}
	@Test
	void createVirtualMachineCorrectly()
	{
		var groups="";
		var ubuntuImageId = "fd80bca9kcrb3ubq7eaf";
		try {
			var vm = cloudService.create("test", "test", publicSSHKey, ubuntuImageId, 2, 2, new ArrayList<>(), groups);
			Assertions.assertNotNull(vm.getExternalIP());
			cloudService.delete(vm.getId());
		} catch (Exception e) {
			Assertions.fail("Не удалось создать виртуальную машину");
		}

	}
	@Test
	void executeSSHCorrectly()
	{
		var execut
	}

}
