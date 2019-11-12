# The rpc-service

Simple Spring Boot web service with one endpoint.

## Build and Deploy

### Build rpc-service Docker Image

```sh
./mvnw clean package
docker build -t gcr.io/distributed-system-course/rpc-service:latest .
docker push gcr.io/distributed-system-course/rpc-service:latest
```

### Deploy the rpc-service into K8s Cluster

```sh
kubectl apply -f ./deployment.yaml
kubectl apply -f ./service.yaml
kubectl get service rpc-service
```

## Useful Tricks

### How to Forcibly Refresh Deployment in K8s

In order to update the deployment in K8s you need either: change the deployment object (meaning you need to increase image version each time) or delete/apply again:

```sh
kubectl get all
kubectl delete deployment.apps/rpc-service
kubectl apply -f ./deployment.yaml
```
