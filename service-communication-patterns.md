# 🔄 Inter-Service Communication Patterns

## 🎯 **Communication Strategy Overview**

### **Communication Patterns**
1. **Synchronous Communication** (API calls)
2. **Asynchronous Communication** (Event-driven)
3. **Hybrid Communication** (API + Events)

## 🌐 **API Gateway Pattern**

### **Kong API Gateway Configuration**

#### **1. Gateway Setup**
```yaml
# kong.yml
_format_version: "3.0"

services:
  - name: auth-service
    url: http://auth-service:8081
    routes:
      - name: auth-routes
        paths:
          - /api/auth
        methods:
          - POST
          - GET
        plugins:
          - name: rate-limiting
            config:
              minute: 100
              hour: 1000

  - name: user-service
    url: http://user-service:8082
    routes:
      - name: user-routes
        paths:
          - /api/moms
          - /api/doctors
        methods:
          - GET
          - PUT
          - POST
        plugins:
          - name: jwt
            config:
              secret_is_base64: false
              key_claim_name: iss
              algorithm: HS256

  - name: ecommerce-service
    url: http://ecommerce-service:8083
    routes:
      - name: ecommerce-routes
        paths:
          - /api/categories
          - /api/products
          - /api/sku-offers
        methods:
          - GET
          - POST
          - PUT
          - DELETE
        plugins:
          - name: jwt
            config:
              secret_is_base64: false
              key_claim_name: iss
              algorithm: HS256

  - name: shopping-service
    url: http://shopping-service:8084
    routes:
      - name: shopping-routes
        paths:
          - /api/cart
          - /api/orders
        methods:
          - GET
          - POST
          - PUT
          - DELETE
        plugins:
          - name: jwt
            config:
              secret_is_base64: false
              key_claim_name: iss
              algorithm: HS256

  - name: payment-service
    url: http://payment-service:8085
    routes:
      - name: payment-routes
        paths:
          - /api/payments
        methods:
          - GET
          - POST
          - PUT
        plugins:
          - name: jwt
            config:
              secret_is_base64: false
              key_claim_name: iss
              algorithm: HS256

  - name: file-service
    url: http://file-service:8086
    routes:
      - name: file-routes
        paths:
          - /api/files
          - /uploads
        methods:
          - GET
          - POST
          - PUT
          - DELETE
        plugins:
          - name: jwt
            config:
              secret_is_base64: false
              key_claim_name: iss
              algorithm: HS256

  - name: admin-service
    url: http://admin-service:8087
    routes:
      - name: admin-routes
        paths:
          - /api/admin
        methods:
          - GET
          - POST
          - PUT
          - DELETE
        plugins:
          - name: jwt
            config:
              secret_is_base64: false
              key_claim_name: iss
              algorithm: HS256

  - name: analytics-service
    url: http://analytics-service:8088
    routes:
      - name: analytics-routes
        paths:
          - /api/analytics
        methods:
          - GET
          - POST
        plugins:
          - name: jwt
            config:
              secret_is_base64: false
              key_claim_name: iss
              algorithm: HS256
```

#### **2. Authentication Plugin**
```yaml
# jwt-auth-plugin.yml
plugins:
  - name: jwt
    config:
      secret_is_base64: false
      key_claim_name: iss
      algorithm: HS256
      claims_to_verify:
        - exp
        - iat
      run_on_preflight: true
```

#### **3. Rate Limiting Plugin**
```yaml
# rate-limiting-plugin.yml
plugins:
  - name: rate-limiting
    config:
      minute: 100
      hour: 1000
      day: 10000
      policy: local
      fault_tolerant: true
      hide_client_headers: false
```

## 🔄 **Event-Driven Architecture**

### **Apache Kafka Setup**

#### **1. Kafka Topics Configuration**
```yaml
# kafka-topics.yml
topics:
  - name: user-events
    partitions: 3
    replication-factor: 2
    config:
      retention.ms: 604800000  # 7 days
      cleanup.policy: delete

  - name: order-events
    partitions: 3
    replication-factor: 2
    config:
      retention.ms: 2592000000  # 30 days
      cleanup.policy: delete

  - name: payment-events
    partitions: 3
    replication-factor: 2
    config:
      retention.ms: 2592000000  # 30 days
      cleanup.policy: delete

  - name: ecommerce-events
    partitions: 3
    replication-factor: 2
    config:
      retention.ms: 604800000  # 7 days
      cleanup.policy: delete

  - name: admin-events
    partitions: 3
    replication-factor: 2
    config:
      retention.ms: 2592000000  # 30 days
      cleanup.policy: delete
```

