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

import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.conn.ssl.TrustStrategy
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients

import groovy.json.JsonSlurper

class JenkinsDownloader{
	
	private String jenkinsServer
	private String folderOfCurrentWorkspace
	private String saveToSubFolder
	private Logger logger = new Logger()
	
	/**
	 * 
	 * @param jenkinsServer
	 * @param folderOfCurrentWorkspace
	 * @param saveToSubFolder
	 */
	def JenkinsDownloader(String jenkinsServer,String folderOfCurrentWorkspace, String saveToSubFolder){
		this.jenkinsServer = jenkinsServer
		this.folderOfCurrentWorkspace = folderOfCurrentWorkspace
		this.saveToSubFolder = saveToSubFolder
	}
	
	/**
	 *
	 * @param jenkinsServer
	 * @param folderOfCurrentWorkspace
	 * @param saveToSubFolder
	 */
	def JenkinsDownloader(String jenkinsServer,String folderOfCurrentWorkspace, String saveToSubFolder, Logger log){
		this.jenkinsServer = jenkinsServer
		this.folderOfCurrentWorkspace = folderOfCurrentWorkspace
		this.saveToSubFolder = saveToSubFolder
		this.logger = log
	}
	
	/**
	 * Downlaod test results of the specified job from another jenkins server,
	 * it only considers the result created in the specified date
	 *
	 * @param jobName
	 * @param date
	 * @param relativePathOfToBeDownloadedFile
	 * 
	 * @return: URI of the downloaded file
	 */
	def List<String> downloadFromJenkins( String jobName, Date date,  String relativePathOfToBeDownloadedFile){
		SimpleDateFormat sdf = new SimpleDateFormat('yyyyMMdd')
		String targetDateStr = sdf.format(date)
		
		List<String> downloadedFiles = new ArrayList<String>()
		
		File targetFolder = new File(folderOfCurrentWorkspace)
		FileTreeBuilder targetFolderBuilder = new FileTreeBuilder(targetFolder)
		targetFolder = targetFolderBuilder.dir(saveToSubFolder)
		/*
		if(!targetFolder.exists() || !targetFolder.isDirectory()){
			this.logger.add("The specified target folder ${folderOfCurrentWorkspace} does not exist.")
			return downloadedFiles
		}else{
			FileTreeBuilder targetFolderBuilder = new FileTreeBuilder(targetFolder)
			targetFolder = targetFolderBuilder.dir(saveToSubFolder)
		}*/
		
		//what builds the job had produced in the specified date
		String url = jenkinsServer + '/job/' + jobName + '/api/json'
		String s = ""
		try{
			s = this.HttpsGetWithoutCert(url)
		} catch( Exception ex){
			this.logger.add(ex.getMessage())
			return downloadedFiles
		}
		if(s == null || s.isEmpty()){
			this.logger.add( "it seems we cannot read data of job ${jobName} via api")
			return downloadedFiles
		}
		
		def jsonSlurper = new JsonSlurper()
		def jobJsonObject = null
		ArrayList jobBuilds = null
		try{
			jobJsonObject = jsonSlurper.parseText(s)
			jobBuilds = jobJsonObject.builds
		}catch(Exception ex){
			this.logger.add("${ex.getMessage()}. The json string: ${s}")
			return downloadedFiles
		}
		
		ArrayList toBeDownloadedBuilds = new ArrayList()
		
		//calculates which builds to download?
		jobBuilds.each( { build ->
			try{
				s = this.HttpsGetWithoutCert(build.url + "/api/json")
				if(s != null && !s.isEmpty()){
					def buildJsonObject = jsonSlurper.parseText(s)
					
					def ts = new Date(buildJsonObject.timestamp)
					if(sdf.format(ts).equals( targetDateStr) ){
						toBeDownloadedBuilds.add(build);
					}
				}
			}catch(Exception ex){
			}
		})
		
		//download the builds
		toBeDownloadedBuilds.each({ build ->
			url = build.url + relativePathOfToBeDownloadedFile
			String[] path = relativePathOfToBeDownloadedFile.split('/')
			
			String fileName = targetDateStr + '_' + jobName + '_' + build.number 
			if(path != null && path.length > 0){
				fileName = fileName + '_' + path[path.length - 1]
			}else{
				fileName = fileName + '_unknown'
			}
			
			if(this.download(url, targetFolder, fileName)){
				downloadedFiles.add(saveToSubFolder + '/' + fileName)
			}else{
				this.logger.add( "download file ${fileName} fail")
			}
		})
		
		return downloadedFiles
	}
	
	/**
	 * Download one test result file from the specified URL, save it to the specified file
	 *
	 * @param url
	 * @param targetFolder
	 * @param fileName
	 * @return
	 */
	@NonCPS
	private def boolean download(String url, File targetFolder, String fileName){
		
		//create new file to save the download content
		FileTreeBuilder targetFolderBuilder = new FileTreeBuilder(targetFolder)
		
		String s = ""
		try{
			s = this.HttpsGetWithoutCert(url)
		}catch(Exception ex){
			this.logger.add(ex.getMessage())
			return false
		}
		if(s != null && !s.isEmpty()){
			def targetFile = targetFolderBuilder.file(fileName)
			targetFile.withWriter('utf-8'){ writer ->
				writer.write(s)
			};
		}else{
			return false;
		}
		
		return true;
	}
	
	/**
	 * read content from one url, this is basis of other methods which like to download data
	 *
	 * @param url
	 * @return
	 * @throws Exception
	 */
	@NonCPS
	private def String HttpsGetWithoutCert(String url) throws Exception {
		
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
  
		HttpGet httpget = new HttpGet(url)
		
		this.logger.add("Executing request " + httpget.getRequestLine())
		CloseableHttpResponse response = httpclient.execute(httpget)
		int respCode = response.getStatusLine().getStatusCode();
		
		String result = ""
		if(respCode < 200 || respCode >= 300){
			this.logger.add( 'Get non-health http code: ' + respCode )
		}else{
			try {
				HttpEntity entity = response.getEntity()
				this.logger.add(response.getStatusLine().toString())
				result = IOUtils.toString(entity.getContent())
				EntityUtils.consume(entity)
			}catch(Exception ex){
				this.logger.add(ex.getMessage())
				throw ex
			}
		}
		
		return result
	}
	
}