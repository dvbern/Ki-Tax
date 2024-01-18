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
              )
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
        video: true
    },
    scrollBehavior: 'nearest',
});
