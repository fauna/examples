# Kubernetes and FaunaDB 😍

### Tools

* [docker](https://docs.docker.com/install/)
* [kubectl](https://kubernetes.io/docs/tasks/tools/install-kubectl/)
* [minikube](https://kubernetes.io/docs/tutorials/hello-minikube/)


### Begin

```
$ minikube start --kubernetes-version v1.11.10 --memory=12288 --cpus=4
# or add extra args to run on OSx with full log output and extra docker options
# minikube start --kubernetes-version v1.11.10 --memory=12288 --cpus=4 --v=10 --logtostderr -- docker-opt bip=172.18.0.1/16
$ minikube dashboard
```

*Warning:* before continue, ensure that your `kubectl` is configured with the appropriate context:

```
$ kubectl config get-contexts   
$ kubectl config use-context minikube
```

### Basic ops

* **create the cluster**: `kubectl create -f faunadb-cluster.yaml`
* **scaling**: `kubectl scale statefulsets faunadb-cluster --replicas=3`
* **wipe out the cluster**: `kubectl delete -f fauna-cluster.yaml && kubectl delete pv,pvc -l app=faunadb-cluster`


### Make targets

_the `make` command will add the resources to the namespace configured in the [Makefile](./Makefile) `K8S_NAMESPACE`_

* `create-cluster`: create the cluster
* `destroy`: destroy the cluster
* `scale` : scale the cluster to a specific size, eg: `make scale TO=3`
* `ping` : curl the `/ping` endpoint in the LoadBalancer exposed by the minikube (and `grep` the output)
* `status` : runs the `faunadb-admin status` command inside the POD
* `host-version` : runs the `faunadb-admin host-version` command inside the POD
* `seed` : creates (with `curl`) some data
* `paginate` : performs (with `curl`) paginate query for the data created in the `seed` target
* `fauna-shell` : runs the `fauna shell` 


### Tips

You can use the `watch` command to inspect the cluster, eg:

`$ watch -d -n 1 kubectl get -n faunadb statefulset,pods,services,pv,pvc`
`$ watch make ping`
`$ watch -d make status`