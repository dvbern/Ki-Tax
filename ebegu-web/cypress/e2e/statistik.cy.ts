import {StatistikPO, TestFaellePO} from '@dv-e2e/page-objects';
import {getUser} from '@dv-e2e/types';

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
        cy.intercept({resourceType: 'xhr'}, {log: false}); // don't log XHRs
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
            'Bereit zum Download',
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
                        refs: 'A8:CL50000',
                    },
                    {custom: true},
                ),
            )
            .then(([{data}]) => {
                checkFirstHeaderRow(data);
                checkSecondHeaderRow(data);
                checkThirdHeaderRow(data);
                checkValuesOfTwoLastVerfuegteBetreuung(data);
            });
    });
});

function checkFirstHeaderRow(data: any): void {
    // Check top header row
    expect(data[0][0]).to.eq('Institution');
    expect(data[0][1]).to.eq('Angebot');
    expect(data[0][2]).to.eq('Periode');
    expect(data[0][3]).to.eq('Eingangsdatum');
    expect(data[0][4]).to.eq('Verfügungsdatum');
    expect(data[0][5]).to.eq('Fall-ID');
    expect(data[0][6]).to.eq('Gemeinde');
    expect(data[0][7]).to.eq('Referenznummer');
    expect(data[0][8]).to.eq('Auszahlung');
    expect(data[0][10]).to.eq('Antragsteller/in 1');
    expect(data[0][26]).to.eq('Antragsteller/in 2');
    expect(data[0][42]).to.eq('Familie');
    expect(data[0][44]).to.eq('Einkommen');
    expect(data[0][53]).to.eq('Geprüft durch Steuerbüro der Gemeinde');
    expect(data[0][54]).to.eq('Veranlagt');
    expect(data[0][55]).to.eq('Kind');
    expect(data[0][68]).to.eq('Betreuung');
}

function checkSecondHeaderRow(data: any): void {
    // Check second header row
    expect(data[1][8]).to.eq('IBAN-Nummer');
    expect(data[1][9]).to.eq('Kontoinhaber/in');
    expect(data[1][10]).to.eq('Nachname');
    expect(data[1][11]).to.eq('Vorname');
    expect(data[1][12]).to.eq('Strasse');
    expect(data[1][13]).to.eq('Nr');
    expect(data[1][14]).to.eq('Zusatz');
    expect(data[1][15]).to.eq('Plz');
    expect(data[1][16]).to.eq('Ort');
    expect(data[1][17]).to.eq('Diplomat');
    expect(data[1][18]).to.eq('Beschäftigungspensum');
    expect(data[1][26]).to.eq('Nachname');
    expect(data[1][27]).to.eq('Vorname');
    expect(data[1][28]).to.eq('Strasse');
    expect(data[1][29]).to.eq('Nr');
    expect(data[1][30]).to.eq('Zusatz');
    expect(data[1][31]).to.eq('Plz');
    expect(data[1][32]).to.eq('Ort');
    expect(data[1][33]).to.eq('Diplomat');
    expect(data[1][34]).to.eq('Beschäftigungspensum');
    expect(data[1][42]).to.eq('Situation');
    expect(data[1][43]).to.eq('Familiengrösse');
    expect(data[1][44]).to.eq('Anrechenbares Einkommen vor Familienabzug');
    expect(data[1][45]).to.eq('Familienabzug');
    expect(data[1][46]).to.eq('Massgebendes Einkommen');
    expect(data[1][47]).to.eq('Einkommensjahr');
    expect(data[1][48]).to.eq('Einkommensverschlechterung (Einkommensjahr +1)');
    expect(data[1][49]).to.eq('Einkommensverschlechterung (Einkommensjahr +2)');
    expect(data[1][50]).to.eq('Einkommensverschlechterung annuliert (Einkommensjahr +1)');
    expect(data[1][51]).to.eq('Einkommensverschlechterung annuliert (Einkommensjahr +2)');
    expect(data[1][52]).to.eq('Sozialhilfebeziehende');
    expect(data[1][55]).to.eq('Nachname');
    expect(data[1][56]).to.eq('Vorname');
    expect(data[1][57]).to.eq('Geburtsdatum');
    expect(data[1][58]).to.eq('Fachstelle');
    expect(data[1][59]).to.eq('Integration');
    expect(data[1][60]).to.eq('Baby-Faktor');
    expect(data[1][61]).to.eq('Besondere Bedürfnisse');
    expect(data[1][62]).to.eq('Spricht Amtssprache');
    expect(data[1][63]).to.eq('Schulstufe');
    expect(data[1][64]).to.eq('Bis 12 Monate');
    expect(data[1][65]).to.eq('13-47 Monate');
    expect(data[1][66]).to.eq('48-72 Monate');
    expect(data[1][67]).to.eq('Ab 73 Monate');
    expect(data[1][68]).to.eq('Von');
    expect(data[1][69]).to.eq('Bis');
    expect(data[1][72]).to.eq('Status');
    expect(data[1][73]).to.eq('Anteil Monat');
    expect(data[1][74]).to.eq('Pensum');
    expect(data[1][85]).to.eq('Kosten');
}

