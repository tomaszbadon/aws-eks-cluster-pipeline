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
                sh "aws s3api create-bucket --bucket $S3_BUCKET_NAME"
            }
        }
      }
    }

  }
}
