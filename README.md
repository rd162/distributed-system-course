# distributed-system-course

Boilerplate for course tasks (Communication in Distributed Systems)

## Deployment Instruction

### Google Cloud Project (GCP)

#### GCP Prerequisites

* Create account in GCP (you also need to install GCP cli <https://cloud.google.com/sdk/install> and login there: `gcloud init`)
* Enable the following services (you will be prompted through deployment steps as well):
    1. Google Kubernetes Engine (GKE)
    1. Google Container Registry (GCR)

#### Deployment Steps

##### Create GCP Project

```sh
gcloud projects create distributed-system-course
gcloud config set project distributed-system-course
gcloud config set compute/zone us-central1-b
gcloud auth configure-docker
```

Note: after the project is created you also may need to enable billing for this project in the console <https://cloud.google.com/billing/docs/how-to/modify-project#enable_billing_for_an_existing_project>. However, you may freely skip this instruction, because each further command that crete services with billing will prompt for that.

##### Build Container Images

###### Build web-service Image

```sh
cd web-service
./mvnw clean package
docker build -t gcr.io/distributed-system-course/web-service:latest .
docker push gcr.io/distributed-system-course/web-service:latest
```

###### Build rpc-service Image

```sh
cd rpc-service
./mvnw clean package
docker build -t gcr.io/distributed-system-course/rpc-service:latest .
docker push gcr.io/distributed-system-course/rpc-service:latest
```

##### Create Cluster in GKE

```sh
gcloud container clusters create distributed-system-course --num-nodes=3
gcloud container clusters get-credentials distributed-system-course
```

##### Deploy the Services into GKE

###### Deploy web-service

```sh
cd web-service
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml
kubectl get service web-service
```

###### Deploy rpc-service

```sh
cd rpc-service
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml
kubectl get service rpc-service
```

##### Verify the Service

```sh
curl http://{EXTERNAL-IP}:60000/api/hello
```

#### Useful tricks

In order to delete all resources at once you can delete the whole GCP project and then "undelete" it after some time.
The resources will not be restored after undelete, however you will need to enable services again.

```sh
gcloud projects delete distributed-system-course
```

How to forcibly refresh deployment (if you pushed docker image, but had not changed version)?

```sh
kubectl get all
kubectl delete deployment.apps/web-service
kubectl delete deployment.apps/rpc-service
kubectl apply -f rpc-service/deployment.yaml
kubectl apply -f web-service/deployment.yaml
```

### Deployment for Azure AKS

There are also deploymnenbt instruction for Azure AKS (for those who experiences problems with GCP due to billing)

#### Azure Prerequisites

* Create Azure account (you will be given free $200 annually).
* Install Azure CLI: <https://docs.microsoft.com/en-us/cli/azure/install-azure-cli?view=azure-cli-latest>

#### Deployment with Azure CLI

```sh
# Create the Azure resource group
AZ_LOCATION=westus
az login # Login into Azure with cli (needed once)
az group create --name distributed-system-course --location $AZ_LOCATION

# Create Azure container registry
ACR_NAME=myacr162 # Warning: this name must be globally unique. Please use your own!
az acr create -g distributed-system-course -n $ACR_NAME --sku Basic --location $AZ_LOCATION
az acr login -n $ACR_NAME

# Build the services (use instructions from GCP above how to compile these services with maven)
docker build -t $ACR_NAME.azurecr.io/distributed-system-course/web-service:latest ./web-service/
docker push $ACR_NAME.azurecr.io/distributed-system-course/web-service:latest
docker build -t $ACR_NAME.azurecr.io/distributed-system-course/rpc-service:latest ./rpc-service/
docker push $ACR_NAME.azurecr.io/distributed-system-course/rpc-service:latest

# Create Azure AKS cluster
CLUSTER_NAME=AKSCluster
az aks create -g distributed-system-course -n $CLUSTER_NAME --node-count 3 --generate-ssh-keys --attach-acr $ACR_NAME
az aks get-credentials -g distributed-system-course -n $CLUSTER_NAME # this will also update and re-purpose your kube config to the Azure AKS cluster

# Deploy the web-service into cluster
kubectl apply -f ./web-service/deployment-azure.yaml
kubectl apply -f ./web-service/service.yaml
kubectl get service web-service

# Deploy the rpc-service into cluster
kubectl apply -f ./rpc-service/deployment-azure.yaml
kubectl apply -f ./rpc-service/service.yaml
kubectl get service rpc-service

# Verify the service
curl http://{EXTERNAL-IP}:60000/api/hello

# Delete the resource gorup and all the created resources
az group delete --name distributed-system-course
```
