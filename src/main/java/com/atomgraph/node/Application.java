/**
 *  Copyright 2014 Martynas Jusevičius <martynas@atomgraph.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.atomgraph.node;

import static com.atomgraph.client.Application.getSource;
import com.atomgraph.client.MediaTypes;
import com.atomgraph.client.locator.PrefixMapper;
import com.atomgraph.client.mapper.ClientErrorExceptionMapper;
import com.atomgraph.client.mapper.jersey.ClientHandlerExceptionMapper;
import com.atomgraph.client.mapper.jersey.UniformInterfaceExceptionMapper;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.ServletConfig;
import javax.ws.rs.core.Context;
import com.atomgraph.client.util.DataManager;
import com.atomgraph.client.vocabulary.AC;
import com.atomgraph.client.writer.ModelXSLTWriter;
import com.atomgraph.core.io.ResultSetProvider;
import com.atomgraph.core.io.UpdateRequestReader;
import com.atomgraph.core.provider.MediaTypesProvider;
import com.atomgraph.core.provider.QueryParamProvider;
import com.atomgraph.core.vocabulary.A;
import com.atomgraph.core.vocabulary.SD;
import com.atomgraph.processor.vocabulary.AP;
import com.atomgraph.processor.vocabulary.LDT;
import com.atomgraph.server.mapper.ClientExceptionMapper;
import com.atomgraph.server.mapper.ConfigurationExceptionMapper;
import com.atomgraph.server.mapper.ModelExceptionMapper;
import com.atomgraph.server.mapper.NotFoundExceptionMapper;
import com.atomgraph.server.mapper.OntologyExceptionMapper;
import com.atomgraph.server.mapper.ParameterExceptionMapper;
import com.atomgraph.server.mapper.jena.DatatypeFormatExceptionMapper;
import com.atomgraph.server.mapper.jena.QueryParseExceptionMapper;
import com.atomgraph.server.mapper.jena.RiotExceptionMapper;
import com.atomgraph.server.model.impl.ResourceBase;
import com.atomgraph.server.provider.ApplicationProvider;
import com.atomgraph.server.provider.OntologyProvider;
import com.atomgraph.server.io.SkolemizingModelProvider;
import com.atomgraph.server.provider.TemplateCallProvider;
import com.atomgraph.server.provider.TemplateProvider;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import java.net.URISyntaxException;
import javax.annotation.PostConstruct;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFWriterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import javax.xml.transform.Source;
import org.apache.jena.query.Dataset;
import static com.atomgraph.core.Application.getClient;
import com.atomgraph.core.io.ModelProvider;
import com.atomgraph.core.mapper.AuthenticationExceptionMapper;
import com.atomgraph.core.provider.ClientProvider;
import com.atomgraph.core.provider.DataManagerProvider;
import com.atomgraph.core.provider.ServiceProvider;
import com.atomgraph.core.riot.RDFLanguages;
import com.atomgraph.core.riot.lang.RDFPostReaderFactory;
import com.atomgraph.server.mapper.ConstraintViolationExceptionMapper;
import javax.ws.rs.WebApplicationException;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.riot.RDFParserRegistry;

/**
 * JAX-RS application class.
 * Combines Server and Web-Client applications.
 * 
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
public class Application extends com.atomgraph.server.Application
{
    
    private static final Logger log = LoggerFactory.getLogger(Application.class);
    
    private final DataManager dataManager;
    private final Source stylesheet;
    private final Boolean cacheStylesheet;
    private final Templates templates;
    private final Set<Class<?>> classes = new HashSet<>();
    private final Set<Object> singletons = new HashSet<>();
    
    public Application(@Context ServletConfig servletConfig) throws URISyntaxException, IOException
    {
        this(
            servletConfig.getServletContext().getInitParameter(A.dataset.getURI()) != null ? getDataset(servletConfig.getServletContext().getInitParameter(A.dataset.getURI()), null) : null,
            servletConfig.getServletContext().getInitParameter(SD.endpoint.getURI()) != null ? servletConfig.getServletContext().getInitParameter(SD.endpoint.getURI()) : null,
            servletConfig.getServletContext().getInitParameter(A.graphStore.getURI()) != null ? servletConfig.getServletContext().getInitParameter(A.graphStore.getURI()) : null,
            servletConfig.getServletContext().getInitParameter(org.apache.jena.sparql.engine.http.Service.queryAuthUser.getSymbol()) != null ? servletConfig.getServletContext().getInitParameter(org.apache.jena.sparql.engine.http.Service.queryAuthUser.getSymbol()) : null,
            servletConfig.getServletContext().getInitParameter(org.apache.jena.sparql.engine.http.Service.queryAuthPwd.getSymbol()) != null ? servletConfig.getServletContext().getInitParameter(org.apache.jena.sparql.engine.http.Service.queryAuthPwd.getSymbol()) : null,
            new MediaTypes(), getClient(new DefaultClientConfig()),
            servletConfig.getServletContext().getInitParameter(A.maxGetRequestSize.getURI()) != null ? Integer.parseInt(servletConfig.getServletContext().getInitParameter(A.maxGetRequestSize.getURI())) : null,
            servletConfig.getServletContext().getInitParameter(A.preemptiveAuth.getURI()) != null ? Boolean.parseBoolean(servletConfig.getServletContext().getInitParameter(A.preemptiveAuth.getURI())) : false,
            com.atomgraph.client.Application.getDataManager(new PrefixMapper(servletConfig.getServletContext().getInitParameter(AC.prefixMapping.getURI()) != null ? servletConfig.getServletContext().getInitParameter(AC.prefixMapping.getURI()) : null),
                com.atomgraph.client.Application.getClient(new DefaultClientConfig()),
                new MediaTypes(),
                servletConfig.getServletContext().getInitParameter(A.preemptiveAuth.getURI()) != null ? Boolean.parseBoolean(servletConfig.getServletContext().getInitParameter(A.preemptiveAuth.getURI())) : false,
                servletConfig.getServletContext().getInitParameter(AC.resolvingUncached.getURI()) != null ? Boolean.parseBoolean(servletConfig.getServletContext().getInitParameter(AC.resolvingUncached.getURI())) : false),
            servletConfig.getServletContext().getInitParameter(LDT.ontology.getURI()) != null ? servletConfig.getServletContext().getInitParameter(LDT.ontology.getURI()) : null,
            servletConfig.getServletContext().getInitParameter(AP.sitemapRules.getURI()) != null ? servletConfig.getServletContext().getInitParameter(AP.sitemapRules.getURI()) : null,
            servletConfig.getServletContext().getInitParameter(AP.cacheSitemap.getURI()) != null ? Boolean.valueOf(servletConfig.getServletContext().getInitParameter(AP.cacheSitemap.getURI())) : true,
            getSource(servletConfig.getServletContext(), servletConfig.getServletContext().getInitParameter(AC.stylesheet.getURI()) != null ? servletConfig.getServletContext().getInitParameter(AC.stylesheet.getURI()) : null),
            servletConfig.getServletContext().getInitParameter(AC.cacheStylesheet.getURI()) != null ? Boolean.parseBoolean(servletConfig.getServletContext().getInitParameter(AC.cacheStylesheet.getURI())) : false,
            servletConfig.getServletContext().getInitParameter(AC.resolvingUncached.getURI()) != null ? Boolean.parseBoolean(servletConfig.getServletContext().getInitParameter(AC.resolvingUncached.getURI())) : false
        );
    }
    
    public Application(final Dataset dataset, final String endpointURI, final String graphStoreURI, final String authUser, final String authPwd,
            final MediaTypes mediaTypes, final Client client, final Integer maxGetRequestSize, final boolean preemptiveAuth,
            final DataManager dataManager, final String ontologyURI, final String rulesString, boolean cacheSitemap,
            final Source stylesheet, final boolean cacheStylesheet, final boolean resolvingUncached)
    {
        super(dataset, endpointURI, graphStoreURI, authUser, authPwd,
                mediaTypes, client, maxGetRequestSize, preemptiveAuth,
                dataManager, ontologyURI, rulesString, cacheSitemap);
        this.dataManager = dataManager;
        this.stylesheet = stylesheet;
        this.cacheStylesheet = cacheStylesheet;
        
        // add RDF/POST serialization
        RDFLanguages.register(RDFLanguages.RDFPOST);
        RDFParserRegistry.registerLangTriples(RDFLanguages.RDFPOST, new RDFPostReaderFactory());
        // register plain RDF/XML writer as default
        RDFWriterRegistry.register(Lang.RDFXML, RDFFormat.RDFXML_PLAIN);
        
        SAXTransformerFactory transformerFactory = ((SAXTransformerFactory)TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null));
        transformerFactory.setURIResolver(dataManager);
        try
        {
            this.templates = transformerFactory.newTemplates(stylesheet);
        }
        catch (TransformerConfigurationException ex)
        {
            if (log.isErrorEnabled()) log.error("System XSLT stylesheet error: {}", ex);
            throw new WebApplicationException(ex);
        }
    }

    @PostConstruct
    @Override
    public void init()
    {
        classes.add(ResourceBase.class);

        // Core singletons
        singletons.add(new MediaTypesProvider(getMediaTypes()));
        singletons.add(new ResultSetProvider());
        singletons.add(new QueryParamProvider());
        singletons.add(new UpdateRequestReader());
        singletons.add(new DataManagerProvider(getDataManager()));

        // Server singletons
        singletons.add(new ServiceProvider(getService()));
        singletons.add(new ApplicationProvider(getApplication()));
        singletons.add(new OntologyProvider(OntDocumentManager.getInstance(), getOntologyURI(), getOntModelSpec(), true));
        singletons.add(new TemplateProvider());
        singletons.add(new TemplateCallProvider());
        singletons.add(new SkolemizingModelProvider());
        singletons.add(new RiotExceptionMapper());
        singletons.add(new ModelExceptionMapper());
        singletons.add(new ConstraintViolationExceptionMapper());
        singletons.add(new DatatypeFormatExceptionMapper());
        singletons.add(new NotFoundExceptionMapper());
        singletons.add(new ClientExceptionMapper());
        singletons.add(new ConfigurationExceptionMapper());
        singletons.add(new OntologyExceptionMapper());
        singletons.add(new ParameterExceptionMapper());
        singletons.add(new QueryParseExceptionMapper());
        
        // Web-Client singletons
        singletons.add(new ModelProvider());
        singletons.add(new com.atomgraph.client.provider.DataManagerProvider(getDataManager()));
        singletons.add(new ClientProvider(getClient()));
        singletons.add(new com.atomgraph.client.mapper.NotFoundExceptionMapper());
        singletons.add(new ClientErrorExceptionMapper());
        singletons.add(new UniformInterfaceExceptionMapper());
        singletons.add(new ClientHandlerExceptionMapper());
        singletons.add(new AuthenticationExceptionMapper());
        singletons.add(new ModelXSLTWriter(getTemplates(), getOntModelSpec())); // writes XHTML responses
        
        if (log.isTraceEnabled()) log.trace("Application.init() with Classes: {} and Singletons: {}", classes, singletons);
    }
    
    @Override
    public DataManager getDataManager()
    {
        return dataManager;
    }
    
    public Source getStylesheet()
    {
        return stylesheet;
    }
    
    public Boolean isCacheStylesheet()
    {
        return cacheStylesheet;
    }
    
    public Templates getTemplates()
    {
        return templates;
    }
    
    @Override
    public Set<Class<?>> getClasses()
    {
        return classes;
    }

    @Override
    public Set<Object> getSingletons()
    {
        return singletons;
    }
    
}