/// <reference types="cypress" />

type OnlyValidSelectors<T> = T extends string ? (T extends `${string}${'[data-test='}${string}` ? 'Please specify the value given to data-test="", getByData automatically wraps the value in [data-test="..."]' : T) : never

declare namespace Cypress {
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    interface Chainable<Subject> {
        login(user: string): void;
        changeLogin(user: string): void;

        getByData<T extends string>(name: OnlyValidSelectors<T>, ...nestedNames: OnlyValidSelectors<T>[]): Chainable<Subject>;

        checkMaterial(): Subject;
    }
}
Cypress.Commands.add('login', (user: string) => {
    cy.session(
        'login' + user,
        () => {
            cy.intercept({ pathname: '**/auth/authenticated-user', method: 'GET', times: 1 }).as('authCall');
            cy.visit('/#/locallogin');
            cy.get(`[data-test="test-user-${user}"]`).click();
            cy.wait('@authCall');
        },
        {
            validate: () => {
                cy.intercept({ pathname: '**/auth/authenticated-user', method: 'GET', times: 1 }).as('authCallValidation');
                cy.visit('/#/');
                cy.reload();
                cy.wait('@authCallValidation').its('response.body').then(response => {
                    expect(`${response.vorname}-${response.nachname}`).eq(user);
                });
            },
        },
    );
});
Cypress.Commands.add('getByData', (name, ...names) => {
    return cy.get([name, ...names].map(name => `[data-test="${name}"]`).join(' '));
});
Cypress.Commands.add('checkMaterial', { prevSubject: 'element' }, (subject) => {
    return cy.wrap(subject).find('input').check({ force: true }).then(() => subject);
});
Cypress.Commands.add('changeLogin', (user: string) => {
    cy.clearAllSessionStorage();
    cy.clearAllCookies();

    cy.visit('/#/');
    cy.reload();

    cy.login(user);
});
