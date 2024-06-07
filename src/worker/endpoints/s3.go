package endpoints

import (
	"context"
	"fmt"
	"net/http"
	"src/manager/awsutils"

	"github.com/aws/aws-sdk-go-v2/aws"
	"github.com/aws/aws-sdk-go-v2/config"
	"github.com/aws/aws-sdk-go-v2/service/s3"
	"github.com/gin-gonic/gin"
)

// S3Endpoints defines the routes for the S3 manager.
// /s3
//   - GET - returns list of buckets
//   - POST - creates new bucket
func S3Endpoints(engine *gin.Engine) {
	engine.GET("/s3", listBuckets)
	engine.POST("/s3/:bucket", uploadFileToBucket)

	engine.POST("/s3", createBucket)
}

type UploadResult struct {
	Path string `json:"path" xml:"path"`
}

// uploadFileToBucket handles the upload of a file to a specified bucket.
//
// It expects a POST request with a form field named "file" containing the file to be uploaded.
// The bucket name is extracted from the route parameter "bucket".
// If the bucket name is missing or the file cannot be opened, a JSON response with an error message is returned.
// The file is uploaded to the specified bucket using the awsutils.UploadToS3 function.
// If the upload is successful, a JSON response with the path of the uploaded file is returned.
//
// Parameters:
//   - c: The gin context object representing the HTTP request and response.
//
// Returns:
// - None.
func uploadFileToBucket(c *gin.Context) {

	// get bucket name
	bucketName := c.Param("bucket")
	if bucketName == "" {
		c.JSON(http.StatusBadRequest, gin.H{
			"error": "Missing bucket name",
		})
		return
	}

	// get file from request
	file, err := c.FormFile("file")
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	// open file
	src, err := file.Open()
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	defer src.Close()

	// upload to bucket
	result, err := awsutils.UploadToS3(bucketName, file.Filename, src)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	// return result
	data := &UploadResult{
		Path: result,
	}
	c.JSON(http.StatusOK, data)
}

func createBucket(c *gin.Context) {
	region := c.Query("region")
	bucketName := c.Query("bucket_name")

	if region == "" || bucketName == "" {
		c.JSON(http.StatusBadRequest, gin.H{
			"error": "Missing region or bucket name",
		})
		return
	}

	cfg, err := config.LoadDefaultConfig(context.TODO(), config.WithRegion(region))
	if err != nil {
		c.JSON(http.StatusInternalServerError,
			gin.H{"error": err.Error()},
		)
		return
	}

	_, err = s3.NewFromConfig(cfg).CreateBucket(
		context.TODO(),
		&s3.CreateBucketInput{Bucket: aws.String(bucketName)},
	)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"error": err.Error(),
		})
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"message": fmt.Sprintf("Bucket %s created in region %s", bucketName, region),
	})
}

func listBuckets(c *gin.Context) {
	svc, err := awsutils.GetS3Client()
	if err != nil {
		c.JSON(http.StatusInternalServerError,
			gin.H{"error": err.Error()},
		)
		return
	}

	regions, err := awsutils.ListBuckets(svc)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"error": err.Error(),
		})
		return
	}

	c.JSON(http.StatusOK, regions)
}