#### **2. Event Schema Registry**
```json
{
  "schemas": [
    {
      "name": "UserRegisteredEvent",
      "type": "record",
      "fields": [
        {"name": "userId", "type": "string"},
        {"name": "userType", "type": "string"},
        {"name": "email", "type": "string"},
        {"name": "timestamp", "type": "long"}
      ]
    },
    {
      "name": "OrderCreatedEvent",
      "type": "record",
      "fields": [
        {"name": "orderId", "type": "string"},
        {"name": "momId", "type": "string"},
        {"name": "totalAmount", "type": "double"},
        {"name": "items", "type": {"type": "array", "items": "string"}},
        {"name": "timestamp", "type": "long"}
      ]
    },
    {
      "name": "PaymentProcessedEvent",
      "type": "record",
      "fields": [
        {"name": "paymentId", "type": "string"},
        {"name": "orderId", "type": "string"},
        {"name": "amount", "type": "double"},
        {"name": "status", "type": "string"},
        {"name": "timestamp", "type": "long"}
      ]
    }
  ]
}
```

### **Event Producers**

#### **1. User Service Event Producer**
```kotlin
// UserServiceEventProducer.kt
@Component
class UserServiceEventProducer(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {
    
    fun publishUserRegistered(user: User) {
        val event = UserRegisteredEvent(
            userId = user.id,
            userType = user.userType.name,
            email = user.email,
            timestamp = System.currentTimeMillis()
        )
        
        kafkaTemplate.send("user-events", user.id, event)
    }
    
    fun publishUserProfileUpdated(user: User) {
        val event = UserProfileUpdatedEvent(
            userId = user.id,
            fullName = user.fullName,
            email = user.email,
            timestamp = System.currentTimeMillis()
        )
        
        kafkaTemplate.send("user-events", user.id, event)
    }
}
```

#### **2. Order Service Event Producer**
```kotlin
// OrderServiceEventProducer.kt
@Component
class OrderServiceEventProducer(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {
    
    fun publishOrderCreated(order: Order) {
        val event = OrderCreatedEvent(
            orderId = order.id,
            momId = order.momId,
            totalAmount = order.grandTotal,
            items = order.items.map { it.skuId },
            timestamp = System.currentTimeMillis()
        )
        
        kafkaTemplate.send("order-events", order.id, event)
    }
    
    fun publishOrderStatusUpdated(order: Order) {
        val event = OrderStatusUpdatedEvent(
            orderId = order.id,
            momId = order.momId,
            status = order.status,
            timestamp = System.currentTimeMillis()
        )
        
        kafkaTemplate.send("order-events", order.id, event)
    }
}
```

### **Event Consumers**

#### **1. Analytics Service Event Consumer**
```kotlin
// AnalyticsEventConsumer.kt
@Component
class AnalyticsEventConsumer(
    private val analyticsService: AnalyticsService
) {
    
    @KafkaListener(topics = ["user-events", "order-events", "payment-events"])
    fun handleEvent(event: Any) {
        when (event) {
            is UserRegisteredEvent -> {
                analyticsService.recordUserRegistration(event)
            }
            is OrderCreatedEvent -> {
                analyticsService.recordOrderCreation(event)
            }
            is PaymentProcessedEvent -> {
                analyticsService.recordPayment(event)
            }
        }
    }
}
```

#### **2. E-commerce Service Event Consumer**
```kotlin
// EcommerceEventConsumer.kt
@Component
class EcommerceEventConsumer(
    private val inventoryService: InventoryService
) {
    
    @KafkaListener(topics = ["order-events"])
    fun handleOrderEvent(event: Any) {
        when (event) {
            is OrderCreatedEvent -> {
                // Update inventory
                event.items.forEach { skuId ->
                    inventoryService.decreaseStock(skuId, 1)
                }
            }
            is OrderCancelledEvent -> {
                // Restore inventory
                event.items.forEach { skuId ->
                    inventoryService.increaseStock(skuId, 1)
                }
            }
        }
    }
}
```

## 🔗 **Service-to-Service Communication**

### **HTTP Client Configuration**

