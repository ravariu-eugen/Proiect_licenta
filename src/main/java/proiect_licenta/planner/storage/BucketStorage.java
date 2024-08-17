package proiect_licenta.planner.storage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proiect_licenta.planner.helper.ClientHelper;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;

public class BucketStorage implements Storage {
	private static final Logger logger = LogManager.getLogger();
	private final S3Client client;
	private final String name;

	public BucketStorage(String bucketName) {
		this.client = ClientHelper.createS3Client(Region.US_EAST_1);
		this.name = bucketName;
	}

	public boolean bucketExists() {
		HeadBucketResponse response = client.headBucket(b -> b.bucket(name));
		return response.sdkHttpResponse().isSuccessful();
	}

	public List<String> listObjects() {
		// TODO
		ListObjectsResponse response = client.listObjects(b -> b.bucket(name));

		return response.contents().stream().map(S3Object::key).toList();
	}

	@Override
	public boolean objectExists(String objectName) {
		return objectsExist(List.of(objectName));
	}

	@Override
	public boolean objectsExist(List<String> objectNames) {
		return new HashSet<>(listObjects()).containsAll(objectNames);
	}

	@Override
	public boolean put(String fileName, String objectName) {
		PutObjectResponse response = client.putObject(
				b -> b.bucket(name).key(objectName),
				Paths.get(fileName)
		);
		logger.info(response.sdkHttpResponse().statusCode());
		return response.sdkHttpResponse().isSuccessful();
	}

	@Override
	public boolean putBytes(String objectName, byte[] bytes) {
		// TODO
		PutObjectResponse response = client.putObject(
				b -> b.bucket(name).key(objectName),
				RequestBody.fromBytes(bytes)
		);
		logger.info(response.sdkHttpResponse().statusCode());
		return response.sdkHttpResponse().isSuccessful();
	}

	@Override
	public boolean delete(String objectName) {
		DeleteObjectResponse response = client.deleteObject(b -> b.bucket(name).key(objectName));
		logger.info(response.sdkHttpResponse().statusCode());
		return response.sdkHttpResponse().isSuccessful();
	}

	@Override
	public boolean copy(String initialObjectName, String copyObjectName) {
		// TODO
		CopyObjectRequest request = CopyObjectRequest.builder()
				.sourceBucket(name).sourceKey(initialObjectName)
				.destinationBucket(name).destinationKey(copyObjectName)
				.build();
		CopyObjectResponse response = client.copyObject(request);
		return response.sdkHttpResponse().isSuccessful();
	}

	@Override
	public boolean rename(String initialObjectName, String copyObjectName) {
		if (initialObjectName.equals(copyObjectName)) {
			return true;
		}
		if (copy(initialObjectName, copyObjectName)) {
			return delete(initialObjectName);
		}
		return false;
	}

	@Override
	public boolean get(String fileName, String objectName) {
		GetObjectResponse response = client.getObject(
				b -> b.bucket(name).key(objectName),
				Paths.get(fileName)
		);

		logger.info(response.sdkHttpResponse().statusCode());
		return response.sdkHttpResponse().isSuccessful();
	}

	@Override
	public byte[] getBytes(String objectName) {
		return client.getObjectAsBytes(b -> b.bucket(name).key(objectName)).asByteArray();
	}


}
