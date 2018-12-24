FROM maven:3.5.3-jdk-8 as maven

### Clone and build AtomGraph Core (2.0.1-SNAPSHOT is not on Maven central)

WORKDIR /usr/src

RUN git clone https://github.com/AtomGraph/Core.git

WORKDIR /usr/src/Core

RUN mvn clean install

### Clone and Build AtomGraph Web-Client

WORKDIR /usr/src

RUN git clone https://github.com/AtomGraph/Web-Client.git

WORKDIR /usr/src/Web-Client

RUN mvn -Pstandalone clean install

### Clone and build our fork of SPIN RDF API

WORKDIR /usr/src

RUN git clone https://github.com/AtomGraph/spinrdf.git

WORKDIR /usr/src/spinrdf

RUN mvn clean install

### Clone and build AtomGraph Processor

WORKDIR /usr/src

RUN git clone https://github.com/AtomGraph/Processor.git

WORKDIR /usr/src/Processor

RUN mvn -Pdependency clean install

### Build AtomGraph Web-Node

WORKDIR /usr/src/Web-Node

COPY . /usr/src/Web-Node

RUN mvn clean install

### Deploy Web-Node webapp on Tomcat

FROM tomcat:8.0.52-jre8

ARG VERSION=node-1.1.4-SNAPSHOT

WORKDIR /usr/local/tomcat/webapps/

RUN rm -rf * # remove Tomcat's default webapps

# copy exploded WAR folder from the maven stage
COPY --from=maven /usr/src/Web-Node/target/$VERSION/ ROOT/

WORKDIR $CATALINA_HOME

COPY src/main/webapp/META-INF/context.xml conf/Catalina/localhost/ROOT.xml

### Install XSLT processor

RUN apt-get update && \
  apt-get -y install xsltproc

### Copy entrypoint

COPY entrypoint.sh entrypoint.sh

RUN chmod +x entrypoint.sh

COPY context.xsl conf/Catalina/localhost/context.xsl

ENTRYPOINT ["/usr/local/tomcat/entrypoint.sh"]

EXPOSE 8080

# system location mapping
ENV LOCATION_MAPPING="/usr/local/tomcat/webapps/ROOT/WEB-INF/classes/location-mapping.n3"

# user-defined location mapping
ENV CUSTOM_LOCATION_MAPPING="/usr/local/tomcat/webapps/ROOT/WEB-INF/classes/custom-mapping.n3"