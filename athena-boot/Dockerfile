# 运行环境：Java 21。
# pom.xml 的 java.version 为 21，原 adoptopenjdk/openjdk17 镜像无法加载目标字节码。
FROM eclipse-temurin:21-jre

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo 'Asia/Shanghai' >/etc/timezone \
    && mkdir -p /athena/prod_athena

COPY athena-boot.jar /athena/prod_athena

# 默认以 prod 启动。
# 不要在这里硬编码 dev：那会让 application-prod.yml 完全不生效，接口文档、
# DEBUG 日志、Druid 控制台都会按 dev 配置暴露。
# 本地需要覆盖时：docker run -e SPRING_PROFILES_ACTIVE=dev ...
# （Spring Boot 中环境变量优先级高于 application.yml 里的 spring.profiles.active）
ENV SPRING_PROFILES_ACTIVE=prod

CMD ["java", "-jar", "/athena/prod_athena/athena-boot.jar"]
