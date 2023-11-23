/// <reference types="cypress" />

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
        }
    }
}

Cypress.Commands.add('login', (user: User) => {
    console.log('/.*] (.*)/.exec(user)', /.*] (.*)/.exec(user));
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
