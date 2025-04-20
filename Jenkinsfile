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

    parameters {
        booleanParam(name: 'CREATE_NETWORK_INFRASTRUCTURE', defaultValue: true, description: 'Create Network Infrastructure')
        booleanParam(name: 'CREATE_EC2_INFRASTRUCTURE', defaultValue: false, description: 'Create EC2 Infrastructure and Web Server')
        booleanParam(name: 'CREATE_EKS_INFRASTRUCTURE', defaultValue: false, description: 'Create EKS Infrastructure')
        choice(name: 'AWS_REGION', choices: ['eu-central-1'], description: 'AWS Region') 
    }

    environment {
        VPC_NAME = "Production-VPC"
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
                            gv.uploadFileToS3Bucket('eks.yml')
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
                            --region $params.AWS_REGION \
                            --capabilities CAPABILITY_NAMED_IAM \
                            --parameter-overrides S3BucketName=$S3_BUCKET_NAME VpcName=$VPC_NAME ClusterName=$EKS_CLUSTER_NAME CreateNetworkStack=$CREATE_NETWORK_INFRASTRUCTURE CreateEKSStack=$CREATE_EKS_INFRASTRUCTURE CreateEC2Stack=$CREATE_EC2_INFRASTRUCTURE
                            """
                        }
                    }
                }
            }
        }

        stage('Install AwsLoadBalancerController') {
            when {
                expression {
                    params.CREATE_EKS_INFRASTRUCTURE == true
                }
            }
            stages {
                stage('Update kubectl') {
                    steps {
                        container('awscli') {
                            withCredentials([aws(accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'AwsCredentials', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY')]) {
                                sh "aws eks update-kubeconfig --region $params.AWS_REGION --name $EKS_CLUSTER_NAME"
                            }
                        }
                    }
                }
                stage('Fetch VpcId and AwsLoadBalancerControllerRole') {
                    steps {
                        container('awscli') {
                            withCredentials([aws(accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'AwsCredentials', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY')]) {
                                script {
                                    gv.fetchVpcIdAndLoadBalancerControllerRole(STACK_NAME)
                                }
                            }
                        }
                    }
                }
                stage('Deploy AWS Load Balancer Service Account') {
                    steps {
                        container('awscli') {
                            withCredentials([aws(accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'AwsCredentials', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY')]) {
                                script {
                                    gv.deployAwsLoadBalancerServiceAccount()
                                }
                            }
                        }
                    }
                }
                stage('Check Ingress Controller') {
                    steps {
                        container('awscli') {
                            withCredentials([aws(accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'AwsCredentials', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY')]) {
                                script {
                                    gv.awsLoadBalancerControllerExists()
                                }
                            }
                        }
                    }
                }
                stage('Install Ingress Controller') {
                    when {
                        expression {
                            env.AWS_LOAD_BALANCER_CONTROLLER_EXISTS == 'false'
                        }
                    }
                    steps {
                        container('awscli') {
                            withCredentials([aws(accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'AwsCredentials', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY')]) {
                                script {
                                    gv.installAwsLoadBalancerController()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
