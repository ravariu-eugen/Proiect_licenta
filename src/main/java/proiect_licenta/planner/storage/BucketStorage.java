package proiect_licenta.planner.storage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proiect_licenta.planner.helper.Helper;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.*;

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class BucketStorage implements StorageInterface {
	private static final Logger logger = LogManager.getLogger();
	private final S3AsyncClient client;
	private final String name;

	public BucketStorage(String bucketName) {
		AwsCredentials awsCredentials = Helper.getCredentials();
		AwsCredentialsProvider provider = StaticCredentialsProvider.create(awsCredentials);
		this.client = S3AsyncClient.builder()
				.credentialsProvider(provider)
				.region(Region.EU_NORTH_1)
				.crossRegionAccessEnabled(true)
				.build();
		this.name = bucketName;
	}

	public boolean bucketExists() {
		CompletableFuture<HeadBucketResponse> response = client.headBucket(b -> b.bucket(name));
		return response.join().sdkHttpResponse().isSuccessful();
	}

	public List<String> listObjects() {
		// TODO
		CompletableFuture<ListObjectsResponse> response = client.listObjects(b -> b.bucket(name));

		return response.join().contents().stream().map(S3Object::key).toList();
	}

	public boolean objectExists(String objectName) {
		return objectsExist(List.of(objectName));
	}

	public boolean objectsExist(List<String> objectNames) {
		return new HashSet<>(listObjects()).containsAll(objectNames);
	}

	public boolean put(String fileName, String objectName) {
		CompletableFuture<PutObjectResponse> response = client.putObject(
				b -> b.bucket(name).key(objectName),
				AsyncRequestBody.fromFile(Paths.get(fileName))
		);
		logger.info(response.join().sdkHttpResponse().statusCode());
		return response.join().sdkHttpResponse().isSuccessful();
	}

	@Override
	public boolean delete(String objectName) {
		return false;
	}

	@Override
	public boolean copy(String initialObjectName, String copyObjectName) {
		return false;
	}

	@Override
	public boolean rename(String initialObjectName, String copyObjectName) {
		return false;
	}

	public boolean get(String fileName, String objectName) {
		// TODO
		CompletableFuture<GetObjectResponse> response = client.getObject(
				b -> b.bucket(name).key(objectName),
				AsyncResponseTransformer.toFile(Paths.get(fileName))
		);
		logger.info(response.join().sdkHttpResponse().statusCode());
		return response.join().sdkHttpResponse().isSuccessful();
	}

	public Future<Boolean> deleteObject(String objectName) {
		CompletableFuture<DeleteObjectResponse> response = client.deleteObject(b -> b.bucket(name).key(objectName));
		logger.info(response.join().sdkHttpResponse().statusCode());
		return response.thenApply(r -> r.sdkHttpResponse().isSuccessful());
	}

}
