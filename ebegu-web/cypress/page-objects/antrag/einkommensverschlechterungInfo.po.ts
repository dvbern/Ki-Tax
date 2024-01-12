/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

import {FixtureEinkommensverschlechterungInfo} from '@dv-e2e/fixtures';

// !! -- PAGE OBJECTS -- !!
const getHasEinkommensverschlechterung = (answer: string) => {
	return cy.getByData('einkommensverschlechterung.radio-value.' + answer);
};

// !! -- PAGE ACTIONS -- !!
const getHasEKVFuerBasisJahrPlus = (plus: number) => {
	return cy.getByData('ekv-fuer-basis-jahr-plus#' + plus);
};

const fillEinkommensverschlechterungInfoForm = (dataset: keyof typeof FixtureEinkommensverschlechterungInfo) => {
    FixtureEinkommensverschlechterungInfo[dataset](ekvInfo => {
        getHasEinkommensverschlechterung(ekvInfo.ekv).click();
        if(ekvInfo.basisjahrPlus1) {
            getHasEKVFuerBasisJahrPlus(1).click();
        }
        if (ekvInfo.basisjahrPlus2) {
            getHasEKVFuerBasisJahrPlus(2).click();
        }
    });
};

export const EinkommensverschlechterungInfoPO = {
    // page objects
    getHasEinkommensverschlechterung,
    getHasEKVFuerBasisJahrPlus,
    // page actions
    fillEinkommensverschlechterungInfoForm,
};
