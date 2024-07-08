/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/// <reference types="cypress" />
import * as dvTasks from '@dv-e2e/tasks';

type DvTasks = typeof dvTasks;

import {OnlyValidSelectors, User} from '@dv-e2e/types';
import {Method, WaitOptions} from 'cypress/types/net-stubbing';
import {KiBonMandant} from '../../src/app/core/constants/MANDANTS';

declare global {
    namespace Cypress {
        interface Chainable<Subject> {
            /**
             * Uses **`cy.session`** to create a new session with given user
             *
             * Please use the **`data-test`** values found at **`/#/locallogin`** except the _`test-user-`_ prefix
             *
             * @example
             * // ✅ ok
             * cy.login('E-BEGU-Superuser');
             *
             * // ⛔ not
             * cy.login('test-user-E-BEGU-Superuser');
             */
            login(user: User): void;

            /**
             * Used to change the used during the test, should be used with caution.
             *
             * @see login
             */
            changeLogin(user: User): void;

            /**
             * Used to close an angular material dialog or overlay
             */
            closeMaterialOverlay(): void;

            /**
             * It is a shorthand for **`cy.get('[data-test="..."])`** and also allows to sub-select nested elements.
             *
             * @example
             *   cy.getByData('form-kind', 'vorname');
             *   // equals
             *   cy.get('[data-test="form-kind"] [data-test="vorname"]');
             *
             * @example
             *   cy.getByData('dv-radiobutton').find('label');
             *   // technically equals
             *   cy.get('[data-test="dv-radiobutton"] label');
             */
            getByData<T extends string>(
                name: OnlyValidSelectors<T>,
                ...nestedNames: OnlyValidSelectors<T>[]
            ): Chainable<JQuery<HTMLElement>>;

            /**
             * Download a file using given url and save it with the given name
             *
             * @see Cypress.config('downloadsFolder')
             */
            downloadFile(url: string, fileName: string): Chainable<Subject>;

            /**
             * It is a shortand for **`cy.find('[data-test="..."]=`** and also allowds to sub-select nested elements.
             *
             * Same as with cy.find, requires being chained off a command that yields DOM Element(s).
             * See https://docs.cypress.io/api/commands/find#Rules
             *
             * @example
             *   cy.get('[data-test="dv-radiobutton"]').find('[data-test="dv-label"]');
             *   // equals
             *   cy.get('[data-test="dv-radiobutton"]').findByData('dv-label');
             *   // also equals
             *   cy.getByData('dv-radiobutton').findByData('dv-label');
             */
            findByData<T extends string>(
                name: OnlyValidSelectors<T>,
                ...nestedNames: OnlyValidSelectors<T>[]
            ): Chainable<JQuery<HTMLElement>>;

            /**
             * Use custom dv tasks by using the 3rd param option `{ custom: true }`
             *
             * @see {@link DvTasks}
             */
            task<K extends keyof DvTasks, T extends DvTasks[K]>(
                event: K,
                arg: Parameters<T>[0],
                opts: {
                    custom: true;
                }
            ): Chainable<ReturnType<T>>;

            resetViewport(): Chainable<Subject>;

            /**
             * Group logs from a part of a test
             *
             * @example
             * cy.groupBy('Resultate', () => {
             *   cy.getByData('page-title').should('include.text', gesuchsPeriode.anfang);
             *   EinkommensverschlechterungPO.fillResultateForm('withValid', 'jahr1');
             *   clickSave();
             *
             *   cy.getByData('page-title').should('include.text', gesuchsPeriode.ende);
             *   EinkommensverschlechterungPO.fillResultateForm('withValid', 'jahr2');
             *
             *   // The logs of the commands above will be written within a collapsible group in the cypress log
             * });
             *
             */
            groupBy<T>(context: string, run: () => T): Chainable<Subject>;

            /**
             * An abstraction for `cy.intercept` with the additional benefit that the intercept tracks the given request only 1
             * time
             *
             * @example
             * cy.waitForRequest('POST', '**‍/einkommensverschlechterung/calculateTemp/1', () => {
             *   cy.getByData('container.navigation-save', 'navigation-button').click();
             * });
             * // Equals
             * cy.intercept('POST', '**‍/einkommensverschlechterung/calculateTemp/1').as('...');
             * cy.getByData('container.navigation-save', 'navigation-button').click();
             * cy.wait('...')
             *
             * // More specifically it equals to
             * cy.intercept({ pathname: '**‍/einkommensverschlechterung/calculateTemp/1', method: 'POST', times: 1 }).as('...');
             */
            waitForRequest<T>(
                method: Method,
                urlPart: string,
                run: () => T,
                params?: {waitOptions?: Partial<WaitOptions>}
            ): Chainable<T>;

            /**
             * Run an action and wait for a given download to initiate, the download url is the resulting subject
             *
             * @example
             *  cy.getDownloadUrl(() => {
             *      cy.getByData('statistik#0').click();
             *  }).as('downloadUrl');
             *
             *  cy.get<string>('@downloadUrl').then((url) => {
             *      cy.log(`downloading ${url}`);
             *      cy.downloadFile(url, fileName).as('download');
             *  });
             *
             *  // or
             *
             *  cy.get<string>('@downloadUrl').then((url) => {
             *      cy.request(url).then(response => {
             *          expect(response.headers['content-disposition']).to.match(/Statistik.*\.csv/)
             *      });
             *  });
             */
            getDownloadUrl(downloadAction: () => void): Chainable<string>;

            /**
             *
             */
            changeMandant(mandant: KiBonMandant): void;
        }
    }
}

