/**
 *  Copyright 2014 Martynas Jusevičius <martynas@graphity.org>
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
import com.atomgraph.client.vocabulary.AC;
import com.atomgraph.client.writer.ModelXSLTWriter;
import com.atomgraph.core.provider.ClientProvider;
import com.atomgraph.core.provider.QueryParamProvider;
import com.atomgraph.core.provider.ResultSetProvider;
import com.atomgraph.core.provider.UpdateRequestReader;
import com.atomgraph.core.util.jena.DataManager;
import com.atomgraph.core.vocabulary.A;
import com.atomgraph.server.mapper.ClientExceptionMapper;
import com.atomgraph.server.mapper.ConfigurationExceptionMapper;
import com.atomgraph.server.mapper.ModelExceptionMapper;
import com.atomgraph.server.mapper.NotFoundExceptionMapper;
import com.atomgraph.server.mapper.SPINArgumentExceptionMapper;
import com.atomgraph.server.mapper.jena.DatatypeFormatExceptionMapper;
import com.atomgraph.server.mapper.jena.QueryParseExceptionMapper;
import com.atomgraph.server.mapper.jena.RiotExceptionMapper;
import com.atomgraph.server.model.impl.ResourceBase;
import com.atomgraph.server.provider.DatasetProvider;
import com.atomgraph.server.provider.GraphStoreOriginProvider;
import com.atomgraph.server.provider.GraphStoreProvider;
import com.atomgraph.server.provider.OntologyProvider;
import com.atomgraph.server.provider.SPARQLEndpointOriginProvider;
import com.atomgraph.server.provider.SPARQLEndpointProvider;
import com.atomgraph.server.provider.SkolemizingModelProvider;
import com.atomgraph.server.provider.TemplateCallProvider;
import javax.annotation.PostConstruct;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFWriterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAX-RS application class of the Blog app.
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class Application extends com.atomgraph.server.Application
{
    
    private static final Logger log = LoggerFactory.getLogger(Application.class);
    
    private final Set<Class<?>> classes = new HashSet<>();
    private final Set<Object> singletons = new HashSet<>();
    
    public Application(@Context ServletConfig servletConfig)
    {
        super(servletConfig);
        
	classes.add(ResourceBase.class);

        // Server singletons
	singletons.add(new SkolemizingModelProvider());
	singletons.add(new ResultSetProvider());
	singletons.add(new QueryParamProvider());
	singletons.add(new UpdateRequestReader());
        singletons.add(new com.atomgraph.core.provider.DataManagerProvider());
        singletons.add(new DatasetProvider());
        singletons.add(new ClientProvider());
        singletons.add(new OntologyProvider(servletConfig));
        singletons.add(new TemplateCallProvider());
	singletons.add(new SPARQLEndpointProvider());
	singletons.add(new SPARQLEndpointOriginProvider());
        singletons.add(new GraphStoreProvider());
        singletons.add(new GraphStoreOriginProvider());
        singletons.add(new RiotExceptionMapper());
	singletons.add(new ModelExceptionMapper());
	singletons.add(new DatatypeFormatExceptionMapper());
        singletons.add(new NotFoundExceptionMapper());
        singletons.add(new ClientExceptionMapper());        
        singletons.add(new ConfigurationExceptionMapper());
        singletons.add(new SPINArgumentExceptionMapper());
	singletons.add(new QueryParseExceptionMapper());
        // Client singletons
        singletons.add(new MediaTypesProvider());
        singletons.add(new com.atomgraph.client.provider.DataManagerProvider());
	singletons.add(new ModelXSLTWriter()); // writes XHTML responses
	singletons.add(new TemplatesProvider(servletConfig)); // loads XSLT stylesheet
    }

    @PostConstruct
    @Override
    public void init()
    {
	// initialize mapping for locally stored vocabularies
	LocationMapper mapper = new PrefixMapper("prefix-mapping.n3"); // TO-DO: check if file exists?
	LocationMapper.setGlobalLocationMapper(mapper);
	if (log.isDebugEnabled()) log.debug("LocationMapper.get(): {}", LocationMapper.get());
        
        super.init(); // init Processor
        
        if (log.isTraceEnabled()) log.trace("Application.init() with Classes: {} and Singletons: {}", getClasses(), getSingletons());

        // register plain RDF/XML writer as default
        RDFWriterRegistry.register(Lang.RDFXML, RDFFormat.RDFXML_PLAIN);
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

    @Override
    public void initOntDocumentManager(FileManager fileManager)
    {
        if (!(OntDocumentManager.getInstance().getFileManager() instanceof DataManager))
        {
            FileManager.setGlobalFileManager(fileManager);            
            super.initOntDocumentManager(fileManager);
        }
    }

    @Override
    public FileManager getFileManager()
    {
        com.atomgraph.client.util.DataManager manager = new com.atomgraph.client.util.DataManager(LocationMapper.get(),
                new MediaTypesProvider().getMediaTypes(),
                getBooleanParam(getServletConfig(), A.cacheModelLoads),
                getBooleanParam(getServletConfig(), A.preemptiveAuth),
                getBooleanParam(getServletConfig(), AC.resolvingUncached));
        FileManager.setStdLocators(manager);
        return manager;
    }
    
}