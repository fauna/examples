# Create a GCE Image with VM enabled

_source: https://cloud.google.com/compute/docs/instances/enable-nested-virtualization-vm-instances_

```
$ gcloud compute disks create ubuntu-vm-disk --image-project ubuntu-os-cloud --image-family ubuntu-1604-lts --zone us-central1-a

$ gcloud compute images create ubuntu-nested-vm-image \
  --source-disk ubuntu-vm-disk --source-disk-zone us-central1-a \
  --licenses "https://www.googleapis.com/compute/v1/projects/vm-options/global/licenses/enable-vmx"

# simple instance
$ gcloud compute instances create ubuntu-nested-vm --zone us-central1-a \
          --min-cpu-platform "Intel Haswell" \
          --image ubuntu-nested-vm-image

# improved instance
$ gcloud compute instances create ubuntu-nested-vm-4cpus --zone us-central1-a \
          --min-cpu-platform "Intel Haswell" \
          --custom-cpu=4 \
          --custom-memory=18GB \
          --image ubuntu-nested-vm-image

$ gcloud compute ssh ubuntu-nested-vm

duh@ubuntu-nested-vm:~$ grep -cw vmx /proc/cpuinfo
> 1
```

# Installing docker

_source: https://docs.docker.com/install/linux/docker-ce/ubuntu/_

```
# remove older versions
# $ sudo apt-get remove docker docker-engine docker.io containerd runc
$ sudo apt-get update
$ sudo apt-get install \
    apt-transport-https \
    ca-certificates \
    curl \
    gnupg-agent \
    software-properties-common
$ curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
$ sudo apt-key fingerprint 0EBFCD88
$ sudo add-apt-repository \
   "deb [arch=amd64] https://download.docker.com/linux/ubuntu \
   $(lsb_release -cs) \
   stable"
$ sudo apt-get update
$ sudo apt-get install docker-ce=18.06.3~ce~3-0~ubuntu containerd.io

# run docker without root perm
$ sudo groupadd docker
$ sudo usermod -aG docker $USER

# logout, CTRL+D 

$ docker run hello-world
```

# Installing kubectl

_source: https://kubernetes.io/docs/tasks/tools/install-kubectl/#install-using-native-package-management_

```
$ sudo apt-get update && sudo apt-get install -y apt-transport-https
$ curl -s https://packages.cloud.google.com/apt/doc/apt-key.gpg | sudo apt-key add -
$ echo "deb https://apt.kubernetes.io/ kubernetes-xenial main" | sudo tee -a /etc/apt/sources.list.d/kubernetes.list
$ sudo apt-get update
$ sudo apt-get install -y kubectl
```

# Installing minikube

_source: https://kubernetes.io/docs/tasks/tools/install-minikube/_


```
# you can verify that virtualization is enabled if the output is non-empty
# $ egrep 'vmx|svm' /proc/cpuinfo
$ curl -Lo minikube https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64 \
  && chmod +x minikube
$ sudo cp minikube /usr/local/bin && rm minikube

# vm-driver = none : will use docker instead of virtualbox
$ sudo minikube start --vm-driver=none --kubernetes-version v1.11.10 --memory=14000 --cpus=4
```

## Hello World with minikube

```
$ sudo kubectl run hello-minikube --image=k8s.gcr.io/echoserver:1.10 --port=8080
$ sudo kubectl expose deployment hello-minikube --type=NodePort
$ curl $(sudo minikube service hello-minikube --url)
```

## Port Forward

```
# find which port your POD was exposed to
$ sudo minikube service hello-minikube --url
  http://10.128.0.26:30447

# localhost, your machine
$ gcloud compute  ssh --ssh-flag="-L 8088:localhost:30447"  --zone "us-central1-a" "ubuntu-nested-vm-4cpus"
# localhost, other terminal
$ curl localhost:8088

Hostname: hello-minikube-59ddd8676b-qspql
Pod Information:
  -no pod information available-

Server values:
  server_version=nginx: 1.13.3 - lua: 10008
```
