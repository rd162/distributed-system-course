# Install RabbitMQ into K8s Cluster with Helm

Use the following Helm Chart: <https://github.com/helm/charts/tree/master/stable/rabbitm>

```sh
helm install stable/rabbitmq --name rabbitmq --set rabbitmq.username="guest" --set rabbitmq.password="guest"
```

The RabbitMQ address should be: `rabbitmq.default.svc.cluster.local:5672` (accessible only inside cluster).

You can set-up temporary access to the RabbitMQ (or Management interface) through local port forwarding feature of kubectl:

```sh
# Run these commands from different console/terminal windows
kubectl port-forward --namespace default svc/rabbitmq 5672:5672
kubectl port-forward --namespace default svc/rabbitmq 15675:15672
```

**Note:** With Helm v3 please omit the --name parameter

Now you can open in the browser <http://127.0.0.1:15675>. Login: guest / guest.

How to delete RabbitMQ from the cluster:
**Note:** by default chart removal does not delete these persistent volume claims, so that old configuration files would be used. <https://kubernetes.io/docs/concepts/storage/persistent-volumes/>

```sh
helm delete --purge rabbitmq
# Delete persistent volume claims
# Note: by default chart removal does not delete these persistent volume claims, so that old configuration files would be used.
kubectl get pvc # List persistent volume claims
kubectl delete pvc data-rabbitmq-0
```
