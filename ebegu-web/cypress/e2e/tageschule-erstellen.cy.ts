/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
import {FixtureTagesschule} from '@dv-e2e/fixtures';
import {
    CreateTagesschulePO,
    EditTagesschulePO,
    InstitutionListPO,
    RemoveDialogPO,
    TagesschuleModulDialogPO,
    TagesschuleModulImportDialogPO,
} from '@dv-e2e/page-objects';
import {getUser} from '@dv-e2e/types';

describe('Kibon - generate Tagesschule Institutionen', () => {
    const superAdmin = getUser('[1-Superadmin] E-BEGU Superuser');
    const gemeindeAdministator = getUser('[6-P-Admin-Gemeinde] Gerlinde Hofstetter');

    let tagesschuleDynamischName = '';

    beforeEach(() => {
        cy.intercept({resourceType: 'xhr'}, {log: false}); // don't log XHRs
    });

    it('should create a Tagesschule with Dynamische Module',  () => {
        cy.login(gemeindeAdministator);
        cy.visit('/#/institution/list');

        CreateTagesschulePO.createTagesschule('dynamisch', 'withValid');

        EditTagesschulePO.editTagesschuleForm('dynamisch', 'withValid');

        controllEditTagesschuleForm('dynamisch', 'withValid');

        cy.getByData('institution.edit.submit').click();
        cy.getByData('institution.edit.name').invoke('val').then((value) => {
            tagesschuleDynamischName = value.toString();
        });
        // Module 1
        EditTagesschulePO.getGesuchsperiodeTab(1).find('.dv-accordion-tab-title').click();
        EditTagesschulePO.getAddModuleButton(1).click();
        TagesschuleModulDialogPO.getBezeichnungDe().type('Dynamo');
        TagesschuleModulDialogPO.getBezeichnungFr().type('Dynamique');
        TagesschuleModulDialogPO.getZeitVon().type('08:00');
        TagesschuleModulDialogPO.getZeitBis().type('12:00');
        TagesschuleModulDialogPO.getVerpflegungskosten().type('4');
        TagesschuleModulDialogPO.getMontag().find('.mat-checkbox-inner-container').click();
        TagesschuleModulDialogPO.getDienstag().find('.mat-checkbox-inner-container').click();
        TagesschuleModulDialogPO.getDonnerstag().find('.mat-checkbox-inner-container').click();
        TagesschuleModulDialogPO.getFreitag().find('.mat-checkbox-inner-container').click();
        TagesschuleModulDialogPO.getOkButton().click();

        // Module 2
        EditTagesschulePO.getAddModuleButton(1).click();
        TagesschuleModulDialogPO.getBezeichnungDe().type('Dynamo Nachmittag');
        TagesschuleModulDialogPO.getBezeichnungFr().type('Après-midi dynamique');
        TagesschuleModulDialogPO.getZeitVon().type('13:00');
        TagesschuleModulDialogPO.getZeitBis().type('17:00');
        TagesschuleModulDialogPO.getVerpflegungskosten().type('3');
        TagesschuleModulDialogPO.getMontag().find('.mat-checkbox-inner-container').click();
        TagesschuleModulDialogPO.getMittwoch().find('.mat-checkbox-inner-container').click();
        TagesschuleModulDialogPO.getFreitag().find('.mat-checkbox-inner-container').click();
        TagesschuleModulDialogPO.getWirdPaedagogischBetreut().click();
        TagesschuleModulDialogPO.getIntervall().click();
        TagesschuleModulDialogPO.getIntervallOption('WOECHENTLICH_ODER_ALLE_ZWEI_WOCHEN').click();
        TagesschuleModulDialogPO.getOkButton().click();

        // Check Result: header + 2 Module
        EditTagesschulePO.getGesuchsperiodeModulTable(1).find('tr').its('length').should('eq', 3);
        cy.waitForRequest('PUT', '**/institutionen/**', () => {
            EditTagesschulePO.getEditSaveButton().click();
        });
        // header + 2 Module
        EditTagesschulePO.getGesuchsperiodeModulTable(1).find('tr').its('length').should('eq', 3);
    });

    it('should create a Tagesschule with imported Module', () => {
        cy.login(gemeindeAdministator);
        cy.visit('/#/institution/list');

        CreateTagesschulePO.createTagesschule('import', 'withValid');

        EditTagesschulePO.editTagesschuleForm('import', 'withValid');

        controllEditTagesschuleForm('import', 'withValid');

        // import Module from previously created Tagesschule
        EditTagesschulePO.getEditSaveButton().click();
        EditTagesschulePO.getGesuchsperiodeTab(1).find('.dv-accordion-tab-title').click();

        EditTagesschulePO.getAddImportModuleButton(1).click();
        TagesschuleModulImportDialogPO.getInstitution().select(tagesschuleDynamischName);
        TagesschuleModulImportDialogPO.getGesuchsperiode().select(0);
        TagesschuleModulImportDialogPO.getImportButton().click();
        TagesschuleModulImportDialogPO.getImportButton().should('not.exist');

        // Check Result: header + 2 Module
        cy.waitForRequest('PUT', '**/institutionen/**', () => {
        EditTagesschulePO.getEditSaveButton().click();
        });
        // header + 2 Module
        EditTagesschulePO.getGesuchsperiodeModulTable(1).find('tr').its('length').should('eq', 3);
        // check imported modules values
        EditTagesschulePO.getGesuchsperiodeModulTable(1).find('tr').eq(1).find('td').eq(0).get('span').should('contain', 'Dynamo / Dynamique');
        EditTagesschulePO.getGesuchsperiodeModulTable(1).find('tr').eq(1).find('td').eq(1).should('contain', '8:00 - 12:00');
        EditTagesschulePO.getGesuchsperiodeModulTable(1).find('tr').eq(1).find('td').eq(2).should('contain', '4');
        EditTagesschulePO.getGesuchsperiodeModulTable(1).find('tr').eq(1).find('td').eq(3).should('contain', 'Mo, Di, Do, Fr');
        EditTagesschulePO.getGesuchsperiodeModulTable(1).find('tr').eq(1).find('td').eq(4).should('contain', 'Ja');
        EditTagesschulePO.getGesuchsperiodeModulTable(1).find('tr').eq(1).find('td').eq(5).should('contain', 'Wöchentlich');
        EditTagesschulePO.getGesuchsperiodeModulTable(1).find('tr').eq(2).find('td').eq(0).get('span').should('contain', 'Dynamo Nachmittag / Après-midi dynamique');
        EditTagesschulePO.getGesuchsperiodeModulTable(1).find('tr').eq(2).find('td').eq(1).should('contain', '13:00 - 17:00');
        EditTagesschulePO.getGesuchsperiodeModulTable(1).find('tr').eq(2).find('td').eq(2).should('contain', '3');
        EditTagesschulePO.getGesuchsperiodeModulTable(1).find('tr').eq(2).find('td').eq(3).should('contain', 'Mo, Mi, Fr');
        EditTagesschulePO.getGesuchsperiodeModulTable(1).find('tr').eq(2).find('td').eq(4).should('contain', 'Nein');
        EditTagesschulePO.getGesuchsperiodeModulTable(1).find('tr').eq(2).find('td').eq(5).should('contain', 'Wöchentlich oder alle zwei Wochen');
    });

    it('should delete created Tagesschule', () => {
        cy.login(superAdmin);
        cy.visit('/#/institution/list');
        InstitutionListPO.getSearchField().type('-cy-');
        InstitutionListPO.getAllListItemNames()
            .each($el => cy.wrap($el).should('include.text', '-cy-'));
        InstitutionListPO.getAllInstitutionLoeschenButtons().its('length').then(length => {
            for (let i = length - 1; i >= 0; i--) {
                InstitutionListPO.getAllInstitutionLoeschenButtons().eq(i).click();
                cy.waitForRequest('GET', '**/institutionen/editable/currentuser/listdto', () => {
                    RemoveDialogPO.getRemoveOkButton().click();
                });
            }
        });
        InstitutionListPO.getSearchField().clear().type('-cy-');
        InstitutionListPO.getAllListItemNames().should('have.length', 0);
    })
});

function controllEditTagesschuleForm(
    tagesschuleArt: 'dynamisch' | 'scolaris' | 'import',
    dataset: keyof typeof FixtureTagesschule,
) {
    FixtureTagesschule[dataset](data => {
        EditTagesschulePO.getEditSaveButton().click();
        EditTagesschulePO.getAnschrift().should('have.value', data[tagesschuleArt].anschrift);
        EditTagesschulePO.getStrasse().should('have.value', data[tagesschuleArt].strasse);
        EditTagesschulePO.getHausnummer().should('have.value', data[tagesschuleArt].hausnummer);
        EditTagesschulePO.getPlz().should('have.value', data[tagesschuleArt].plz);
        EditTagesschulePO.getOrt().should('have.value', data[tagesschuleArt].ort);
        EditTagesschulePO.getGueltigAb().should('have.value', data[tagesschuleArt].gueltigAb);
        EditTagesschulePO.getCancelButton().click();
    });
}

