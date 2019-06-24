FAUNADB_FILE 	:= "faunadb-cluster.yaml"
K8S_NAMESPACE	:= "faunadb"
ENDPOINT		:= $(shell minikube service faunadb-lb -n $(K8S_NAMESPACE) --url)

.PHONY: create-cluster destroy scale ping status host-version check-minikube seed paginate faunadb-shell

create-cluster:
	kubectl create namespace $(K8S_NAMESPACE)
	kubectl apply -n $(K8S_NAMESPACE) -f ${FAUNADB_FILE}

destroy:
	kubectl delete -n $(K8S_NAMESPACE) -f ${FAUNADB_FILE}
	@while [ ! -z "$(kubectl get pods -n $(K8S_NAMESPACE) -l app=faunadb-cluster)" ] ; do sleep 20s ; done
	kubectl delete pv,pvc -n $(K8S_NAMESPACE) -l app=faunadb-cluster

scale: TO ?= "2" 
scale:
	kubectl scale statefulsets -n $(K8S_NAMESPACE) faunadb-cluster --replicas=${TO}

ping:
	@curl -sIXGET ${ENDPOINT}/ping | egrep "(HTTP|Host|Build)"

status: TARGET ?= "faunadb-cluster-0"
status:
	kubectl exec -n $(K8S_NAMESPACE) -it ${TARGET} -- bash -c "faunadb-admin status"

host-version: TARGET ?= "faunadb-cluster-0"
host-version:
	kubectl exec -n $(K8S_NAMESPACE) -it ${TARGET} -- bash -c "faunadb-admin host-version"

# TODO: verify the context?
-check-minikube:
	@if [ `kubectl config current-context` != minikube ] ; then echo "WARNING: Please setup minikube context!"; else echo "Minikube context set"; fi
	kubectl config use-context minikube

# create some data
seed:
	@curl -u secret: ${ENDPOINT} --data "{\"create_class\":{\"object\":{\"name\":\"stocks\"}}}"
	@curl -u secret: ${ENDPOINT} --data "{\"create_index\":{\"object\":{\"name\":\"all_stocks\",\"source\":{\"class\":\"stocks\"},\"values\":[{\"object\":{\"field\":[\"data\",\"ticker\"]}},{\"object\":{\"field\":[\"ref\"]}}],\"active\":true}}}"
	@curl -u secret: ${ENDPOINT} --data "{\"create\":{\"class\":\"stocks\"},\"params\":{\"object\":{\"data\":{\"object\":{\"ticker\":\"APPL\"}}}}}"
	@curl -u secret: ${ENDPOINT} --data "{\"create\":{\"class\":\"stocks\"},\"params\":{\"object\":{\"data\":{\"object\":{\"ticker\":\"NVDA\"}}}}}"
	@curl -u secret: ${ENDPOINT} --data "{\"create\":{\"class\":\"stocks\"},\"params\":{\"object\":{\"data\":{\"object\":{\"ticker\":\"NFLX\"}}}}}"
	@curl -u secret: ${ENDPOINT} --data "{\"create\":{\"class\":\"stocks\"},\"params\":{\"object\":{\"data\":{\"object\":{\"ticker\":\"FAUNA\"}}}}}"

paginate:
	curl --silent --connect-timeout 3 --fail -u secret: ${ENDPOINT} --data "{\"paginate\":{\"match\":{\"index\":\"all_stocks\"},\"terms\":[]}}"

fauna-shell:
	kubectl exec -it faunadb-shell -- bash -c "fauna shell"
