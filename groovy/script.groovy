def s3BucketExist() {
    def status = sh(script: "aws s3api head-bucket --bucket $S3_BUCKET_NAME", returnStatus: true)
    if (status != 0) {
        status = sh(script:"aws s3api create-bucket --bucket $S3_BUCKET_NAME --region $AWS_DEFAULT_REGION --create-bucket-configuration LocationConstraint=$AWS_DEFAULT_REGION", returnStatus: true)
        echo "The S3 Bucket: ${S3_BUCKET_NAME} created with status: ${status}"
        env.S3_BUCKET_EXISTS = 'false'
    } else {
        env.S3_BUCKET_EXISTS = 'true'
    }
}

def testApp() {
    echo 'testing the application...'
} 

def deployApp() {
    echo 'deplying the application...'
} 

return this
