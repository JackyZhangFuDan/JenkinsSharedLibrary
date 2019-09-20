package com.sap.calm.ptm.jenkinslibrary

import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import java.text.SimpleDateFormat
import java.nio.charset.StandardCharsets

//@Grab('commons-io:commons-io:2.5')
import org.apache.commons.io.IOUtils
import org.apache.http.HttpEntity
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.conn.ssl.TrustStrategy
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.ssl.SSLContexts
import org.apache.http.util.EntityUtils

import groovy.json.JsonSlurper

class DownloadUtil{
	
	def DownloadUtil(){
	}
	
	/**
	 * Downlaod test results of the specified job from another jenkins server, 
	 * it only considers the result created in the specified date
	 * @return
	 */
	def String downloadFromJenkins(String jenkinsServer, String jobName, Date date, String folderOfCurrentWorkspace, String relativePathOfToBeDownloadedFile){
		SimpleDateFormat sdf = new SimpleDateFormat('yyyyMMdd')
		String targetDateStr = sdf.format(date)
		
		File targetFolder = new File(folderOfCurrentWorkspace);
		if(!targetFolder.exists() || !targetFolder.isDirectory()){
			println 'The specified target folder does not exist.'
			return 'The specified target folder does not exist.'
			//return false;
		}else{
			FileTreeBuilder targetFolderBuilder = new FileTreeBuilder(targetFolder)
			targetFolder = targetFolderBuilder.dir("downloadedTestResult")
		}
		
		String url = jenkinsServer + '/job/' + jobName + '/api/json'
		String s = ""
		try{
			s = this.HttpsGetWithoutCert(url);
		} catch( Exception ex){
			return ex.getMessage();
			//return false;
		}
		
		def jsonSlurper = new JsonSlurper()
		def jobJsonObject = jsonSlurper.parseText(s);
		ArrayList jobBuilds = jobJsonObject.builds;
		ArrayList toBeDownloadedBuilds = new ArrayList()
		
		//which builds to download?
		jobBuilds.each( { build ->
			try{
				s = this.HttpsGetWithoutCert(build.url + "/api/json")
				def buildJsonObject = jsonSlurper.parseText(s)
				def ts = new Date(buildJsonObject.timestamp)
				if(sdf.format(ts).equals( targetDateStr) ){
					toBeDownloadedBuilds.add(build);
				}
			}catch(Exception ex){
				
			}
		})
		
		//download the builds
		toBeDownloadedBuilds.each({ build ->
			url = build.url + relativePathOfToBeDownloadedFile
			this.download(url, targetFolder,jobName+'_'+build.number+'_resultfile')
		})
		
		return 'download is done!'
	}
	
	private def boolean download(String url, File targetFolder, String fileName){
		
		File targetFile = new File(targetFolder, fileName);
		
		
		String s = ""
		try{
			s = this.HttpsGetWithoutCert(url)
		}catch(Exception ex){
			ex.printStackTrace()
			return false
		}
		
		targetFile.withWriter('utf-8'){ writer ->
			writer.write(s)
		};
		
		return true;
		
		/*
		//download the file
		def nullTrustManager = [
			checkClientTrusted: { chain, authType ->  },
			checkServerTrusted: { chain, authType ->  },
			getAcceptedIssuers: { null }
		]
		
		def nullHostnameVerifier = [
			verify: { hostname, session ->
				true
			}
		]
		javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL")
		sc.init(null, [nullTrustManager as  javax.net.ssl.X509TrustManager] as  javax.net.ssl.X509TrustManager[], null)
		javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory())
		javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(nullHostnameVerifier as javax.net.ssl.HostnameVerifier)
		
		def ur = new URL(url)
		def connection = null
		try{
			connection = ur.openConnection()
			connection.requestMethod = 'GET'
		}catch(Exception ex){
			println 'Download fail, there is exception when opening the url, '+ex.getMessage()
			return false;
		}
		if (connection.responseCode == 200) {
			String s = "";
			connection.getInputStream().withReader('utf-8'){reader ->
				s = reader.getText();
				targetFile.withWriter('utf-8'){ writer ->
					writer.write(s)
				};
			}
			
			//print some helpful information
			//println connection.content.text
			//println connection.contentEncoding
			println 'type of the downloaded content: ' + connection.contentType
			println 'last modify timestamp of the content: ' + connection.lastModified
			println 'HTTP header of the response: '
			connection.headerFields.each { 
				println "> ${it}"
			}
			
		} else {
			println 'download fail, the http reponse code isn''t 200: ' + connection.responseCode
			return false
		}
		return true
		*/
	}
	
	
	
	private def String HttpsGetWithoutCert(String url) throws Exception {
		println 'Url to be downloaded: ' + url
		
		/*
		def nullTrustManager = [
			checkClientTrusted: { chain, authType ->  },
			checkServerTrusted: { chain, authType ->  },
			getAcceptedIssuers: { null }
		]
		
		def nullHostnameVerifier = [
			verify: { hostname, session ->
				true
			}
		]
		javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("TLSv1")
		sc.init(null, [nullTrustManager as  javax.net.ssl.X509TrustManager] as  javax.net.ssl.X509TrustManager[], null)
		javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory())
		javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(nullHostnameVerifier as javax.net.ssl.HostnameVerifier)
		*/
		
		def ur = new URL(url)
		def connection = null
		connection = ur.openConnection()
		connection.requestMethod = 'GET'
		println connection.content.text
		
		if (connection.responseCode == 200) {
			String s = "";
			connection.getInputStream().withReader('utf-8'){reader ->
				s = reader.getText();
			}
			return s;
		} else {
			
			return 'download fail, the http reponse code is not 200: ' + connection.responseCode
		}
		
		/*
		SSLContext sslcontext = SSLContexts.custom()
			.loadTrustMaterial(new TrustStrategy() {
				//ignore checking server's certification
				public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					return true;
				}
			}
			).build()
		
		String[] ps = ["TLSv1","TLSv1.1","TLSv1.2"]
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
		
		System.out.println("Executing request " + httpget.getRequestLine())
		httpclient.execute(httpget)
		CloseableHttpResponse response = httpclient.execute(httpget)
		String result = ""
		try {
			HttpEntity entity = response.getEntity()
			System.out.println(response.getStatusLine())
			result = IOUtils.toString(entity.getContent())
			//EntityUtils.consume(entity)
		}catch(Exception ex){
			response.close()
			throw ex
		}
		
		return result
		*/
	}
}