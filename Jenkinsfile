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

        stage('S3 Bucket Check') {
            steps {
                container('awscli') {
                     withCredentials([aws(accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'AwsCredentials', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY')]) {
                        script {
                            gv.s3BucketExist();
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
                        echo env.S3_BUCKET_EXISTS
                        echo "${S3_BUCKET_EXISTS}"
                        echo "S3 Bucket Created"
                     }
                }
            }
        }

        stage('Deploy AWS Infrastructure') {
            steps {
                container('awscli') {
                    withCredentials([aws(accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'AwsCredentials', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY')]) {


                        //sh "aws s3api put-object --bucket $S3_BUCKET_NAME --key network-template.yml --body cloud-formation-scripts/network-template.yml"
                        //sh "aws s3api put-object --bucket $S3_BUCKET_NAME --key eks-cluster-roles.yml --body cloud-formation-scripts/eks-cluster-roles.yml"
                        //sh "aws s3api put-object --bucket $S3_BUCKET_NAME --key ec2-template.yml --body cloud-formation-scripts/ec2-template.yml"

                        //sh "aws cloudformation deploy --template-file ./cloud-formation-scripts/main-stack.yml --stack-name $STACK_NAME --region $AWS_DEFAULT_REGION --capabilities CAPABILITY_NAMED_IAM"

                        // sh "aws eks update-kubeconfig --region eu-central-1 --name $EKS_CLUSTER_NAME"

                        // sh 'kubectl get all'

                        // sh 'helm repo add eks https://aws.github.io/eks-charts'

                        // sh 'helm repo update eks'

                        //script {
                            // def vpcId = sh(script: "aws cloudformation describe-stacks --stack-name eks-application-cluster --query 'Stacks[0].Outputs[?OutputKey==`ApplicationEksClusterVpc`].OutputValue' --output text", returnStdout: true).trim()
                            // echo "VpcId: ${vpcId}"
                            // def loadBalancerControllerRole = sh(script: "aws cloudformation describe-stacks --stack-name eks-application-cluster --query 'Stacks[0].Outputs[?OutputKey==`LoadBalancerControllerRole`].OutputValue' --output text", returnStdout: true).trim()
                            // echo "LoadBalancerControllerRole: ${loadBalancerControllerRole}"

                            // def fileContent = readFile('./k8s/load-balancer-service-account.yml')
                            // fileContent = fileContent.replace('{{ROLE_ARN}}', 'arn:aws:iam::141643165132:role/AmazonEKSLoadBalancerControllerRole')
                            // writeFile file: './k8s/load-balancer-service-account.yml', text: "${fileContent}"
                            // echo fileContent

                            // sh(script:'kubectl apply -f ./k8s/load-balancer-service-account.yml')

                    // def statusCode = sh(script: "helm status -n kube-system $AWS_CONTROLLER_RELEASE_NAME", returnStatus: true)
                    // if (statusCode != 0) {
                    //     sh(script: "helm install $AWS_CONTROLLER_RELEASE_NAME eks/aws-load-balancer-controller -n kube-system --set clusterName=$EKS_CLUSTER_NAME --set serviceAccount.create=false --set serviceAccount.name=load-balancer-service-account --set region=$AWS_DEFAULT_REGION --set vpcId=${vpcId}")
                    //     echo 'Ingress Controller: eks/aws-load-balancer-controller was installed'
                    // } else {
                    //     echo 'Ingress Controller: eks/aws-load-balancer-controller installation was skipped'
                    // }
                    //}
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
