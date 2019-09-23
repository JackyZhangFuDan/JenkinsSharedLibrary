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

@Grab('org.apache.httpcomponents:httpcore:4.4.10')
import org.apache.http.client.methods.CloseableHttpResponse
@Grab('org.apache.httpcomponents:httpcore:4.4.10')
import org.apache.http.client.methods.HttpGet
@Grab('org.apache.httpcomponents:httpcore:4.4.10')
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
@Grab('org.apache.httpcomponents:httpcore:4.4.10')
import org.apache.http.conn.ssl.TrustStrategy
@Grab('org.apache.httpcomponents:httpcore:4.4.10')
import org.apache.http.impl.client.CloseableHttpClient
@Grab('org.apache.httpcomponents:httpcore:4.4.10')
import org.apache.http.impl.client.HttpClients

import groovy.json.JsonSlurper

class DownloadUtil{
	
	public static String DOWNLOADSUBFOLDER = 'pds_download_folder'
	
	def DownloadUtil(){
	}
	
	/**
	 * Downlaod test results of the specified job from another jenkins server, 
	 * it only considers the result created in the specified date 
	 * 
	 * @param jenkinsServer
	 * @param jobName
	 * @param date
	 * @param folderOfCurrentWorkspace
	 * @param relativePathOfToBeDownloadedFile
	 * @return: URI of the downloaded file
	 */
	def List<String> downloadFromJenkins(String jenkinsServer, String jobName, Date date, String folderOfCurrentWorkspace, String relativePathOfToBeDownloadedFile){
		SimpleDateFormat sdf = new SimpleDateFormat('yyyyMMdd')
		String targetDateStr = sdf.format(date)
		
		List<String> downloadedFiles = new ArrayList<String>()
		
		File targetFolder = new File(folderOfCurrentWorkspace)
		if(!targetFolder.exists() || !targetFolder.isDirectory()){
			println 'The specified target folder does not exist.'
			return downloadedFiles
		}else{
			FileTreeBuilder targetFolderBuilder = new FileTreeBuilder(targetFolder)
			targetFolder = targetFolderBuilder.dir(DOWNLOADSUBFOLDER)
		}
		
		//what builds the job had produced in the specified date
		String url = jenkinsServer + '/job/' + jobName + '/api/json'
		String s = ""
		try{
			s = this.HttpsGetWithoutCert(url)
		} catch( Exception ex){
			ex.printStackTrace()
			return downloadedFiles
		}
		
		def jsonSlurper = new JsonSlurper()
		def jobJsonObject = jsonSlurper.parseText(s)
		ArrayList jobBuilds = jobJsonObject.builds
		ArrayList toBeDownloadedBuilds = new ArrayList()
		
		//calculates which builds to download?
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
			String fileName = targetDateStr + '_' + jobName + '_' + build.number + '_resultfile'
			this.download(url, targetFolder, fileName)
			downloadedFiles.add(DOWNLOADSUBFOLDER + '/' + fileName)
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
		def targetFile = targetFolderBuilder.file(fileName)
		
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
		
		System.out.println("Executing request " + httpget.getRequestLine())
		CloseableHttpResponse response = httpclient.execute(httpget)
		String result = ""
		try {
			HttpEntity entity = response.getEntity()
			System.out.println(response.getStatusLine())
			result = IOUtils.toString(entity.getContent())
			EntityUtils.consume(entity)
		}catch(Exception ex){
			throw ex
		}
		
		return result
	}
	
	public boolean testAccessHTTP(String url, String folder){
		return this.download(url, new File(folder), 'bytestdownloadfromhttp.txt')
	}
}