#### **1. Feign Client Setup**
```kotlin
// UserServiceClient.kt
@FeignClient(
    name = "user-service",
    url = "\${services.user-service.url}",
    configuration = [FeignConfig::class]
)
interface UserServiceClient {
    
    @GetMapping("/api/moms/{momId}")
    fun getMom(@PathVariable momId: String): Mom
    
    @GetMapping("/api/doctors/{doctorId}")
    fun getDoctor(@PathVariable doctorId: String): Doctor
    
    @PutMapping("/api/moms/{momId}/sessions")
    fun updateMomSessions(
        @PathVariable momId: String,
        @RequestBody sessionUpdate: SessionUpdateRequest
    ): BasicApiResponse<Mom>
}
```

#### **2. Circuit Breaker Configuration**
```kotlin
// CircuitBreakerConfig.kt
@Configuration
class CircuitBreakerConfig {
    
    @Bean
    fun userServiceCircuitBreaker(): CircuitBreaker {
        return CircuitBreaker.ofDefaults("user-service")
            .toBuilder()
            .slidingWindowSize(10)
            .minimumNumberOfCalls(5)
            .failureRateThreshold(50.0f)
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .build()
    }
    
    @Bean
    fun ecommerceServiceCircuitBreaker(): CircuitBreaker {
        return CircuitBreaker.ofDefaults("ecommerce-service")
            .toBuilder()
            .slidingWindowSize(10)
            .minimumNumberOfCalls(5)
            .failureRateThreshold(50.0f)
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .build()
    }
}
```

#### **3. Retry Configuration**
```kotlin
// RetryConfig.kt
@Configuration
class RetryConfig {
    
    @Bean
    fun retryTemplate(): RetryTemplate {
        return RetryTemplate.builder()
            .maxAttempts(3)
            .exponentialBackoff(1000, 2.0, 10000)
            .retryOn(ConnectException::class.java)
            .retryOn(SocketTimeoutException::class.java)
            .build()
    }
}
```

### **Service Discovery**

#### **1. Consul Configuration**
```yaml
# consul-config.yml
services:
  - name: auth-service
    port: 8081
    check:
      http: http://localhost:8081/health
      interval: 10s
      timeout: 3s
      deregister_critical_service_after: 30s

  - name: user-service
    port: 8082
    check:
      http: http://localhost:8082/health
      interval: 10s
      timeout: 3s
      deregister_critical_service_after: 30s

  - name: ecommerce-service
    port: 8083
    check:
      http: http://localhost:8083/health
      interval: 10s
      timeout: 3s
      deregister_critical_service_after: 30s

  - name: shopping-service
    port: 8084
    check:
      http: http://localhost:8084/health
      interval: 10s
      timeout: 3s
      deregister_critical_service_after: 30s

  - name: payment-service
    port: 8085
    check:
      http: http://localhost:8085/health
      interval: 10s
      timeout: 3s
      deregister_critical_service_after: 30s

  - name: file-service
    port: 8086
    check:
      http: http://localhost:8086/health
      interval: 10s
      timeout: 3s
      deregister_critical_service_after: 30s

  - name: admin-service
    port: 8087
    check:
      http: http://localhost:8087/health
      interval: 10s
      timeout: 3s
      deregister_critical_service_after: 30s

  - name: analytics-service
    port: 8088
    check:
      http: http://localhost:8088/health
      interval: 10s
      timeout: 3s
      deregister_critical_service_after: 30s
```

#### **2. Service Discovery Client**
```kotlin
// ServiceDiscoveryClient.kt
@Component
class ServiceDiscoveryClient(
    private val consulClient: ConsulClient
) {
    
    fun getServiceUrl(serviceName: String): String {
        val services = consulClient.healthClient().getHealthyServiceInstances(serviceName)
        if (services.response.isEmpty()) {
            throw ServiceUnavailableException("Service $serviceName not available")
        }
        
        val service = services.response.random()
        return "http://${service.service.address}:${service.service.port}"
    }
    
    fun getServiceInstances(serviceName: String): List<ServiceInstance> {
        val services = consulClient.healthClient().getHealthyServiceInstances(serviceName)
        return services.response.map { service ->
            ServiceInstance(
                host = service.service.address,
                port = service.service.port,
                serviceId = service.service.id
            )
        }
    }
}
```

## 📊 **Communication Monitoring**

### **Distributed Tracing**

