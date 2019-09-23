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

public class PDSNotificationBuilder{
	private PDSNotification notification = new PDSNotification();
	
	public PDSNotificationBuilder(String project, String module, String server, String serverType, String category, String username, String pwd){
		this.notification.project = project
		this.notification.module = module
		this.notification.server = server
		this.notification.type = serverType
		this.notification.category = category
		this.notification.files = new ArrayList()
		
		this.notification.pdsUsername = username
		this.notification.pdsPwd = pwd
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
		this.notification.files.add([format:format, url:relativeUrl])
		return this;
	}
	
	public PDSNotificationBuilder testMode(boolean test){
		if(test){
			this.notification.pdsNotificationEndPoint = 'https://projectdashboarddaemon-test.cfapps.sap.hana.ondemand.com/api/v1/send'
		}else{
			this.notification.pdsNotificationEndPoint = 'https://solmancf1-approuter-pds.cfapps.sap.hana.ondemand.com/pdd/api/v1/send'
		}
		println "PDS notification will be sent to endpoint ${this.notification.pdsNotificationEndPoint}"
		return this
	}
	
	public PDSNotification build(){
		if(this.notification.validate()){
			return this.notification
		}else{
			return null;
		}
	}
	
}