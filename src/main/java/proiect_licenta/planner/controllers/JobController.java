package proiect_licenta.planner.controllers;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
public class JobController {
	Logger logger = LogManager.getLogger();

	@GetMapping("/job")
	public String job() {
		logger.info("job");
		return "hello";
	}

}
