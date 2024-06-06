package awsutils

import (
	"context"
	"mime/multipart"

	"github.com/aws/aws-sdk-go-v2/aws"
	"github.com/aws/aws-sdk-go-v2/feature/s3/manager"
	"github.com/aws/aws-sdk-go-v2/service/s3"
)

func UploadToS3(bucketName, filename string, src multipart.File) (string, error) {

	client, err := GetS3Client()
	if err != nil {
		return "", err
	}
	uploader := manager.NewUploader(client)
	result, err := uploader.Upload(context.TODO(), &s3.PutObjectInput{
		Bucket: aws.String(bucketName),
		Key:    aws.String(filename),
		Body:   src,
	})
	if err != nil {
		return "", err
	}
	return result.Location, nil
}
