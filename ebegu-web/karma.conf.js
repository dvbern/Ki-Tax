/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

'use strict';
const path = require('path');

module.exports = function (config) {
    config.set({
        basePath: '',
        frameworks: ['jasmine', '@angular-devkit/build-angular'],
        plugins: [
            require('karma-jasmine'),
            require('karma-chrome-launcher'),
            require('karma-firefox-launcher'),
            require('karma-junit-reporter'),
            require('karma-mocha-reporter'),
            require('karma-coverage-istanbul-reporter'),
            require('@angular-devkit/build-angular/plugins/karma'),
            require('karma-sonarqube-reporter')
        ],
        // list of files / patterns to load in the browser
        // we are building the test environment in ./spec-bundle.ts
        files: [
            {pattern: 'src/assets/**', included: false, serve: true},
        ],
        client: {
            clearContext: false // leave Jasmine Spec Runner output visible in browser
        },

        // needed for Chrome
        // mime: {
        //     'text/x-typescript': ['ts','tsx']
        // },

        // any of these options are valid:
        // tslint:disable-next-line:max-line-length
        // https://github.com/istanbuljs/istanbul-api/blob/47b7803fbf7ca2fb4e4a15f3813a8884891ba272/lib/config.js#L33-L38
        coverageIstanbulReporter: {

            // reports can be any that are listed here:
            // https://github.com/istanbuljs/istanbul-reports/tree/590e6b0089f67b723a1fdf57bc7ccc080ff189d7/lib
            reports: ['html', 'cobertura', 'lcovonly', 'text-summary'],

            // base output directory. If you include %browser% in the path it will be replaced with the karma browser
            // name
            dir: path.join(__dirname, 'build', 'coverage'),

            // if using webpack and pre-loaders, work around webpack breaking the source path
            fixWebpackSourcePaths: true,

            // stop istanbul outputting messages like `File [${filename}] ignored, nothing could be mapped`
            skipFilesWithNoCoverage: true,

            // Most reporters accept additional config options. You can pass these through the `report-config` option
            'report-config': {

                // all options available at:
                // tslint:disable-next-line:max-line-length
                // https://github.com/istanbuljs/istanbul-reports/blob/590e6b0089f67b723a1fdf57bc7ccc080ff189d7/lib/html/index.js#L135-L137
                html: {
                    // outputs the report in ./coverage/html
                    subdir: 'html'
                }

            }

            // to enforce thresholds see https://github.com/mattlewis92/karma-coverage-istanbul-reporter
        },
        angularCli: {
            environment: 'dev'
        },

        junitReporter: {
            outputFile: 'build/karma-results.xml',
            useBrowserName: false,
            xmlVersion: null
        },

        sonarqubeReporter: {
            basePath: 'src',        // test files folder
            filePattern: '**/*.spec.ts', // test files glob pattern
            encoding: 'utf-8',          // test files encoding
            outputFolder: 'build',      // report destination
            legacyMode: false,          // report for Sonarqube < 6.2 (disabled)
            reportName: function (metadata) { // report name callback
                /**
                 * Report metadata array:
                 * - metadata[0] = browser name
                 * - metadata[1] = browser version
                 * - metadata[2] = plataform name
                 * - metadata[3] = plataform version
                 */
                // return metadata.concat('xml').join('.');
                return 'sonar-report.xml';
            }
        },

        // suppress skipped tests in reporter
        mochaReporter: {
            ignoreSkipped: true
        },

        // test results reporter to use
        // possible values: 'dots', 'progress'
        // available reporters: https://npmjs.org/browse/keyword/karma-reporter
        reporters: ['mocha', 'junit', 'sonarqube', 'coverage-istanbul'],

        // web server port
        port: 9876,

        // enable / disable colors in the output (reporters and logs)
        colors: true,

        // level of logging
        // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO ||
        // config.LOG_DEBUG
        logLevel: config.LOG_INFO,

        // enable / disable watching file and executing tests whenever any file changes
        autoWatch: true,

        // start these browsers
        // available browser launchers: https://npmjs.org/browse/keyword/karma-launcher
        browsers: [
            // 'Chrome',
            'ChromeHeadless'
        ],

        // timeout when there's no activity, increased because the start up time is quite long with webpack
        captureTimeout: 120000,
        browserDisconnectTimeout: 30000,
        browserNoActivityTimeout: 120000,

        // if true, Karma captures browsers, runs the tests and exits
        singleRun: false
    });

};
