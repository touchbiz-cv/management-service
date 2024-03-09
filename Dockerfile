FROM apache/beam_java17_sdk
#FROM registry.cn-hangzhou.aliyuncs.com/aiprime-backend-v2/java:apache-beam_java17_sdk

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai  /etc/localtime

RUN ls -l /


# 移动文件到目标文件夹
ARG APP_LOCATION=jeecg-server-cloud/jeecg-system-cloud-start/target/management-service.jar


ADD ${APP_LOCATION} /