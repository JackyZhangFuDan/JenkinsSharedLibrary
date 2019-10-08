import com.lesfurets.jenkins.unit.global.lib.Library

@Library('pdsjenkinslibrary@master') _

import com.sap.calm.ptm.jenkinslibrary.DownloadUtil
import com.sap.calm.ptm.jenkinslibrary.PDSNotificationUtil
import com.sap.calm.ptm.jenkinslibrary.Logger

def execute() {
    node() {
		String username = 'P2001706186'
		String password = '#Sue123456'
		def notificationData = []
		def log = new Logger()
		String jenkinsUrl = JENKINS_URL
		
        stage("Prepare") {
            //downloadUtil = new DownloadUtil()
			if(jenkinsUrl.endsWith('/')){
				jenkinsUrl = jenkinsUrl.substring(0,jenkinsUrl.length() - 1)
				echo "cutted Jenkins URL: ${jenkinsUrl}"
			}
        }
        stage("Middle") {
            
			def dateToBeDownloaded = new Date() - 1
			/*
			List<String> result = DownloadUtil.downloadFromOtherJenkins(
				'https://gketestpipeline.jaas-gcp.cloud.sap.corp', //server of the jenkins
				'rc_pipeline_Master',                                    //job name
				dateToBeDownloaded,                              //which date's builds of this job will be downloaded
				"${WORKSPACE}",                                       //save to which folder of the server?
				'artifact/jenkins_data_tags.json')
			notificationData.add(DownloadUtil.prepareDataForNotification('rc','rc','ut_coverage_backend','ut_jacoco',result, "${BUILD_URL}artifact/"))
			*/
			List<String> result = DownloadUtil.downloadFromOtherJenkins(
				'https://gkecalmdevshanghai.jaas-gcp.cloud.sap.corp',
				'SIC_UT_ABAP_Weekly',
				dateToBeDownloaded,
				"${WORKSPACE}",
				'artifact/Backend_UT_Coverage.xml'
			)
			notificationData.add(DownloadUtil.prepareDataForNotification('rc','sic','ut_coverage_backend','ut_nw',result, "${BUILD_URL}artifact/"))
			echo "${result.size()} backend ut result files are downloaded"
			
			List<String> msg = DownloadUtil.logger.allMsgs()
			if(msg != null && msg.size()> 0){
				for(int i = 0 ; i < msg.size(); i++){
					echo msg.get(i)
				}
			}
			DownloadUtil.logger.clear()
			
        }
		stage('Archive Files'){
			archiveArtifacts artifacts: DownloadUtil.DOWNLOADSUBFOLDER + '/**'
			echo 'Downloaded fileds are archived, they are putted to folder ' + DownloadUtil.DOWNLOADSUBFOLDER + ' which is subfolder of build"s archieve folder'
			DownloadUtil.clearTmpFolder("${WORKSPACE}")
	    }
	    stage('Notify PDS'){
			for(int i = 0; i < notificationData.size; i++){
				def notiData = notificationData.get(i)
				if(notiData == null) continue
				PDSNotificationUtil.notifyPDS(
					notiData.project,
					notiData.module,
					jenkinsUrl,
					'jenkins',
					notiData.category,
					"${JOB_NAME}",
					"${BUILD_ID}",
					new Date(),
					notiData.files,
					
					username,
					password,
					
					true,
					log
				)
			}
			List<String> msg = log.allMsgs()
			if(msg != null && msg.size()> 0){
				for(int i = 0 ; i < msg.size(); i++){
					echo msg.get(i)	
				}
			}
			echo 'Notification(s) is sent.'
	    }
    }
	
}

return this