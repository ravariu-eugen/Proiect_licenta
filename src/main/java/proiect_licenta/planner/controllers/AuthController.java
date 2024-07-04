package proiect_licenta.planner.controllers;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.regions.Region;

@RestController
@RequestMapping("/auth")
public class AuthController {

	@PostMapping("/credentials")
	public ResponseEntity<String> setCredentials() {
		// TODO: implement set credentials
		return ResponseEntity.ok("setting credentials");
	}


	@GetMapping("/region")
	public ResponseEntity<String> getRegion() {
		// TODO: implement get region
		return ResponseEntity.ok("getting region");
	}
	@PostMapping("/region")
	public ResponseEntity<String> setRegion() {


		// TODO: implement set region
		return ResponseEntity.ok("setting region");
	}


}
