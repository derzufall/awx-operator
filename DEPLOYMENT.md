# 🚀 AWX Operator Deployment Guide

**Welcome to the ultimate deployment guide for your spectacular AWX Operator!** 🎯✨

## 🏃‍♂️ Quick Start

### Option 1: ArgoCD Single Application (Recommended) 🎯

Deploy to **production**:
```bash
kubectl apply -f argocd/awx-operator-app.yaml
```

Deploy to **development**:
```bash
kubectl apply -f argocd/awx-operator-dev-app.yaml
```

### Option 2: ArgoCD ApplicationSet (Advanced) 🚀

Deploy to **all environments automatically**:
```bash
kubectl apply -f argocd/awx-operator-appset.yaml
```

This will automatically discover and deploy to all environments in `manifests/overlays/`!

### Option 3: Direct Kustomize (Manual) 🔧

**Production deployment**:
```bash
kubectl apply -k manifests/overlays/production
```

**Development deployment**:
```bash
kubectl apply -k manifests/overlays/development
```

## 🏗️ What Gets Deployed

Your **magnificent AWX Operator** includes:

### 🎯 Core Components
- ✅ **Namespace**: `awx-operator-system` (production) or `awx-operator-dev` (development)
- ✅ **ServiceAccount**: `awx-operator-controller` with proper RBAC
- ✅ **ClusterRole**: Permissions for AWX CRDs and Kubernetes resources
- ✅ **Deployment**: The operator pod(s) with health checks
- ✅ **Service**: Metrics endpoint on port 8080

### 🎨 Custom Resource Definitions (CRDs)
- ✅ **AwxConnection**: Manages AWX server connections
- ✅ **AwxProject**: Manages AWX projects

### 🛡️ Security Features
- ✅ **Non-root user** (UID 1001)
- ✅ **Read-only filesystem**
- ✅ **Dropped capabilities**
- ✅ **Security contexts**
- ✅ **RBAC with minimal permissions**

## 🔍 Verification Commands

### Check Deployment Status
```bash
# Production
kubectl get pods -n awx-operator-system
kubectl get deployment awx-operator-controller -n awx-operator-system

# Development  
kubectl get pods -n awx-operator-dev
kubectl get deployment awx-operator-controller -n awx-operator-dev
```

### View Logs
```bash
# Production logs
kubectl logs -n awx-operator-system deployment/awx-operator-controller -f

# Development logs
kubectl logs -n awx-operator-dev deployment/awx-operator-controller -f
```

### Check CRDs
```bash
# Verify CRDs are installed
kubectl get crd | grep wolkenzentrale.de

# List AWX resources
kubectl get awxconnections --all-namespaces
kubectl get awxprojects --all-namespaces
```

### Health Checks
```bash
# Port-forward to access health endpoints
kubectl port-forward -n awx-operator-system deployment/awx-operator-controller 8081:8081

# Check health (in another terminal)
curl http://localhost:8081/actuator/health/liveness
curl http://localhost:8081/actuator/health/readiness
```

### Metrics
```bash
# Port-forward to access metrics
kubectl port-forward -n awx-operator-system deployment/awx-operator-controller 8080:8080

# Access metrics (in another terminal)
curl http://localhost:8080/actuator/metrics
curl http://localhost:8080/actuator/prometheus
```

## 🎛️ Configuration Examples

### Example AWX Connection
```yaml
apiVersion: wolkenzentrale.de/v1alpha1
kind: AwxConnection
metadata:
  name: my-awx-server
  namespace: default
spec:
  name: "My AWX Server"
  url: "https://awx.example.com"
  username: "admin"
  password: "secret-password"
  insecureSkipTlsVerify: false
```

### Example AWX Project
```yaml
apiVersion: wolkenzentrale.de/v1alpha1
kind: AwxProject
metadata:
  name: my-ansible-project
  namespace: default
spec:
  awxConnectionRef:
    name: my-awx-server
  name: "My Ansible Project"
  description: "Ansible project for automation"
  scmType: "git"
  scmUrl: "https://github.com/example/ansible-playbooks.git"
  scmBranch: "main"
```

## 🔄 ArgoCD Integration

### Sync Applications
```bash
# Sync production
argocd app sync awx-operator

# Sync development
argocd app sync awx-operator-dev

# Sync all (with ApplicationSet)
argocd appset sync awx-operator-environments
```

### Monitor Applications
```bash
# List applications
argocd app list | grep awx-operator

# Get application details
argocd app get awx-operator

# View application logs
argocd app logs awx-operator
```

## 🚀 Advanced Deployment Scenarios

### Custom Image Version
```bash
# Deploy specific version to production
kubectl patch application awx-operator -n argocd --type='merge' -p='{"spec":{"source":{"kustomize":{"images":["ghcr.io/wolkenzentrale/awx-operator:v1.2.3"]}}}}'
```

### Scale Replicas
```bash
# Scale production deployment
kubectl scale deployment awx-operator-controller -n awx-operator-system --replicas=3
```

### Resource Adjustment
Create a custom patch file and update your kustomization.yaml:
```yaml
# custom-patch.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: awx-operator-controller
spec:
  template:
    spec:
      containers:
      - name: manager
        resources:
          limits:
            cpu: 2000m
            memory: 2Gi
```

## 🔧 Troubleshooting

### Common Issues

**Operator not starting:**
```bash
# Check events
kubectl get events -n awx-operator-system --sort-by=.metadata.creationTimestamp

# Check pod status
kubectl describe pod -n awx-operator-system -l app.kubernetes.io/name=awx-operator
```

**CRDs not found:**
```bash
# Manually install CRDs
kubectl apply -f src/main/resources/crds/v1alpha1-awxconnection-crd.yaml
kubectl apply -f src/main/resources/crds/v1alpha1-awxproject-crd.yaml
```

**RBAC issues:**
```bash
# Check service account permissions
kubectl auth can-i get awxconnections --as=system:serviceaccount:awx-operator-system:awx-operator-controller
```

### Reset Deployment
```bash
# Delete and redeploy production
kubectl delete -k manifests/overlays/production
kubectl apply -k manifests/overlays/production

# Or with ArgoCD
argocd app delete awx-operator
kubectl apply -f argocd/awx-operator-app.yaml
```

## 📊 Monitoring & Observability

### Prometheus Integration
The operator exposes metrics at `/actuator/prometheus`:
```yaml
# ServiceMonitor for Prometheus
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: awx-operator-metrics
spec:
  selector:
    matchLabels:
      app.kubernetes.io/name: awx-operator
  endpoints:
  - port: http-metrics
    path: /actuator/prometheus
```

### Grafana Dashboard
Key metrics to monitor:
- `awx_connections_total` - Total AWX connections
- `awx_projects_total` - Total AWX projects  
- `awx_sync_duration_seconds` - Sync operation duration
- `jvm_memory_used_bytes` - JVM memory usage

## 🎉 Success!

**Congratulations!** 🎊 Your **AWX Operator** is now deployed and ready to manage your AWX infrastructure! 

The operator will automatically:
- 🔄 **Reconcile** AWX connections and projects
- 📊 **Report status** back to Kubernetes  
- 🚨 **Handle failures** with retries
- 📈 **Expose metrics** for monitoring

---

**Happy automating!** 🚀🎯 Your infrastructure is now **absolutely magnificent**! ⭐️ 