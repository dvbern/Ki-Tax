import { FixtureBeschaeftigungspensum } from '@dv-e2e/fixtures';

const createBeschaeftigungspensum = (gesuchSteller: 'GS1' | 'GS2', dataset: keyof typeof FixtureBeschaeftigungspensum) => {
    FixtureBeschaeftigungspensum[dataset](data => {
        cy.getByData(`container.add-erwerbungspensum-erwerbspensen${gesuchSteller}`, 'navigation-button').click();
        cy.getByData('bezeichnung').type(data[gesuchSteller].bezeichnung);
        cy.getByData('taetigkeit').select(`string:${data[gesuchSteller].taetigkeit}`);
        cy.getByData('taetigkeit-pensum').type(data[gesuchSteller].taetigkeitPensum);
        cy.getByData('taetigkeit-ab').find('input').type(data[gesuchSteller].taetigkeitAb);

        cy.intercept('GET', '**/erwerbspensen/required/**').as(`reloadingTaetigkeiten${gesuchSteller}`);
        cy.get('[data-test="container.navigation-save"] [data-test="navigation-button"]:not([disaFbled])').click();
        cy.wait(`@reloadingTaetigkeiten${gesuchSteller}`);
    });
};

export const AntragBeschaeftigungspensumPO = {
    createBeschaeftigungspensum,
};
