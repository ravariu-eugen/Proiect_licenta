package proiect_licenta.planner.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import proiect_licenta.planner.helper.Helper;
import proiect_licenta.planner.instance_manager.InstanceManager;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.InstanceType;

import java.util.List;


@RestController
public class HelloController {

    private static final Logger logger = LogManager.getLogger();
    @GetMapping("/")
    public String index() {

        return "Greetings from Spring Boot!";
    }


    private static Ec2Client createEC2Client(Region region) {

        AwsCredentials awsCredentials = Helper.getCredentials();
        AwsCredentialsProvider provider = StaticCredentialsProvider.create(awsCredentials);
        return Ec2Client.builder()
                .region(region).credentialsProvider(provider)
                .build();
    }
    private static final String userDataFile = "instanceUserData.txt";

    private static String getUserData() {
        return Helper.getResourceAsString(userDataFile);
    }


    @GetMapping(value = "/hello", produces = "text/json")
    public ResponseEntity<String> hello() {
        Ec2Client client = createEC2Client(Region.EU_NORTH_1);
        logger.info(client.serviceClientConfiguration().region());
        logger.info(getUserData());
        String userData = getUserData();
        String ami = "ami-01b1be742d950fb7f";
        InstanceManager instanceManager = new InstanceManager(client,
                "inst",
                InstanceType.T3_MICRO,
                ami,
                userData);
        instanceManager.createInstances(3);

        logger.info(instanceManager.getIPs());
        List<String> ips = instanceManager.getIPs();
        instanceManager.cleanUp();
        client.close();


        ObjectMapper objectMapper = new ObjectMapper();
        String json = "";
        try {
            json = objectMapper.writeValueAsString(ips);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok(json);

    }

}