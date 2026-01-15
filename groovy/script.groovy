def s3BucketExist() {
    def status = sh(script: "aws s3api head-bucket --bucket $S3_BUCKET_NAME", returnStatus: true)
    env.S3_BUCKET_EXISTS = status == 0 ? 'true' : 'false'
}

def createS3Bucket() {
    def command = """aws s3api create-bucket \
            --bucket $S3_BUCKET_NAME \
            --region $params.AWS_REGION \
            --create-bucket-configuration \
            LocationConstraint=$params.AWS_REGION"""
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

def awsEfsCsiDriverExists() {
    def status = sh(script: 'helm status -n kube-system aws-efs-csi-driver', returnStatus: true)
    env.AWS_EFS_CSI_DRIVER_EXISTS = status == 0 ? 'true' : 'false'
}

def fetchVpcIdAndLoadBalancerControllerRole(stackName) {
    def vpcId = sh(script: """aws cloudformation describe-stacks \
        --stack-name $stackName \
        --query 'Stacks[0].Outputs[?OutputKey==`ApplicationEksClusterVpc`].OutputValue' \
        --output text""", returnStdout: true).trim()
    echo "vpcId: ${vpcId}"

    def loadBalancerControllerRole = sh(script: """aws cloudformation describe-stacks \
        --stack-name $stackName \
        --query 'Stacks[0].Outputs[?OutputKey==`LoadBalancerControllerRoleArn`].OutputValue' \
        --output text""", returnStdout: true).trim()
    echo "LoadBalancerControllerRole: ${loadBalancerControllerRole}"

    env.VPC_ID = vpcId
    env.LOAD_BALANCER_ROLE = loadBalancerControllerRole
}

def fetchEfsCsiRoleArn(stackName) {
    def efsCsiRoleArn = sh(script: """aws cloudformation describe-stacks \
        --stack-name $stackName \
        --query 'Stacks[0].Outputs[?OutputKey==`EFSIAMRoleArn`].OutputValue' \
        --output text""", returnStdout: true).trim()
    echo "efsCsiRoleArn: ${efsCsiRoleArn}"

    env.AWS_EFS_CSI_ROLE_ARN = efsCsiRoleArn
}

def fetchEFSFileSystemId(stackName) {
    def efsFileSystemId = sh(script: """aws cloudformation describe-stacks \
        --stack-name $stackName \
        --query 'Stacks[0].Outputs[?OutputKey==`EFSFileSystemId`].OutputValue' \
        --output text""", returnStdout: true).trim()
    echo "efsFileSystemId: ${efsFileSystemId}"

    env.EFS_FILE_SYSTEM_ID = efsFileSystemId
}

def deployAwsLoadBalancerServiceAccount() {
    def fileContent = readFile('./k8s/aws-load-balancer-controller-service-account.yml')
    fileContent = fileContent.replace('{{ROLE_ARN}}', env.LOAD_BALANCER_ROLE)
    writeFile file: './k8s/aws-load-balancer-controller-service-account.yml', text: "${fileContent}"
    echo fileContent

    sh(script:'kubectl apply -f ./k8s/aws-load-balancer-controller-service-account.yml')
}

def replaceToken(filePath, token, value) {
    def fileContent = readFile(filePath)
    fileContent = fileContent.replace(token, value)
    writeFile file: filePath, text: "${fileContent}"
    echo fileContent
}

def installAwsLoadBalancerController() {
    sh(script: 'helm repo add eks https://aws.github.io/eks-charts')
    sh(script: 'helm repo update eks')
    sh(script: """helm install $AWS_CONTROLLER_RELEASE_NAME eks/aws-load-balancer-controller \
        -n kube-system --set clusterName=$EKS_CLUSTER_NAME \
        --set serviceAccount.create=false \
        --set serviceAccount.name=aws-load-balancer-controller \
        --set region=$params.AWS_REGION \
        --set vpcId=${env.VPC_ID}""")
}

def installAwsEfsCsiDriver() {
    sh(script: 'helm repo add aws-efs-csi-driver https://kubernetes-sigs.github.io/aws-efs-csi-driver/')

    sh(script: 'helm repo update aws-efs-csi-driver')

    sh(script: """
        helm upgrade --install aws-efs-csi-driver \
        --namespace kube-system aws-efs-csi-driver/aws-efs-csi-driver \
        --set controller.serviceAccount.create=false \
        --set controller.serviceAccount.name=efs-csi-controller-sa
    """);
}

def fetchS3BucketAccessRoleArn(stackName) {
    def s3BucketAccessRole = sh(script: """aws cloudformation describe-stacks \
        --stack-name $stackName \
        --query 'Stacks[0].Outputs[?OutputKey==`S3BucketAccessRoleArn`].OutputValue' \
        --output text""", returnStdout: true).trim()
    echo "S3BucketAccessRoleArn: ${s3BucketAccessRole}"

    env.S3_BUCKET_ACCESS_ROLE = s3BucketAccessRole
}

def fetchDNSNameAndHostedZoneId() {
    def dnsName = sh(script: """
        aws elbv2 describe-load-balancers \
        --query 'LoadBalancers[?VpcId==`$VPC_ID`].[DNSName]' \
        --output text
        """, returnStdout: true).trim()

    echo "dnsName: ${dnsName}"
    env.DNS_NAME = dnsName

    def canonicalHostedZoneId = sh(script: """
        aws elbv2 describe-load-balancers \
        --query 'LoadBalancers[?VpcId==`$VPC_ID`].[CanonicalHostedZoneId]' \
        --output text
        """, returnStdout: true).trim()
        
    echo "canonicalHostedZoneId: ${canonicalHostedZoneId}"
    env.CANONICAL_HOSTED_ZONE_ID = canonicalHostedZoneId
}

return this
