package proiect_licenta.planner.testrun;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class TestGen {


	private static final List<Integer> task_sizes = List.of(5, 10, 25, 50, 100);
	private static final List<Integer> config_list = List.of(1, 2, 3);
	private static final List<Integer> vcpu_list = List.of(4, 8, 16, 32);
	private static final String testFolder = "src/main/resources/joblists/tests";

	private static void writeTest(int task_size, int config, int vcpu) {
		String test_name = "test-%d-%d-%d".formatted(task_size, config, vcpu);
		String test_content = ("""
				{
				  "jobs": [
				    {
				      "name": "matmul%s",
				      "description": "description",
				      "type": "Compute",
				      "image": "matmul.zip",
				      "input": "test_matrices_split_%d.zip",
				      "shared": [
				        {
				          "file": "config%d.zip",
				          "name": "config"
				        },
				        {
				          "file": "matrix.zip",
				          "name": "matrix"
				        }
				      ],
				      "output": [
				        "output.zip"
				      ]
				    }
				  ],
				  "vcpu_count": %d
				}
				""").formatted(test_name, task_size, config, vcpu);


		String fileName = "%s\\%s.json".formatted(testFolder, test_name);

		try (FileWriter fileWriter = new FileWriter(fileName);
		     BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
			bufferedWriter.write(test_content);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	private static void generateTests() {
		for (int task_size : task_sizes)
			for (int config : config_list)
				for (int vcpu : vcpu_list)
					writeTest(task_size, config, vcpu);
	}


	public static void main(String[] args) {
		generateTests();
	}
}
