describe('Kibon - generate Testfälle [Superadmin]', () => {
    const adminUser = 'E-BEGU-Superuser';

    beforeEach(() => {
        cy.intercept({ resourceType: 'xhr' }, { log: false }); // don't log XHRs
        cy.login(adminUser);
        cy.visit('/#/faelle');
    });

    it('should create a prefilled new Testfall Antrag', () => {
        cy.getByData('page-title').contains('Alle Fälle');
        cy.getByData('page-menu').click();
        cy.getByData('action-admin.testdaten').click();
        cy.getByData('gemeinde').click();
        cy.getByData('gemeinde.London').click();
        cy.getByData('periode').click();
        cy.getByData('periode.2022/23').click();
        cy.getByData('creationType.warten').find('label').click();
        cy.getByData('testfall-2').click();
        cy.get('[data-test="dialog-link"]', { timeout: 20000 }).click();
        cy.getByData('fall-creation-eingangsdatum').find('input').should('have.value', '15.2.2016');
    });
});

