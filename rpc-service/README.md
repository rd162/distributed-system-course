# The rpc-service

Simple gRPC service.

## Build and Deploy

### Build rpc-service Docker Image

```sh
./mvnw clean package
CR_NAME=gcr.io # Change this to $ACR_NAME.azurecr.io if you use Azure as Container Registry provider, where ACR_NAME your unique Azure Container registry instance (see master README)
docker build -t $CR_NAME/distributed-system-course/rpc-service:latest .
docker push $CR_NAME/distributed-system-course/rpc-service:latest
```

### Deploy the rpc-service into K8s Cluster

```sh
kubectl apply -f ./kube/deployment.yaml # Note you need to update deployment.yaml file with proper CR ($ACR_NAME.azurecr.io) if you use Azure CR
kubectl apply -f ./kube/service.yaml
kubectl get service rpc-service
```

## Build and Deploy with Skaffold

```sh
./mvnw clean package
skaffold run # This will publish docker images and create service in K8s

# You can simply remove the deployment and service
skaffold delete
```

## Useful Tricks

### How to Forcibly Refresh Deployment in K8s

In order to update the deployment in K8s you need either: change the deployment object (meaning you need to increase image version each time) or delete/apply again:

```sh
kubectl delete deployment.apps/rpc-service
kubectl apply -f ./deployment.yaml
```
