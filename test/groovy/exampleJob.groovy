import com.lesfurets.jenkins.unit.global.lib.Library

@Library('pdsjenkinslibrary@master') _

import com.sap.calm.ptm.jenkinslibrary.DownloadUtil

def execute() {
    node() {
        stage("First") {
            echo "Something"
        }
        stage("Middle") {
            DownloadUtil util = new DownloadUtil('https://jaas.wdf.sap.corp:30132/job/PTM_API_Test_Q/api$ptm_api_test_quality/585/testReport/Quality_PM_API_Test/Task/Update_task_N/','c:/jacky')
            println util.download()
        }
        stage("Last") {
            echo "We're done"
        }
    }
}

return this