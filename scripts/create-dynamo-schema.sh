#!/bin/bash
# IMPORTANT: table name depends on $ENV environment variable

ENVIRONMENT=${ENV:-dev}
TABLE_NAME="playing-cards-$ENVIRONMENT"
REGION="us-west-1"

echo "Creating DynamoDB table '$TABLE_NAME' in region $REGION"
aws dynamodb create-table \
    --table-name "$TABLE_NAME" \
    --attribute-definitions AttributeName=id,AttributeType=S \
    --key-schema AttributeName=id,KeyType=HASH \
    --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5 \
    --region "$REGION"

echo "Waiting for table to become active..."
aws dynamodb wait table-exists --table-name "$TABLE_NAME" --region "$REGION"
echo "Table '$TABLE_NAME' created successfully!"

aws dynamodb tag-resource \
    --resource-arn "arn:aws:dynamodb:$REGION:$(aws sts get-caller-identity --query Account --output text):table/$TABLE_NAME" \
    --tags \
        Key=Environment,Value="$ENVIRONMENT" \
        Key=Project,Value=poker-room \
        Key=Component,Value=playing-cards \
    --region "$REGION"
echo "Tags added to table '$TABLE_NAME'"