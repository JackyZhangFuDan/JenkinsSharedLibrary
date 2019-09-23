import com.lesfurets.jenkins.unit.global.lib.Library

@Library('pdsjenkinslibrary@master') _

import com.sap.calm.ptm.jenkinslibrary.DownloadUtil
import com.sap.calm.ptm.jenkinslibrary.PDSNotificationUtil

def execute() {
    node() {
		DownloadUtil downloadUtil
		String username = 'JonSnow'
		String password = 'Solman00'
		
        stage("First") {
            downloadUtil = new DownloadUtil()
        }
        stage("Middle") {
            
			def dateToBeDownloaded = new Date() - 1
			
			/*
			List<String> result = downloadUtil.downloadFromJenkins(
				'https://gkecalmdevshanghai.jaas-gcp.cloud.sap.corp', //server of the jenkins
				'PTM_CN_FRAME_UI',                                    //job name
				new Date(1568611787631),                              //which date's builds of this job will be downloaded 
				"${WORKSPACE}",                                       //save to which folder of the server?
				'artifact/target/frame/UT/coverage/IE%2011.0.0%20(Windows%2010.0.0)/node_modules/karma-qunit/lib/index.html')
			*/
			List<String> result = downloadUtil.downloadFromJenkins(
				'https://gketestpipeline.jaas-gcp.cloud.sap.corp', //server of the jenkins
				'rc_pipeline_Master',                                    //job name
				dateToBeDownloaded,                              //which date's builds of this job will be downloaded
				"${WORKSPACE}",                                       //save to which folder of the server?
				'artifact/jenkins_data_tags.json')
			
			echo 'Downloaded files: '
			
			result.forEach({line ->
				println line
			})
			
			echo "Current workspace: ${WORKSPACE}"
        }
		stage('Archive Files'){
			archiveArtifacts artifacts: DownloadUtil.DOWNLOADSUBFOLDER + '/**'
			echo 'Downloaded fileds are archived, they are putted to folder ' + DownloadUtil.DOWNLOADSUBFOLDER + ' which is subfolder of build"s archieve folder'
	    }
	    stage('Notify PDS'){
			boolean notiResult = PDSNotificationUtil.notifyPDS(
				'calmptm', 
				'project', 
				'https://gkecalmdevshanghai.jaas-gcp.cloud.sap.corp', 
				'jenkins', 
				'ut_coverage_backend',
				'jacky_sl_ut',
				'1',
				new Date(),
				[
					[format:'ut_jacoco', url:'job/PTM_CN_Project_JAVA/1/artifact/srv/target/site/jacoco/jacoco.xml']
				],
				
				username,
				password,
				
				true
			)
			
			if(notiResult){
				echo 'Notification is sent'
			}else{
				echo 'Send Notification fail'
			}
	    }
    }
	
}

return this