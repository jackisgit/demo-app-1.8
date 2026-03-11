# K8s集群部署文档

## 服务器信息

### 集群节点
| 节点名称 | IP地址 | 用户名 | 密码 | 角色 |
|---------|--------|--------|------|------|
| master | 10.211.55.6 | root | 12315 | control-plane |
| node1 | 10.211.55.7 | root | 12315 | worker |
| node2 | 10.211.55.8 | root | 12315 | worker |

### K8s版本
- Kubernetes: v1.30.14
- Container Runtime: containerd://2.2.1
- OS: CentOS Stream 9
- Kernel: 5.14.0-687.el9.aarch64

---

## 数据库配置

### MySQL (本地MCP服务)
- **地址**: 10.211.55.2 (主机IP)
- **端口**: 3306
- **用户名**: root
- **密码**: dabao520
- **数据库**: demo_db
- **授权**: 已授权 10.211.55.% 网段访问

### Redis (本地服务)
- **地址**: 10.211.55.2 (主机IP)
- **端口**: 6379
- **密码**: 无
- **配置**: bind 0.0.0.0, protected-mode no

---

## 应用部署信息

### 当前部署
- **应用名称**: demo-app
- **命名空间**: default
- **镜像**: demo-app:2.0.0
- **副本数**: 2
- **Service类型**: ClusterIP
- **Service端口**: 8080

### 环境变量配置
```yaml
env:
  - name: SPRING_DATASOURCE_URL
    value: jdbc:mysql://10.211.55.2:3306/demo_db?useSSL=false&serverTimezone=UTC&characterEncoding=utf-8&allowPublicKeyRetrieval=true
  - name: SPRING_DATASOURCE_USERNAME
    value: root
  - name: SPRING_DATASOURCE_PASSWORD
    value: dabao520
  - name: SPRING_DATA_REDIS_HOST
    value: 10.211.55.2
  - name: SPRING_DATA_REDIS_PORT
    value: "6379"
  - name: SERVER_PORT
    value: "8080"
```

---

## 发版流程

### 1. 本地编译
```bash
cd /Users/abao/IdeaProjects/demo-app-1
./mvnw clean package -DskipTests
```

### 2. 上传JAR包到Master节点
```bash
sshpass -p '12315' scp target/demo-app-1.0.0.jar root@10.211.55.6:/tmp/
```

### 3. 构建Docker镜像 (在Master节点)
```bash
# 创建Dockerfile
cat > /tmp/Dockerfile << 'EOF'
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY demo-app-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
EOF

# 构建镜像 (使用新版本号)
docker build -t demo-app:<NEW_VERSION> /tmp/
```

### 4. 导出镜像并分发到所有节点
```bash
# 导出镜像
docker save demo-app:<NEW_VERSION> > /tmp/demo-app-<NEW_VERSION>.tar

# 分发到node1和node2
sshpass -p '12315' scp /tmp/demo-app-<NEW_VERSION>.tar root@10.211.55.7:/tmp/
sshpass -p '12315' scp /tmp/demo-app-<NEW_VERSION>.tar root@10.211.55.8:/tmp/
```

### 5. 导入镜像到containerd (在所有节点执行)
```bash
# 在node1执行
sshpass -p '12315' ssh root@10.211.55.7 "ctr -n k8s.io images import /tmp/demo-app-<NEW_VERSION>.tar"

# 在node2执行
sshpass -p '12315' ssh root@10.211.55.8 "ctr -n k8s.io images import /tmp/demo-app-<NEW_VERSION>.tar"
```

### 6. 更新K8s Deployment
```bash
# 更新镜像版本
sshpass -p '12315' ssh root@10.211.55.6 "kubectl set image deployment/demo-app demo-app=demo-app:<NEW_VERSION> -n default"

# 查看滚动更新状态
sshpass -p '12315' ssh root@10.211.55.6 "kubectl rollout status deployment/demo-app -n default"
```

### 7. 验证部署
```bash
# 查看Pod状态
sshpass -p '12315' ssh root@10.211.55.6 "kubectl get pods -l app=demo-app -n default"

# 查看日志
sshpass -p '12315' ssh root@10.211.55.6 "kubectl logs -l app=demo-app -n default --tail=50"

# 测试API
sshpass -p '12315' ssh root@10.211.55.6 "kubectl exec <POD_NAME> -n default -- curl -s http://localhost:8080/api/hello"
sshpass -p '12315' ssh root@10.211.55.6 "kubectl exec <POD_NAME> -n default -- curl -s http://localhost:8080/api/users/all"
```

---

## 快速发版脚本

