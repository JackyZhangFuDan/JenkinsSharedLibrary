package com.sap.calm.ptm.jenkinslibrary

import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import java.text.SimpleDateFormat
import java.nio.charset.StandardCharsets
import com.cloudbees.groovy.cps.NonCPS

import org.apache.commons.io.IOUtils

import org.apache.http.HttpEntity
import org.apache.http.ssl.SSLContexts
import org.apache.http.util.EntityUtils

import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.impl.auth.BasicScheme

import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.conn.ssl.TrustStrategy
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.entity.StringEntity

import groovy.json.JsonSlurper
import groovy.json.JsonOutput

class PDSNotification{
	//private String pdsNotificationEndPoint = 'https://projectdashboarddaemon-test.cfapps.sap.hana.ondemand.com/api/v1/send'
	public String pdsNotificationEndPoint = 'https://solmancf1-approuter-pds.cfapps.sap.hana.ondemand.com/pdd/api/v1/send'
	
	public String pdsUsername
	public String pdsPwd
	
	public String project
	public String module
	public String server
	public String type
	
	public String job
	public String build
	public String category
	public List files
	
	public String whenTestRun
	
	public Logger logger = new Logger()
	
	public PDSNotification(){
		
	}
	
	public boolean send(){
		String msg = ''
		if(!this.validate()){
			msg = 'validation before sending fail, notification sending is cancelled.'
			println msg
			this.logger.add(msg)
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
			whentestrun:this.whenTestRun,
			files:this.files
		])
		msg = 'Json data to be sent to PDS: ' + jsonStr
		println msg
		this.logger.add(msg)
		
		try{
			this.httpsPostData(this.pdsNotificationEndPoint, jsonStr)
		}catch(Exception ex){
			ex.printStackTrace()
			return false
		}
		
		return true
	}
	
	private boolean validate(){
		if(this.pdsUsername == null || this.pdsUsername.isEmpty()){
			this.logger.add('Notification validation fail: username isn\'t right')
			return false
		}
		if(this.pdsPwd == null || this.pdsPwd.isEmpty()){
			this.logger.add('Notification validation fail: pwd isn\'t right')
			return false
		}
		if(this.project == null || this.project.isEmpty()){
			this.logger.add('Notification validation fail: project cannot be empty.')
			return false;
		}
		if(this.module == null || this.module.isEmpty()){
			this.logger.add('Notification validation fail: module cannot be empty')
			return false;
		}
		if(this.server == null || this.server.isEmpty()){
			this.logger.add('Notification validation fail: server cannot be empty')
			return false;
		}
		if(this.type == null || this.type.isEmpty()){
			this.logger.add('Notification validation fail: notification type cannot be empty')
			return false;
		}
		if(this.category == null || this.category.isEmpty()){
			this.logger.add('Notification validation fail: category cannot be empty')
			return false;
		}
		return true
	}
	
	@NonCPS
	private def boolean httpsPostData(String url,String jsonStr) throws Exception {
		String msg
		
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
		
		UsernamePasswordCredentials creds = new UsernamePasswordCredentials(this.pdsUsername, this.pdsPwd)
		httppost.addHeader(new BasicScheme().authenticate(creds, httppost, null));
		
		CloseableHttpResponse response = httpclient.execute(httppost)
		
		int respCode = response.getStatusLine().getStatusCode();
		if(respCode < 200 || respCode >= 300){
			msg = 'PDS Daemon returns non-health http code: ' + respCode
			println msg
			this.logger.add(msg)
			return false
		}
		
		try {
			println response.getStatusLine()
			HttpEntity respEntity = response.getEntity()
			String jsonResp = EntityUtils.toString(respEntity,"UTF-8")
			def resp = (new JsonSlurper()).parseText(jsonResp)			
			
			EntityUtils.consume(respEntity)
			
			if(resp.code != 0){
				msg = 'PDS Daemon rejects the notification: ' + jsonResp
				println msg
				this.logger.add(msg) 
				return false
			}
		}catch(Exception ex){
			throw ex
		}
		
	}
	
}
	