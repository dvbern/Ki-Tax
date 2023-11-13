const createNewBetreuung = () => {
    cy.getByData('container.create-betreuung', 'navigation-button').click();
};

const fillBetreuungForm = () => {
    cy.getByData('betreuungsangebot').select('Kita');
    cy.getByData('institution').find('input').type('Brünnen');
    cy.getByData('institution').find('input').should('have.value', 'Brünnen');
    cy.getByData('instutions-suchtext').click();
};

const fillKeinePlatzierung = () => {
    cy.getByData('keineKesbPlatzierung.radio-value.ja').click();
};

const fillErweiterteBeduerfnisse = () => {
    cy.getByData('erweiterteBeduerfnisse.radio-value.ja').click();
    cy.getByData('fachstelle').select('string:46d37d8e-4083-11ec-a836-b89a2ae4a038');
};

const fillEingewoehnung = () => {
    cy.getByData('eingewoehnung').click();
};

const platzBestaetigungAnfordern = () => {
    cy.intercept('PUT', '**/betreuungen/betreuung/false').as('savingBetreuung');
    cy.getByData('container.platzbestaetigung-anfordern', 'navigation-button').click();
    cy.getByData('container.confirm', 'navigation-button').click();
    cy.wait('@savingBetreuung');
};

export const AntragBetreuungPO = {
    createNewBetreuung,
    fillBetreuungForm,
    fillKeinePlatzierung,
    fillErweiterteBeduerfnisse,
    fillEingewoehnung,
    platzBestaetigungAnfordern,
};
