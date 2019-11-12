# Install RabbitMQ into K8s Clsuter with Helm

Use the following Helm Chart: <https://github.com/helm/charts/tree/master/stable/rabbitm>

```sh
helm install --name rabbitmq --set rabbitmq.username=admin,rabbitmq.password=admin stable/rabbitmq
```

The RabbitMQ address should be: rabbitmq.default.svc.cluster.local:5672 (accessible only inside cluster).

You can set-up temporary access to the RabbitMQ Management interface through local port forwarding feature of kubectl:

```sh
kubectl port-forward --namespace default svc/rabbitmq 15675:15672
```

Now you can open in the browser <http://127.0.0.1:15675>. Login: admin / admin.

Upu can delete RabbitMQ from the cluster:

```sh
helm delete --purge rabbitmq
```
