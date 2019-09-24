package com.sap.calm.ptm.jenkinslibrary

import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import java.text.SimpleDateFormat
import java.nio.charset.StandardCharsets
import com.cloudbees.groovy.cps.NonCPS

import com.sap.calm.ptm.jenkinslibrary.JenkinsDownloader

class DownloadUtil{
	
	public static String DOWNLOADSUBFOLDER = 'pds_download_folder'
	
	/**
	 * Download test result from other jenkins server, this is static method which can be accessed from class, so it is easy for pipeline to comsume
	 * 
	 * @param jenkinsServer
	 * @param jobName
	 * @param date
	 * @param folderOfCurrentWorkspace
	 * @param relativePathOfToBeDownloadedFile
	 * @return: URI of the downloaded file
	 */
	public static def List<String> downloadFromOtherJenkins(String jenkinsServer, String jobName, Date date, String folderOfCurrentWorkspace, String relativePathOfToBeDownloadedFile){
		JenkinsDownloader loader = new JenkinsDownloader(jenkinsServer,folderOfCurrentWorkspace,DOWNLOADSUBFOLDER)
		return loader.downloadFromJenkins(jobName, date , relativePathOfToBeDownloadedFile)
	}
	
}