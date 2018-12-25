package com.atomgraph.node.filter.request;

import com.atomgraph.client.vocabulary.AC;
import com.sun.jersey.api.uri.UriComponent;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.UriBuilder;

/**
 *
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
public class ClientParamRemovalFilter implements ContainerRequestFilter, ResourceFilter
{

    public final static List<String> CLIENT_PARAMS = Arrays.asList(AC.uri.getLocalName(), AC.endpoint.getLocalName(),
            AC.mode.getLocalName(), AC.accept.getLocalName());
    
    @Override
    public ContainerRequest filter(ContainerRequest request)
    {
        UriBuilder requestUriWithoutAC = UriBuilder.fromUri(request.getRequestUri());
        requestUriWithoutAC.replaceQuery(null);

        Iterator<Map.Entry<String, List<String>>> it = request.getQueryParameters().entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry<String, List<String>> entry = it.next();
            for (String value : entry.getValue())
                if (!CLIENT_PARAMS.contains(entry.getKey()))
                    // we URI-encode values ourselves because Jersey 1.x fails to do so: https://java.net/jira/browse/JERSEY-1717
                    requestUriWithoutAC.queryParam(entry.getKey(), UriComponent.encode(value, UriComponent.Type.UNRESERVED));
        }

        request.setUris(request.getBaseUri(), requestUriWithoutAC.build());
        
        return request;
    }
    
    @Override
    public ContainerRequestFilter getRequestFilter()
    {
        return this;
    }

    @Override
    public ContainerResponseFilter getResponseFilter()
    {
        return null;
    }
    
}
