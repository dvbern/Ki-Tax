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
import {getUser} from '@dv-e2e/types';
import {ControllEditTagesschulePO, CreateTagesschulePO, EditTagesschulePO} from '../page-objects/antrag/tagesschule.po';

describe('Kibon - generate Tagesschule Institutionen', () => {
    const superAdmin = getUser('[1-Superadmin] E-BEGU Superuser')
    const gemeindeAdministator = getUser('[6-P-Admin-Gemeinde] Gerlinde Hofstetter');

    let tagesschuleDynamischName = '';

    beforeEach(() => {
        cy.intercept({resourceType: 'xhr'}, {log: false}); // don't log XHRs
    });

    it('should create a Tagesschule with Dynamische Module', function () {
        cy.login(gemeindeAdministator);
        cy.visit('/#/institution/list');

        CreateTagesschulePO.createTagesschule('dynamisch', 'withValid');

        EditTagesschulePO.editTagesschuleForm('dynamisch', 'withValid')

        ControllEditTagesschulePO.controllEditTagesschuleForm('dynamisch', 'withValid')

        cy.getByData('institution.edit.submit').click();
        cy.getByData('institution.edit.name').invoke('val').then((value) => {
            tagesschuleDynamischName = value;
        })
        // Module 1
        cy.getByData('institution.gesuchsperiode-1').find('.dv-accordion-tab-title').click();
        cy.getByData('institution.gesuchsperiode.add.modul-1').click();
        cy.getByData('institution.tageschule.modul.bezeichnungDe').type('Dynamo');
        cy.getByData('institution.tageschule.modul.bezeichnungFr').type('Dynamique');
        cy.getByData('institution.tageschule.modul.zeitVon').type('08:00');
        cy.getByData('institution.tageschule.modul.zeitBis').type('12:00');
        cy.getByData('institution.tageschule.modul.verpflegungskosten').type('4');
        cy.getByData('institution.tageschule.modul.montag').find('.mat-checkbox-inner-container').click();
        cy.getByData('institution.tageschule.modul.dienstag').find('.mat-checkbox-inner-container').click();
        cy.getByData('institution.tageschule.modul.donnerstag').find('.mat-checkbox-inner-container').click();
        cy.getByData('institution.tageschule.modul.freitag').find('.mat-checkbox-inner-container').click();
        cy.getByData('institution.tageschule.modul.ok').click();

        // Module 2
        cy.getByData('institution.gesuchsperiode.add.modul-1').click();
        cy.getByData('institution.tageschule.modul.bezeichnungDe').type('Dynamo Nachmittag');
        cy.getByData('institution.tageschule.modul.bezeichnungFr').type('Après-midi dynamique');
        cy.getByData('institution.tageschule.modul.zeitVon').type('13:00');
        cy.getByData('institution.tageschule.modul.zeitBis').type('17:00');
        cy.getByData('institution.tageschule.modul.verpflegungskosten').type('3');
        cy.getByData('institution.tageschule.modul.montag').find('.mat-checkbox-inner-container').click();
        cy.getByData('institution.tageschule.modul.mittwoch').find('.mat-checkbox-inner-container').click();
        cy.getByData('institution.tageschule.modul.freitag').find('.mat-checkbox-inner-container').click();
        cy.getByData('institution.tageschule.modul.wirdPaedagogischBetreut').click();
        cy.getByData('institution.tageschule.modul.intervall').click();
        cy.getByData('institution.tageschule.modul.intervall.WOECHENTLICH_ODER_ALLE_ZWEI_WOCHEN').click();
        cy.getByData('institution.tageschule.modul.ok').click();

        // Check Result: header + 2 Module
        cy.getByData('institution.gesuchsperiode.module.table-1').find('tr').its('length').should('eq', 3);
        cy.intercept('PUT', '**/institutionen/**').as('saveInstitution');
        cy.getByData('institution.edit.submit').click();
        cy.wait('@saveInstitution');
        // header + 2 Module
        cy.getByData('institution.gesuchsperiode.module.table-1').find('tr').its('length').should('eq', 3);
    });

    it('should create a Tagesschule with imported Module', function () {
        cy.login(gemeindeAdministator);
        cy.visit('/#/institution/list');

        CreateTagesschulePO.createTagesschule('import', 'withValid');

        EditTagesschulePO.editTagesschuleForm('import', 'withValid')

        ControllEditTagesschulePO.controllEditTagesschuleForm('import', 'withValid')

        // import Module from previously created Tagesschule
        cy.getByData('institution.edit.submit').click();
        cy.getByData('institution.gesuchsperiode-1').find('.dv-accordion-tab-title').click();
        cy.getByData('institution.gesuchsperiode.import.modul-1').click();
        cy.getByData('institution.tageschule.modul.import.institution').select(tagesschuleDynamischName);
        cy.getByData('institution.tageschule.modul.import.gesuchsperiode').select(0);
        cy.getByData('institution.tageschule.modul.import.button').click();
        cy.getByData('institution.tageschule.modul.import.button').should('not.exist');

        // Check Result: header + 2 Module
        cy.intercept('PUT', '**/institutionen/**').as('saveInstitution');
        cy.getByData('institution.edit.submit').click();
        cy.wait('@saveInstitution');
        // header + 2 Module
        cy.getByData('institution.gesuchsperiode.module.table-1').find('tr').its('length').should('eq', 3);
    });

    it('should delete created Tagesschule', () => {
        cy.login(superAdmin);
        cy.visit('/#/institution/list');
        cy.getByData('list-search-field').type('-cy-');
        cy.getByData('item-name')
            .each($el => cy.wrap($el).should('include.text', '-cy-'));
        cy.getByData('remove-entry').its('length').then(length => {
            for (let i = length - 1; i >= 0; i--) {
                cy.getByData('remove-entry').eq(i).click();
                cy.intercept({method: 'GET', pathname: '**/institutionen/editable/currentuser/listdto', times: 1})
                    .as('deletingRow');
                cy.getByData('remove-ok').click();
                cy.wait('@deletingRow');
            }
        });
        cy.getByData('list-search-field').clear().type('-cy-');
        cy.getByData('item-name').should('have.length', 0);
    })
});