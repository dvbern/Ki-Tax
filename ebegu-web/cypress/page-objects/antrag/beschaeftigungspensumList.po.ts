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

const getGS1 = () => {
    return cy.getByData('erwerbspensen-gs1');
};

const getGS2 = () => {
    return cy.getByData('erwerbspensen-gs2');
};

const getWarnungGS2Ausfuellen = () => {
    return cy.getByData('warnung-gs2-ausfuellen');
};

export const BeschaeftigungspensumListPO = {
    getGS1,
    getGS2,
    getWarnungGS2Ausfuellen
};
