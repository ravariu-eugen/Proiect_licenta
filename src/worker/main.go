package main

import (
	"github.com/gin-gonic/gin"
)

// http://localhost:8080/albums

func main() {
	router := gin.Default()

	router.Run()
}