#### **1. Jaeger Configuration**
```yaml
# jaeger-config.yml
tracing:
  service-name: momcare-platform
  sampler:
    type: const
    param: 1
  reporter:
    log-spans: true
    agent-host: jaeger-agent
    agent-port: 6831
  headers:
    trace-context-header-name: trace-id
    baggage-header-prefix: baggage-
```

#### **2. Tracing Interceptor**
```kotlin
// TracingInterceptor.kt
@Component
class TracingInterceptor(
    private val tracer: Tracer
) : HandlerInterceptor {
    
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val span = tracer.nextSpan()
            .name("${request.method} ${request.requestURI}")
            .tag("http.method", request.method)
            .tag("http.url", request.requestURL.toString())
            .start()
        
        tracer.withSpanInScope(span)
        request.setAttribute("span", span)
        
        return true
    }
    
    override fun afterCompletion(request: HttpServletRequest, response: HttpServletResponse, handler: Any, ex: Exception?) {
        val span = request.getAttribute("span") as Span
        span.tag("http.status_code", response.status.toString())
        
        if (ex != null) {
            span.tag("error", true)
            span.tag("error.message", ex.message)
        }
        
        span.end()
    }
}
```

### **Metrics Collection**

#### **1. Prometheus Metrics**
```kotlin
// MetricsConfig.kt
@Configuration
class MetricsConfig {
    
    @Bean
    fun httpRequestDuration(): Timer {
        return Timer.builder("http_request_duration")
            .description("HTTP request duration")
            .register(Metrics.globalRegistry)
    }
    
    @Bean
    fun serviceCallCounter(): Counter {
        return Counter.builder("service_calls_total")
            .description("Total service calls")
            .tag("service", "unknown")
            .register(Metrics.globalRegistry)
    }
    
    @Bean
    fun circuitBreakerState(): Gauge {
        return Gauge.builder("circuit_breaker_state")
            .description("Circuit breaker state")
            .tag("service", "unknown")
            .register(Metrics.globalRegistry)
    }
}
```

#### **2. Custom Metrics**
```kotlin
// ServiceMetrics.kt
@Component
class ServiceMetrics {
    
    private val orderCreationCounter = Counter.builder("orders_created_total")
        .description("Total orders created")
        .register(Metrics.globalRegistry)
    
    private val paymentProcessingTime = Timer.builder("payment_processing_duration")
        .description("Payment processing duration")
        .register(Metrics.globalRegistry)
    
    fun recordOrderCreation() {
        orderCreationCounter.increment()
    }
    
    fun recordPaymentProcessing(duration: Duration) {
        paymentProcessingTime.record(duration)
    }
}
```

## 🔒 **Security in Communication**

### **JWT Token Propagation**

#### **1. Token Interceptor**
```kotlin
// TokenPropagationInterceptor.kt
@Component
class TokenPropagationInterceptor : RequestInterceptor {
    
    override fun apply(template: RequestTemplate) {
        val request = RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes
        val token = request.request.getHeader("Authorization")
        
        if (token != null) {
            template.header("Authorization", token)
        }
    }
}
```

#### **2. Service-to-Service Authentication**
```kotlin
// ServiceAuthConfig.kt
@Configuration
class ServiceAuthConfig {
    
    @Bean
    fun serviceAuthFilter(): FilterRegistrationBean<ServiceAuthFilter> {
        val registration = FilterRegistrationBean<ServiceAuthFilter>()
        registration.filter = ServiceAuthFilter()
        registration.addUrlPatterns("/api/*")
        registration.order = 1
        return registration
    }
}

class ServiceAuthFilter : Filter {
    
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpRequest = request as HttpServletRequest
        val token = httpRequest.getHeader("Authorization")
        
        if (token != null && token.startsWith("Bearer ")) {
            // Validate token and extract service claims
            val serviceClaims = validateServiceToken(token)
            if (serviceClaims != null) {
                // Add service context to request
                httpRequest.setAttribute("serviceContext", serviceClaims)
            }
        }
        
        chain.doFilter(request, response)
    }
}
```

### **API Rate Limiting**

#### **1. Redis-based Rate Limiting**
```kotlin
// RateLimitingService.kt
@Service
class RateLimitingService(
    private val redisTemplate: RedisTemplate<String, String>
) {
    
    fun isAllowed(serviceName: String, endpoint: String, limit: Int, window: Duration): Boolean {
        val key = "rate_limit:$serviceName:$endpoint"
        val current = redisTemplate.opsForValue().increment(key)
        
        if (current == 1L) {
            redisTemplate.expire(key, window)
        }
        
        return current <= limit
    }
}
```

