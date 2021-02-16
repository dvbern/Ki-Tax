// Continuous delivery pipeline for feature branch builds.

pipeline {
	// It is important to use agent none, so stages that do not need an agent are not hogging a heavyweight executor.
	agent none
	triggers {
		pollSCM('H 22 * * *')
	}
	options {
		// Only keep the most recent build
		buildDiscarder(logRotator(numToKeepStr: "10"))
		// If the build (including waiting time for user input) takes longer, it will be aborted.
		timeout(time: 4, unit: 'HOURS')
		disableConcurrentBuilds()
		throttle(['ebegu-job-limit'])
	}
	stages {
		stage("Test") {
			agent {
				docker {
					image "docker.dvbern.ch/build-environment/mvn-npm-gitflow-chromium:jdk11"
					args "--privileged"
				}
			}

			steps {
				withMaven(options: [
						junitPublisher(healthScaleFactor: 1.0),
						findbugsPublisher(disabled: true),
						spotbugsPublisher(disabled: true),
						artifactsPublisher(disabled: true)
				]) {
					sh 'mvn -B -U -T 1C ' +
							'-P dvbern.oss -P test-wildfly-managed -P ci -P frontend clean install'
				}
			}

			post {
				always {
					recordIssues(enabledForFailure: true, tools: [
							pmdParser(),
							checkStyle(),
							spotBugs(pattern: '**/target/spotbugs/spotbugsXml.xml', useRankAsPriority: true),
							tsLint(pattern: '**/tslint-checkstyle-report.xml')
					])
					junit allowEmptyResults: true,
							testResults: 'target/surefire-reports/*.xml build/karma-results.xml'
					cleanWs cleanWhenFailure: false, cleanWhenNotBuilt: false, cleanWhenUnstable: false, notFailBuild: true
				}
			}
		}
	}
}

