FROM 729964090428.dkr.ecr.us-east-1.amazonaws.com/jboss-fuse-6/fis-java-openshift:latest
WORKDIR /

USER root
ADD ca/* /etc/pki/ca-trust/source/anchors/
RUN update-ca-trust

RUN mkdir /deployments || true
ADD ./target/subscription-billing-history-1.0.2-SNAPSHOT.jar /deployments
ENV JAVA_APP_DIR=/deployments
COPY docker/entrypoint.sh ./

RUN chmod +x *.sh
USER jboss
ENTRYPOINT ["/entrypoint.sh"]
EXPOSE 8080 8081 8778 9779


