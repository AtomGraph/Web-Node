/**
 *  Copyright 2018 Martynas Jusevičius <martynas@atomgraph.com>
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
package com.atomgraph.node.client;

import com.atomgraph.client.vocabulary.AC;
import com.atomgraph.node.ClientUriInfo;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import javax.xml.transform.Templates;
import org.apache.jena.ontology.OntModelSpec;

/**
 *
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
public class ModelXSLTWriter extends com.atomgraph.client.writer.ModelXSLTWriter
{
    
    public ModelXSLTWriter(Templates templates, OntModelSpec ontModelSpec)
    {
        super(templates, ontModelSpec);
    }
    
    @Override
    public List<URI> getModes(Set<String> namespaces) // mode is a client parameter, no need to parse hypermedia state here
    {
        return getModes(getClientUriInfo(), namespaces);
    }
    
    @Override
    public URI getURI() throws URISyntaxException
    {
        return getURIParam(getClientUriInfo(), AC.uri.getLocalName());
    }

    @Override
    public URI getEndpointURI() throws URISyntaxException
    {
        return getURIParam(getClientUriInfo(), AC.endpoint.getLocalName());
    }

    @Override
    public String getQuery()
    {
        if (getClientUriInfo().getQueryParameters().containsKey(AC.query.getLocalName()))
            return getClientUriInfo().getQueryParameters().getFirst(AC.query.getLocalName());
        
        return null;
    }
    
    public ClientUriInfo getClientUriInfo()
    {
        return getProviders().getContextResolver(ClientUriInfo.class, null).getContext(ClientUriInfo.class);
    }
    
}
