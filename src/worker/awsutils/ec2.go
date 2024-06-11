package awsutils

import (
	"context"

	"github.com/aws/aws-sdk-go-v2/aws"
	"github.com/aws/aws-sdk-go-v2/service/ec2"
)

func ListInstances() ([]string, error) {
	c, err := GetEC2Client()
	if err != nil {
		return nil, err
	}
	instances, err := c.DescribeInstances(
		context.TODO(),
		&ec2.DescribeInstancesInput{
			MaxResults: aws.Int32(100),
		},
	)
	if err != nil {
		return nil, err
	}
	var instanceIds []string
	for _, reservation := range instances.Reservations {
		for _, instance := range reservation.Instances {
			instanceIds = append(instanceIds, aws.ToString(instance.InstanceId))
		}
	}
	return instanceIds, nil
}
