import { defineConfig } from 'cypress';
import * as fs from 'fs';
import * as path from 'path';

const baseUrl = process.env.baseURL ?? 'http://local-be.kibon.ch:4200/';

export default defineConfig({
    e2e: {
        setupNodeEvents(on, config) {
            // implement node event listeners here

            on('task', {
                deleteDownload({ dirPath, fileName }) {
                    fs.readdir(dirPath, (err, files) => {
                        for (const file of files) {
                            if (fileName && file === fileName) {
                                fs.unlinkSync(path.join(dirPath, file));
                            }
                        }
                    });
                    return null;
                },
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
