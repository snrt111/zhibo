FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 第一步：先安装基础工具（curl/tzdata），这层更新频率可能更高
RUN apk add --no-cache curl tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

# 第二步：单独安装ffmpeg，只要这行指令不变，就会复用缓存，不重复下载
RUN apk add --no-cache ffmpeg

ENV LANG=zh_CN.UTF-8
ENV LANGUAGE=zh_CN:zh
ENV LC_ALL=zh_CN.UTF-8

COPY target/backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8"

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -sf http://localhost:8081/api/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
