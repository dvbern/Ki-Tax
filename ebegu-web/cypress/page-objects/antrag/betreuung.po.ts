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
import {TSDayOfWeek} from '../../../src/models/enums/TSDayOfWeek';
import {ConfirmDialogPO} from '../dialogs';

// !! -- PAGE OBJECTS -- !!
const getPageTitle = () => {
    return cy.getByData('page-title');
};

const getBetreuung = (kindIndex: number, betreuungsIndex: number) => {
    return cy.getByData('container.kind#' + kindIndex, 'container.betreuung#' + betreuungsIndex);
};

const getBetreuungLoeschenButton = (kindIndex: number, betreuungsIndex: number) => {
    return cy.getByData('container.kind#' + kindIndex, 'container.betreuung#' + betreuungsIndex)
        .findByData('container.delete', 'navigation-button');
};

const getBetreuungErstellenButton = (kindIndex: number) => {
	return cy.getByData('container.kind#' + kindIndex, 'container.create-betreuung', 'navigation-button');
};

const getAnmeldungErstellenButton = (kindIndex: number) => {
	return cy.getByData('container.kind#' + kindIndex, 'container.create-tagesschule', 'navigation-button');
};

const getBetreuungsstatus = (kindIndex: number, betreuungsIndex: number) => {
	return getBetreuung(kindIndex, betreuungsIndex).findByData('betreuungs-status');
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
    return cy.getByData('betreuung-datum-ab#' + betreuungspensumIndex);
};

const getBetreuungspensumBis = (betreuungspensumIndex: number) => {
    return cy.getByData('betreuung-datum-bis#' + betreuungspensumIndex);
};

const getKorrekteKostenBestaetigung = () => {
    return cy.getByData('korrekte-kosten-bestaetigung');
};

const getSaveButton = () => {
	return cy.getByData('container.save','navigation-button');
};

const getPlatzbestaetigungAnfordernButton = () => {
	return cy.getByData('container.platzbestaetigung-anfordern', 'navigation-button');
};

const getPlatzBestaetigenButton = () => {
	return cy.getByData('container.platz-bestaetigen', 'navigation-button');
};

const getPlatzAbweisenButton = () => {
	return cy.getByData('container.platz-abweisen', 'navigation-button');
};

const getPlatzAkzeptierenButton = () => {
    return cy.getByData('container.akzeptieren', 'navigation-button');
};

const getMutationsmeldungErstellenButton = () => {
	return cy.getByData('mutationsmeldung-erstellen');
};

const getMutationsmeldungSendenButton = () => {
	return cy.getByData('mutationsmeldung-senden');
};

const getBetreuungsangebot = () => {
	return cy.getByData('betreuungsangebot');
};

const getInstitution = () => {
	return cy.getByData('institution');
};

const getInstitutionMobile = () => {
	return cy.getByData('institution-mobile');
};

const getInstitutionSuchtext = () => {
	return cy.getByData('instutions-suchtext');
};

const getHasVertrag = (answer: string) => {
	return cy.getByData('container.vertrag', 'radio-value.' + answer);
};

const getKesbPlatzierung = (answer: string) => {
	return cy.getByData('keineKesbPlatzierung.radio-value.' + answer);
};

const getXthTagesschulModulOfDay = (index: number, day: TSDayOfWeek) => {
	return cy.get(`[data-test^="modul-"][data-test$="-${day}"]`).eq(index);
};

const getAGBTSAkzeptiert = () => {
	return cy.getByData('agb-tsakzeptiert');
};

const getHasErweiterteBeduerfnisse = (answer: string) => {
    return cy.getByData('erweiterteBeduerfnisse.radio-value.' + answer);
};

const getFachstelle = () => {
	return cy.getByData('fachstelle');
};

const getEingewoehnung = () => {
	return cy.getByData('eingewoehnung');
};

const getGrundAblehnung = () => {
	return cy.getByData('grund-ablehnung');
};

// !! -- PAGE ACTIONS -- !!
const createNewBetreuung = (kindIndex: number = 0) => {
    cy.waitForRequest('GET', '**/institutionstammdaten/gesuchsperiode/gemeinde/*', () => {
        getBetreuungErstellenButton(kindIndex).click();
    });
};

const createNewTagesschulAnmeldung = () => {
    cy.getByData('container.create-tagesschule', 'navigation-button').click();
};

const fillKitaBetreuungsForm = (dataset: keyof typeof FixtureBetreuung, gemeinde: GemeindeTestFall) => {
    FixtureBetreuung[dataset]((data) => {
        const kita = data[gemeinde].kita;
        getBetreuungsangebot().select(kita.betreuungsangebot);
        getInstitution().find('input').type(kita.institution, { delay: 30 });
        getInstitutionSuchtext().click();
        getInstitution().find('input').should('have.value', kita.institution);
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
        getBetreuungsangebot().select(kita.betreuungsangebot);
        getHasVertrag('ja').click();
        if (opts?.mobile) {
            getInstitutionMobile().select(kita.institution);
        } else {
            getInstitution().find('input').type(kita.institution);
            getInstitutionSuchtext().click();
            getInstitution().find('input').should('have.value', kita.institution);
        }
    });
};

