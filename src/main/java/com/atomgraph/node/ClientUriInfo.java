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
package com.atomgraph.node;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
public class ClientUriInfo implements UriInfo
{
    private final URI baseUri, requestUri;
    private final MultivaluedMap<String, String> queryParams; 
    
    public ClientUriInfo(URI baseUri, URI requestUri, MultivaluedMap<String, String> queryParams)
    {
        this.baseUri = baseUri;
        this.requestUri = requestUri;
        this.queryParams = queryParams;
    }
    
    @Override
    public String getPath()
    {
        return requestUri.getPath();
    }

    @Override
    public String getPath(boolean decode)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<PathSegment> getPathSegments()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<PathSegment> getPathSegments(boolean decode)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public URI getRequestUri()
    {
        return requestUri;
    }

    @Override
    public UriBuilder getRequestUriBuilder()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public URI getAbsolutePath()
    {
        try
        {
            return new URI(requestUri.getScheme(), requestUri.getAuthority(), requestUri.getPath(), null, requestUri.getFragment());
        }
        catch (URISyntaxException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public UriBuilder getAbsolutePathBuilder()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public URI getBaseUri()
    {
        return baseUri;
    }

    @Override
    public UriBuilder getBaseUriBuilder()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public MultivaluedMap<String, String> getPathParameters()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public MultivaluedMap<String, String> getPathParameters(boolean decode)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public MultivaluedMap<String, String> getQueryParameters()
    {
        return queryParams;
    }

    @Override
    public MultivaluedMap<String, String> getQueryParameters(boolean decode)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getMatchedURIs()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getMatchedURIs(boolean decode)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Object> getMatchedResources()
    {
        throw new UnsupportedOperationException();
    }
    
}
