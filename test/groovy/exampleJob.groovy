import com.lesfurets.jenkins.unit.global.lib.Library

@Library('pdsjenkinslibrary@master') _

import com.sap.calm.ptm.jenkinslibrary.DownloadUtil

def execute() {
    node() {
        stage("First") {
            echo "Something"
        }
        stage("Middle") {
            DownloadUtil util = new DownloadUtil()
            
			List<String> result = util.downloadFromJenkins(
				'https://gkecalmdevshanghai.jaas-gcp.cloud.sap.corp', //server of the jenkins
				'PTM_CN_FRAME_UI',                                    //job name
				new Date(1568611787631),                              //which date's builds of this job will be downloaded 
				pwd(),                                                //save to which folder of the server?
				'artifact/target/frame/UT/coverage/IE%2011.0.0%20(Windows%2010.0.0)/node_modules/karma-qunit/lib/index.html')
			
			result.forEach({line ->
				println line
			})
        }
		
        stage("Last") {
            echo "We're done"
        }
    }
	
}

def pwd(){
	return 'C:\\Users\\i042102\\Downloads'
}

return this