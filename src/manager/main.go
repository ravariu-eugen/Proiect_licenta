package main

import (
	"log"
	"src/manager/endpoints"

	"github.com/gin-gonic/gin"
)

func main() {
	r := gin.Default()
	r.Use(gin.Logger())

	endpoints.S3Endpoints(r)
	endpoints.EC2Endpoints(r)
	endpoints.OtherEndpoints(r)

	if err := r.Run(":8080"); err != nil {
		log.Fatal(err)
	}
}
