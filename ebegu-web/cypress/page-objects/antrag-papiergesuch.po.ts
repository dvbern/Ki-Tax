import { FixturePapierAntrag } from '../support/fixture-helper';

const createPapierGesuch = (dataset: keyof typeof FixturePapierAntrag) => {
    cy.getByData('fall-eroeffnen').click();
    FixturePapierAntrag[dataset](data => {
        cy.getByData('fall-creation-eingangsdatum').find('input').type(data.fallCreationEingangsdatum);
    });
    cy.getByData('gesuchsperioden.2022/23').find('label').click();
    cy.intercept('POST', '**/gesuche');
    cy.intercept('GET', '**/PAPIERGESUCH').as('getPapierGesuch');
    cy.getByData('container.navigation-save', 'navigation-button').click();
    cy.wait('@getPapierGesuch');
}

export const PapierAntragPO = {
    createPapierGesuch
}

