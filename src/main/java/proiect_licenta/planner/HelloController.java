package proiect_licenta.planner;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;

import java.util.List;


@RestController
public class HelloController {

    @GetMapping("/")
    public String index() {

        return "Greetings from Spring Boot!";
    }

    @GetMapping("/buckets")
    public String buckets() {

        Region region = Region.US_EAST_1;
        S3Client s3 = S3Client.builder()
                .region(region)
                .build();

        // List buckets
        ListBucketsRequest listBucketsRequest = ListBucketsRequest.builder().build();
        ListBucketsResponse listBucketsResponse = s3.listBuckets(listBucketsRequest);
        List<String> buckets = listBucketsResponse.buckets().stream().map(Bucket::name).toList();
        return buckets.toString();
    }

}