Cypress.Commands.add('login', (user: User) => {
    const userSelector = /.*] (.*)/.exec(user)[1].split(' ').join('-');
    cy.session(
        'login' + user,
        () => {
            cy.intercept({
                pathname: '**/auth/authenticated-user',
                method: 'GET',
                times: 1
            }).as('authCall');
            cy.visit('/#/locallogin');
            cy.get(`[data-test="test-user-${userSelector}"]`).click();
            cy.wait('@authCall', {timeout: 3000});
        },
        {
            validate: () => {
                cy.intercept({
                    pathname: '**/auth/authenticated-user',
                    method: 'GET',
                    times: 1
                }).as('authCallValidation');
                cy.visit('/#/');
                cy.reload();
                cy.wait('@authCallValidation', {timeout: 3000})
                    .its('response.body')
                    .then(response => {
                        expect(`${response.vorname}-${response.nachname}`).eq(
                            userSelector
                        );
                    });
            }
        }
    );
});
Cypress.Commands.add('groupBy', (context, run) => {
    Cypress.log({message: context, displayName: 'Group:'});
    return cy.get('body', {log: false}).within(() => {
        run();
    });
});
Cypress.Commands.add('waitForRequest', (method, pathname, run, params?) => {
    const alias = `Request ${method} ${pathname}`;
    cy.intercept({method, pathname, times: 1}).as(alias);
    run();
    cy.wait(`@${alias}`, params?.waitOptions);
});
Cypress.Commands.addQuery('getByData', (name, ...names) => {
    const getFn = cy.now(
        'get',
        [name, ...names].map(name => `[data-test="${name}"]`).join(' '),
        {}
    ) as (subject: any) => any;
    return subject => getFn(subject);
});
Cypress.Commands.addQuery('findByData', (name, ...names) => {
    return subject =>
        subject.find(
            [name, ...names].map(name => `[data-test="${name}"]`).join(' ')
        );
});
Cypress.Commands.add('changeLogin', (user: User) => {
    cy.clearAllSessionStorage();
    cy.clearAllCookies();

    cy.visit('/#/');
    cy.reload();

    cy.login(user);
});
Cypress.Commands.add('downloadFile', (url, fileName) => {
    return cy
        .request({
            url: url as string,
            method: 'GET',
            encoding: 'binary'
        })
        .then(res => {
            if (res.status === 200) {
                return cy
                    .writeFile(
                        `${Cypress.config('downloadsFolder')}/${fileName}`,
                        res.body,
                        'binary'
                    )
                    .then(() => fileName);
            }
            throw new Error(`Failed to download: ${url}`);
        });
});
Cypress.Commands.add('resetViewport', () => {
    const width = Cypress.config('viewportWidth');
    const height = Cypress.config('viewportHeight');

    cy.viewport(width, height);
});
Cypress.Commands.add('closeMaterialOverlay', () => {
    cy.log('Closing material dialog/overlay');
    cy.get('.md-menu-backdrop').should('not.have.class', 'ng-animate').click();
});
Cypress.Commands.add('getDownloadUrl', action => {
    cy.window().then(win => {
        const result = new Promise<string>(resolve => {
            // Mock the first window.open call to render the download preparation page into an iframe
            cy.stub(win, 'open').callsFake(url => {
                const iframe = win.document.createElement('iframe');
                iframe.src = url;
                win.document.body.appendChild(iframe);
                const newWin = iframe.contentWindow;

                iframe.onload = function (this: any) {
                    // Mock the second window.open to obtain the download url
                    cy.stub(this.contentWindow, 'open').callsFake(url => {
                        resolve(url);
                        iframe.onload = null;
                        iframe.remove();

                        return this.contentWindow;
                    });
                };

                return newWin;
            });
        });
        action();
        return cy.wrap(result).then((result: string) => {
            return cy.location('origin').then(origin => origin.concat(result));
        });
    });
});
Cypress.Commands.add('changeMandant', (mandant: KiBonMandant) => {
    // this only works for environments like dev, demo, uat, preview with a "-" in the url
    if (Cypress.config().baseUrl.includes('-')) {
        let prefixUrl: string[] = Cypress.config().baseUrl.split('-');
        let urlToSplit: string[] = prefixUrl[1].split('.');
        let newBaseUrl: string =
            prefixUrl[0] +
            '-' +
            mandant.hostname +
            '.' +
            urlToSplit[1] +
            '.' +
            urlToSplit[2];
        Cypress.config('baseUrl', newBaseUrl);
    }
});
