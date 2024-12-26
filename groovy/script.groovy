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
        --key ${file} \
        --body cloud-formation-scripts/${file}""")
}

def awsLoadBalancerControllerExists() {
    def status = sh(script: 'helm status -n kube-system aws-load-balancer-controller', returnStatus: true)
    env.AWS_LOAD_BALANCER_CONTROLLER_EXISTS = status == 0 ? 'true' : 'false'
}

def fetchVpcIdAndLoadBalancerControllerRole() {
    def vpcId = sh(script: """aws cloudformation describe-stacks \
        --stack-name eks-application-cluster \
        --query 'Stacks[0].Outputs[?OutputKey==`ApplicationEksClusterVpc`].OutputValue' \
        --output text""", returnStdout: true).trim()
    echo "vpcId: ${vpcId}"

    def loadBalancerControllerRole = sh(script: """aws cloudformation describe-stacks \
        --stack-name eks-application-cluster \
        --query 'Stacks[0].Outputs[?OutputKey==`LoadBalancerControllerRole`].OutputValue' \
        --output text""", returnStdout: true).trim()
    echo "LoadBalancerControllerRole: ${loadBalancerControllerRole}"

    env.VPC_ID = vpcId
    env.LOAD_BALANCER_ROLE = loadBalancerControllerRole
}

def deployAwsLoadBalancerServiceAccount() {
    def fileContent = readFile('./k8s/aws-load-balancer-controller-service-account.yml')
    fileContent = fileContent.replace('{{ROLE_ARN}}', env.LOAD_BALANCER_ROLE)
    writeFile file: './k8s/aws-load-balancer-controller-service-account.yml', text: "${fileContent}"
    echo fileContent

    sh(script:'kubectl apply -f ./k8s/aws-load-balancer-controller-service-account.yml')
}

def installAwsLoadBalancerController() {
    sh(script: 'helm repo add eks https://aws.github.io/eks-charts')

    sh(script: 'helm repo update eks')

    sh(script: """helm install $AWS_CONTROLLER_RELEASE_NAME eks/aws-load-balancer-controller \
        -n kube-system --set clusterName=$EKS_CLUSTER_NAME \
        --set serviceAccount.create=false \
        --set serviceAccount.name=load-balancer-service-account \
        --set region=$AWS_DEFAULT_REGION \
        --set vpcId=${env.VPC_ID}""")
}

return this