#### **2. Rate Limiting Filter**
```kotlin
// RateLimitingFilter.kt
@Component
class RateLimitingFilter(
    private val rateLimitingService: RateLimitingService
) : Filter {
    
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpRequest = request as HttpServletRequest
        val httpResponse = response as HttpServletResponse
        
        val serviceName = extractServiceName(httpRequest)
        val endpoint = httpRequest.requestURI
        
        if (!rateLimitingService.isAllowed(serviceName, endpoint, 100, Duration.ofMinutes(1))) {
            httpResponse.status = HttpStatus.TOO_MANY_REQUESTS.value()
            httpResponse.writer.write("Rate limit exceeded")
            return
        }
        
        chain.doFilter(request, response)
    }
}
```

## 🚀 **Performance Optimization**

### **Connection Pooling**

#### **1. HTTP Client Pool Configuration**
```kotlin
// HttpClientConfig.kt
@Configuration
class HttpClientConfig {
    
    @Bean
    fun httpClient(): CloseableHttpClient {
        return HttpClients.custom()
            .setMaxConnTotal(100)
            .setMaxConnPerRoute(20)
            .setConnectionTimeToLive(30, TimeUnit.SECONDS)
            .setDefaultRequestConfig(
                RequestConfig.custom()
                    .setConnectTimeout(5000)
                    .setSocketTimeout(10000)
                    .build()
            )
            .build()
    }
}
```

#### **2. Database Connection Pool**
```yaml
# application.yml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/momcare_db
      options:
        max-connection-pool-size: 100
        min-connection-pool-size: 10
        max-connection-idle-time: 30000
        max-connection-life-time: 60000
```

### **Caching Strategy**

#### **1. Redis Cache Configuration**
```kotlin
// CacheConfig.kt
@Configuration
@EnableCaching
class CacheConfig {
    
    @Bean
    fun cacheManager(): CacheManager {
        val config = RedisCacheManager.RedisCacheManagerBuilder
            .fromConnectionFactory(redisConnectionFactory())
            .cacheDefaults(cacheConfiguration(Duration.ofMinutes(10)))
            .build()
        
        return config
    }
    
    private fun cacheConfiguration(ttl: Duration): RedisCacheConfiguration {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(ttl)
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(GenericJackson2JsonRedisSerializer()))
    }
}
```

#### **2. Service Response Caching**
```kotlin
// CachedUserService.kt
@Service
class CachedUserService(
    private val userServiceClient: UserServiceClient
) {
    
    @Cacheable(value = ["users"], key = "#userId")
    fun getUser(userId: String): User {
        return userServiceClient.getUser(userId)
    }
    
    @CacheEvict(value = ["users"], key = "#user.id")
    fun updateUser(user: User): User {
        return userServiceClient.updateUser(user)
    }
}
```

## 📋 **Communication Patterns Summary**

### **When to Use Each Pattern**

#### **Synchronous Communication (API Calls)**
- **Use for**: Real-time data access, immediate responses
- **Examples**: User authentication, order validation, payment processing
- **Trade-offs**: Higher latency, tighter coupling, failure propagation

#### **Asynchronous Communication (Events)**
- **Use for**: Background processing, data synchronization, notifications
- **Examples**: Analytics updates, inventory management, audit logging
- **Trade-offs**: Eventual consistency, complexity, debugging challenges

#### **Hybrid Communication**
- **Use for**: Critical operations with background processing
- **Examples**: Order creation (sync) + inventory update (async)
- **Trade-offs**: Best of both worlds, but increased complexity

### **Best Practices**

1. **Circuit Breakers**: Implement for all external service calls
2. **Retry Logic**: Use exponential backoff for transient failures
3. **Timeouts**: Set appropriate timeouts for all service calls
4. **Monitoring**: Track all inter-service communication
5. **Security**: Validate all service-to-service communication
6. **Caching**: Cache frequently accessed data
7. **Rate Limiting**: Implement rate limiting for all services
8. **Tracing**: Use distributed tracing for debugging

---

**Note**: This communication strategy should be implemented gradually, starting with the most critical services and expanding to cover all inter-service communication.
