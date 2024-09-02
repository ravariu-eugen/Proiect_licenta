package proiect_licenta.planner.storage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proiect_licenta.planner.helper.ClientHelper;
import software.amazon.awssdk.core.BytesWrapper;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BucketStorage implements Storage {
	private static final Logger logger = LogManager.getLogger();
	private final S3AsyncClient client;
	private final String name;

	public BucketStorage(String bucketName) {
		this.client = ClientHelper.createS3AsyncClient(Region.US_EAST_1);
		this.name = bucketName;
	}

	public boolean bucketExists() {
		return client.headBucket(b -> b.bucket(name)).thenApply(r -> r.sdkHttpResponse().isSuccessful()).join();
	}

	public CompletableFuture<List<String>> listObjects() {
		// TODO
		return client.listObjects(b -> b.bucket(name))
				.thenApply(r -> r.contents().stream().map(S3Object::key).toList());

	}

	@Override
	public CompletableFuture<Boolean> objectExists(String objectName) {
		return objectsExist(List.of(objectName)).thenApply(List::getFirst);
	}

	@Override
	public CompletableFuture<List<Boolean>> objectsExist(List<String> objectNames) {
		return CompletableFuture.supplyAsync(() -> {
			var objects = listObjects().join();

			return objectNames.stream().map(objects::contains).toList();


		});
	}

	@Override
	public CompletableFuture<Boolean> put(String fileName, String objectName) {
		return client.putObject(
				b -> b.bucket(name).key(objectName),
				Paths.get(fileName)
		).thenApply(r -> r.sdkHttpResponse().isSuccessful());
	}

	@Override
	public CompletableFuture<Boolean> putBytes(String objectName, byte[] bytes) {
		// TODO
		return client.putObject(
				b -> b.bucket(name).key(objectName),
				AsyncRequestBody.fromBytes(bytes)
		).thenApply(r -> r.sdkHttpResponse().isSuccessful());
	}

	@Override
	public CompletableFuture<Boolean> delete(String objectName) {
		return client.deleteObject(b -> b.bucket(name).key(objectName))
				.thenApply(r -> r.sdkHttpResponse().isSuccessful());
	}

	@Override
	public CompletableFuture<Boolean> copy(String initialObjectName, String copyObjectName) {
		// TODO
		CopyObjectRequest request = CopyObjectRequest.builder()
				.sourceBucket(name).sourceKey(initialObjectName)
				.destinationBucket(name).destinationKey(copyObjectName)
				.build();
		return client.copyObject(request).thenApply(r -> r.sdkHttpResponse().isSuccessful());
	}

	@Override
	public CompletableFuture<Boolean> rename(String initialObjectName, String copyObjectName) {
		if (initialObjectName.equals(copyObjectName)) {
			return CompletableFuture.completedFuture(true);
		}

		return copy(initialObjectName, copyObjectName)
				.thenCompose(success ->
						success ? delete(initialObjectName)
								: CompletableFuture.completedFuture(false));


	}

	@Override
	public CompletableFuture<Boolean> get(String fileName, String objectName) {
		return client.getObject(
				b -> b.bucket(name).key(objectName),
				Paths.get(fileName)
		).thenApply(r -> r.sdkHttpResponse().isSuccessful());
	}

	@Override
	public CompletableFuture<byte[]> getBytes(String objectName) {
		return client.getObject(b -> b.bucket(name).key(objectName),
						AsyncResponseTransformer.toBytes())
				.thenApply(BytesWrapper::asByteArray);
	}


}
