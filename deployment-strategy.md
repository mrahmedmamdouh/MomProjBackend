# 🐳 Containerization & Orchestration Strategy

## 🎯 **Containerization Strategy**

### **Docker Configuration**

#### **1. Base Dockerfile Template**
```dockerfile
# Dockerfile.template
FROM openjdk:17-jre-slim

# Set working directory
WORKDIR /app

# Copy application JAR
COPY build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### **2. Service-Specific Dockerfiles**

##### **Authentication Service**
```dockerfile
# auth-service/Dockerfile
FROM openjdk:17-jre-slim
WORKDIR /app
COPY build/libs/auth-service-*.jar app.jar
EXPOSE 8081
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8081/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
```

##### **User Management Service**
```dockerfile
# user-service/Dockerfile
FROM openjdk:17-jre-slim
WORKDIR /app
COPY build/libs/user-service-*.jar app.jar
EXPOSE 8082
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8082/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
```

##### **E-commerce Service**
```dockerfile
# ecommerce-service/Dockerfile
FROM openjdk:17-jre-slim
WORKDIR /app
COPY build/libs/ecommerce-service-*.jar app.jar
EXPOSE 8083
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8083/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
```

##### **Shopping Service**
```dockerfile
# shopping-service/Dockerfile
FROM openjdk:17-jre-slim
WORKDIR /app
COPY build/libs/shopping-service-*.jar app.jar
EXPOSE 8084
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8084/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
```

##### **Payment Service**
```dockerfile
# payment-service/Dockerfile
FROM openjdk:17-jre-slim
WORKDIR /app
COPY build/libs/payment-service-*.jar app.jar
EXPOSE 8085
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8085/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
```

##### **File Service**
```dockerfile
# file-service/Dockerfile
FROM openjdk:17-jre-slim
WORKDIR /app
COPY build/libs/file-service-*.jar app.jar
EXPOSE 8086
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8086/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
```

##### **Admin Service**
```dockerfile
# admin-service/Dockerfile
FROM openjdk:17-jre-slim
WORKDIR /app
COPY build/libs/admin-service-*.jar app.jar
EXPOSE 8087
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8087/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
```

##### **Analytics Service**
```dockerfile
# analytics-service/Dockerfile
FROM openjdk:17-jre-slim
WORKDIR /app
COPY build/libs/analytics-service-*.jar app.jar
EXPOSE 8088
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8088/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### **Docker Compose for Development**

