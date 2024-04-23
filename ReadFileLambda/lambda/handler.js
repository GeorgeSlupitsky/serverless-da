const AWS = require('aws-sdk');
const s3 = new AWS.S3();
const sqs = new AWS.SQS();

exports.handler = async (event) => {
    try {
        // Assuming the S3 event contains bucket name and object key
        const bucketName = event.Records[0].s3.bucket.name;
        const objectKey = event.Records[0].s3.object.key;

        // Get the object from S3
        const s3Object = await s3.getObject({ Bucket: bucketName, Key: objectKey }).promise();
        const fileContent = s3Object.Body.toString('utf-8');

        // Parse the JSON content
        const products = JSON.parse(fileContent);

        // Queue URL from environment variable
        const queueUrl = process.env.PRODUCTS_QUEUE;

        // Iterate over each product and send it as a separate message to the SQS queue
        for (const product of products) {
            const params = {
                MessageBody: JSON.stringify(product),
                QueueUrl: queueUrl
            };

            // Send message to SQS
            await sqs.sendMessage(params).promise();
            console.log('Message sent to SQS:', product);
        }

    } catch (error) {
        console.error('Error processing S3 event:', error);
        throw error;
    }
};