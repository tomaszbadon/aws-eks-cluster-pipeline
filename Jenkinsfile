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
                sh "aws s3api create-bucket --bucket $S3_BUCKET_NAME --region $AWS_DEFAULT_REGION --create-bucket-configuration LocationConstraint=$AWS_DEFAULT_REGION" 
                sh "aws s3api put-object --bucket $S3_BUCKET_NAME --key main-stack.yml --body cloud-formation-scripts/main-stack.yml"
                sh "aws s3api put-object --bucket $S3_BUCKET_NAME --key network-template.yml --body cloud-formation-scripts/network-template.yml"
                sh "aws s3api put-object --bucket $S3_BUCKET_NAME --key eks-cluster-roles.yml --body cloud-formation-scripts/eks-cluster-roles.yml"
                sh "aws s3api put-object --bucket $S3_BUCKET_NAME --key ec2-template.yml --body cloud-formation-scripts/ec2-template.yml"
            }
        }
      }
    }

    stage('S3 Bucket Removal') {
        steps {
            container('awscli') {
                withCredentials([aws(accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'AwsCredentials', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY')]) {
                    //sh "aws s3 rb --force s3://$S3_BUCKET_NAME --region $AWS_DEFAULT_REGION"
                }
            }
        }
    }

  }

}
