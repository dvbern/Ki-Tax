// Continuous delivery pipeline for feature branch builds.

pipeline {
	agent any
	triggers {
		pollSCM('H 22 * * *')
	}
	options {
		// Only keep the most recent build
		buildDiscarder(logRotator(numToKeepStr: "10"))
		// If the build (including waiting time for user input) takes longer, it will be aborted.
		timeout(time: 4, unit: 'HOURS')
		disableConcurrentBuilds()
	}
	stages {
        stage("Backend only") {
           when {not {anyOf {changeset 'ebegu-web/**'; environment name: 'BUILD_NUMBER', value: '1'; branch 'hotfix/*'}}}
           steps {
              withMaven(jdk: 'OpenJDK_11.0.4', options: [
                    junitPublisher(healthScaleFactor: 1.0),
                    findbugsPublisher(disabled: true),
                    spotbugsPublisher(disabled: true),
                    artifactsPublisher(disabled: true)
              ]) {
                 sh './mvnw -B -U -T 1C -P dvbern.oss -P ci clean test'
              }
           }
        }
        stage("Backend & Frontend") {
           when {allOf {
           			anyOf {changeset 'ebegu-web/**'; environment name: 'BUILD_NUMBER', value: '1'}
           			not {branch 'hotfix/*'}
           			}
           		}
           steps {
              withMaven(jdk: 'OpenJDK_11.0.4', options: [
                    junitPublisher(healthScaleFactor: 1.0),
                    findbugsPublisher(disabled: true),
                    spotbugsPublisher(disabled: true),
                    artifactsPublisher(disabled: true)
              ]) {
                 sh './mvnw -B -U -T 1C -P dvbern.oss -P ci -P frontend clean test'
              }
           }
        }
		stage("Backend & Frontend hotfix") {
           when {branch 'hotfix/*'}
           steps {
              withMaven(jdk: 'OpenJDK_11.0.4', options: [
                    junitPublisher(healthScaleFactor: 1.0),
                    findbugsPublisher(disabled: true),
                    spotbugsPublisher(disabled: true),
                    artifactsPublisher(disabled: true)
              ]) {
                 sh './mvnw -B -U -T 1C -P dvbern.oss -P test-wildfly-managed -P ci -P frontend clean install'
              }
           }
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

