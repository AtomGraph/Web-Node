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
import com.atomgraph.client.locator.PrefixMapper;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.LocationMapper;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.ServletConfig;
import javax.ws.rs.core.Context;
import org.apache.jena.ontology.OntDocumentManager;
import com.atomgraph.client.provider.MediaTypesProvider;
import com.atomgraph.client.provider.TemplatesProvider;
import com.atomgraph.client.util.DataManager;
import com.atomgraph.client.vocabulary.AC;
import com.atomgraph.client.writer.ModelXSLTWriter;
import com.atomgraph.core.io.ResultSetProvider;
import com.atomgraph.core.io.UpdateRequestReader;
import com.atomgraph.core.provider.ClientProvider;
import com.atomgraph.core.provider.DatasetProvider;
import com.atomgraph.core.provider.GraphStoreClientProvider;
import com.atomgraph.core.provider.GraphStoreProvider;
import com.atomgraph.core.provider.QueryParamProvider;
import com.atomgraph.core.provider.SPARQLClientProvider;
import com.atomgraph.core.provider.SPARQLEndpointProvider;
import com.atomgraph.core.provider.ServiceProvider;
import com.atomgraph.core.vocabulary.A;
import com.atomgraph.core.vocabulary.SD;
import com.atomgraph.processor.vocabulary.AP;
import com.atomgraph.processor.vocabulary.LDT;
import static com.atomgraph.server.Application.getFileManager;
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
import com.atomgraph.server.provider.SkolemizingModelProvider;
import com.atomgraph.server.provider.TemplateCallProvider;
import com.atomgraph.server.provider.TemplateProvider;
import java.net.URISyntaxException;
import javax.annotation.PostConstruct;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFWriterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import javax.xml.transform.Source;
import static com.atomgraph.server.Application.getOntModelSpec;

