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
    def status = sh(script: command, returnStatus: true) == 0 ? 'true' : 'false'
    echo "The S3 Bucket: ${S3_BUCKET_NAME} created with status: ${status}"
}

def uploadFileToS3Bucket(file) {
    sh(script: """aws s3api put-object \
        --bucket $S3_BUCKET_NAME \
        --key network-template.yml \
        --body cloud-formation-scripts/${file}""")

}

def deployApp() {
    echo 'deplying the application...'
}

return this