#### **1. Development Environment**
```yaml
# docker-compose.dev.yml
version: '3.8'

services:
  # Infrastructure Services
  mongodb:
    image: mongo:6.0
    container_name: momcare-mongodb
    ports:
      - "27017:27017"
    environment:
      MONGO_INITDB_ROOT_USERNAME: admin
      MONGO_INITDB_ROOT_PASSWORD: password
    volumes:
      - mongodb_data:/data/db
      - ./scripts/mongo-init.js:/docker-entrypoint-initdb.d/mongo-init.js:ro

  redis:
    image: redis:7-alpine
    container_name: momcare-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

  kafka:
    image: confluentinc/cp-kafka:latest
    container_name: momcare-kafka
    ports:
      - "9092:9092"
    environment:
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    depends_on:
      - zookeeper

  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    container_name: momcare-zookeeper
    ports:
      - "2181:2181"
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000

  consul:
    image: consul:1.15
    container_name: momcare-consul
    ports:
      - "8500:8500"
    command: agent -server -ui -node=server-1 -bootstrap-expect=1 -client=0.0.0.0

  kong:
    image: kong:3.0
    container_name: momcare-kong
    ports:
      - "8000:8000"
      - "8001:8001"
    environment:
      KONG_DATABASE: "off"
      KONG_DECLARATIVE_CONFIG: /kong/kong.yml
    volumes:
      - ./kong/kong.yml:/kong/kong.yml:ro

  # Application Services
  auth-service:
    build:
      context: ./auth-service
      dockerfile: Dockerfile
    container_name: momcare-auth-service
    ports:
      - "8081:8081"
    environment:
      MONGODB_URI: mongodb://admin:password@mongodb:27017/auth_db?authSource=admin
      REDIS_URL: redis://redis:6379
      KAFKA_BOOTSTRAP_SERVERS: kafka:9092
      CONSUL_HOST: consul
      CONSUL_PORT: 8500
    depends_on:
      - mongodb
      - redis
      - kafka
      - consul

  user-service:
    build:
      context: ./user-service
      dockerfile: Dockerfile
    container_name: momcare-user-service
    ports:
      - "8082:8082"
    environment:
      MONGODB_URI: mongodb://admin:password@mongodb:27017/user_db?authSource=admin
      REDIS_URL: redis://redis:6379
      KAFKA_BOOTSTRAP_SERVERS: kafka:9092
      CONSUL_HOST: consul
      CONSUL_PORT: 8500
    depends_on:
      - mongodb
      - redis
      - kafka
      - consul

  ecommerce-service:
    build:
      context: ./ecommerce-service
      dockerfile: Dockerfile
    container_name: momcare-ecommerce-service
    ports:
      - "8083:8083"
    environment:
      MONGODB_URI: mongodb://admin:password@mongodb:27017/ecommerce_db?authSource=admin
      REDIS_URL: redis://redis:6379
      KAFKA_BOOTSTRAP_SERVERS: kafka:9092
      CONSUL_HOST: consul
      CONSUL_PORT: 8500
    depends_on:
      - mongodb
      - redis
      - kafka
      - consul

  shopping-service:
    build:
      context: ./shopping-service
      dockerfile: Dockerfile
    container_name: momcare-shopping-service
    ports:
      - "8084:8084"
    environment:
      MONGODB_URI: mongodb://admin:password@mongodb:27017/shopping_db?authSource=admin
      REDIS_URL: redis://redis:6379
      KAFKA_BOOTSTRAP_SERVERS: kafka:9092
      CONSUL_HOST: consul
      CONSUL_PORT: 8500
    depends_on:
      - mongodb
      - redis
      - kafka
      - consul

  payment-service:
    build:
      context: ./payment-service
      dockerfile: Dockerfile
    container_name: momcare-payment-service
    ports:
      - "8085:8085"
    environment:
      MONGODB_URI: mongodb://admin:password@mongodb:27017/payment_db?authSource=admin
      REDIS_URL: redis://redis:6379
      KAFKA_BOOTSTRAP_SERVERS: kafka:9092
      CONSUL_HOST: consul
      CONSUL_PORT: 8500
    depends_on:
      - mongodb
      - redis
      - kafka
      - consul

  file-service:
    build:
      context: ./file-service
      dockerfile: Dockerfile
    container_name: momcare-file-service
    ports:
      - "8086:8086"
    environment:
      MONGODB_URI: mongodb://admin:password@mongodb:27017/file_db?authSource=admin
      REDIS_URL: redis://redis:6379
      KAFKA_BOOTSTRAP_SERVERS: kafka:9092
      CONSUL_HOST: consul
      CONSUL_PORT: 8500
    volumes:
      - file_uploads:/app/uploads
    depends_on:
      - mongodb
      - redis
      - kafka
      - consul

  admin-service:
    build:
      context: ./admin-service
      dockerfile: Dockerfile
    container_name: momcare-admin-service
    ports:
      - "8087:8087"
    environment:
      MONGODB_URI: mongodb://admin:password@mongodb:27017/admin_db?authSource=admin
      REDIS_URL: redis://redis:6379
      KAFKA_BOOTSTRAP_SERVERS: kafka:9092
      CONSUL_HOST: consul
      CONSUL_PORT: 8500
    depends_on:
      - mongodb
      - redis
      - kafka
      - consul

  analytics-service:
    build:
      context: ./analytics-service
      dockerfile: Dockerfile
    container_name: momcare-analytics-service
    ports:
      - "8088:8088"
    environment:
      MONGODB_URI: mongodb://admin:password@mongodb:27017/analytics_db?authSource=admin
      REDIS_URL: redis://redis:6379
      KAFKA_BOOTSTRAP_SERVERS: kafka:9092
      CONSUL_HOST: consul
      CONSUL_PORT: 8500
    depends_on:
      - mongodb
      - redis
      - kafka
      - consul

volumes:
  mongodb_data:
  redis_data:
  file_uploads:
```

