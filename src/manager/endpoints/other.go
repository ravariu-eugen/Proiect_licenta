package endpoints

import (
	"net/http"

	"github.com/gin-gonic/gin"
)

func OtherEndpoints(engine *gin.Engine) {

	engine.GET("/", func(c *gin.Context) {
		c.String(http.StatusOK, "Hello World!")
	})

}
