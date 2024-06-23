package proiect_licenta.planner.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;

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
}
