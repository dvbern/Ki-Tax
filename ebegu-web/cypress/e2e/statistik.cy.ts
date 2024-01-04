import {StatistikPO, TestFaellePO} from '@dv-e2e/page-objects';
import { getUser } from '@dv-e2e/types';

describe('Kibon - generate Statistiken', () => {
    const downloadsPath = Cypress.config('downloadsFolder');
    const fileName = 'StatistikTest.xlsx';
    const userSuperadmin = getUser('[1-Superadmin] E-BEGU Superuser');
    const userSB = getUser('[6-P-SB-Gemeinde] Stefan Wirth');

    before(() => {
        cy.login(userSuperadmin);
        cy.visit('/#/faelle');

        TestFaellePO.createPapierTestfall({
            testFall: 'testfall-2',
            gemeinde: 'Paris',
            periode: '2022/23',
            betreuungsstatus: 'verfuegt'
        });
    });

    beforeEach(() => {
        cy.intercept({ resourceType: 'xhr' }, { log: false }); // don't log XHRs
        cy.login(userSB);
        cy.visit('/#/statistik');
    });

    it('should correctly create the Betreuungsgutscheine: Antragsstellende-Kinder-Betreuung statistik', () => {
        StatistikPO.getGesuchstellendeKinderBetreuungTab().click();

        StatistikPO.getVon().find('input').type('01.07.2023');
        StatistikPO.getBis().find('input').type('31.07.2023');
        StatistikPO.getGesuchsperiode().select('2022/23');

        cy.waitForRequest('GET', '**/admin/batch/userjobs/**', () => {
            StatistikPO.getGenerierenButton().click();
        });

        StatistikPO.getStatistikJobStatus(0).should(
            'include.text',
            'Bereit zum Download'
        );

        cy.getDownloadUrl(() => {
            StatistikPO.getStatistikJob(0).click();
        }).as('downloadUrl');
        cy.get<string>('@downloadUrl').then((url) => {
            cy.log(`downloading ${url}`);
            cy.downloadFile(url, fileName).as('download');
        });

        cy.get('@download').should('not.equal', false);
        cy.get<string>('@download')
            .then((fileName) =>
                cy.task(
                    'convertXlsxToJson',
                    {
                        dirPath: downloadsPath,
                        fileName,
                        refs: 'A8:CL50000'
                    },
                    { custom: true }
                )
            )
            .then(([{ data }]) => {
                // Check header
                expect(data[0][0]).to.eq('Institution');

                const last = data.length - 1;
                // Check Name
                expect(data[last - 1][56]).to.eq('Tamara');
                expect(data[last][56]).to.eq('Leonard');

                // Check Betreuung
                expect(data[last - 1][74]).to.gt(0);
                expect(data[last][74]).to.gt(0);

                // Check Gutschein Total
                expect(data[last - 1][89]).to.gt(100);
                expect(data[last][89]).to.gt(100);
            });
    });
});
