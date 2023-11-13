const createBeschaeftigungspensum = (gesuchSteller: 'GS1' | 'GS2') => {
    cy.getByData(`container.add-erwerbungspensum-erwerbspensen${gesuchSteller}`, 'navigation-button').click();
    cy.getByData('bezeichnung').type('DVBern');
    cy.getByData('taetigkeit').select('string:ANGESTELLT');
    cy.getByData('taetigkeit-pensum').type('70');
    cy.getByData('taetigkeit-ab').find('input').type('01.01.1990');

    cy.intercept('GET', '**/erwerbspensen/required/**').as(`reloadingTaetigkeiten${gesuchSteller}`);
    cy.get('[data-test="container.navigation-save"] [data-test="navigation-button"]:not([disabled])').click();
    cy.wait(`@reloadingTaetigkeiten${gesuchSteller}`);
}

export const AntragBeschaeftigungspensumPO = {
    createBeschaeftigungspensum
}