/**
 * JAX-RS application class.
 * Combines Server and Web-Client applications.
 * 
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
public class Application extends com.atomgraph.server.Application
{
    
    private static final Logger log = LoggerFactory.getLogger(Application.class);
    
    private final Source stylesheet;
    private final Boolean cacheStylesheet;    
    private final Set<Class<?>> classes = new HashSet<>();
    private final Set<Object> singletons = new HashSet<>();
    
    public Application(@Context ServletConfig servletConfig) throws URISyntaxException, IOException
    {
        this(
            servletConfig.getInitParameter(A.dataset.getURI()) != null ? servletConfig.getInitParameter(A.dataset.getURI()) : null,
            servletConfig.getInitParameter(SD.endpoint.getURI()) != null ? servletConfig.getInitParameter(SD.endpoint.getURI()) : null,
            servletConfig.getInitParameter(A.graphStore.getURI()) != null ? servletConfig.getInitParameter(A.graphStore.getURI()) : null,
            servletConfig.getInitParameter(org.apache.jena.sparql.engine.http.Service.queryAuthUser.getSymbol()) != null ? servletConfig.getInitParameter(org.apache.jena.sparql.engine.http.Service.queryAuthUser.getSymbol()) : null,
            servletConfig.getInitParameter(org.apache.jena.sparql.engine.http.Service.queryAuthPwd.getSymbol()) != null ? servletConfig.getInitParameter(org.apache.jena.sparql.engine.http.Service.queryAuthPwd.getSymbol()) : null,
            servletConfig.getInitParameter(A.preemptiveAuth.getURI()) != null ? Boolean.parseBoolean(servletConfig.getInitParameter(A.preemptiveAuth.getURI())) : false,
            getFileManager(servletConfig, "/WEB-INF/classes/location-mapping.n3"),
            servletConfig.getInitParameter(LDT.ontology.getURI()) != null ? servletConfig.getInitParameter(LDT.ontology.getURI()) : null,
            getOntModelSpec(servletConfig),
            servletConfig.getInitParameter(AP.cacheSitemap.getURI()) != null ? Boolean.valueOf(servletConfig.getInitParameter(AP.cacheSitemap.getURI())) : true,
            getSource(servletConfig.getServletContext(), servletConfig.getInitParameter(AC.stylesheet.getURI()) != null ? servletConfig.getInitParameter(AC.stylesheet.getURI()) : null),
            servletConfig.getInitParameter(AC.cacheStylesheet.getURI()) != null ? Boolean.parseBoolean(servletConfig.getInitParameter(AC.cacheStylesheet.getURI())) : false,
            servletConfig.getInitParameter(AC.resolvingUncached.getURI()) != null ? Boolean.parseBoolean(servletConfig.getInitParameter(AC.resolvingUncached.getURI())) : false
        );
    }
    
    public Application(final String datasetLocation, final String endpointURI, final String graphStoreURI, final String authUser, final String authPwd, final boolean preemptiveAuth,
            final FileManager fileManager, final String ontologyURI, final OntModelSpec ontModelSpec, boolean cacheSitemap,
            final Source stylesheet, final boolean cacheStylesheet, final boolean resolvingUncached)
    {
        super(datasetLocation, endpointURI, graphStoreURI, authUser, authPwd, preemptiveAuth,
                fileManager, ontologyURI, ontModelSpec, cacheSitemap);
        this.stylesheet = stylesheet;
        this.cacheStylesheet = cacheStylesheet;
        
        // initialize mapping for locally stored vocabularies
        LocationMapper mapper = new PrefixMapper("prefix-mapping.n3"); // TO-DO: check if file exists?
        LocationMapper.setGlobalLocationMapper(mapper);
        if (log.isDebugEnabled()) log.debug("LocationMapper.get(): {}", LocationMapper.get());

        DataManager manager = new DataManager(LocationMapper.get(), new ClientProvider().getClient(), new MediaTypesProvider().getMediaTypes(), preemptiveAuth, resolvingUncached);
        FileManager.setStdLocators(manager);
        FileManager.setGlobalFileManager(manager);
        if (log.isDebugEnabled()) log.debug("FileManager.get(): {}", FileManager.get());

        OntDocumentManager.getInstance().setFileManager(FileManager.get());
        OntDocumentManager.getInstance().setCacheModels(isCacheSitemap()); // need to re-set after chaning FileManager
        if (log.isDebugEnabled()) log.debug("OntDocumentManager.getInstance().getFileManager(): {} Cache ontologies: {}", OntDocumentManager.getInstance().getFileManager(), isCacheSitemap());
        
        // register plain RDF/XML writer as default
        RDFWriterRegistry.register(Lang.RDFXML, RDFFormat.RDFXML_PLAIN);
    }

    @PostConstruct
    @Override
    public void init()
    {
        classes.add(ResourceBase.class);

        // Server singletons
        singletons.add(new ApplicationProvider());
        singletons.add(new ServiceProvider(getService()));
        singletons.add(new OntologyProvider(getOntologyURI(), getOntModelSpec(), true));
        singletons.add(new TemplateProvider());
        singletons.add(new TemplateCallProvider());
        singletons.add(new SPARQLEndpointProvider());
        singletons.add(new GraphStoreProvider());
        singletons.add(new DatasetProvider(getDataset()));
        singletons.add(new SPARQLClientProvider());
        singletons.add(new GraphStoreClientProvider());        
        singletons.add(new SkolemizingModelProvider());
        singletons.add(new ResultSetProvider());
        singletons.add(new QueryParamProvider());
        singletons.add(new UpdateRequestReader());
        //singletons.add(new com.atomgraph.core.provider.MediaTypesProvider());
        //singletons.add(new DataManagerProvider(getServletConfig()));
        singletons.add(new ClientProvider());
        singletons.add(new RiotExceptionMapper());
        singletons.add(new ModelExceptionMapper());
        singletons.add(new DatatypeFormatExceptionMapper());
        singletons.add(new NotFoundExceptionMapper());
        singletons.add(new ClientExceptionMapper());        
        singletons.add(new ConfigurationExceptionMapper());
        singletons.add(new OntologyExceptionMapper());
        singletons.add(new ParameterExceptionMapper());
        singletons.add(new QueryParseExceptionMapper());
        
        // Client singletons
        singletons.add(new MediaTypesProvider());
        singletons.add(new com.atomgraph.client.provider.DataManagerProvider());
        singletons.add(new ModelXSLTWriter()); // writes XHTML responses
        singletons.add(new TemplatesProvider(getStylesheet(), isCacheStylesheet())); // loads XSLT stylesheet
        
        if (log.isTraceEnabled()) log.trace("Application.init() with Classes: {} and Singletons: {}", classes, singletons);
    }
    
    public Source getStylesheet()
    {
        return stylesheet;
    }
    
    public Boolean isCacheStylesheet()
    {
        return cacheStylesheet;
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