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

import { OnlyValidSelectors, User } from '@dv-e2e/types';

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
            getByData<T extends string>(name: OnlyValidSelectors<T>, ...nestedNames: OnlyValidSelectors<T>[]): Chainable<Subject>;

            /**
             * Download a file using given url and save it with the given name
             *
             * @see Cypress.config('downloadsFolder')
             */
            downloadFile(url: string, fileName: string): Chainable<Subject>;

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
        }
    }
}

Cypress.Commands.add('login', (user: User) => {
    const userSelector = /.*] (.*)/.exec(user)[1].split(' ').join('-');
    cy.session(
        'login' + user,
        () => {
            cy.intercept({ pathname: '**/auth/authenticated-user', method: 'GET', times: 1 }).as('authCall');
            cy.visit('/#/locallogin');
            cy.get(`[data-test="test-user-${userSelector}"]`).click();
            cy.wait('@authCall');
        },
        {
            validate: () => {
                cy.intercept({ pathname: '**/auth/authenticated-user', method: 'GET', times: 1 }).as('authCallValidation');
                cy.visit('/#/');
                cy.reload();
                cy.wait('@authCallValidation')
                    .its('response.body')
                    .then((response) => {
                        expect(`${response.vorname}-${response.nachname}`).eq(userSelector);
                    });
            },
        }
    );
});
Cypress.Commands.add('getByData', (name, ...names) => {
    return cy.get([name, ...names].map((name) => `[data-test="${name}"]`).join(' '));
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
            encoding: 'binary',
        })
        .then((res) => {
            if (res.status === 200) {
                return cy.writeFile(`${Cypress.config('downloadsFolder')}/${fileName}`, res.body, 'binary').then(() => fileName);
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
