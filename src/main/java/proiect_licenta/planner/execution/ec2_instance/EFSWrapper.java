package proiect_licenta.planner.execution.ec2_instance;

import proiect_licenta.planner.helper.ClientHelper;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.efs.EfsClient;
import software.amazon.awssdk.services.efs.model.*;

import java.util.List;

public class EFSWrapper {


	private final EfsClient client;
	private final String name;
	private final String id;

	public EFSWrapper(Region region, String name) {
		this.client = ClientHelper.createEfsClient(region);
		this.name = name;
		Tag nameTag = Tag.builder().key("Name").value(name).build();
		CreateFileSystemResponse response = client.createFileSystem(
				req -> req.performanceMode(PerformanceMode.GENERAL_PURPOSE)
						.throughputMode(ThroughputMode.ELASTIC)
						.encrypted(true)
						.tags(nameTag)
		);

		id = response.fileSystemId();
	}

	public static void main(String[] args) {
		var efs = new EFSWrapper(Region.US_EAST_1, "test1");

		efs.delete();
		efs = new EFSWrapper(Region.US_EAST_1, "test2");
		efs.delete();

	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	public void createMountPoint(List<String> securityGroupIDs) {
		CreateMountTargetRequest request = CreateMountTargetRequest.builder()
				.fileSystemId(id)
				.securityGroups(securityGroupIDs)
				.build();


		client.createMountTarget(request);


		String command = String.format("sudo mount -t efs -o tls %s:/ /mnt/efs", id);

	}

	public void createMountPoint(String securityGroupID) {
		createMountPoint(List.of(securityGroupID));
	}

	public void delete() {
		client.deleteFileSystem(req -> req.fileSystemId(id));
	}
}