const fillOnlineTfoBetreuungsForm = (dataset: keyof typeof FixtureBetreuung, gemeinde: GemeindeTestFall, opts?: { mobile: boolean }) => {
    FixtureBetreuung[dataset]((data) => {
        const tfo = data[gemeinde].tfo;
        getBetreuungsangebot().select(tfo.betreuungsangebot);
        getHasVertrag('ja').click();
        if (opts?.mobile) {
            getInstitutionMobile().select(tfo.institution);
        } else {
            getInstitution().find('input').type(tfo.institution);
            getInstitutionSuchtext().click();
            getInstitution().find('input').should('have.value', tfo.institution);
        }
    });
};

const selectTagesschulBetreuung = () => {
    getBetreuungsangebot().select('Tagesschule');
};

const fillTagesschulBetreuungsForm = (dataset: keyof typeof FixtureBetreuung, gemeinde: GemeindeTestFall) => {
    FixtureBetreuung[dataset]((data) => {
        const tagesschule = data[gemeinde].tagesschule.institution;
        getHasVertrag('nein').should('not.exist');
        cy.wait(1000);
        getInstitution().find('input').focus().type(tagesschule, { force: true, delay: 30 });
        getInstitutionSuchtext().first().click();
        getInstitution().find('input').should('have.value', tagesschule);
        getKesbPlatzierung('nein').click();
        getXthTagesschulModulOfDay(0, TSDayOfWeek.MONDAY).click();
        getXthTagesschulModulOfDay(0, TSDayOfWeek.THURSDAY).click();
        getAGBTSAkzeptiert().click();
    });
};

const fillKeinePlatzierung = () => {
    getKesbPlatzierung('nein').click();
};

const fillErweiterteBeduerfnisse = () => {
    getHasErweiterteBeduerfnisse('ja').click();
    getFachstelle().select(1);
};

const fillEingewoehnung = () => {
    getEingewoehnung().click();
};

const platzBestaetigungAnfordern = () => {
    cy.waitForRequest('PUT', '**/betreuungen/betreuung/false', () => {
        getPlatzbestaetigungAnfordernButton().click();

    })
};

const saveBetreuung = () => {
    cy.waitForRequest('PUT', '**/betreuungen/betreuung/*', () => {
        cy.getByData('container.save','navigation-button').click();
    });
};

const saveAndConfirmBetreuung = () => {
    cy.waitForRequest('PUT', '**/betreuungen/betreuung/*', () => {
        getSaveButton().click();
        ConfirmDialogPO.getDvLoadingConfirmButton().click();
    });
};

const platzBestaetigen = () => {
    getKorrekteKostenBestaetigung().click();
    getPlatzBestaetigenButton().click();
    cy.waitForRequest('GET', '**/search/pendenzenBetreuungen', () => {
        ConfirmDialogPO.getDvLoadingConfirmButton().click();
    });
};

const platzAbweisen = (grundAbweisung: string) => {
    getGrundAblehnung().click().type(grundAbweisung);
    getGrundAblehnung().should('have.value', grundAbweisung);
    cy.waitForRequest('PUT', '**/betreuungen/abweisen', () => {
        getPlatzAbweisenButton().click();
    });
};

const platzAkzeptieren = () => {
    cy.waitForRequest('GET', '**/gesuchBetreuungenStatus/*', () => {
        cy.waitForRequest('PUT', '**/schulamt/akzeptieren', () => {
            getPlatzAkzeptierenButton().click();
            ConfirmDialogPO.getDvLoadingConfirmButton().click();
        });
    });
    getPageTitle().should('contain.text', 'Betreuung');
};


export const AntragBetreuungPO = {
    // page objects
    getPageTitle,
    getBetreuung,
    getBetreuungsstatus,
    getBetreuungspensum,
    getMonatlicheBetreuungskosten,
    getBetreuungspensumAb,
    getBetreuungspensumBis,
    getKorrekteKostenBestaetigung,
    getPlatzBestaetigenButton,
    getPlatzAkzeptierenButton,
    getWeiteresBetreuungspensumErfassenButton,
    getMutationsmeldungErstellenButton,
    getBetreuungLoeschenButton,
    getMutationsmeldungSendenButton,
    getPlatzbestaetigungAnfordernButton,
    getSaveButton,
    getBetreuungErstellenButton,
    getAnmeldungErstellenButton,
    getBetreuungsangebot,
    getInstitution,
    getInstitutionMobile,
    getInstitutionSuchtext,
    getHasVertrag,
    getKesbPlatzierung,
    getXthTagesschulModulOfDay,
    getAGBTSAkzeptiert,
    getHasErweiterteBeduerfnisse,
    getFachstelle,
    getEingewoehnung,
    getGrundAblehnung,
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
    platzAbweisen,
    platzAkzeptieren,
};
