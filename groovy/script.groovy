def s3BucketExist() {
    def status = sh(script: "aws s3api head-bucket --bucket $S3_BUCKET_NAME", returnStatus: true)
    env.S3_BUCKET_EXISTS = status == 0 ? 'true' : 'false'
}

def createS3Bucket() {
    def command = """aws s3api create-bucket \
            --bucket $S3_BUCKET_NAME \
            --region $AWS_DEFAULT_REGION \
            --create-bucket-configuration \
            LocationConstraint=$AWS_DEFAULT_REGION"""
    def status = sh(script: command, returnStatus: true)
    echo "The S3 Bucket: ${S3_BUCKET_NAME} created with status: ${status}"
}

def testApp() {
    echo 'testing the application...'
}

def deployApp() {
    echo 'deplying the application...'
}

def aws_credentials = [aws(accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'AwsCredentials', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY')];

return this
