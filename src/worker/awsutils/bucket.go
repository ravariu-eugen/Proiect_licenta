package awsutils

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"log"

	"github.com/aws/aws-sdk-go-v2/aws"
	"github.com/aws/aws-sdk-go-v2/service/s3"
)

type BucketRegion struct {
	Bucket string `json:"bucket"`
	Region string `json:"region"`
}

var validLocations = []string{

	// US East
	"us-east-1", // N. Virginia
	"us-east-2", // Ohio

	// US West
	"us-west-1", // Northern California
	"us-west-2", // Oregon

	// Asia Pacific
	"ap-south-1",     // Mumbai
	"ap-northeast-1", // Tokyo
	"ap-northeast-2", // Seoul
	"ap-northeast-3", // Osaka
	"ap-southeast-1", // Singapore
	"ap-southeast-2", // Sydney

	// Canada
	"ca-central-1", // Central

	// Europe
	"eu-central-1", // Frankfurt
	"eu-west-1",    // Ireland
	"eu-west-2",    // London
	"eu-west-3",    // Paris
	"eu-north-1",   // Stockholm

	// South America
	"sa-east-1", // Sao Paulo

}

// IsValidLocation checks if the given region is a valid AWS location.
//
// Parameters:
//   - region: a string representing the AWS region.
//
// Returns:
//   - a boolean indicating whether the region is valid or not.
func IsValidLocation(region string) bool {

	for _, v := range validLocations {
		if v == region {
			return true
		}
	}
	return false

}

func BucketLocation(client *s3.Client, bucketName string) (string, error) {

	log.Println("Bucket name: ", bucketName)

	result, err := client.HeadBucket(
		context.TODO(),
		&s3.HeadBucketInput{
			Bucket: aws.String(bucketName),
		},
	)
	if err != nil {
		return "", err
	}
	return string(*result.BucketRegion), nil
}

func ListBuckets(client *s3.Client) ([]BucketRegion, error) {

	result, err := client.ListBuckets(context.TODO(), nil)
	if err != nil {
		return nil, err
	}

	// get the region for each bucket
	var regions []BucketRegion

	log.Println("result")

	jsonData, err := json.Marshal(result)
	if err != nil {
		return nil, err
	}

	log.Println(string(jsonData))

	for _, b := range result.Buckets {

		// get bucket properties
		bucketName := *b.Name
		// get bucket region
		regionName, _ := BucketLocation(client, *b.Name)
		//if err != nil {
		//	return nil, err
		//}

		log.Println("Bucket: ", bucketName, regionName)

		regions = append(
			regions,
			BucketRegion{
				Bucket: bucketName,
				Region: regionName,
			},
		)

	}

	return regions, nil
}

func CreateBucket(client *s3.Client, region, bucketName string) (*s3.CreateBucketOutput, error) {

	// check if region and bucket name are valid
	if bucketName == "" {

		return nil, errors.New("missing bucket name")
	}
	if !IsValidLocation(region) {
		return nil, fmt.Errorf("invalid region: %s", region)
	}

	cbo, err := client.CreateBucket(
		context.TODO(),
		&s3.CreateBucketInput{
			Bucket: aws.String(bucketName),
		},
	)

	if err != nil {

		return nil, err
	}

	return cbo, nil

}
