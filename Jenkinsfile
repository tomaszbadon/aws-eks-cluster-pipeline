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
    S3_BUCKET_NAME="bucket-with-stacks"
  }
  
  stages {
    stage('Deploy AWS Infrastructure') {
      steps {
        container('awscli') {
            withCredentials([aws(accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'AwsCredentials', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY')]) {

                aws 'aws cloudformation deploy --template-file ./cloud-formation-scripts/main-stack.yml --stack-name eks-application-cluster  --region eu-central-1 --capabilities CAPABILITY_NAMED_IAM'
            
            }
        }
      }
    }
  }

}
