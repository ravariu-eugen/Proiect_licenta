package endpoints

import (
	"net/http"
	"src/manager/awsutils"

	"github.com/gin-gonic/gin"
)

// /ec2
//   - GET - returns list of ec2 instances
func EC2Endpoints(engine *gin.Engine) {

	engine.GET("/ec2", getInstances)
}

func getInstances(c *gin.Context) {
	instances, err := awsutils.ListInstances()
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"error": err.Error(),
		})
		return
	}
	c.JSON(http.StatusOK, instances)
}
