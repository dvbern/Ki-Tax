// Continuous delivery pipeline for feature branch builds.

pipeline {
	agent any
	triggers {
		pollSCM('H 22 * * *')
        gitlab(
            branchFilterType: 'All',
            triggerOnPush: true,
            triggerOnMergeRequest: false,
            triggerOpenMergeRequestOnPush: "never",
            triggerOnNoteRequest: true,
            noteRegex: "Jenkins please retry a build",
            skipWorkInProgressMergeRequest: true,
            ciSkip: false,
            setBuildDescription: true,
            addNoteOnMergeRequest: true,
            addCiMessage: true,
            addVoteOnMergeRequest: true,
            acceptMergeRequestOnSuccess: false,
            includeBranchesSpec: "*",
            excludeBranchesSpec: "",
        )
	}
	options {
		// Only keep the most recent build
		buildDiscarder(logRotator(numToKeepStr: "10"))
		// If the build (including waiting time for user input) takes longer, it will be aborted.
		timeout(time: 4, unit: 'HOURS')
		disableConcurrentBuilds()
	}
	stages {
        stage("Backend & Frontend without Arquillian") {
           when {not {branch 'hotfix/*'}}
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
		stage("Backend & Frontend with Arquillian") {
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
        stage("Backend & Frontend with Arquillian") {
           when {branch 'feature-test/*'}
           steps {
              withMaven(jdk: 'OpenJDK_11.0.4', options: [
                    junitPublisher(healthScaleFactor: 1.0),
                    findbugsPublisher(disabled: true),
                    spotbugsPublisher(disabled: true),
                    artifactsPublisher(disabled: true)
              ]) {
                 sh './mvnw -B -U -T 1C -P dvbern.oss -P test-wildfly-managed -P ci clean install'
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

