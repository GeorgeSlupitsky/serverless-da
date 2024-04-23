import os
import json
import boto3
import decimal

# Initialize a DynamoDB client
dynamodb = boto3.resource('dynamodb')

# Custom JSON Encoder to handle Decimal types
class DecimalEncoder(json.JSONEncoder):
    def default(self, o):
        if isinstance(o, decimal.Decimal):
            if o % 1 == 0:
                return int(o)
            else:
                return float(o)
        return super(DecimalEncoder, self).default(o)

def handler(event, context):
    # Table name from the environment variable
    table_name = os.environ.get('PRODUCTS_TABLE')

    # Access the DynamoDB table
    table = dynamodb.Table(table_name)

    # Scan the table for all products
    response = table.scan()

    # Retrieve items from the response
    items = response['Items']

    # Format the response using the custom encoder for Decimal types
    return {
        'statusCode': 200,
        'body': json.dumps(items, cls=DecimalEncoder),
        'headers': {
            'Content-Type': 'application/json'
        }
    }
