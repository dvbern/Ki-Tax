// Continuous delivery pipeline for feature branch builds.

pipeline {
	// It is important to use agent none, so stages that do not need an agent are not hogging a heavyweight executor.
	agent none
	triggers {
		pollSCM('H 22 * * *')
	}
	options {
		// Only keep the most recent build
		buildDiscarder(logRotator(numToKeepStr: "1"))
		// If the build (including waiting time for user input) takes longer, it will be aborted.
		timeout(time: 2, unit: 'HOURS')
		disableConcurrentBuilds()
	}
	stages {
		stage("Test") {
			agent {
				docker {
					image "docker.dvbern.ch/build-environment/mvn-npm-gitflow-chromium:latest"
					args "--privileged"
				}
			}

			steps {
				ansiColor('xterm') {
					lock('ebegu-tests') {
						withMaven(options: [
								junitPublisher(healthScaleFactor: 1.0),
								spotbugsPublisher(),
								artifactsPublisher(disabled: true)
						]) {
							sh 'export PATH=$MVN_CMD_DIR:$PATH && mvn -B -U -T 1C -P dvbern.oss -P ' +
									'test-wildfly-managed -P ci clean install'
						}
					}
				}
			}

			post {
				always {
					recordIssues(enabledForFailure: true, tools: [pmdParser(), checkStyle(), spotBugs
							(useRankAsPriority: true), tsLint(pattern: 'ebegu-web/build/tslint-checkstyle-report.xml')])
					junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
					cleanWs notFailBuild: true
				}
			}
		}
	}
}

