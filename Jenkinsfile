pipeline {
  agent {
    kubernetes {
      yaml '''
        apiVersion: v1
        kind: Pod
        metadata:
          labels:
            some-label: some-label-value
        spec:
          containers:
          - name: awscli
            image: tomaszbadon/alpine-jenkins-toolkit:221220240937
            command:
            - cat
            tty: true
        '''
      retries 2
    }
  }
  
  environment {
    AWS_DEFAULT_REGION="eu-central-1"
    STACK_NAME="eks-application-cluster"
    S3_BUCKET_NAME="bucket-with-stacks"
    EKS_CLUSTER_NAME="ApplicationEksCluster"
  }
  
  stages {
    stage('Deploy AWS Infrastructure') {
            steps {
                container('awscli') {
                    withCredentials([aws(accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'AwsCredentials', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY')]) {
                        script{
                            def status = sh(script: "aws s3api head-bucket --bucket $S3_BUCKET_NAME", returnStatus: true);
                            if(status != 0) {
                                status = sh(script:"aws s3api create-bucket --bucket $S3_BUCKET_NAME --region $AWS_DEFAULT_REGION --create-bucket-configuration LocationConstraint=$AWS_DEFAULT_REGION", returnStatus: true);
                                echo "The S3 Bucket: ${S3_BUCKET_NAME} created with status: ${status}";
                            }
                        }

                        // sh "aws s3api put-object --bucket $S3_BUCKET_NAME --key network-template.yml --body cloud-formation-scripts/network-template.yml"
                        // sh "aws s3api put-object --bucket $S3_BUCKET_NAME --key eks-cluster-roles.yml --body cloud-formation-scripts/eks-cluster-roles.yml"
                        // sh "aws s3api put-object --bucket $S3_BUCKET_NAME --key ec2-template.yml --body cloud-formation-scripts/ec2-template.yml"

                        // sh "aws cloudformation deploy --template-file ./cloud-formation-scripts/main-stack.yml --stack-name $STACK_NAME --region $AWS_DEFAULT_REGION --capabilities CAPABILITY_NAMED_IAM"

                        // sh "aws eks update-kubeconfig --region eu-central-1 --name $EKS_CLUSTER_NAME"

                        // sh "kubectl get all"

                        // sh "helm repo add eks https://aws.github.io/eks-charts"

                        // sh "helm repo update eks"

                        script {
                            def fileContent = readFile('./k8s/load-balancer-service-account.yml')
                            fileContent = fileContent.replace("{{ROLE_ARN}}", "arn:aws:iam::141643165132:role/AmazonEKSLoadBalancerControllerRole")
                            echo fileContent
                        }

                        // script {
                        //     def vpcId = sh(script: "aws cloudformation describe-stacks --stack-name eks-application-cluster --query 'Stacks[0].Outputs[?OutputKey==`ApplicationEksClusterVpc`].OutputValue' --output text", returnStdout: true).trim();
                        //     echo "VpcId: ${vpcId}"
                        //     def loadBalancerControllerRole = sh(script: "aws cloudformation describe-stacks --stack-name eks-application-cluster --query 'Stacks[0].Outputs[?OutputKey==`LoadBalancerControllerRole`].OutputValue' --output text", returnStdout: true).trim();
                        //     echo "LoadBalancerControllerRole: ${loadBalancerControllerRole}"
                        // }

                    }




                // script {
                //         def version = sh(script: 'ps aux', returnStdout: true).trim()
                //         echo "Version: ${version}"
                // }

                //sh "aws cloudformation deploy --template-file ./cloud-formation-scripts/main-stack.yml --stack-name $STACK_NAME --region $AWS_DEFAULT_REGION --capabilities CAPABILITY_NAMED_IAM"
            
                }
            }
        }
    }
}