### 一键发版脚本 (deploy.sh)
```bash
#!/bin/bash
VERSION=$1

if [ -z "$VERSION" ]; then
  echo "Usage: ./deploy.sh <version>"
  echo "Example: ./deploy.sh 3.0.0"
  exit 1
fi

echo "=== 开始发版 demo-app:$VERSION ==="

# 1. 编译
echo "[1/7] 编译项目..."
./mvnw clean package -DskipTests

# 2. 上传JAR包
echo "[2/7] 上传JAR包..."
sshpass -p '12315' scp target/demo-app-1.0.0.jar root@10.211.55.6:/tmp/

# 3. 构建镜像
echo "[3/7] 构建Docker镜像..."
sshpass -p '12315' ssh root@10.211.55.6 "cd /tmp && docker build -t demo-app:$VERSION ."

# 4. 导出镜像
echo "[4/7] 导出镜像..."
sshpass -p '12315' ssh root@10.211.55.6 "docker save demo-app:$VERSION > /tmp/demo-app-$VERSION.tar"

# 5. 分发镜像
echo "[5/7] 分发镜像到所有节点..."
sshpass -p '12315' ssh root@10.211.55.6 "scp /tmp/demo-app-$VERSION.tar root@10.211.55.7:/tmp/"
sshpass -p '12315' ssh root@10.211.55.6 "scp /tmp/demo-app-$VERSION.tar root@10.211.55.8:/tmp/"

# 6. 导入到containerd
echo "[6/7] 导入镜像到containerd..."
sshpass -p '12315' ssh root@10.211.55.7 "ctr -n k8s.io images import /tmp/demo-app-$VERSION.tar"
sshpass -p '12315' ssh root@10.211.55.8 "ctr -n k8s.io images import /tmp/demo-app-$VERSION.tar"

# 7. 更新Deployment
echo "[7/7] 更新K8s Deployment..."
sshpass -p '12315' ssh root@10.211.55.6 "kubectl set image deployment/demo-app demo-app=demo-app:$VERSION -n default"

echo "=== 发版完成! ==="
echo "查看状态: sshpass -p '12315' ssh root@10.211.55.6 'kubectl get pods -l app=demo-app -n default'"
```

---

## 常用命令

### 查看集群状态
```bash
# 查看节点
sshpass -p '12315' ssh root@10.211.55.6 "kubectl get nodes -o wide"

# 查看所有Pod
sshpass -p '12315' ssh root@10.211.55.6 "kubectl get pods -A"

# 查看demo-app相关资源
sshpass -p '12315' ssh root@10.211.55.6 "kubectl get all -l app=demo-app -n default"
```

### 查看日志
```bash
# 查看应用日志
sshpass -p '12315' ssh root@10.211.55.6 "kubectl logs -l app=demo-app -n default --tail=100"

# 实时查看日志
sshpass -p '12315' ssh root@10.211.55.6 "kubectl logs -f -l app=demo-app -n default"
```

### 重启应用
```bash
# 滚动重启
sshpass -p '12315' ssh root@10.211.55.6 "kubectl rollout restart deployment/demo-app -n default"

# 查看重启状态
sshpass -p '12315' ssh root@10.211.55.6 "kubectl rollout status deployment/demo-app -n default"
```

### 回滚
```bash
# 查看历史版本
sshpass -p '12315' ssh root@10.211.55.6 "kubectl rollout history deployment/demo-app -n default"

# 回滚到上一版本
sshpass -p '12315' ssh root@10.211.55.6 "kubectl rollout undo deployment/demo-app -n default"

# 回滚到指定版本
sshpass -p '12315' ssh root@10.211.55.6 "kubectl rollout undo deployment/demo-app -n default --to-revision=2"
```

---

## 注意事项

1. **镜像必须导入到containerd**: K8s使用containerd作为容器运行时,必须使用`ctr -n k8s.io images import`导入镜像
2. **版本号必须递增**: 每次发版使用新的版本号,避免镜像ID相同导致不更新
3. **imagePullPolicy**: 当前设置为Never,不会从远程仓库拉取镜像
4. **数据库连接**: 确保MySQL和Redis已授权虚拟机访问
5. **环境变量**: 注意`spring.data.redis.host`和`spring.redis.host`的区别

---

## 故障排查

### Pod无法启动
```bash
# 查看Pod详情
kubectl describe pod <POD_NAME> -n default

# 查看事件
kubectl get events -n default --sort-by='.lastTimestamp'
```

### 镜像拉取失败
```bash
# 检查containerd中的镜像
crictl images | grep demo-app

# 重新导入镜像
ctr -n k8s.io images import /tmp/demo-app-<VERSION>.tar
```

### 应用连接数据库失败
```bash
# 检查环境变量
kubectl exec <POD_NAME> -n default -- env | grep SPRING

# 测试数据库连接
kubectl exec <POD_NAME> -n default -- nc -zv 10.211.55.2 3306
kubectl exec <POD_NAME> -n default -- nc -zv 10.211.55.2 6379
```

---

## 联系信息

- **项目路径**: /Users/abao/IdeaProjects/demo-app-1
- **文档更新时间**: 2026-03-11
- **当前版本**: demo-app:2.0.0
