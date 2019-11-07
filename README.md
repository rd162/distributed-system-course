# distributed-system-course

Boilerplate for course tasks (Communication in Distributed Systems)

Create GCP project

```sh
gcloud projects create distributed-system-course
gcloud config set project distributed-system-course
gcloud config set compute/zone us-central1-b
```

Build image

```sh
cd web-service
./mvnw clean package
docker build -t gcr.io/distributed-system-course/web-service:latest .
gcloud auth configure-docker
docker push gcr.io/distributed-system-course/web-service:latest
```

Create cluster in GKE

```sh
gcloud container clusters create distributed-system-course --num-nodes=3
gcloud container clusters get-credentials distributed-system-course
```

Deploy the service into GKE

```sh
cd web-service
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml
kubectl get service web-service
```

Verify the service

```sh
curl http://{EXTERNAL-IP}:60000/api/hello
```
