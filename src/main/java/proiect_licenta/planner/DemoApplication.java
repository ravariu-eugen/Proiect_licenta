package proiect_licenta.planner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import proiect_licenta.planner.helper.Helper;
import proiect_licenta.planner.manager.InstanceManager;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.model.InstanceType;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		System.out.println("Hello World!");
		System.out.println(Helper.myIP());
		InstanceManager i = new InstanceManager(Region.US_EAST_1, "ami-08a0d1e16fc3f61ea", InstanceType.T2_MICRO);
		i.cleanUp();
//		SpringApplication.run(DemoApplication.class, args);
	}


}