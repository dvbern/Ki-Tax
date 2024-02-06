import { defineConfig } from 'cypress';

import * as dvTasks from './cypress/support/tasks';
import * as fs from 'fs';

const baseUrl = process.env.baseURL ?? 'http://local-be.kibon.ch:4200/';

export default defineConfig({
    watchForFileChanges: false,
    e2e: {
        setupNodeEvents(on, config) {
            // implement node event listeners here

            on('task', {
                ...dvTasks,
            });
            on('after:spec',
                (spec: Cypress.Spec, results: CypressCommandLine.RunResult) => {
                  if (results && results.video) {
                    // Do we have failures for any retry attempts?
                    const failures = results.tests.some((test) =>
                      test.attempts.some((attempt) => attempt.state === 'failed')
                    )
                    if (!failures) {
                      // delete the video if the spec passed and no tests retried
                      fs.unlinkSync(results.video)
                    }
                  }
                }
              );
            on('before:browser:launch', (browser , launchOptions) => {
                // the browser width and height we want to get
                // our screenshots and videos will be of that resolution
                const width = 1920;
                const height = 1080;

                if (browser.name === 'chrome' && browser.isHeadless) {
                    launchOptions.args.push(`--window-size=${width},${height}`);

                    // force screen to be non-retina and just use our given resolution
                    launchOptions.args.push('--force-device-scale-factor=1');
                }

                if (browser.name === 'electron' && browser.isHeadless) {
                    // might not work on CI for some reason
                    launchOptions.preferences.width = width;
                    launchOptions.preferences.height = height;
                }

                if (browser.name === 'firefox' && browser.isHeadless) {
                    launchOptions.args.push(`--width=${width}`);
                    launchOptions.args.push(`--height=${height}`);
                }

                // IMPORTANT: return the updated browser launch options
                return launchOptions;
            });
        },

        projectId: 'ebegu-web',
        defaultCommandTimeout: 8000,
        experimentalStudio: true,
        viewportWidth: 1920,
        viewportHeight: 1080,
        baseUrl,
        fixturesFolder: './cypress/fixtures',
        experimentalRunAllSpecs: true,
        requestTimeout: 10000,
        reporter: 'junit',
        reporterOptions: {
            mochaFile: './cypress/results/test-result-[hash].xml'
        },
        video: true,
        videoCompression: 0
    },
    scrollBehavior: 'nearest',
});
