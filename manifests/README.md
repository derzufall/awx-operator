# 🚀 AWX Operator Kubernetes Deployment

This directory contains the **complete Kubernetes deployment manifests** for the AWX Operator using **Kustomize** and **ArgoCD**! 🎯✨

## 📁 Directory Structure

```
manifests/
├── base/                           # 🏗️ Base Kustomize configuration
│   ├── deployment.yaml            # Core operator deployment
│   └── kustomization.yaml         # Base Kustomize config
├── overlays/                       # 🎨 Environment-specific customizations
│   ├── production/                 # 🏭 Production environment
│   │   ├── kustomization.yaml     # Production Kustomize config
│   │   └── deployment-production-patch.yaml
│   └── development/                # 🧪 Development environment
│       ├── kustomization.yaml     # Development Kustomize config
│       └── deployment-development-patch.yaml
└── README.md                       # 📚 This file
```

## 🎯 ArgoCD Applications

### Production Deployment 🏭

Deploy to production using ArgoCD:

```bash
kubectl apply -f argocd/awx-operator-app.yaml
```

**Features:**
- ✅ High availability (2 replicas)
- ✅ Production resource limits
- ✅ Anti-affinity rules
- ✅ Enhanced health checks
- ✅ OpenTelemetry integration
- ✅ Automated sync with self-healing

### Development Deployment 🧪

Deploy to development using ArgoCD:

```bash
kubectl apply -f argocd/awx-operator-dev-app.yaml
```

**Features:**
- ✅ Single replica for cost efficiency
- ✅ Debug logging enabled
- ✅ Minimal resource requirements
- ✅ Manual sync for controlled testing
- ✅ Latest image for rapid iteration

## 🔨 Manual Deployment with Kustomize

If you prefer to deploy manually without ArgoCD:

### Production
```bash
# Build and apply production manifests
kubectl apply -k manifests/overlays/production

# Verify deployment
kubectl get pods -n awx-operator-system
kubectl logs -n awx-operator-system deployment/awx-operator-controller
```

### Development
```bash
# Build and apply development manifests
kubectl apply -k manifests/overlays/development

# Verify deployment
kubectl get pods -n awx-operator-dev
kubectl logs -n awx-operator-dev deployment/awx-operator-controller
```

## 🎛️ Customization

### Image Version Override

For production with specific version:
```bash
kubectl apply -k manifests/overlays/production --set-image ghcr.io/wolkenzentrale/awx-operator=ghcr.io/wolkenzentrale/awx-operator:v1.2.3
```

### Resource Customization

Create a custom patch file:
```yaml
# custom-resources-patch.yaml
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
          requests:
            cpu: 500m
            memory: 512Mi
```

Then reference it in your kustomization.yaml:
```yaml
patchesStrategicMerge:
  - custom-resources-patch.yaml
```

## 🔍 Monitoring & Observability

The operator exposes metrics on port 8080:

```bash
# Port-forward to access metrics
kubectl port-forward -n awx-operator-system deployment/awx-operator-controller 8080:8080

# Access metrics endpoint
curl http://localhost:8080/actuator/metrics
```

Health checks are available on port 8081:
```bash
# Health endpoints
curl http://localhost:8081/actuator/health/liveness
curl http://localhost:8081/actuator/health/readiness
```

## 🛡️ Security Configuration

The operator runs with:
- ✅ **Non-root user** for security
- ✅ **Read-only root filesystem**
- ✅ **Dropped capabilities**
- ✅ **Security context** with seccomp profile
- ✅ **RBAC** with minimal required permissions

## 📊 Scaling

### Horizontal Scaling
```bash
# Scale replicas (production only)
kubectl scale deployment awx-operator-controller -n awx-operator-system --replicas=3
```

### Vertical Scaling
Update the resource limits in the appropriate overlay patch file.

## 🔧 Troubleshooting

### Check Operator Status
```bash
kubectl get pods -n awx-operator-system
kubectl describe deployment awx-operator-controller -n awx-operator-system
```

### View Logs
```bash
# All logs
kubectl logs -n awx-operator-system deployment/awx-operator-controller

# Follow logs
kubectl logs -n awx-operator-system deployment/awx-operator-controller -f

# Previous container logs
kubectl logs -n awx-operator-system deployment/awx-operator-controller --previous
```

### Check Custom Resources
```bash
# List AWX connections
kubectl get awxconnections --all-namespaces

# List AWX projects
kubectl get awxprojects --all-namespaces

# Detailed status
kubectl describe awxconnection <name> -n <namespace>
```

## 🚀 Development Workflow

1. **Make code changes** in your IDE
2. **Build and push** new container image:
   ```bash
   docker build -t ghcr.io/wolkenzentrale/awx-operator:latest .
   docker push ghcr.io/wolkenzentrale/awx-operator:latest
   ```
3. **Sync ArgoCD** development application:
   ```bash
   argocd app sync awx-operator-dev
   ```
4. **Test your changes** in the development environment
5. **Create a tag** for production deployment:
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```

## 📝 Prerequisites

- **Kubernetes cluster** (v1.25+)
- **ArgoCD** installed (for GitOps deployment)
- **Kustomize** (for manual deployment)
- **kubectl** configured with cluster access

## 🎉 What's Included

- 🏗️ **Complete RBAC configuration**
- 🔄 **Custom Resource Definitions (CRDs)**
- 📊 **Metrics and health endpoints**
- 🛡️ **Security best practices**
- 🎯 **Multi-environment support**
- 🔄 **GitOps-ready ArgoCD applications**
- 🚀 **Production-ready configuration**

---

**Happy deploying!** 🎯🚀 Your AWX Operator will be **absolutely magnificent** in your cluster! ⭐️ 