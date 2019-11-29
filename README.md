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

**NOTE:** after the project is created you also may need to enable billing for this project in the console <https://cloud.google.com/billing/docs/how-to/modify-project#enable_billing_for_an_existing_project>. Most of commands being run first time will prompt for this. If you experience any problem you can enable in console vy this link: <https://console.cloud.google.com/apis/library/container.googleapis.com?q=kubernetes%20engine&_ga=2.268948466.-287001566.1551209143>

##### Create K8s Cluster in GKE

**Note:** The number of cluster nodes might be different for each course task.
**Note:** For Kafka deployment there should be at least 5 nodes

```sh
CLUSTER_NAME=gcloud-cluster
gcloud container clusters create $CLUSTER_NAME --num-nodes=3 --machine-type="custom-1-1024" --disk-type="pd-standard" --disk-size="10GB" # More cheap options, but might not work: f1-micro 0.2CPU/0.6Gb, g1-small 0.5CPU 1.7Gb
gcloud container clusters get-credentials $CLUSTER_NAME # This will also update your kube config.
```

#### Delete all the Cloud Resources in GCP

In order to delete all resources at once you can delete the whole GCP project by the following command:

```sh
gcloud projects delete distributed-system-course
```

**NOTE:** Deleting the project in GCP takes unpredictable amount of time while you will not be able to use the same project name for new project. To mitigate this, you can `undelete` the project.

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
CLUSTER_NAME=AKSCluster
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

## Install Helm

[Helm](https://helm.sh/) is the cluster package manager for Kubernetes. In short it allows easily install third party services into cluster (like Kafka or RabbitMQ).

### Install Helm Client Using Package Managers (Homebrew or Scoop)

#### Install on macOS with Homebrew

```sh
brew install kubernetes-helm
```

#### Install on Windows with Scoop

**Note:** you can also install with [Chocolatey](https://chocolatey.org/).

Install Scoop itself if you didn't.
Make sure PowerShell 5 (or later, include PowerShell Core) and .NET Framework 4.5 (or later) are installed. Then run in Powershell:

```sh
iwr -useb get.scoop.sh | iex
```

When the Scoop is installed, install helm:

```sh
scoop install helm
```

### Install Helm Client Manually

If you experience problems with package managers or do not want to use them, there is instruction how to install manually the helm binary file.

<https://helm.sh/docs/using_helm/#installing-the-helm-client>

### Install Helm Server (Tiller) into the K8s Cluster

**NOTE:** Helm v3 does not use Tiller any more and does not require to perform these steps.

There are two parts to Helm: The Helm client (helm) and the Helm server (Tiller).
Tiller, the server portion of Helm, typically runs inside of your Kubernetes cluster. But for development, it can also be run locally, and configured to talk to a remote Kubernetes cluster.

**Note:** On Windows you may need to add environment variable `HELM_HOME=C:\Users\{your user id}\.helm`
**Note:** the service account is required: <https://helm.sh/docs/using_helm/#role-based-access-control>

```sh
kubectl config current-context # make sure the desired cluster is the current

# Create service account and cluster role binding for tiller
kubectl --namespace kube-system create serviceaccount tiller
kubectl create clusterrolebinding tiller --clusterrole=cluster-admin --serviceaccount=kube-system:tiller

# Install Tiller into the current cluster
helm init --service-account tiller --history-max 200

# Check the Tiller is installed
kubectl get pods --namespace kube-system | grep tiller
```

## Kubernetes Developer Tools

### Skaffold

> [Skaffold](https://skaffold.dev) handles the workflow for building, pushing and deploying your application, allowing you to focus on what matters most: writing code.

#### How to Install Skaffold

##### Install Skaffold on macOS

```sh
brew install skaffold
```

##### Install Skaffold on Windows

```sh
scoop bucket add extras
scoop install skaffold
```

##### Install Skaffold Manually

<https://skaffold.dev/docs/install/>

#### Useful Features of the Skaffold Tool

```sh
skaffold run --tail # Build and deploy into the current K8s cluster and start producing logs
skaffold delete # Delete the application and service from K8s cluster
```

Full CLI reference: <https://skaffold.dev/docs/references/cli/>

### Draft

<https://draft.sh/>

## Useful Tricks

### How to Switch Current K8s Cluster

If you are working with several clusters (like local Minikube or docker-desktop, or different cloud providers like GCP or Azure) you need to switch kubectl to point another cluster.

```sh
kubectl config current-context
kubectl config get-contexts
kubectl config use-context <Context NAME>
```

### How to get logs of the pod

```sh
kubectl get pods
kubectl logs <pod-name>
```

### kubectl Cheat Sheet

<https://kubernetes.io/docs/reference/kubectl/cheatsheet/>
