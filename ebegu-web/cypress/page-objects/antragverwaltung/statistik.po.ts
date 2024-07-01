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

const getGesuchstellendeKinderBetreuungTab = () => {
    return cy.getByData('statistik-gesuchsteller-kinder-betreuung');
};

const getVon = () => {
    return cy.getByData('statistik-von');
};

const getBis = () => {
    return cy.getByData('statistik-bis');
};

const getGesuchsperiode = () => {
    return cy.getByData('gesuchsperiode');
};

const getGenerierenButton = () => {
    return cy.getByData('container.generieren', 'navigation-button');
};

const getStatistikJob = (listIndex: number) => {
    return cy.getByData('statistik#' + listIndex);
};
const getStatistikJobStatus = (listIndex: number, timeout = 20000) => {
    return getStatistikJob(listIndex).find('[data-test="job-status"]', {
        timeout: 1500
    });
};

export const StatistikPO = {
    getGesuchstellendeKinderBetreuungTab,
    getVon,
    getBis,
    getGesuchsperiode,
    getGenerierenButton,
    getStatistikJob,
    getStatistikJobStatus
};
