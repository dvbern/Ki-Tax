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

import {FixtureFinSit} from '@dv-e2e/fixtures';
import {NavigationPO} from './navigation.po';

// !! -- PAGE OBJECTS -- !!
const getNettoLohn = () => {
    return cy.getByData('nettolohn');
};

const getFamilienzulagen = () => {
    return cy.getByData('familienzulage');
};

const getErsatzeinkommen = () => {
    return cy.getByData('ersatzeinkommen');
};

const getErhalteneAlimente = () => {
    return cy.getByData('erhaltene-alimente');
};

const getBruttoertraegeVermoegen = () => {
    return cy.getByData('brutto-ertraege-vermoegen');
};

const getNettoertraegeErbengemeinschaft = () => {
    return cy.getByData('nettoertraege_erbengemeinschaften');
};

const getEinkommenInVereinfachtemVerfahrenNein = () => {
    return cy.getByData(
        'einkommenInVereinfachtemVerfahrenAbgerechnet1.radio-value.nein'
    );
};

const getGeleisteteAlimente = () => {
    return cy.getByData('geleistete-alimente');
};

const getAbzugSchuldzinsen = () => {
    return cy.getByData('abzug-schuldzinsen');
};

const getGewinnungskosten = () => {
    return cy.getByData('gewinnungskosten');
};

const getSteuerdatenzugriff = (answer: string) => {
    return cy.getByData('steuerdatenzugriff.radio-value.' + answer);
};

const getAutomatischePruefung = (answer: string) => {
    return cy.getByData('automatischePruefung.radio-value.' + answer);
};

const getShowSelbstaendig = () => {
    return cy.getByData('show-selbstaendig');
};

const getGeschaeftsgewinnBasisjahr = () => {
    return cy.getByData('geschaeftsgewinn-basisjahr');
};

const getGeschaeftsgewinnBasisjahrMinus1 = () => {
    return cy.getByData('geschaeftsgewinn-basisjahr-minus-1');
};

const getGeschaeftsgewinnBasisjahrMinus2 = () => {
    return cy.getByData('geschaeftsgewinn-basisjahr-minus-2');
};
const getShowErsatzeinkommenSelbststaendigkeit = () => {
    return cy.getByData('show-ersatzeinkommen-selbststaendigkeit');
};

const getShowErsatzeinkommenSelbststaendigkeitRadioButton = (
    answer: string
) => {
    return cy.getByData(
        'showErsatzeinkommenSelbststaendigkeit.radio-value.' + answer
    );
};

const getErsatzeinkommenSelbststaendigkeitBasisjahr = () => {
    return cy.getByData('ersatzeinkommen-selbststaendigkeit-basisjahr');
};

const getErsatzeinkommenSelbststaendigkeitBasisjahrMinus1 = () => {
    return cy.getByData('ersatzeinkommen-selbststaendigkeit-basisjahr-minus-1');
};

const getErsatzeinkommenSelbststaendigkeitBasisjahrMinus2 = () => {
    return cy.getByData('ersatzeinkommen-selbststaendigkeit-basisjahr-minus-2');
};

const getErsatzeinkommenInvalidErrorMessage = () => {
    return cy.getByData('ersatzeinkommen-invalid-error-message');
};

const getAllErsatzeinkommenSelbststaendigkeitZeroErrorMessage = () => {
    return cy.getByData(
        'all-ersatzeinkommen-selbststaendigkeit-zero-error-message'
    );
};

// !! -- PAGE ACTIONS -- !!
const fillFinanzielleSituationForm = (
    dataset: keyof typeof FixtureFinSit,
    gs: 'GS1' | 'GS2'
) => {
    FixtureFinSit[dataset](allData => {
        cy.url().should('include', 'finanzielleSituation');
        cy.wait(2000);
        FinanzielleSituationPO.getNettoLohn()
            .find('input')
            .type(allData[gs].nettolohn);
        FinanzielleSituationPO.getFamilienzulagen()
            .find('input')
            .type(allData[gs].familienzulage);
        FinanzielleSituationPO.getErsatzeinkommen()
            .find('input')
            .type(allData[gs].ersatzeinkommen);
        FinanzielleSituationPO.getErhalteneAlimente()
            .find('input')
            .type(allData[gs].erhalteneAlimente);
        FinanzielleSituationPO.getBruttoertraegeVermoegen()
            .find('input')
            .type(allData[gs].bruttoErtraegeVermoegen);
        FinanzielleSituationPO.getNettoertraegeErbengemeinschaft()
            .find('input')
            .type(allData[gs].nettoertraegeErbengemeinschaften);
        FinanzielleSituationPO.getEinkommenInVereinfachtemVerfahrenNein().click();
        FinanzielleSituationPO.getGeleisteteAlimente()
            .find('input')
            .type(allData[gs].geleisteteAlimente);
        FinanzielleSituationPO.getAbzugSchuldzinsen()
            .find('input')
            .type(allData[gs].abzugSchuldzinsen);
        FinanzielleSituationPO.getGewinnungskosten()
            .find('input')
            .type(allData[gs].gewinnungskosten);
    });
};

const saveFormAndGoNext = () => {
    cy.waitForRequest('POST', '**/finanzielleSituation/calculateTemp', () => {
        NavigationPO.saveAndGoNext();
    });
};

export const FinanzielleSituationPO = {
    // page objects
    getNettoLohn,
    getFamilienzulagen,
    getErsatzeinkommen,
    getErhalteneAlimente,
    getBruttoertraegeVermoegen,
    getNettoertraegeErbengemeinschaft,
    getEinkommenInVereinfachtemVerfahrenNein,
    getGeleisteteAlimente,
    getAbzugSchuldzinsen,
    getGewinnungskosten,
    getSteuerdatenzugriff,
    getAutomatischePruefung,
    getShowSelbstaendig,
    getGeschaeftsgewinnBasisjahr,
    getGeschaeftsgewinnBasisjahrMinus1,
    getGeschaeftsgewinnBasisjahrMinus2,
    getShowErsatzeinkommenSelbststaendigkeit,
    getShowErsatzeinkommenSelbststaendigkeitRadioButton,
    getErsatzeinkommenSelbststaendigkeitBasisjahr,
    getErsatzeinkommenSelbststaendigkeitBasisjahrMinus1,
    getErsatzeinkommenSelbststaendigkeitBasisjahrMinus2,
    getErsatzeinkommenInvalidErrorMessage,
    getAllErsatzeinkommenSelbststaendigkeitZeroErrorMessage,
    // page actions
    fillFinanzielleSituationForm,
    saveFormAndGoNext
};