function checkThirdHeaderRow(data: any): void {
    // Check third header row
    expect(data[2][18]).to.eq('Total');
    expect(data[2][19]).to.eq('Angestellt');
    expect(data[2][20]).to.eq('In Aus- oder Weiterbildung');
    expect(data[2][21]).to.eq('Selbständig');
    expect(data[2][22]).to.eq('Arbeitssuchend');
    expect(data[2][23]).to.eq('In Integrations- oder Beschäftigungsprogramm');
    expect(data[2][24]).to.eq('Gesundheitliche Indikation');
    expect(data[2][25]).to.eq('Freiwilligenarbeit');
    expect(data[2][34]).to.eq('Total');
    expect(data[2][35]).to.eq('Angestellt');
    expect(data[2][36]).to.eq('In Aus- oder Weiterbildung');
    expect(data[2][37]).to.eq('Selbständig');
    expect(data[2][38]).to.eq('Arbeitssuchend');
    expect(data[2][39]).to.eq('In Integrations- oder Beschäftigungsprogramm');
    expect(data[2][40]).to.eq('Gesundheitliche Indikation');
    expect(data[2][41]).to.eq('Freiwilligenarbeit');
    expect(data[2][70]).to.eq('Nettoarbeitstage Monat');
    expect(data[2][71]).to.eq('Nettoarbeitstage Intervall');
    expect(data[2][74]).to.eq('Betreuung');
    expect(data[2][75]).to.eq('Anspruchberechtigt Kanton');
    expect(data[2][76]).to.eq('Anspruchberechtigt Gemeinde');
    expect(data[2][77]).to.eq('Anspruchberechtigt Total');
    expect(data[2][78]).to.eq('BG-Pensum Kanton');
    expect(data[2][79]).to.eq('BG-Pensum Gemeinde');
    expect(data[2][80]).to.eq('BG-Pensum in %');
    expect(data[2][81]).to.eq('BG-Pensum');
    expect(data[2][82]).to.eq('Einheit BG-Pensum');
    expect(data[2][83]).to.eq('BG-Monatspensum');
    expect(data[2][84]).to.eq('Ausserordentlicher Anspruch');
    expect(data[2][85]).to.eq('Kosten');
    expect(data[2][86]).to.eq('Elternbeitrag');
    expect(data[2][87]).to.eq('Gutschein Kanton');
    expect(data[2][88]).to.eq('Gutschein Gemeinde');
    expect(data[2][89]).to.eq('Gutschein Total');
}

