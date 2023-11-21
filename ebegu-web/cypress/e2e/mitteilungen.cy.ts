describe('Kibon - Test Mitteilungen', () => {
    const userSuperAdmin = 'E-BEGU-Superuser';
    const userSB = 'Jörg-Keller';
    const userGS = 'Michael-Berger';
    let gesuchUrl: string;

    before(() => {
        cy.intercept({ resourceType: 'xhr' }, { log: false }); // don't log XHRs
        cy.login(userSuperAdmin);
        cy.visit('/#/faelle');

        cy.getByData('page-menu').click();
        cy.getByData('action-admin.testdaten').click();
        cy.getByData('gesuchsteller').click();
        cy.getByData(`gesuchsteller.${userGS}`).click();
        cy.getByData('gemeinde').click();
        cy.getByData('gemeinde.London').click();
        cy.getByData('periode').click();
        cy.getByData('periode.2022/23').click();
        cy.getByData('creationType.verfuegt').find('label').click();

        cy.getByData('testfall-2').click();
        cy.get('[data-test="dialog-link"]', { timeout: 20000 }).click();
        cy.getByData('fall-creation-eingangsdatum').find('input').should('have.value', '15.2.2016');
        cy.getByData('verantwortlicher').click();
        cy.getByData(`option.${userSB}`).click();

        cy.url().then(url => {
            const parts = new URL(url);
            gesuchUrl = `${parts.pathname}${parts.hash}`;
        });
    });

    it('Gesuchsteller send Message to Sachbearbeiter', () => {
        cy.login(userGS);

        cy.intercept('GET', '**/gesuchsperioden/gemeinde/**').as('untilReadyGS1');
        cy.visit(gesuchUrl);
        cy.wait('@untilReadyGS1');

        cy.getByData('toolbar.mitteilungen').click();
        cy.getByData('subject').type('Mitteilung GS - 1');
        cy.getByData('nachricht').type('Irgend ein Inhalt');
        cy.getByData('container.senden', 'navigation-button').click()
    });

    it('Sachbearbeiter sees message and responds', () => {
        cy.login(userSB);

        cy.intercept('GET', '**/mitteilungen/amountnewforuser/**').as('mitteilungCount');
        cy.intercept('GET', '**/gesuchsperioden/gemeinde/**').as('untilReadySB');
        cy.visit(gesuchUrl);
        cy.wait('@untilReadySB');
        cy.wait('@mitteilungCount');

        cy.getByData('posteingang-link').should('include.text', '(1)');
        cy.getByData('posteingang-link').click();

        cy.getByData('mitteilung#0').click();

        cy.getByData('container.mitteilung#0', 'nachricht-subject').should('include.text', 'Mitteilung GS - 1');
        cy.getByData('container.mitteilung#0').click();
        cy.getByData('container.mitteilung#0', 'nachricht-inhalt').should('include.text', 'Irgend ein Inhalt');

        cy.getByData('empfaenger').select('Antragsteller/in');
        cy.getByData('subject').type('Mitteilung SB - 1');
        cy.getByData('nachricht').type('Irgend ein anderer Inhalt');
        cy.getByData('container.senden', 'navigation-button').click();
    });

    it('Gesuchsteller sees Sachbearbeiter message', () => {
        cy.login(userGS);

        cy.intercept('GET', '**/amountnew/dossier/**').as('mitteilungCount');
        cy.visit('/#/');
        cy.wait('@mitteilungCount');

        cy.getByData('toolbar.mitteilungen').should('include.text', '(1)');
        cy.getByData('toolbar.mitteilungen').click();

        cy.getByData('container.mitteilung#0', 'nachricht-subject').should('include.text', 'Mitteilung SB - 1');
        cy.getByData('container.mitteilung#0').click();
        cy.getByData('container.mitteilung#0', 'nachricht-inhalt').should('include.text', 'Irgend ein anderer Inhalt');
    });
});
