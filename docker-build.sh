#!/bin/sh
echo '登陆阿里云镜像, 密码: 123456qwerty'
docker login --username=mrjiangyan@aliyun.com registry.cn-hangzhou.aliyuncs.com
version=$1
serviceName=cv-management-service
targetTagName=registry.cn-hangzhou.aliyuncs.com/touchbiz/$serviceName:$version

echo "begin to build image"
echo "[exec]: docker build -t $targetTagName -f Dockerfile ."
# docker build --platform linux/amd64 -t $targetTagName .
docker build  --platform linux/amd64 -t $targetTagName .
echo "[exec]: docker push $targetTagName"
docker push $targetTagName

#docker save -o $version $targetTagName