## ☸️ **Kubernetes Orchestration**

### **Namespace Configuration**
```yaml
# k8s/namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: momcare-platform
  labels:
    name: momcare-platform
    environment: production
```

### **ConfigMaps**
```yaml
# k8s/configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: momcare-config
  namespace: momcare-platform
data:
  MONGODB_URI: "mongodb://mongodb-service:27017"
  REDIS_URL: "redis://redis-service:6379"
  KAFKA_BOOTSTRAP_SERVERS: "kafka-service:9092"
  CONSUL_HOST: "consul-service"
  CONSUL_PORT: "8500"
  JWT_SECRET: "your-jwt-secret-key"
  JWT_ISSUER: "momcare-platform"
  JWT_AUDIENCE: "momcare-users"
```

### **Secrets**
```yaml
# k8s/secrets.yaml
apiVersion: v1
kind: Secret
metadata:
  name: momcare-secrets
  namespace: momcare-platform
type: Opaque
data:
  mongodb-username: YWRtaW4=  # admin
  mongodb-password: cGFzc3dvcmQ=  # password
  jwt-secret: eW91ci1qd3Qtc2VjcmV0LWtleQ==  # your-jwt-secret-key
  payment-api-key: cGF5bWVudC1hcGkta2V5  # payment-api-key
```

### **Service Deployments**

#### **1. Authentication Service**
```yaml
# k8s/auth-service-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: auth-service
  namespace: momcare-platform
spec:
  replicas: 3
  selector:
    matchLabels:
      app: auth-service
  template:
    metadata:
      labels:
        app: auth-service
    spec:
      containers:
      - name: auth-service
        image: momcare/auth-service:latest
        ports:
        - containerPort: 8081
        env:
        - name: MONGODB_URI
          valueFrom:
            configMapKeyRef:
              name: momcare-config
              key: MONGODB_URI
        - name: REDIS_URL
          valueFrom:
            configMapKeyRef:
              name: momcare-config
              key: REDIS_URL
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: momcare-secrets
              key: jwt-secret
        livenessProbe:
          httpGet:
            path: /health
            port: 8081
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /health
            port: 8081
          initialDelaySeconds: 5
          periodSeconds: 5
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"

---
apiVersion: v1
kind: Service
metadata:
  name: auth-service
  namespace: momcare-platform
spec:
  selector:
    app: auth-service
  ports:
  - port: 8081
    targetPort: 8081
  type: ClusterIP
```

#### **2. User Management Service**
```yaml
# k8s/user-service-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: user-service
  namespace: momcare-platform
spec:
  replicas: 3
  selector:
    matchLabels:
      app: user-service
  template:
    metadata:
      labels:
        app: user-service
    spec:
      containers:
      - name: user-service
        image: momcare/user-service:latest
        ports:
        - containerPort: 8082
        env:
        - name: MONGODB_URI
          valueFrom:
            configMapKeyRef:
              name: momcare-config
              key: MONGODB_URI
        - name: REDIS_URL
          valueFrom:
            configMapKeyRef:
              name: momcare-config
              key: REDIS_URL
        livenessProbe:
          httpGet:
            path: /health
            port: 8082
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /health
            port: 8082
          initialDelaySeconds: 5
          periodSeconds: 5
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"

---
apiVersion: v1
kind: Service
metadata:
  name: user-service
  namespace: momcare-platform
spec:
  selector:
    app: user-service
  ports:
  - port: 8082
    targetPort: 8082
  type: ClusterIP
```

### **Infrastructure Services**

