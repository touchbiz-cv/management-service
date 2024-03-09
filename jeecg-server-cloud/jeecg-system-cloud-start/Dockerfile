FROM apache/beam_java17_sdk:latest
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime


WORKDIR /jeecg-system-cloud

# 移动文件到目标文件夹
ARG APP_LOCATION=./target/management-service.jar

ADD ${APP_LOCATION} /
