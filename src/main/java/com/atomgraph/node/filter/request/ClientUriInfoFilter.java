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
package com.atomgraph.node.filter.request;

import com.atomgraph.node.ClientUriInfo;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

/**
 *
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
public class ClientUriInfoFilter implements ContainerRequestFilter
{
    
    @Context HttpServletRequest httpServletRequest;

    @Override
    public ContainerRequest filter(ContainerRequest request)
    {
        // we need to save the current URI state somewhere, as it will be overridden by app base URI etc. (below)
        if (getHttpServletRequest().getAttribute(ClientUriInfo.class.getName()) == null)
        {
            ClientUriInfo clientUriInfo = new ClientUriInfo(request.getBaseUri(), request.getAbsolutePath(), request.getQueryParameters());
            getHttpServletRequest().setAttribute(ClientUriInfo.class.getName(), clientUriInfo); // used in ClientUriInfoProvider
        }
        
        return request;
    }
    
    public HttpServletRequest getHttpServletRequest()
    {
        return httpServletRequest;
    }

}
