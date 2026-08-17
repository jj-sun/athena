FROM adoptopenjdk/openjdk17:latest
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
RUN echo 'Asia/Shanghai' >/etc/timezone
RUN mkdir -p /athena/prod_athena
COPY athena-boot.jar /athena/prod_athena
CMD ["java", "-jar", "/athena/prod_athena/athena-boot.jar", "--spring.profiles.active=dev"]