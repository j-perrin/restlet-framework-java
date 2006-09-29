/*
 * Copyright 2005-2006 Noelios Consulting.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * http://www.opensource.org/licenses/cddl1.txt
 * If applicable, add the following below this CDDL
 * HEADER, with the fields enclosed by brackets "[]"
 * replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.impl.connector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.Parameter;
import org.restlet.data.Representation;
import org.restlet.data.Status;
import org.restlet.data.Tag;

import com.noelios.restlet.data.ContentType;
import com.noelios.restlet.data.InputRepresentation;
import com.noelios.restlet.data.ReadableRepresentation;

/**
 * Low-level HTTP client call.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class HttpClientCall extends HttpCall
{
	/**
	 * Constructor setting the request address to the local host.
    * @param method The method name.
    * @param requestUri The request URI.
	 */
	public HttpClientCall(String method, String requestUri)
	{
		setMethod(method);
		setRequestUri(requestUri);
      setClientAddress(getLocalAddress());
	}

	/**
	 * Returns the local IP address or 127.0.0.1 if the resolution fails.
	 * @return The local IP address or 127.0.0.1 if the resolution fails.
	 */
	public static String getLocalAddress()
	{
      try
      {
         return InetAddress.getLocalHost().getHostAddress();
      }
      catch(UnknownHostException e)
      {
         return "127.0.0.1";
      }
	}

   /**
    * Returns the request entity channel if it exists.
    * @return The request entity channel if it exists.
    */
   public WritableByteChannel getRequestChannel()
   {
      return null;
   }
   
   /**
    * Returns the request entity stream if it exists.
    * @return The request entity stream if it exists.
    */
   public OutputStream getRequestStream()
   {
      return null;
   }
   
	/**
	 * Sends the request to the client. Commits the request line, headers and optional input and 
	 * send them over the network. 
	 * @param input The optional input representation to send.
	 */
   public Status sendRequest(Representation input) throws IOException
   {
      if(getRequestStream() != null)
      {
   		if(input != null)
   		{
   			input.write(getRequestStream());
   		}
   		
         getRequestStream().flush();
      }
      else if(getRequestChannel() != null)
      {
   		if(input != null)
   		{
   			input.write(getRequestChannel());
   		}
      }
      
      Status result = new Status(getStatusCode(), null, getReasonPhrase(), null);

      if(getRequestStream() != null)
      {
         getRequestStream().close();
      }
      else if(getRequestChannel() != null)
      {
         getRequestChannel().close();
      }
      
		return result;
   }

   /**
    * Returns the response channel if it exists.
    * @return The response channel if it exists.
    */
   public ReadableByteChannel getResponseChannel()
   {
      return null;
   }
   
   /**
    * Returns the response stream if it exists.
    * @return The response stream if it exists.
    */
   public InputStream getResponseStream()
   {
      return null;
   }

   /**
    * Returns the response output representation if available. Note that no metadata is associated by default, 
    * you have to manually set them from your headers.
    * @return The response output representation if available.
    */
   public Representation getResponseOutput()
   {
   	Representation result = null;
   	
      if(getResponseStream() != null)
      {
         result = new InputRepresentation(getResponseStream(), null);
      }
      else if(getResponseChannel() != null)
      {
         result = new ReadableRepresentation(getResponseChannel(), null);
      }

      if(result != null)
      {
      	for(Parameter header : getResponseHeaders())
         {
            if(header.getName().equalsIgnoreCase(HttpConstants.HEADER_CONTENT_TYPE))
            {
               ContentType contentType = new ContentType(header.getValue());
               if(contentType != null) 
               {
               	result.setMediaType(contentType.getMediaType());
               	result.setCharacterSet(contentType.getCharacterSet());
               }
            }
            else if(header.getName().equalsIgnoreCase(HttpConstants.HEADER_CONTENT_LENGTH))
            {
               result.setSize(Long.parseLong(header.getValue()));
            }
            else if(header.getName().equalsIgnoreCase(HttpConstants.HEADER_EXPIRES))
            {
            	result.setExpirationDate(parseDate(header.getValue(), false));
            }
            else if(header.getName().equalsIgnoreCase(HttpConstants.HEADER_CONTENT_ENCODING))
            {
            	result.setEncoding(new Encoding(header.getValue()));
            }
            else if(header.getName().equalsIgnoreCase(HttpConstants.HEADER_CONTENT_LANGUAGE))
            {
            	result.setLanguage(new Language(header.getValue()));
            }
            else if(header.getName().equalsIgnoreCase(HttpConstants.HEADER_LAST_MODIFIED))
            {
            	result.setModificationDate(parseDate(header.getValue(), false));
            }
            else if(header.getName().equalsIgnoreCase(HttpConstants.HEADER_ETAG))
            {
            	result.setTag(new Tag(header.getValue()));
            }
            else if(header.getName().equalsIgnoreCase(HttpConstants.HEADER_CONTENT_LOCATION))
            {
            	result.setIdentifier(header.getValue());
            }
         }
      }
   	
   	return result;
   }

}
