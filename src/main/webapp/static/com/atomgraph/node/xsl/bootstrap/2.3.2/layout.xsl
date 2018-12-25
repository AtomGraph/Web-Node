<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
    <!ENTITY rdf    "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY ldt    "https://www.w3.org/ns/ldt#">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:rdf="&rdf;"
xmlns:ldt="&ldt;"
exclude-result-prefixes="#all"
>

    <xsl:import href="../../../../client/xsl/bootstrap/2.3.2/external.xsl"/>

    <!-- DEFAULT -->

    <!-- resources with URIs not relative to app base -->
    <xsl:template match="@rdf:resource[starts-with(., $ldt:base)] | sparql:uri[starts-with(., $ldt:base)]" priority="2">
        <xsl:next-match>
            <xsl:with-param name="href" select="."/>
        </xsl:next-match>
    </xsl:template>
    
    <!-- override Web-Client's template which always adds ?uri= -->
    <xsl:template match="*[@rdf:about[starts-with(., $ldt:base)]]" mode="xhtml:Anchor">
        <xsl:param name="href" select="@rdf:about" as="xs:anyURI"/>
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="title" select="@rdf:about" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        
        <xsl:next-match>
            <xsl:with-param name="href" select="$href"/>
            <xsl:with-param name="id" select="$id"/>
            <xsl:with-param name="title" select="$title"/>
            <xsl:with-param name="class" select="$class"/>
        </xsl:next-match>
    </xsl:template>

</xsl:stylesheet>