#### **1. MongoDB StatefulSet**
```yaml
# k8s/mongodb-statefulset.yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: mongodb
  namespace: momcare-platform
spec:
  serviceName: mongodb
  replicas: 1
  selector:
    matchLabels:
      app: mongodb
  template:
    metadata:
      labels:
        app: mongodb
    spec:
      containers:
      - name: mongodb
        image: mongo:6.0
        ports:
        - containerPort: 27017
        env:
        - name: MONGO_INITDB_ROOT_USERNAME
          valueFrom:
            secretKeyRef:
              name: momcare-secrets
              key: mongodb-username
        - name: MONGO_INITDB_ROOT_PASSWORD
          valueFrom:
            secretKeyRef:
              name: momcare-secrets
              key: mongodb-password
        volumeMounts:
        - name: mongodb-storage
          mountPath: /data/db
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
  volumeClaimTemplates:
  - metadata:
      name: mongodb-storage
    spec:
      accessModes: ["ReadWriteOnce"]
      resources:
        requests:
          storage: 10Gi

---
apiVersion: v1
kind: Service
metadata:
  name: mongodb-service
  namespace: momcare-platform
spec:
  selector:
    app: mongodb
  ports:
  - port: 27017
    targetPort: 27017
  type: ClusterIP
```

#### **2. Redis Deployment**
```yaml
# k8s/redis-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: redis
  namespace: momcare-platform
spec:
  replicas: 1
  selector:
    matchLabels:
      app: redis
  template:
    metadata:
      labels:
        app: redis
    spec:
      containers:
      - name: redis
        image: redis:7-alpine
        ports:
        - containerPort: 6379
        volumeMounts:
        - name: redis-storage
          mountPath: /data
        resources:
          requests:
            memory: "128Mi"
            cpu: "100m"
          limits:
            memory: "256Mi"
            cpu: "200m"
      volumes:
      - name: redis-storage
        persistentVolumeClaim:
          claimName: redis-pvc

---
apiVersion: v1
kind: Service
metadata:
  name: redis-service
  namespace: momcare-platform
spec:
  selector:
    app: redis
  ports:
  - port: 6379
    targetPort: 6379
  type: ClusterIP

---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: redis-pvc
  namespace: momcare-platform
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 1Gi
```

### **Ingress Configuration**
```yaml
# k8s/ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: momcare-ingress
  namespace: momcare-platform
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
    nginx.ingress.kubernetes.io/ssl-redirect: "false"
    nginx.ingress.kubernetes.io/rate-limit: "100"
    nginx.ingress.kubernetes.io/rate-limit-window: "1m"
spec:
  rules:
  - host: api.momcare.com
    http:
      paths:
      - path: /api/auth
        pathType: Prefix
        backend:
          service:
            name: auth-service
            port:
              number: 8081
      - path: /api/moms
        pathType: Prefix
        backend:
          service:
            name: user-service
            port:
              number: 8082
      - path: /api/doctors
        pathType: Prefix
        backend:
          service:
            name: user-service
            port:
              number: 8082
      - path: /api/categories
        pathType: Prefix
        backend:
          service:
            name: ecommerce-service
            port:
              number: 8083
      - path: /api/products
        pathType: Prefix
        backend:
          service:
            name: ecommerce-service
            port:
              number: 8083
      - path: /api/cart
        pathType: Prefix
        backend:
          service:
            name: shopping-service
            port:
              number: 8084
      - path: /api/orders
        pathType: Prefix
        backend:
          service:
            name: shopping-service
            port:
              number: 8084
      - path: /api/payments
        pathType: Prefix
        backend:
          service:
            name: payment-service
            port:
              number: 8085
      - path: /api/files
        pathType: Prefix
        backend:
          service:
            name: file-service
            port:
              number: 8086
      - path: /uploads
        pathType: Prefix
        backend:
          service:
            name: file-service
            port:
              number: 8086
      - path: /api/admin
        pathType: Prefix
        backend:
          service:
            name: admin-service
            port:
              number: 8087
      - path: /api/analytics
        pathType: Prefix
        backend:
          service:
            name: analytics-service
            port:
              number: 8088
```

## 🚀 **CI/CD Pipeline**

