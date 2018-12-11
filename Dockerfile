FROM apache/nifi:1.8.0

ENV NIFI_BASE_DIR=/opt/nifi
ENV NIFI_HOME ${NIFI_BASE_DIR}/nifi-current
ENV NIFI_INPUT_PATH ${NIFI_HOME}/input
ENV NIFI_OUTPUT_PATH ${NIFI_HOME}/output
ENV NIFI_LIB_DIR ${NIFI_HOME}/lib
ENV NIFI_CUSTOM_NAR=nifi-converter-nar-1.0-SNAPSHOT.nar

ENV GITHUB_BASE_URL=https://raw.github.com/test-data/avro-converter/master/
ENV NAR_NAME=nifi-converter-nar-1.0-SNAPSHOT.nar
ENV GITHUB_RAW_NAR ${GITHUB_BASE_URL}/avro-converter/nifi-converter-nar/target/${NAR_NAME}
ENV GITHUB_RAW_INPUT_FILE ${GITHUB_BASE_URL}/titanic.csv
ENV GITHUB_RAW_TEMPLATE ${GITHUB_BASE_URL}/flow.xml.gz

# Download NAR for Apache NiFi binary.
RUN curl -fSL ${GITHUB_RAW_NAR} -o $NIFI_LIB_DIR/${NAR_NAME} \
&& mkdir -p ${NIFI_INPUT_PATH} \
&& mkdir -p ${NIFI_OUTPUT_PATH}

# Download input csv file & template Apache NiFi
RUN curl -fSL ${GITHUB_RAW_INPUT_FILE} -o ${NIFI_INPUT_PATH}/titanic.csv \
&& curl -fSL ${GITHUB_RAW_TEMPLATE} -o ${NIFI_HOME}/conf/flow.xml.gz

# Clear nifi-env.sh in favour of configuring all environment variables in the Dockerfile
RUN echo "#!/bin/sh\n" > $NIFI_HOME/bin/nifi-env.sh

EXPOSE 8080

WORKDIR ${NIFI_HOME}

# Apply configuration and start NiFi
#
# We need to use the exec form to avoid running our command in a subshell and omitting signals,
# thus being unable to shut down gracefully:
# https://docs.docker.com/engine/reference/builder/#entrypoint
#
# Also we need to use relative path, because the exec form does not invoke a command shell,
# thus normal shell processing does not happen:
# https://docs.docker.com/engine/reference/builder/#exec-form-entrypoint-example
ENTRYPOINT ["../scripts/start.sh"]