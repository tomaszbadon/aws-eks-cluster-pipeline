def gv

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
        AWS_DEFAULT_REGION = 'eu-central-1'
        STACK_NAME = 'eks-application-cluster'
        S3_BUCKET_NAME = 'bucket-with-stacks'
        EKS_CLUSTER_NAME = 'ApplicationEksCluster'
        AWS_CONTROLLER_RELEASE_NAME = 'aws-load-balancer-controller'
    }

    stages {
        stage('Init') {
            steps {
                script {
                    gv = load './groovy/script.groovy'
                }
            }
        }

        stage('Check if S3 Bucket exists') {
            steps {
                container('awscli') {
                    withCredentials([aws(accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'AwsCredentials', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY')]) {
                        script {
                            gv.s3BucketExist()
                        }
                    }
                }
            }
        }

        stage('Create S3 Bucket') {
            when {
                expression {
                    env.S3_BUCKET_EXISTS == 'false'
                }
            }
            steps {
                container('awscli') {
                    withCredentials([aws(accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'AwsCredentials', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY')]) {
                        script {
                            gv.createS3Bucket()
                        }
                    }
                }
            }
        }

        stage('Upload Cloud Formation templates to S3 Bucket') {
            steps {
                container('awscli') {
                    withCredentials([aws(accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'AwsCredentials', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY')]) {
                        script {
                            gv.uploadFileToS3Bucket('network-template.yml')
                            gv.uploadFileToS3Bucket('eks-cluster-roles.yml')
                            gv.uploadFileToS3Bucket('ec2-template.yml')
                        }
                    }
                }
            }
        }

        stage('Deploy Cloud Formation Stack') {
            steps {
                container('awscli') {
                    withCredentials([aws(accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'AwsCredentials', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY')]) {
                        script {
                            sh """aws cloudformation deploy \
                            --template-file ./cloud-formation-scripts/main-stack.yml \
                            --stack-name $STACK_NAME \
                            --region $AWS_DEFAULT_REGION \
                            --capabilities CAPABILITY_NAMED_IAM"""
                        }
                    }
                }
            }
        }

        stage('Update kubectl') {
            steps {
                container('awscli') {
                    withCredentials([aws(accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'AwsCredentials', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY')]) {
                        sh "aws eks update-kubeconfig --region $AWS_DEFAULT_REGION --name $EKS_CLUSTER_NAME"
                    }
                }
            }
        }

        // stage('Fetch VpcId and AwsLoadBalancerControllerRole') {
        //     steps {
        //         container('awscli') {
        //             withCredentials([aws(accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'AwsCredentials', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY')]) {
        //                 script {
        //                     gv.fetchVpcIdAndLoadBalancerControllerRole()
        //                 }
        //             }
        //         }
        //     }
        // }

        // stage('Deploy AWS Load Balancer Service Account') {
        //     steps {
        //         container('awscli') {
        //             withCredentials([aws(accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'AwsCredentials', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY')]) {
        //                 script {
        //                     gv.deployAwsLoadBalancerServiceAccount()
        //                 }
        //             }
        //         }
        //     }
        // }

        // stage('Check Ingress Controller') {
        //     steps {
        //         container('awscli') {
        //             withCredentials([aws(accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'AwsCredentials', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY')]) {
        //                 script {
        //                     gv.awsLoadBalancerControllerExists()
        //                 }
        //             }
        //         }
        //     }
        // }

        // stage('Install Ingress Controller') {
        //     when {
        //         expression {
        //             env.AWS_LOAD_BALANCER_CONTROLLER_EXISTS == 'false'
        //         }
        //     }
        //     steps {
        //         container('awscli') {
        //             withCredentials([aws(accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'AwsCredentials', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY')]) {
        //                 script {
        //                     gv.installAwsLoadBalancerController()
        //                 }
        //             }
        //         }
        //     }
        // }
    }
}
