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

import {FixtureBetreuung} from '@dv-e2e/fixtures';
import {GemeindeTestFall} from '@dv-e2e/types';
import {ConfirmDialogPO} from '../dialogs';

// !! -- PAGE OBJECTS -- !!
const getPageTitle = () => {
    return cy.getByData('page-title');
};

const getBetreuung = (kindIndex: number, betreuungsIndex: number) => {
    return cy.getByData('container.kind#' + kindIndex, 'container.betreuung#' + betreuungsIndex);
};

const getBetreuungspensum = (betreuungspensumIndex: number) => {
    return cy.getByData(`betreuungspensum-${betreuungspensumIndex}`);
};

const getWeiteresBetreuungspensumErfassenButton = () => {
    return cy.getByData('container.add-betreuungspensum', 'navigation-button');
};

const getMonatlicheBetreuungskosten = (betreuungspensumIndex: number) => {
    return cy.getByData('monatliche-betreuungskosten#' + betreuungspensumIndex);
};

const getBetreuungspensumAb = (betreuungspensumIndex: number) => {
    return cy.getByData('betreuung-datum-ab#0');
};

const getBetreuungspensumBis = (betreuungspensumIndex: number) => {
    return cy.getByData('betreuung-datum-bis#0');
};

const getKorrekteKostenBestaetigung = () => {
    return cy.getByData('korrekte-kosten-bestaetigung');
};

const getPlatzBestaetigenButton = () => {
	return cy.getByData('container.platz-bestaetigen', 'navigation-button');
};

const getMutationsmeldungErstellenButton = () => {
	return cy.getByData('mutationsmeldung-erstellen');
};

const getMutationsmeldungSendenButton = () => {
	return cy.getByData('mutationsmeldung-senden');
};


// !! -- PAGE ACTIONS -- !!
const createNewBetreuung = (kindIndex: number = 0) => {
    cy.intercept('**/institutionstammdaten/gesuchsperiode/gemeinde/*').as('getInstitutionsStammdaten');
    cy.getByData('container.create-betreuung', 'navigation-button').eq(kindIndex).click();
    cy.wait('@getInstitutionsStammdaten');
};

const createNewTagesschulAnmeldung = () => {
    cy.getByData('container.create-tagesschule', 'navigation-button').click();
};

const fillKitaBetreuungsForm = (dataset: keyof typeof FixtureBetreuung, gemeinde: GemeindeTestFall) => {
    FixtureBetreuung[dataset]((data) => {
        const kita = data[gemeinde].kita;
        cy.getByData('betreuungsangebot').select(kita.betreuungsangebot);
        cy.getByData('institution').find('input').type(kita.institution, { delay: 30 });
        cy.getByData('instutions-suchtext').click();
        cy.getByData('institution').find('input').should('have.value', kita.institution);
    });
};

const fillKitaBetreuungspensumForm = (dataset: keyof typeof FixtureBetreuung, gemeinde: GemeindeTestFall) => {
    cy.wait(2000);
    FixtureBetreuung[dataset]((data) => {
       const pensen = data[gemeinde].kita.betreuungspensen;
       pensen.forEach((pensum, index) => {
           if (index > 0) {
               AntragBetreuungPO.getWeiteresBetreuungspensumErfassenButton().click()
           }
           AntragBetreuungPO.getBetreuungspensum(index).type(pensum.monatlichesBetreuungspensum);
           AntragBetreuungPO.getMonatlicheBetreuungskosten(index).type(pensum.monatlicheBetreuungskosten);
           AntragBetreuungPO.getBetreuungspensumAb(index).find('input').type(pensum.von);
           AntragBetreuungPO.getBetreuungspensumBis(index).find('input').type(pensum.bis);
       });
    });
};

const fillOnlineKitaBetreuungsForm = (dataset: keyof typeof FixtureBetreuung, gemeinde: GemeindeTestFall, opts?: { mobile: boolean }) => {
    FixtureBetreuung[dataset]((data) => {
        const kita = data[gemeinde].kita;
        cy.getByData('betreuungsangebot').select(kita.betreuungsangebot);
        cy.getByData('container.vertrag', 'radio-value.ja').click();
        if (opts?.mobile) {
            cy.getByData('institution-mobile').select(kita.institution);
        } else {
            cy.getByData('institution').find('input').type(kita.institution);
            cy.getByData('instutions-suchtext').click();
            cy.getByData('institution').find('input').should('have.value', kita.institution);
        }
    });
};

