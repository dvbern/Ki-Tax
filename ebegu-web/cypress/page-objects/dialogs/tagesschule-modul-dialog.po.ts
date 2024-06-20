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

const getBezeichnungDe = () => {
    return cy.getByData('institution.tageschule.modul.bezeichnungDe');
};

const getBezeichnungFr = () => {
    return cy.getByData('institution.tageschule.modul.bezeichnungFr');
};

const getZeitVon = () => {
    return cy.getByData('institution.tageschule.modul.zeitVon');
};

const getZeitBis = () => {
    return cy.getByData('institution.tageschule.modul.zeitBis');
};

const getVerpflegungskosten = () => {
    return cy.getByData('institution.tageschule.modul.verpflegungskosten');
};

const getMontag = () => {
    return cy
        .getByData('institution.tageschule.modul.montag')
        .find('.mdc-checkbox');
};

const getDienstag = () => {
    return cy
        .getByData('institution.tageschule.modul.dienstag')
        .find('.mdc-checkbox');
};

const getMittwoch = () => {
    return cy
        .getByData('institution.tageschule.modul.mittwoch')
        .find('.mdc-checkbox');
};

const getDonnerstag = () => {
    return cy
        .getByData('institution.tageschule.modul.donnerstag')
        .find('.mdc-checkbox');
};

const getFreitag = () => {
    return cy
        .getByData('institution.tageschule.modul.freitag')
        .find('.mdc-checkbox');
};

const getOkButton = () => {
    return cy.getByData('institution.tageschule.modul.ok');
};

const getWirdPaedagogischBetreut = () => {
    return cy
        .getByData('institution.tageschule.modul.wirdPaedagogischBetreut')
        .find('.mdc-checkbox');
};

const getIntervall = () => {
    return cy.getByData('institution.tageschule.modul.intervall');
};

const getIntervallOption = (option: string) => {
    return cy.getByData('institution.tageschule.modul.intervall.' + option);
};

export const TagesschuleModulDialogPO = {
    getBezeichnungDe,
    getBezeichnungFr,
    getZeitVon,
    getZeitBis,
    getVerpflegungskosten,
    getMontag,
    getDienstag,
    getMittwoch,
    getDonnerstag,
    getFreitag,
    getWirdPaedagogischBetreut,
    getIntervall,
    getIntervallOption,
    getOkButton
};
