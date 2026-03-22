# 🚀 Mom Care Platform CI/CD Pipeline

This document describes the comprehensive CI/CD pipeline setup for the Mom Care Platform backend.

## 📋 Overview

The CI/CD pipeline automates the entire software development lifecycle from code commit to production deployment.

## 🔄 Pipeline Stages

### 1. 🧪 Test & Quality Check
- **Code Quality**: Checkstyle, SpotBugs analysis
- **Unit Tests**: All 50+ test cases run automatically
- **Code Coverage**: JaCoCo reports with minimum 80% coverage
- **Build Verification**: Ensures code compiles and packages correctly

### 2. 🔒 Security Scan
- **Dependency Check**: OWASP vulnerability scanning
- **Security Analysis**: Identifies potential security issues
- **License Compliance**: Ensures all dependencies are properly licensed

### 3. ⚡ Performance Testing
- **Load Testing**: API endpoint performance validation
- **Resource Monitoring**: Memory and CPU usage analysis
- **Response Time**: Ensures APIs meet performance requirements

### 4. 🚀 Deployment
- **Staging**: Automatic deployment to staging environment
- **Production**: Manual approval required for production deployment
- **Health Checks**: Automated verification of deployment success

## 🛠️ Technologies Used

- **GitHub Actions**: CI/CD orchestration
- **Docker**: Containerization
- **Kubernetes**: Container orchestration
- **Nginx**: Reverse proxy and load balancing
- **MongoDB**: Database
- **Redis**: Caching
- **Prometheus**: Monitoring
- **Grafana**: Dashboards

## 📁 File Structure

```
├── .github/workflows/
│   └── ci-cd.yml                 # Main CI/CD pipeline
├── k8s/
│   ├── staging/
│   │   └── deployment.yaml       # Staging Kubernetes config
│   └── production/
│       └── deployment.yaml       # Production Kubernetes config
├── docker-compose.yml            # Local development setup
├── Dockerfile                    # Application containerization
├── nginx.conf                    # Nginx configuration
├── prometheus.yml                # Monitoring configuration
├── mongo-init.js                 # Database initialization
└── deploy.sh                     # Deployment script
```

## 🚀 Getting Started

### Prerequisites
- Docker and Docker Compose
- Kubernetes cluster (for production)
- GitHub repository with Actions enabled

### Local Development
```bash
# Start all services locally
docker-compose up -d

# View logs
docker-compose logs -f mom-care-backend

# Stop services
docker-compose down
```

### Manual Deployment
```bash
# Deploy to staging
./deploy.sh latest staging

# Deploy to production
./deploy.sh latest production
```

## 🔧 Configuration

### Environment Variables
- `MONGODB_URI`: Database connection string
- `JWT_SECRET`: JWT signing secret
- `JWT_ISSUER`: JWT issuer claim
- `JWT_AUDIENCE`: JWT audience claim
- `ENVIRONMENT`: Deployment environment (staging/production)

### Secrets Management
Secrets are managed through GitHub Secrets and Kubernetes Secrets:
- `JWT_SECRET`: JWT signing key
- `MONGODB_URI`: Database connection string
- SSL certificates for HTTPS

## 📊 Monitoring

### Health Endpoints
- `GET /health`: Overall service health
- `GET /health/ready`: Readiness check
- `GET /health/live`: Liveness check

### Metrics
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)

### Logs
- Application logs: `docker-compose logs mom-care-backend`
- Nginx logs: `docker-compose logs nginx`
- Database logs: `docker-compose logs mongodb`

## 🔄 Workflow Triggers

### Automatic Triggers
- **Push to `develop`**: Deploy to staging
- **Push to `main`**: Deploy to production (after tests pass)
- **Pull Request**: Run tests and quality checks

### Manual Triggers
- **Workflow Dispatch**: Manual pipeline execution
- **Release**: Tag-based deployments

## 🚨 Alerts and Notifications

### Success Notifications
- ✅ Tests passing
- ✅ Deployment successful
- ✅ Health checks passing

### Failure Notifications
- ❌ Test failures
- ❌ Build failures
- ❌ Deployment failures
- ❌ Health check failures

## 🔒 Security Features

### Network Security
- HTTPS enforcement
- Rate limiting
- CORS configuration
- Security headers

### Container Security
- Non-root user execution
- Read-only root filesystem
- Minimal base images
- Security scanning

### Application Security
- JWT authentication
- Input validation
- SQL injection prevention
- XSS protection

## 📈 Performance Optimization

### Caching
- Redis for session storage
- Nginx for static content
- Application-level caching

### Scaling
- Horizontal Pod Autoscaler (HPA)
- Load balancing
- Resource limits and requests

### Monitoring
- CPU and memory usage
- Response times
- Error rates
- Throughput metrics

## 🛠️ Troubleshooting

### Common Issues

#### Build Failures
```bash
# Check build logs
./gradlew build --info

# Clean and rebuild
./gradlew clean build
```

#### Deployment Issues
```bash
# Check container status
docker-compose ps

# View container logs
docker-compose logs mom-care-backend

# Restart services
docker-compose restart
```

#### Health Check Failures
```bash
# Test health endpoint
curl http://localhost:8080/health

# Check service connectivity
docker-compose exec mom-care-backend curl localhost:8080/health
```

### Debug Commands
```bash
# Enter container
docker-compose exec mom-care-backend bash

# Check database connectivity
docker-compose exec mom-care-backend curl mongodb:27017

# View network configuration
docker network ls
docker network inspect momproj_momcare-network
```

## 📚 Additional Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Docker Documentation](https://docs.docker.com/)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Nginx Documentation](https://nginx.org/en/docs/)
- [Prometheus Documentation](https://prometheus.io/docs/)

## 🤝 Contributing

1. Create a feature branch
2. Make your changes
3. Ensure all tests pass
4. Create a pull request
5. Wait for CI/CD pipeline to complete
6. Merge after approval

## 📞 Support

For issues with the CI/CD pipeline:
1. Check the GitHub Actions logs
2. Review the troubleshooting section
3. Create an issue in the repository
4. Contact the DevOps team

---

**Happy Deploying! 🚀**
