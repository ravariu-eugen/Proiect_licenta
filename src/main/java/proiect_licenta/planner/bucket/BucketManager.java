package proiect_licenta.planner.bucket;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;

import java.nio.ByteBuffer;

public class BucketManager {
	private final S3Client client;
	private final String name;
	public BucketManager(S3Client client, String name) {
		this.client = client;
		this.name = name;
	}

	public void createBucket() {
		client.createBucket(b -> b.bucket(name));
	}

	public void deleteBucket() {
		client.deleteBucket(b -> b.bucket(name));
	}

	public boolean bucketExists() {
		HeadBucketResponse response = client.headBucket(b -> b.bucket(name));
		return response.sdkHttpResponse().isSuccessful();
	}

	public boolean fileExists(String fileName) {
		return false; // TODO
	}

	public boolean uploadFile() {
		return false; // TODO
	}

	public void downloadFile(String fileName) {
		// TODO
	}

	public void deleteFile() {
		// TODO
	}

}
