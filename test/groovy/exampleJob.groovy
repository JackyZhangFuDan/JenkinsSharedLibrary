import com.lesfurets.jenkins.unit.global.lib.Library

@Library('pdsjenkinslibrary@master') _

import com.sap.calm.ptm.jenkinslibrary.DownloadUtil
import com.sap.calm.ptm.jenkinslibrary.PDSNotificationUtil
import com.sap.calm.ptm.jenkinslibrary.Logger

def execute() {
    node() {
		String username = 'JonSnow'
		String password = 'Solman00'
		def notificationData = []
		def log = new Logger()
		
        stage("Prepare") {
            //downloadUtil = new DownloadUtil()
        }
        stage("Middle") {
            
			def dateToBeDownloaded = new Date() - 4
			List<String> result = DownloadUtil.downloadFromOtherJenkins(
				'https://gketestpipeline.jaas-gcp.cloud.sap.corp', //server of the jenkins
				'rc_pipeline_Master',                                    //job name
				dateToBeDownloaded,                              //which date's builds of this job will be downloaded
				"${WORKSPACE}",                                       //save to which folder of the server?
				'artifact/jenkins_data_tags.json')
			notificationData.add(DownloadUtil.prepareDataForNotification('rc','rc','ut_coverage_backend','ut_jacoco',result, "${BUILD_URL}artifact/"))
		
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
					"${JENKINS_URL}",
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