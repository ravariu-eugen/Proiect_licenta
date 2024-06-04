package main

import (
	"net/http"
	"os"

	"github.com/gin-gonic/gin"
)

// http://localhost:8080/albums

func main() {
	router := gin.Default()

	router.POST("/credentials", setAzureCredentials)

	router.Run()
}

// AzureCredentials represents the credentials for Azure.
type AzureCredentials struct {
	ClientID       string `json:"client_id"`
	ClientSecret   string `json:"client_secret"`
	TenantID       string `json:"tenant_id"`
	SubscriptionID string `json:"subscription_id"`
}

func setAzureCredentials(c *gin.Context) {
	var credentials AzureCredentials

	// Bind the JSON received in the request body to credentials
	if err := c.BindJSON(&credentials); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	// Validate the credentials
	if credentials.ClientID == "" || credentials.ClientSecret == "" || credentials.TenantID == "" || credentials.SubscriptionID == "" {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid credentials"})
		return
	}

	// Store the credentials in memory or a database
	// set in the environment
	envVars := map[string]string{
		"AZURE_CLIENT_ID":       credentials.ClientID,
		"AZURE_CLIENT_SECRET":   credentials.ClientSecret,
		"AZURE_TENANT_ID":       credentials.TenantID,
		"AZURE_SUBSCRIPTION_ID": credentials.SubscriptionID,
	}

	for name, value := range envVars {
		err := os.Setenv(name, value)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
			return
		}
	}

	c.JSON(http.StatusOK, gin.H{"message": "Credentials set successfully"})
}
