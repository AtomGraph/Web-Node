# Web-Node
Includes both the Processor/Server and the Web-Client

Nodes can manage each others RDF data in a distributed way, using [LDT hypermedia](https://atomgraph.github.io/Linked-Data-Templates/#hypermedia) as the read-write Linked Data protocol.
Web-Node processes LDT ontologies and serves/accepts RDF data as the [Processor](../../../Processor), but also renders that data, as well RDF data from external sources, as the [Web-Client](../../../Web-Client) does.

![AtomGraph Web-Node architecture](../../raw/master/architecture.png)

Usage
=====

Docker
------

Processor is available from Docker Hub as [`atomgraph/web-node](https://hub.docker.com/r/atomgraph/web-node/) image.
It accepts the following environment variables (that become webapp context parameters):

<dl>
    <dt><code>ENDPOINT</code></dt>
    <dd><a href="https://www.w3.org/TR/sparql11-protocol/">SPARQL 1.1 Protocol</a> endpoint</dd>
    <dd>URI</dd>
    <dt><code>GRAPH_STORE</code></dt>
    <dd><a href="https://www.w3.org/TR/sparql11-http-rdf-update/">SPARQL 1.1 Graph Store Protocol</a> endpoint</dd>
    <dd>URI</dd>
    <dt><code>ONTOLOGY</code></dt>
    <dd><a href="https://atomgraph.github.io/Linked-Data-Templates/">Linked Data Templates</a> ontology</dd>
    <dd>URI</dd>
    <dt><code>AUTH_USER</code></dt>
    <dd>SPARQL service HTTP Basic auth username</dd>
    <dd>string, optional</dd>
    <dt><code>AUTH_PWD</code></dt>
    <dd>SPARQL service HTTP Basic auth password</dd>
    <dd>string, optional</dd>
    <dt><code>PREEMPTIVE_AUTH</code></dt>
    <dd>use premptive HTTP Basic auth?</dd>
    <dd><code>true</code>/<code>false</code>, optional</dd>
    <dt><code>STYLESHEET</code></dt>
    <dd>Custom XSLT stylesheet</dd>
    <dd>URI, optional</dd>
    <dt><code>RESOLVING_UNCACHED</code></dt>
    <dd>If <code>true</code>, the stylesheet will attempt to load RDF resources by dereferencing URIs in the main data to improve the UX</dd>
    <dd><code>true</code>/<code>false</code>, optional</dd>
</dl>

If you want to have your ontologies read from a local file rather than their URIs, you can define a custom [location mapping](https://jena.apache.org/documentation/notes/file-manager.html#the-locationmapper-configuration-file) that will be appended to the system location mapping.
The mapping has to be a file in N3 format and mounted to the `/usr/local/tomcat/webapps/ROOT/WEB-INF/classes/custom-mapping.n3` path. Validate the file syntax beforehand to avoid errors.

To enable logging, mount `log4j.properties` file to `/usr/local/tomcat/webapps/ROOT/WEB-INF/classes/log4j.properties`.

Run the container with Wikidata's example like this (replace `//c/Users/namedgraph/WebRoot/...` paths with your own; the paths have to be _absolute_):

    docker run \
        -p 8080:8080 \
        -e ENDPOINT="https://query.wikidata.org/bigdata/namespace/wdq/sparql" \
        -e GRAPH_STORE="https://query.wikidata.org/bigdata/namespace/wdq/service" \
        -e ONTOLOGY="https://github.com/AtomGraph/Processor/blob/develop/examples/wikidata#" \
        -v "//c/Users/namedgraph/WebRoot/Web-Node/src/main/resources/log4j.properties":"/usr/local/tomcat/webapps/ROOT/WEB-INF/classes/log4j.properties" \
        -v "//c/Users/namedgraph/WebRoot/Processor/examples/wikidata.ttl":"/usr/local/tomcat/webapps/ROOT/WEB-INF/classes/org/wikidata/ldt.ttl" \
        -v "//c/Users/namedgraph/WebRoot/Processor/examples/location-mapping.n3":"/usr/local/tomcat/webapps/ROOT/WEB-INF/classes/custom-mapping.n3" \
        atomgraph/processor

After that, access http://localhost:8080/birthdays?limit=10 and you will retrieve RDF data with 10 people (or "entities") that have a birthday today.

Maven
-----

Web-Node will be released on Maven central when it reaches the 2.1 version.