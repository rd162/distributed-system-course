# Install Kafka into K8s Cluster with Helm

## Simple Kafka Release

It is possible to use single Kafka deployment with the following Helm chart: <https://github.com/bitnami/charts/tree/master/bitnami/kafka>

Also, see this guide <https://bitnami.com/stack/kafka/helm>

### Deployment Instruction

```sh
# Add custom Helm repository
helm repo add bitnami https://charts.bitnami.com/bitnami

# Install durable 5-node Kafka cluster
NODE_COUNT=5
REPLICATION_FACTOR=3
PARTITIONS=100
# Partitions = ConsumerReplicaCount * ParallelConsumerCount
# Partitions = DesiredThroughput / SingleConsumerThroughput
# ParallelConsumerCount = DesiredThroughput/ConsumerReplicaCount/SingleConsumerThroughput

helm install --name kafka bitnami/kafka --set replicaCount=$NODE_COUNT --set deleteTopicEnable="true" --set logRetentionHours=24 --set defaultReplicationFactor=$REPLICATION_FACTOR --set offsetsTopicReplicationFactor=3 --set transactionStateLogReplicationFactor=3 --set transactionStateLogMinIsr=3 --set numPartitions=$PARTITIONS

# What happens if the producers send messages faster than the consumers can process them?
# What happens if nodes crash or temporarily go offline — are any messages lost?
```

**Note:** With Helm v3 please omit the --name parameter

#### Verify the Kafka Deployment

```sh
# Create a topic using Kafka CLI tools
kubectl --namespace default exec -it kafka-0 -- kafka-topics.sh --zookeeper kafka-zookeeper:2181 --create --topic test

# List all topics
kubectl --namespace default exec -it kafka-0 -- kafka-topics.sh --zookeeper kafka-zookeeper:2181 --list

# Get full info for particular topic
kubectl --namespace default exec -it kafka-0 -- kafka-topics.sh --zookeeper kafka-zookeeper:2181 --describe --topic test

# Start Kafka command-line producer
kubectl --namespace default exec -it kafka-1 -- kafka-console-producer.sh --broker-list localhost:9092 --topic test

# Start Kafka command-line consumer
kubectl --namespace default exec -it kafka-2 -- kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test --from-beginning
```

### How to Delete Kafka release from K8s Cluster

**Note:** By default chart removal does not delete the persistent volume claims, so that old configuration files would be used. <https://kubernetes.io/docs/concepts/storage/persistent-volumes/>

```sh
helm delete --purge kafka
# Delete persistent volume claims
# Note: by default chart removal does not delete these persistent volume claims, so that old configuration files would be used.
kubectl get pvc # List persistent volume claims
kubectl delete pvc data-kafka-0
kubectl delete pvc data-kafka-1
kubectl delete pvc data-kafka-2
kubectl delete pvc data-kafka-3
kubectl delete pvc data-kafka-4
kubectl delete pvc data-kafka-zookeeper-0
```

## Full Confluent Platform

Also, there is official Confluent Helm chart with full Confluent platform (including additional services, like [Schema Registry](https://docs.confluent.io/current/schema-registry/index.html)): <https://github.com/confluentinc/cp-helm-charts>. Also see this: <https://docs.confluent.io/5.0.0/installation/installing_cp/cp-helm-charts/docs/index.html>.
