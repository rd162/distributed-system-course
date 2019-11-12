# Communication in Distributed Systems Course

Boilerplate for course home work.

## Cluster Deployment Instruction for Clouds (GCP and Azure)

### Google Cloud Platform (GCP)

#### GCP Prerequisites

* Create account in GCP (you also need to install GCP cli <https://cloud.google.com/sdk/install> and login there: `gcloud init`)
* Enable the following services (you will be prompted through deployment steps as well):
    1. Google Kubernetes Engine (GKE)
    2. Google Container Registry (GCR)

#### Create K8s Cluster with GKE and Initialize Local Development Environment

##### Create GCP Project

The project in GCP is the group of linked resources. All further gclod commands will work against this project. You may delete all the resources by deleting the whole project.  

```sh
gcloud projects create distributed-system-course
gcloud config set project distributed-system-course
gcloud config set compute/zone us-central1-b
gcloud auth configure-docker
```

**NOTE:** after the project is created you also may need to enable billing for this project in the console <https://cloud.google.com/billing/docs/how-to/modify-project#enable_billing_for_an_existing_project>. However, you may freely skip this instruction, because each further command that crete services with billing will prompt for that.

##### Create K8s Cluster in GKE

```sh
gcloud container clusters create distributed-system-course --num-nodes=3 # NOTE: number of nodes might be different for each course task
gcloud container clusters get-credentials distributed-system-course # This will also update your kube config.
```

#### Delete all the Cloud Resources in GCP

In order to delete all resources at once you can delete the whole GCP project by the following command:

```sh
gcloud projects delete distributed-system-course
```

**NOTE:** Deleting the project in GCP takes unpredictable amount of time while you will not be able to use the same project name for new project. To mitigate this, you can `undelete` the project at short time, then all the resources will be cleaned up there.

### Microsoft Azure

There are also deployment instruction for Azure AKS (for those who experiences problems with GCP due to billing or other problems)

#### Azure Prerequisites

* Create Azure account (you will be given free $200 annually).
* Install Azure CLI: <https://docs.microsoft.com/en-us/cli/azure/install-azure-cli?view=azure-cli-latest>

#### Create K8s Cluster with Azure AKS and Initialize Local Development Environment

With Azure you need to create the Resource Group first (this is the some kind of analogue with GCP Project entity).
**NOTE:** With Azure there is no predefined Container Registry service, so that you will need to create your own one (see below scrip).

```sh
# Create the Azure Resource Group
AZ_LOCATION=westus
az login # Login into Azure with cli (needed once)
az group create --name distributed-system-course --location $AZ_LOCATION

# Create Azure container registry
ACR_NAME=myacr162 # Warning: this name must be globally unique. Please use your own!
az acr create -g distributed-system-course -n $ACR_NAME --sku Basic --location $AZ_LOCATION
az acr login -n $ACR_NAME

# Create Azure AKS cluster
CLUSTER_NAME=MyAKSCluster
az aks create -g distributed-system-course -n $CLUSTER_NAME --node-count 3 --generate-ssh-keys --attach-acr $ACR_NAME
az aks get-credentials -g distributed-system-course -n $CLUSTER_NAME # this will also update and re-purpose your kube config to the Azure AKS cluster
```

#### Delete all the Cloud Resources in Azure

In order to delete all the created resources you can delete the whole Azure Resource Group. In contrast with GCP, the Resource Group is deleted at the same time as requested, so that you can easily re-create it by delete/create again.  

```sh
# Delete the resource gorup and all the created resources
az group delete --name distributed-system-course
```

### Deploy the Services into K8s Cluster

#### Build Services Container Images

See build instruction in the appropriate README file of each service.

#### Deploy the Services into GKE

See deployment instruction in the appropriate README file of each service.
