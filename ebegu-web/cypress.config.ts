import { defineConfig } from 'cypress';

import * as dvTasks from './cypress/support/tasks';

const baseUrl = process.env.baseURL ?? 'http://local-be.kibon.ch:4200/';

export default defineConfig({
    e2e: {
        setupNodeEvents(on, config) {
            // implement node event listeners here

            on('task', {
                ...dvTasks,
            });
        },
        projectId: 'ebegu-web',
        defaultCommandTimeout: 8000,
        experimentalStudio: true,
        viewportWidth: 1920,
        viewportHeight: 1080,
        baseUrl,
        fixturesFolder: './cypress/fixtures',
    },
    scrollBehavior: 'nearest',
});
