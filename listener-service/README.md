# The listener-service

Simple messaging consumer service.

## Build and Deploy

### Build listener-service Docker Image

```sh
./mvnw clean package
CR_NAME=gcr.io # Change this to $ACR_NAME.azurecr.io if you use Azure as Container Registry provider, where ACR_NAME your unique Azure Container registry instance (see master README)
docker build -t $CR_NAME/distributed-system-course/listener-service:latest .
docker push $CR_NAME/distributed-system-course/listener-service:latest
```

### Deploy the listener-service into K8s Cluster

```sh
kubectl apply -f ./deployment.yaml # Note you need to update deployment.yaml file with proper CR ($ACR_NAME.azurecr.io) if you use Azure CR
```

## Useful Tricks

### How to Forcibly Refresh Deployment in K8s

In order to update the deployment in K8s you need either: change the deployment object (meaning you need to increase image version each time) or delete/apply again:

```sh
kubectl delete deployment.apps/listener-service
kubectl apply -f ./deployment.yaml
```