function checkValuesOfTwoLastVerfuegteBetreuung(data: any): void {
    const last = data.length - 1;

    // Check Institution Name
    expect(data[last - 1][0]).to.eq('Weissenstein');
    expect(data[last][0]).to.eq('Weissenstein');

    // Check Angebot Typ
    expect(data[last - 1][1]).to.eq('Tagesstätte für Kleinkinder');
    expect(data[last][1]).to.eq('Tagesstätte für Kleinkinder');

    // Check Gesuchsperiode same as last created Fall
    expect(data[last - 1][2]).to.eq('2022/2023');
    expect(data[last][2]).to.eq('2022/2023');

    // Check Eingangsdatum and Verfuegungdatum
    expect(data[last - 1][3]).to.match(/[0-9]+/);
    expect(data[last][3]).to.match(/[0-9]+/);
    expect(data[last - 1][4]).to.match(/[0-9]+/);
    expect(data[last][4]).to.match(/[0-9]+/);

    // Check Fall ID
    expect(data[last - 1][5]).to.match(/[0-9]+/);
    expect(data[last][5]).to.match(/[0-9]+/);

    // Check Gemeinde Name
    expect(data[last - 1][6]).to.eq('Paris');
    expect(data[last][6]).to.eq('Paris');

    // Check Referenznummer
    expect(data[last - 1][7].length).to.eq(17);
    expect(data[last][7].length).to.eq(17);

    // Check IBAN-Nummer
    expect(data[last - 1][8]).to.eq('CH2089144969768441935');
    expect(data[last][8]).to.eq('CH2089144969768441935');

    // Check Kontoinhaber/in
    expect(data[last - 1][9]).to.eq('kiBon Test');
    expect(data[last][9]).to.eq('kiBon Test');

    // Check Nachname
    expect(data[last - 1][10]).to.eq('Feutz');
    expect(data[last][10]).to.eq('Feutz');

    // Check Vorname
    expect(data[last - 1][11]).to.eq('Yvonne');
    expect(data[last][11]).to.eq('Yvonne');

    // Check Strasse
    expect(data[last - 1][12]).to.eq('Testweg');
    expect(data[last][12]).to.eq('Testweg');

    // Check Strasse Nr
    expect(data[last - 1][13]).to.eq('10');
    expect(data[last][13]).to.eq('10');

    // Check Zusatz
    expect(data[last - 1][14]).to.empty;
    expect(data[last][14]).to.empty;

    // Check PLZ
    expect(data[last - 1][15]).to.eq('3000');
    expect(data[last][15]).to.eq('3000');

    // Check Ort
    expect(data[last - 1][16]).to.eq('Bern');
    expect(data[last][16]).to.eq('Bern');

    // Check Diplomat
    expect(data[last - 1][17]).to.empty;
    expect(data[last][17]).to.empty;

    // Check Total
    expect(data[last - 1][18]).to.eq(0);
    expect(data[last][18]).to.eq(0);

    // Check Angestellt
    expect(data[last - 1][19]).to.eq(0.4);
    expect(data[last][19]).to.eq(0.4);

    // Check In Aus- oder Weiterbildung
    expect(data[last - 1][20]).to.eq(0);
    expect(data[last][20]).to.eq(0);

    // Check Selbständig
    expect(data[last - 1][21]).to.eq(0);
    expect(data[last][21]).to.eq(0);

    // Check Arbeitssuchend
    expect(data[last - 1][22]).to.eq(0);
    expect(data[last][22]).to.eq(0);

    // Check In Integrations- oder Beschäftigungsprogramm
    expect(data[last - 1][23]).to.eq(0);
    expect(data[last][23]).to.eq(0);

    // Check Gesundheitliche Indikation
    expect(data[last - 1][24]).to.eq(0);
    expect(data[last][24]).to.eq(0);

    // Check Freiwilligenarbeit
    expect(data[last - 1][25]).to.eq(0);
    expect(data[last][25]).to.eq(0);

    // Check Nachname GS2
    expect(data[last - 1][26]).to.eq('Feutz');
    expect(data[last][26]).to.eq('Feutz');

    // Check Vorname GS2
    expect(data[last - 1][27]).to.eq('Tizian');
    expect(data[last][27]).to.eq('Tizian');

    // Check Strasse GS2
    expect(data[last - 1][28]).to.eq('Testweg');
    expect(data[last][28]).to.eq('Testweg');

    // Check Strasse NR GS2
    expect(data[last - 1][29]).to.eq('10');
    expect(data[last][29]).to.eq('10');

    // Check Zusatz GS2
    expect(data[last - 1][30]).to.empty;
    expect(data[last][30]).to.empty;

    // Check PLZ GS2
    expect(data[last - 1][31]).to.eq('3000');
    expect(data[last][31]).to.eq('3000');

    // Check Ort GS2
    expect(data[last - 1][32]).to.eq('Bern');
    expect(data[last][32]).to.eq('Bern');

    // Check Diplomat GS2
    expect(data[last - 1][33]).to.empty;
    expect(data[last][33]).to.empty;

    // Check Total GS2
    expect(data[last - 1][34]).to.eq(0);
    expect(data[last][34]).to.eq(0);

    // Check Angestellt GS2
    expect(data[last - 1][35]).to.eq(1);
    expect(data[last][35]).to.eq(1);

    // Check In Aus- oder Weiterbildung GS2
    expect(data[last - 1][36]).to.eq(0);
    expect(data[last][36]).to.eq(0);

    // Check Selbständig GS2
    expect(data[last - 1][37]).to.eq(0);
    expect(data[last][37]).to.eq(0);

    // Check Arbeitssuchend GS2
    expect(data[last - 1][38]).to.eq(0);
    expect(data[last][38]).to.eq(0);

    // Check In Integrations- oder Beschäftigungsprogramm GS2
    expect(data[last - 1][39]).to.eq(0);
    expect(data[last][39]).to.eq(0);

    // Check Gesundheitliche Indikation GS2
    expect(data[last - 1][40]).to.eq(0);
    expect(data[last][40]).to.eq(0);

    // Check Freiwilligenarbeit GS2
    expect(data[last - 1][41]).to.eq(0);
    expect(data[last][41]).to.eq(0);

    // Check Situation
    expect(data[last - 1][42]).to.eq('Verheiratet');
    expect(data[last][42]).to.eq('Verheiratet');

    // Check Familiengrösse
    expect(data[last - 1][43]).to.eq(4);
    expect(data[last][43]).to.eq(4);

    // Check Anrechenbares Einkommen vor Familienabzug
    expect(data[last - 1][44]).to.eq(137346);
    expect(data[last][44]).to.eq(137346);

    // Check Familienabzug
    expect(data[last - 1][45]).to.eq(24000);
    expect(data[last][45]).to.eq(24000);

    // Check Massgebendes Einkommen
    expect(data[last - 1][46]).to.eq(113346);
    expect(data[last][46]).to.eq(113346);

    // Check Einkommensjahr
    expect(data[last - 1][47]).to.eq(2021);
    expect(data[last][47]).to.eq(2021);

    // Check Einkommensverschlechterung (Einkommensjahr +1)
    expect(data[last - 1][48]).to.empty;
    expect(data[last][48]).to.empty;

    // Check Einkommensverschlechterung (Einkommensjahr +2)
    expect(data[last - 1][49]).to.empty;
    expect(data[last][49]).to.empty;

    // Check Einkommensverschlechterung annuliert (Einkommensjahr +1)
    expect(data[last - 1][50]).to.empty;
    expect(data[last][50]).to.empty;

    // Check Einkommensverschlechterung annuliert (Einkommensjahr +2)
    expect(data[last - 1][51]).to.empty;
    expect(data[last][51]).to.empty;

    // Check Sozialhilfebeziehende
    expect(data[last - 1][52]).to.empty;
    expect(data[last][52]).to.empty;

    // Check Geprüft durch Steuerbüro der Gemeinde
    expect(data[last - 1][53]).to.empty;
    expect(data[last][53]).to.empty;

    // Check Verlangt
    expect(data[last - 1][54]).to.eq('X');
    expect(data[last][54]).to.eq('X');

    // Check Nachname
    expect(data[last - 1][55]).to.eq('Feutz');
    expect(data[last][55]).to.eq('Feutz');

    // Check Name
    expect(data[last - 1][56]).to.eq('Tamara');
    expect(data[last][56]).to.eq('Leonard');

    // Check Geburtsdatum
    expect(data[last - 1][57]).to.eq(40005);
    expect(data[last][57]).to.eq(41232);

    // Check Fachstelle
    expect(data[last - 1][58]).to.empty;
    expect(data[last][58]).to.empty;

    // Check Integration
    expect(data[last - 1][59]).to.empty;
    expect(data[last][59]).to.empty;

    // Check Baby-Faktor
    expect(data[last - 1][60]).to.eq(0);
    expect(data[last][60]).to.eq(0);

    // Check Besondere Bedürfnisse
    expect(data[last - 1][61]).to.empty;
    expect(data[last][61]).to.empty;

    // Check Spricht Amtssprache
    expect(data[last - 1][62]).to.eq('X');
    expect(data[last][62]).to.eq('X');

    // Check Schulstufe
    expect(data[last - 1][63]).to.eq('Vorschulalter');
    expect(data[last][63]).to.eq('Vorschulalter');

    // Check Bis 12 Monate
    expect(data[last - 1][64]).to.eq(0);
    expect(data[last][64]).to.eq(0);

    // Check 13-47 Monate
    expect(data[last - 1][65]).to.eq(0);
    expect(data[last][65]).to.eq(0);

    // Check 48-72 Monate
    expect(data[last - 1][66]).to.eq(0);
    expect(data[last][66]).to.eq(0);

    // Check Ab 73 Monate
    expect(data[last - 1][67]).to.eq(0);
    expect(data[last][67]).to.eq(0);

    // Check Von
    expect(data[last - 1][68]).to.eq(45108);
    expect(data[last][68]).to.eq(45108);

    // Check Bis
    expect(data[last - 1][69]).to.eq(45138);
    expect(data[last][69]).to.eq(45138);

    // Check hidden field
    expect(data[last - 1][70]).to.eq(0);
    expect(data[last][70]).to.eq(0);

    // Check hidden field
    expect(data[last - 1][71]).to.eq(0);
    expect(data[last][71]).to.eq(0);

    // Check Status
    expect(data[last - 1][72]).to.eq('Verfügt');
    expect(data[last][72]).to.eq('Verfügt');

    // Check Anteil Monat
    expect(data[last - 1][73]).to.eq(0);
    expect(data[last][73]).to.eq(0);

    // Check Betreuung
    expect(data[last - 1][74]).to.eq(0.6);
    expect(data[last][74]).to.eq(0.4);

    // Check Anspruchberechtigt Kanton
    expect(data[last - 1][75]).to.eq(0.6);
    expect(data[last][75]).to.eq(0.6);

    // Check Anspruchberechtigt Gemeinde
    expect(data[last - 1][76]).to.eq(0);
    expect(data[last][76]).to.eq(0);

    // Check Anspruchberechtigt Total
    expect(data[last - 1][77]).to.eq(0.6);
    expect(data[last][77]).to.eq(0.6);

    // Check BG-Pensum Kanton
    expect(data[last - 1][78]).to.eq(0.6);
    expect(data[last][78]).to.eq(0.4);

    // Check BG-Pensum Gemeinde
    expect(data[last - 1][79]).to.eq(0);
    expect(data[last][79]).to.eq(0);

    // Check BG-Pensum in %
    expect(data[last - 1][80]).to.eq(0.6);
    expect(data[last][80]).to.eq(0.4);

    // Check BG-Pensum
    expect(data[last - 1][81]).to.eq(12);
    expect(data[last][81]).to.eq(8);

    // Check Einheit BG-Pensum
    expect(data[last - 1][82]).to.eq('Tage');
    expect(data[last][82]).to.eq('Tage');

    // Check BG-Monatspensum
    expect(data[last - 1][83]).to.eq(0);
    expect(data[last][83]).to.eq(0);

    // Check Ausserordentlicher Anspruch
    expect(data[last - 1][84]).to.empty;
    expect(data[last][84]).to.empty;

    // Check Kosten
    expect(data[last - 1][85]).to.eq(2000);
    expect(data[last][85]).to.eq(2000);

    // Check Elternbeitrag
    expect(data[last - 1][86]).to.eq(1389.5);
    expect(data[last][86]).to.eq(1593);

    // Check Gutschein Kanton
    expect(data[last - 1][87]).to.eq(478.5);
    expect(data[last][87]).to.eq(319);

    // Check Gutschein Gemeinde
    expect(data[last - 1][88]).to.eq(132);
    expect(data[last][88]).to.eq(88);

    // Check Gutschein Total
    expect(data[last - 1][89]).to.eq(610.5);
    expect(data[last][89]).to.eq(407);
}
