package com.rest.api;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

public class Client {
	private String _authToken;
	private String _baseUrl;
	
	
	public Client(String user, String password, String url) 
	{
		_baseUrl = url;		
 
		String authString = user + ":" + password;
		_authToken = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(authString.getBytes());	        
	}
	
	public Response get(String uri)
	{
		return this.execute(uri, "GET", null);
	}
	
	public Response post(String uri, String body)
	{
		return this.execute(uri, "POST", body);
	}
	
	public Response put(String uri, String body)
	{
		return this.execute(uri, "PUT", body);
	}
	
	public void delete(String uri)
	{
		this.execute(uri, "DELETE", null);
	}
	
	public Response execute(String uri, String method, String body) 
	{
		Response response = new Response();

		try 
		{
			URL url = new URL(_baseUrl + uri);
			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
			//System.out.println(conn.);
			conn.setInstanceFollowRedirects(false); 
			conn.setRequestMethod(method); 
			conn.setRequestProperty("Content-Type", "application/json"); 
	        conn.setRequestProperty("Authorization", _authToken);
	        
	        //System.out.println("Get TLS v1.2");
	        SSLContext sc = SSLContext.getInstance("TLSv1.2");
	        // Init the SSLContext with a TrustManager[] and SecureRandom()
	        sc.init(null, null, new java.security.SecureRandom()); 
	       // System.out.println("Done TLS v1.2");
	        
	        conn.setSSLSocketFactory(sc.getSocketFactory());
	       // System.out.println("Post socket factory");
	        if (method == "POST" || method == "PUT") 
	        {
				conn.setDoOutput(true); 
	        	final OutputStream os = conn.getOutputStream();
                os.write(body.getBytes());
                os.flush();
                os.close();
	        }
	        //System.out.println("post post");

	        
	        InputStream is = conn.getInputStream(); 
	        BufferedReader rd = new BufferedReader(new InputStreamReader( is));

	        String line;
	        while ((line = rd.readLine()) != null) 
	        {
	            response.body += line;
	        }	        
	        rd.close();

	        response.statusCode = conn.getResponseCode(); 
	        response.errorStream = conn.getErrorStream().toString();
	        conn.disconnect(); 
		} 
		catch (Exception e) 
		{
			response.exception = e.getMessage();
			e.printStackTrace();
		}
        return response;
	}
}
