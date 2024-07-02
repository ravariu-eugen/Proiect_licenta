package proiect_licenta.planner.helper;

import org.jetbrains.annotations.NotNull;
import proiect_licenta.planner.Application;
import proiect_licenta.planner.HelloController;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;

import java.io.*;
import java.net.URI;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Helper {

    /**
     * @return the public IP address of this machine
     */
    public static String myIP() {

        try {
            URI uri = URI.create("http://checkip.amazonaws.com/");
            try (BufferedReader br = new BufferedReader(new InputStreamReader(uri.toURL().openStream()))) {
                return br.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static @NotNull String getSecret(String secretName) {
        try {
            File file = new File("/run/secrets/" + secretName);
            StringBuilder sb = new StringBuilder();
            try (Scanner scanner = new Scanner(file)) {
                while (scanner.hasNextLine()) {
                    sb.append(scanner.nextLine());
                }
            }
            return sb.toString();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static @NotNull AwsCredentials getCredentials() {
        String awsAccessKey = getSecret("aws_access_key_id");
        String awsSecretKey = getSecret("aws_secret_access_key");
        return AwsBasicCredentials.create(awsAccessKey, awsSecretKey);
    }

    public static String getResourceAsString(String resource) {
        try (InputStream inputStream = Application.class.getClassLoader().getResourceAsStream(resource);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