const fillOnlineTfoBetreuungsForm = (dataset: keyof typeof FixtureBetreuung, gemeinde: GemeindeTestFall, opts?: { mobile: boolean }) => {
    FixtureBetreuung[dataset]((data) => {
        const tfo = data[gemeinde].tfo;
        cy.getByData('betreuungsangebot').select(tfo.betreuungsangebot);
        cy.getByData('container.vertrag', 'radio-value.ja').click();
        if (opts?.mobile) {
            cy.getByData('institution-mobile').select(tfo.institution);
        } else {
            cy.getByData('institution').find('input').type(tfo.institution);
            cy.getByData('instutions-suchtext').click();
            cy.getByData('institution').find('input').should('have.value', tfo.institution);
        }
    });
};

const selectTagesschulBetreuung = () => {
    cy.getByData('betreuungsangebot').select('Tagesschule');
};

const fillTagesschulBetreuungsForm = (dataset: keyof typeof FixtureBetreuung, gemeinde: GemeindeTestFall) => {
    FixtureBetreuung[dataset]((data) => {
        const tagesschule = data[gemeinde].tagesschule.institution;
        cy.getByData('container.vertrag', 'radio-value.nein').should('not.exist');
        cy.wait(1000);
        cy.getByData('institution').find('input').focus().type(tagesschule, { force: true, delay: 30 });
        cy.getByData('instutions-suchtext').first().click();
        cy.getByData('institution').find('input').should('have.value', tagesschule);
        cy.getByData('keineKesbPlatzierung.radio-value.nein').click();
        cy.get('[data-test^="modul-"][data-test$="-MONDAY"]').first().click();
        cy.get('[data-test^="modul-"][data-test$="-THURSDAY"]').first().click();
        cy.getByData('agb-tsakzeptiert').click();
    });
};

const fillKeinePlatzierung = () => {
    cy.getByData('keineKesbPlatzierung.radio-value.nein').click();
};

const fillErweiterteBeduerfnisse = () => {
    cy.getByData('erweiterteBeduerfnisse.radio-value.ja').click();
    cy.getByData('fachstelle').select(1);
};

const fillEingewoehnung = () => {
    cy.getByData('eingewoehnung').click();
};

const platzBestaetigungAnfordern = () => {
    cy.intercept('PUT', '**/betreuungen/betreuung/false').as('savingBetreuung');
    cy.getByData('container.platzbestaetigung-anfordern', 'navigation-button').click();
    cy.wait('@savingBetreuung');
};

const saveBetreuung = () => {
    cy.waitForRequest('PUT', '**/betreuungen/betreuung/*', () => {
        cy.getByData('container.save','navigation-button').click();
    });
};

const saveAndConfirmBetreuung = () => {
    cy.getByData('container.save','navigation-button').click();
    cy.waitForRequest('PUT', '**/betreuungen/betreuung/*', () => {
        cy.getByData('container.confirm', 'navigation-button').click();
    });
};

const platzBestaetigen = () => {
    getKorrekteKostenBestaetigung().click();
    getPlatzBestaetigenButton().click();
    cy.waitForRequest('GET', '**/search/pendenzenBetreuungen', () => {
        ConfirmDialogPO.getConfirmButton().click();
    });
};

export const AntragBetreuungPO = {
    // page objects
    getPageTitle,
    getBetreuung,
    getBetreuungspensum,
    getMonatlicheBetreuungskosten,
    getBetreuungspensumAb,
    getBetreuungspensumBis,
    getKorrekteKostenBestaetigung,
    getPlatzBestaetigenButton,
    getWeiteresBetreuungspensumErfassenButton,
    getMutationsmeldungErstellenButton,
    getMutationsmeldungSendenButton,
    // page actions
    createNewBetreuung,
    createNewTagesschulAnmeldung,
    selectTagesschulBetreuung,
    fillTagesschulBetreuungsForm,
    fillKitaBetreuungsForm,
    fillOnlineKitaBetreuungsForm,
    fillOnlineTfoBetreuungsForm,
    fillKeinePlatzierung,
    fillErweiterteBeduerfnisse,
    fillEingewoehnung,
    platzBestaetigungAnfordern,
    saveBetreuung,
    saveAndConfirmBetreuung,
    fillKitaBetreuungspensumForm,
    platzBestaetigen,
};
