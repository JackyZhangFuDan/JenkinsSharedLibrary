package com.sap.calm.ptm.jenkinslibrary

import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import java.text.SimpleDateFormat
import java.nio.charset.StandardCharsets
import com.cloudbees.groovy.cps.NonCPS

@Grab('commons-io:commons-io:2.5')
import org.apache.commons.io.IOUtils

@Grab('org.apache.httpcomponents:httpclient:4.5.6')
import org.apache.http.HttpEntity
@Grab('org.apache.httpcomponents:httpclient:4.5.6')
import org.apache.http.ssl.SSLContexts
@Grab('org.apache.httpcomponents:httpclient:4.5.6')
import org.apache.http.util.EntityUtils

import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.impl.auth.BasicScheme

@Grab('org.apache.httpcomponents:httpcore:4.4.10')
import org.apache.http.client.methods.CloseableHttpResponse
@Grab('org.apache.httpcomponents:httpcore:4.4.10')
import org.apache.http.client.methods.HttpPost
@Grab('org.apache.httpcomponents:httpcore:4.4.10')
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
@Grab('org.apache.httpcomponents:httpcore:4.4.10')
import org.apache.http.conn.ssl.TrustStrategy
@Grab('org.apache.httpcomponents:httpcore:4.4.10')
import org.apache.http.impl.client.CloseableHttpClient
@Grab('org.apache.httpcomponents:httpcore:4.4.10')
import org.apache.http.impl.client.HttpClients
@Grab('org.apache.httpcomponents:httpcore:4.4.10')
import org.apache.http.entity.StringEntity

import groovy.json.JsonSlurper
import groovy.json.JsonOutput

public class PDSNotificationBuilder{
	private PDSNotification notification = new PDSNotification();
	
	public PDSNotificationBuilder(String project, String module, String server, String serverType, String category){
		this.notification.project = project
		this.notification.module = module
		this.notification.server = server
		this.notification.type = serverType
		this.notification.category = category
		
		this.notification.files = new ArrayList<ResultFile>()
	}
	
	public def PDSNotificationBuilder jobName(String jobName){
		this.notification.job = jobName
		return this;
	}
	
	public PDSNotificationBuilder jobBuildId(String id){
		this.notification.build = id
		return this;
	}
	
	public PDSNotificationBuilder whenTestRun(Date ts){
		SimpleDateFormat sdf = new SimpleDateFormat('yyyyMMddhhmmss')
		this.notification.whenTestRun = sdf.format(ts)
		return this;
	}
	
	public PDSNotificationBuilder addFile(String format, String relativeUrl){
		ResultFile f = new ResultFile()
		f.format = format
		f.url = relativeUrl
		this.notification.files.add(f)
		return this;
	}
	
	public PDSNotification build(){
		if(this.notification.validate()){
			return this.notification
		}else{
			return null;
		}
	}
	
	public static class PDSNotification{
		public String project
		public String module
		public String server
		public String type
		
		public String job
		public String build
		public String category
		public List<ResultFile> files
		
		public String whenTestRun
		
		private PDSNotification(){
			
		}
		
		public boolean send(){
			if(!this.validate()){
				println 'validation before sending fail.'
				return false
			}
			def jsonStr = JsonOutput.toJson([
				project:this.project,
				module:this.module,
				server:this.server,
				type:this.type,
				job:this.job,
				build:this.build,
				category:this.category,
				whenTestRun:this.whenTestRun,
				files:this.files
			])
			println 'Json data to be sent to PDS: ' + jsonStr
			
			try{
				this.httpsPostData('https://solmancf1-approuter-pds.cfapps.sap.hana.ondemand.com/pdd/api/v1/send', jsonStr)
			}catch(Exception ex){
				ex.printStackTrace()
				return false
			}
			
			return true
		}
		
		private boolean validate(){
			if(this.project == null || this.project.isEmpty()){
				return false;
			}
			if(this.module == null || this.module.isEmpty()){
				return false;
			}
			if(this.server == null || this.server.isEmpty()){
				return false;
			}
			if(this.type == null || this.type.isEmpty()){
				return false;
			}
			if(this.category == null || this.category.isEmpty()){
				return false;
			}
			return true
		}
		
		@NonCPS
		private def boolean httpsPostData(String url,String jsonStr) throws Exception {
			
			SSLContext sslcontext = SSLContexts.custom()
				.loadTrustMaterial(new TrustStrategy() {
					//ignore checking server's certification
					@NonCPS
					public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
						return true;
					}
				})
				.build()
			
			String[] ps = ['TLSv1','TLSv1.1','TLSv1.2']
			SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
					sslcontext,
					ps,
					null,
					SSLConnectionSocketFactory.getDefaultHostnameVerifier()
			)
			
			CloseableHttpClient httpclient = HttpClients.custom()
				.setSSLSocketFactory(sslConnectionSocketFactory)
				.build()
	  
			HttpPost httppost = new HttpPost(url)
			StringEntity entity = new StringEntity(jsonStr);
		    httppost.setEntity(entity);
		    httppost.setHeader("Accept", "application/json");
		    httppost.setHeader("Content-type", "application/json");
			
			UsernamePasswordCredentials creds = new UsernamePasswordCredentials("JohnSnow", "Solman00")
			httppost.addHeader(new BasicScheme().authenticate(creds, httppost, null));
			
			CloseableHttpResponse response = httpclient.execute(httppost)
			
			try {
				println response.getStatusLine()
				HttpEntity respEntity = response.getEntity()
				//println respEntity.getContent()
				
				EntityUtils.consume(respEntity)
			}catch(Exception ex){
				throw ex
			}
			
			int respCode = response.getStatusLine().getStatusCode();
			if(respCode >= 200 && respCode < 300){
				return true
			}else{
				return false
			}
			
		}
	}
	
	class ResultFile{
		public String format
		public String url
	}
	
}