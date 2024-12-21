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
            image: tomaszbadon/awscli
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
    VPC_ID="0"
  }
  
  stages {
    stage('Deploy AWS Infrastructure') {
      steps {
        container('awscli') {
            withCredentials([aws(accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'AwsCredentials', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY')]) {

                sh "aws cloudformation deploy --template-file ./cloud-formation-scripts/main-stack.yml --stack-name $STACK_NAME --region $AWS_DEFAULT_REGION --capabilities CAPABILITY_NAMED_IAM"
            
                sh "aws cloudformation describe-stacks --stack-name eks-application-cluster --query 'Stacks[0].Outputs[?OutputKey==`ApplicationEksClusterVpc`].OutputValue' --output text"
            }
        }
      }
    }
  }

}