### **GitHub Actions Workflow**
```yaml
# .github/workflows/microservices-ci-cd.yml
name: Microservices CI/CD

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

env:
  REGISTRY: ghcr.io
  IMAGE_NAME: ${{ github.repository }}

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Cache Gradle packages
      uses: actions/cache@v3
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
        key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
        restore-keys: |
          ${{ runner.os }}-gradle-
    
    - name: Run tests
      run: ./gradlew test
    
    - name: Generate test report
      uses: dorny/test-reporter@v1
      if: success() || failure()
      with:
        name: Test Results
        path: build/test-results/test/*.xml
        reporter: java-junit

  build:
    needs: test
    runs-on: ubuntu-latest
    strategy:
      matrix:
        service: [auth-service, user-service, ecommerce-service, shopping-service, payment-service, file-service, admin-service, analytics-service]
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Build service
      run: ./gradlew :${{ matrix.service }}:build
    
    - name: Log in to Container Registry
      uses: docker/login-action@v2
      with:
        registry: ${{ env.REGISTRY }}
        username: ${{ github.actor }}
        password: ${{ secrets.GITHUB_TOKEN }}
    
    - name: Extract metadata
      id: meta
      uses: docker/metadata-action@v4
      with:
        images: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}/${{ matrix.service }}
        tags: |
          type=ref,event=branch
          type=ref,event=pr
          type=sha,prefix={{branch}}-
          type=raw,value=latest,enable={{is_default_branch}}
    
    - name: Build and push Docker image
      uses: docker/build-push-action@v4
      with:
        context: ./${{ matrix.service }}
        push: true
        tags: ${{ steps.meta.outputs.tags }}
        labels: ${{ steps.meta.outputs.labels }}

  deploy:
    needs: build
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
    - uses: actions/checkout@v3
    
    - name: Configure kubectl
      uses: azure/k8s-set-context@v3
      with:
        method: kubeconfig
        kubeconfig: ${{ secrets.KUBE_CONFIG }}
    
    - name: Deploy to Kubernetes
      run: |
        kubectl apply -f k8s/namespace.yaml
        kubectl apply -f k8s/configmap.yaml
        kubectl apply -f k8s/secrets.yaml
        kubectl apply -f k8s/mongodb-statefulset.yaml
        kubectl apply -f k8s/redis-deployment.yaml
        kubectl apply -f k8s/auth-service-deployment.yaml
        kubectl apply -f k8s/user-service-deployment.yaml
        kubectl apply -f k8s/ecommerce-service-deployment.yaml
        kubectl apply -f k8s/shopping-service-deployment.yaml
        kubectl apply -f k8s/payment-service-deployment.yaml
        kubectl apply -f k8s/file-service-deployment.yaml
        kubectl apply -f k8s/admin-service-deployment.yaml
        kubectl apply -f k8s/analytics-service-deployment.yaml
        kubectl apply -f k8s/ingress.yaml
    
    - name: Wait for deployment
      run: |
        kubectl rollout status deployment/auth-service -n momcare-platform
        kubectl rollout status deployment/user-service -n momcare-platform
        kubectl rollout status deployment/ecommerce-service -n momcare-platform
        kubectl rollout status deployment/shopping-service -n momcare-platform
        kubectl rollout status deployment/payment-service -n momcare-platform
        kubectl rollout status deployment/file-service -n momcare-platform
        kubectl rollout status deployment/admin-service -n momcare-platform
        kubectl rollout status deployment/analytics-service -n momcare-platform
```

## 📊 **Monitoring & Observability**

