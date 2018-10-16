// Continuous delivery pipeline for feature branch builds.

pipeline {
	// It is important to use agent none, so stages that do not need an agent are not hogging a heavyweight executor.
	agent none
	triggers {
		pollSCM('H/5 * * * *')
	}
	options {
		// Only keep the 10 most recent builds
		buildDiscarder(logRotator(numToKeepStr: "10"))
		// If the build (including waiting time for user input) takes longer, it will be aborted.
		timeout(time: 2, unit: 'HOURS')
	}
	stages {
		stage("Test") {
			agent {
				docker {
					image "docker.dvbern.ch:5000/dvbern/build-environment:latest"
					args "--user=jenkins --privileged"
				}
			}

			steps {
				withMaven(options: [junitPublisher(healthScaleFactor: 1.0), findbugsPublisher()]) {
					sh 'export PATH=$MVN_CMD_DIR:$PATH && mvn -B -U -T 1C -P dvbern.oss -P test-wildfly-managed -P ci clean verify'
				}
			}
		}
	}
}

