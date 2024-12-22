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
                    }

                    //sh "aws s3api create-bucket --bucket $S3_BUCKET_NAME --region $AWS_DEFAULT_REGION --create-bucket-configuration LocationConstraint=$AWS_DEFAULT_REGION" 



                // script {
                //         def version = sh(script: 'ps aux', returnStdout: true).trim()
                //         echo "Version: ${version}"
                // }

                //sh "aws cloudformation deploy --template-file ./cloud-formation-scripts/main-stack.yml --stack-name $STACK_NAME --region $AWS_DEFAULT_REGION --capabilities CAPABILITY_NAMED_IAM"
            
                //sh "aws cloudformation describe-stacks --stack-name eks-application-cluster --query 'Stacks[0].Outputs[?OutputKey==`ApplicationEksClusterVpc`].OutputValue' --output text"
                }
            }
        }
    }
}