### **Prometheus Configuration**
```yaml
# monitoring/prometheus-config.yml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

rule_files:
  - "alert_rules.yml"

scrape_configs:
  - job_name: 'kubernetes-pods'
    kubernetes_sd_configs:
    - role: pod
    relabel_configs:
    - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_scrape]
      action: keep
      regex: true
    - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_path]
      action: replace
      target_label: __metrics_path__
      regex: (.+)
    - source_labels: [__address__, __meta_kubernetes_pod_annotation_prometheus_io_port]
      action: replace
      regex: ([^:]+)(?::\d+)?;(\d+)
      replacement: $1:$2
      target_label: __address__
    - action: labelmap
      regex: __meta_kubernetes_pod_label_(.+)
    - source_labels: [__meta_kubernetes_namespace]
      action: replace
      target_label: kubernetes_namespace
    - source_labels: [__meta_kubernetes_pod_name]
      action: replace
      target_label: kubernetes_pod_name

  - job_name: 'momcare-services'
    static_configs:
    - targets: ['auth-service:8081', 'user-service:8082', 'ecommerce-service:8083', 'shopping-service:8084', 'payment-service:8085', 'file-service:8086', 'admin-service:8087', 'analytics-service:8088']
    metrics_path: /actuator/prometheus
    scrape_interval: 30s
```

### **Grafana Dashboard**
```json
{
  "dashboard": {
    "title": "Mom Care Platform - Microservices",
    "panels": [
      {
        "title": "Service Health",
        "type": "stat",
        "targets": [
          {
            "expr": "up{job=\"momcare-services\"}",
            "legendFormat": "{{instance}}"
          }
        ]
      },
      {
        "title": "Request Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(http_requests_total[5m])",
            "legendFormat": "{{service}}"
          }
        ]
      },
      {
        "title": "Response Time",
        "type": "graph",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m]))",
            "legendFormat": "{{service}}"
          }
        ]
      },
      {
        "title": "Error Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(http_requests_total{status=~\"5..\"}[5m])",
            "legendFormat": "{{service}}"
          }
        ]
      }
    ]
  }
}
```

## 🔧 **Deployment Scripts**

### **Build Script**
```bash
#!/bin/bash
# build-services.sh

set -e

echo "Building all microservices..."

services=("auth-service" "user-service" "ecommerce-service" "shopping-service" "payment-service" "file-service" "admin-service" "analytics-service")

for service in "${services[@]}"; do
    echo "Building $service..."
    ./gradlew :$service:build
    echo "✅ $service built successfully"
done

echo "All services built successfully!"
```

### **Deploy Script**
```bash
#!/bin/bash
# deploy-services.sh

set -e

echo "Deploying microservices to Kubernetes..."

# Apply configurations
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secrets.yaml

# Deploy infrastructure
kubectl apply -f k8s/mongodb-statefulset.yaml
kubectl apply -f k8s/redis-deployment.yaml

# Wait for infrastructure
kubectl wait --for=condition=ready pod -l app=mongodb -n momcare-platform --timeout=300s
kubectl wait --for=condition=ready pod -l app=redis -n momcare-platform --timeout=300s

# Deploy services
kubectl apply -f k8s/auth-service-deployment.yaml
kubectl apply -f k8s/user-service-deployment.yaml
kubectl apply -f k8s/ecommerce-service-deployment.yaml
kubectl apply -f k8s/shopping-service-deployment.yaml
kubectl apply -f k8s/payment-service-deployment.yaml
kubectl apply -f k8s/file-service-deployment.yaml
kubectl apply -f k8s/admin-service-deployment.yaml
kubectl apply -f k8s/analytics-service-deployment.yaml

# Deploy ingress
kubectl apply -f k8s/ingress.yaml

echo "Deployment completed successfully!"
```

### **Rollback Script**
```bash
#!/bin/bash
# rollback-services.sh

set -e

echo "Rolling back microservices..."

# Rollback deployments
kubectl rollout undo deployment/auth-service -n momcare-platform
kubectl rollout undo deployment/user-service -n momcare-platform
kubectl rollout undo deployment/ecommerce-service -n momcare-platform
kubectl rollout undo deployment/shopping-service -n momcare-platform
kubectl rollout undo deployment/payment-service -n momcare-platform
kubectl rollout undo deployment/file-service -n momcare-platform
kubectl rollout undo deployment/admin-service -n momcare-platform
kubectl rollout undo deployment/analytics-service -n momcare-platform

echo "Rollback completed successfully!"
```

---

**Note**: This deployment strategy provides a complete containerization and orchestration solution for the Mom Care Platform microservices architecture. It includes development, staging, and production environments with proper monitoring, logging, and CI/CD pipelines.
