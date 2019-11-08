# distributed-system-course

Boilerplate for course tasks (Communication in Distributed Systems)

Create GCP project

```sh
gcloud projects create distributed-system-course
gcloud config set project distributed-system-course
gcloud config set compute/zone us-central1-b
gcloud auth configure-docker
```

Build image (web-service)

```sh
cd web-service
./mvnw clean package
docker build -t gcr.io/distributed-system-course/web-service:latest .
docker push gcr.io/distributed-system-course/web-service:latest
```

Build image (rpc-service)

```sh
cd rpc-service
./mvnw clean package
docker build -t gcr.io/distributed-system-course/rpc-service:latest .
docker push gcr.io/distributed-system-course/rpc-service:latest
```

Create cluster in GKE

```sh
gcloud container clusters create distributed-system-course --num-nodes=3
gcloud container clusters get-credentials distributed-system-course
```

Deploy the service into GKE (web-service)

```sh
cd web-service
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml
kubectl get service web-service
```

Deploy the service into GKE (rpc-service)

```sh
cd rpc-service
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml
kubectl get service rpc-service
```

Verify the service

```sh
curl http://{EXTERNAL-IP}:60000/api/hello
```

In order to delete all resources at once you can delete the whole GCP project and then "undelete" it after some time. 
The resources will not be restored after undelete, however you will need to enable services again. 

```sh
gcloud projects delete distributed-system-course
```
