package proiect_licenta.planner.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import proiect_licenta.planner.helper.Helper;
import proiect_licenta.planner.execution.ec2_instance.EC2InstanceManager;
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








    @GetMapping(value = "/hello", produces = "text/json")
    public ResponseEntity<String> hello() {

        return ResponseEntity.ok("hello");
    }



}