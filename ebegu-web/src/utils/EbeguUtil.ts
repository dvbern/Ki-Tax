/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import {CONSTANTS} from '../app/core/constants/CONSTANTS';
import TSDossier from '../models/TSDossier';
import TSGemeinde from '../models/TSGemeinde';
import TSGesuchsperiode from '../models/TSGesuchsperiode';
import {IFilterService, ILogService} from 'angular';
import TSAbstractEntity from '../models/TSAbstractEntity';
import TSFall from '../models/TSFall';
import DateUtil from './DateUtil';
import {TSAntragTyp} from '../models/enums/TSAntragTyp';
import TSBetreuungsnummerParts from '../models/dto/TSBetreuungsnummerParts';
import TSGesuch from '../models/TSGesuch';
import TSBetreuung from '../models/TSBetreuung';
import ITranslateService = angular.translate.ITranslateService;
import * as moment from 'moment';
import Moment = moment.Moment;

/**
 * Klasse die allgemeine utils Methoden implementiert
 */
export default class EbeguUtil {

    static $inject = ['$filter', '$translate', '$log'];

    constructor(private readonly $filter: IFilterService,
                private readonly $translate: ITranslateService,
                private readonly $log: ILogService) {
    }

    public static isTagesschulangebotEnabled(): boolean {
        return false;
    }

    public static getTitleVerantwortlicher(isSchulamt: boolean): string {
        if (!EbeguUtil.isTagesschulangebotEnabled()) {
            return 'VERANTWORTLICHER_OHNE_SCHULAMT';
        }

        return isSchulamt ? 'VERANTWORTLICHER_SCHULAMT' : 'VERANTWORTLICHER_JUGENDAMT';
    }

    /**
     * Die Methode fuegt 0s (links) hinzu bis die gegebene Nummer, die gegebene Laenge hat und dann gibt die nummer als string zurueck
     * @param number
     * @param length
     * @returns {any}
     */
    public static addZerosToNumber(number: number, length: number): string {
        if (number != null) {
            let fallnummerString = '' + number;
            while (fallnummerString.length < length) {
                fallnummerString = '0' + fallnummerString;
            }
            return fallnummerString;
        }
        return undefined;
    }

    public static addZerosToFallNummer(fallNummer: number): string {
        return EbeguUtil.addZerosToNumber(fallNummer, CONSTANTS.FALLNUMMER_LENGTH);
    }

    public static addZerosToGemeindeNummer(gemeindeNummer: number): string {
        return EbeguUtil.addZerosToNumber(gemeindeNummer, CONSTANTS.GEMEINDENUMMER_LENGTH);
    }

    public static getIndexOfElementwithID(entityToSearch: TSAbstractEntity, listToSearchIn: Array<any>): number {
        const idToSearch = entityToSearch.id;
        for (let i = 0; i < listToSearchIn.length; i++) {
            if (listToSearchIn[i].id === idToSearch) {
                return i;
            }
        }
        return -1;
    }

    public static handleSmarttablesUpdateBug(aList: any[]) {
        // Ugly Fix:
        // Because of a bug in smarttables, the table will only be refreshed if the reverence or the first element
        // changes in table. To resolve this bug, we overwrite the first element by a copy of itself.
        aList[0] = angular.copy(aList[0]);
    }

    /**
     * Erzeugt einen random String mit einer Laenge von numberOfCharacters
     * @param numberOfCharacters
     * @returns {string}
     */
    public static generateRandomName(numberOfCharacters: number): string {
        let text = '';
        const possible = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';

        for (let i = 0; i < numberOfCharacters; i++) {
            text += possible.charAt(Math.floor(Math.random() * possible.length));
        }
        return text;
    }

    public static selectFirst(): void {
        let tmp = angular.element('md-radio-button:not([disabled="disabled"]),'
            + 'fieldset:not([disabled="disabled"],.dv-adresse__fieldset) input:not([disabled="disabled"]),'
            + 'fieldset:not([disabled="disabled"],.dv-adresse__fieldset) textarea:not([disabled="disabled"]),'
            + 'fieldset:not([disabled="disabled"],.dv-adresse__fieldset) select:not([disabled="disabled"]),'
            + 'fieldset:not([disabled="disabled"],.dv-adresse__fieldset) md-checkbox:not([disabled="disabled"]),'
            + '#gesuchContainer button:not([disabled="disabled"]),'
            + '#gesuchContainer .dvb-loading-button button:not([disabled="disabled"]),'
            + '.dv-btn-row,'
            + '#gesuchContainer button.link-underline:not([disabled="disabled"]),'
            + '.dv-dokumente-list a:not([disabled="disabled"])').first();
        if (tmp) {
            let tmpAria = tmp.attr('aria-describedby') === undefined ? '' : tmp.attr('aria-describedby') + ' ';
            const h2 = angular.element('h2:not(.access-for-all-title)').first();
            const h2Id = h2.attr('id') === undefined ? 'aria-describe-form-h2' : h2.attr('id');
            h2.attr('id', h2Id);
            tmpAria += h2Id;
            const h3 = angular.element('h3:not(.access-for-all-title)').first();
            const h3Id = h3.attr('id') === undefined ? 'aria-describe-form-h3' : h3.attr('id');
            h3.attr('id', h3Id);
            tmpAria += ' ' + h3Id;
            tmp.attr('aria-describedby', tmpAria);
            if (tmp.prop('tagName') === 'MD-RADIO-BUTTON') {
                tmp = tmp.parent().first();
            }
            tmp.focus();
        }
    }

    public static selectFirstInvalid(): void {
        const tmp: any = angular.element('md-radio-group.ng-invalid,'
            + ' .ng-invalid>input,'
            + 'input.ng-invalid,'
            + 'textarea.ng-invalid,'
            + 'select.ng-invalid,'
            + 'md-checkbox.ng-invalid').first();
        if (tmp) {
            tmp.focus();
        }
    }

    public static isNullOrUndefined(data: any): boolean {
        return data === null || data === undefined;
    }

    public static isNotNullOrUndefined(data: any): boolean {
        return !EbeguUtil.isNullOrUndefined(data);
    }

    public static isEmptyStringNullOrUndefined(data: string): boolean {
        return !data;
    }

    public static isTagesschulangebotEnabled(): boolean {
        return false;
    }

    public static getTitleVerantwortlicher(isSchulamt: boolean): string {
        if (!EbeguUtil.isTagesschulangebotEnabled()) {
            return 'VERANTWORTLICHER_OHNE_SCHULAMT';
        }
        if (isSchulamt) {
            return 'VERANTWORTLICHER_SCHULAMT';
        } else {
            return 'VERANTWORTLICHER_JUGENDAMT';
        }
    }

    /**
     * Returns the first day of the given Period in the format DD.MM.YYYY
     * @param gesuchsperiode
     * @returns {string}
     */
    public getFirstDayGesuchsperiodeAsString(gesuchsperiode: TSGesuchsperiode): string {
        if (gesuchsperiode && gesuchsperiode.gueltigkeit && gesuchsperiode.gueltigkeit.gueltigAb) {
            return DateUtil.momentToLocalDateFormat(gesuchsperiode.gueltigkeit.gueltigAb, 'DD.MM.YYYY');
        }
        return '';
    }

    public getAntragTextDateAsString(tsAntragTyp: TSAntragTyp, eingangsdatum: Moment, laufnummer: number): string {
        if (tsAntragTyp) {
            if (tsAntragTyp === TSAntragTyp.MUTATION && eingangsdatum) {
                return this.$translate.instant('TOOLBAR_' + TSAntragTyp[tsAntragTyp], {
                    nummer: laufnummer,
                    date: eingangsdatum.format('DD.MM.YYYY')
                });
            }
            return this.$translate.instant('TOOLBAR_' + TSAntragTyp[tsAntragTyp] + '_NO_DATE');
        }
        return '';
    }

    /**
     * Takes the given Gesuchsperiode and returns a string with the format "gueltigAb.year/gueltigBis.year"
     */
    public getBasisJahrPlusAsString(gesuchsperiode: TSGesuchsperiode, plusJahr: number): string {
        if (gesuchsperiode && gesuchsperiode.gueltigkeit) {
            return String(gesuchsperiode.gueltigkeit.gueltigAb.year() + plusJahr);
        }
        return undefined;
    }

    /**
     * Translates the given string using the angular-translate filter
     */
    public translateString(toTranslate: string): string {
        return this.$filter('translate')(toTranslate).toString();
    }

    /**
     * Translates the given list using the angular translate filter
     * @param translationList list of words that will be translated
     * @returns {any} A List of Objects with key and value, where value is the translated word.
     */
    public translateStringList(translationList: Array<any>): Array<any> {
        const listResult: Array<any> = [];
        translationList.forEach((item) => {
            listResult.push({key: item, value: this.translateString(item)});
        });
        return listResult;
    }

    public addZerosToNumber(number: number, length: number): string {
        return EbeguUtil.addZerosToNumber(number, length);
    }

    /* bgNummer is also stored on betreuung when Betreuung is loaded from server! (Don't use this function if you load betreuung from server) */
    public calculateBetreuungsId(gesuchsperiode: TSGesuchsperiode, fall: TSFall, gemeinde: TSGemeinde, kindContainerNumber: number, betreuungNumber: number): string {
        let betreuungsId: string = '';
        if (gesuchsperiode && fall) {
            betreuungsId =
                gesuchsperiode.gueltigkeit.gueltigAb.year().toString().substring(2)
                + '.' + EbeguUtil.addZerosToFallNummer(fall.fallNummer)
                + '.' + EbeguUtil.addZerosToGemeindeNummer(gemeinde.gemeindeNummer)
                + '.' + kindContainerNumber
                + '.' + betreuungNumber;
        }
        return betreuungsId;
    }

    /* bgNummer is also stored on betreuung when Betreuung is loaded from server! (Don't use this function if you load betreuung from server) */
    public calculateBetreuungsIdFromBetreuung(fall: TSFall, gemeinde: TSGemeinde, betreuung: TSBetreuung): string {
        let betreuungsId: string = '';
        if (betreuung && fall) {
            betreuungsId =
                betreuung.gesuchsperiode.gueltigkeit.gueltigAb.year().toString().substring(2)
                + '.' + EbeguUtil.addZerosToFallNummer(fall.fallNummer)
                + '.' + EbeguUtil.addZerosToGemeindeNummer(gemeinde.gemeindeNummer)
                + '.' + betreuung.kindNummer
                + '.' + betreuung.betreuungNummer;
        }
        return betreuungsId;
    }

    /**
     * hilfsmethode um die betreuungsnummer in ihre einzelteile zu zerlegen. gibt ein objekt zurueck welches die werte einzeln enthaelt
     * @param betreuungsnummer im format JJ.Fallnr.GemeindeNr.kindnr.betrnr
     */
    public splitBetreuungsnummer(betreuungsnummer: string): TSBetreuungsnummerParts {
        const parts: Array<string> = betreuungsnummer.split('.');
        if (!parts || parts.length !== CONSTANTS.PARTS_OF_BETREUUNGSNUMMER) {
            this.$log.error('A Betreuungsnummer must always have ' + CONSTANTS.PARTS_OF_BETREUUNGSNUMMER + ' parts. The given one had ' + parts.length);
            return undefined;
        }
        return new TSBetreuungsnummerParts(parts[0], parts[1], parts[2], parts[3], parts[4]);
    }

    /**
     * Returns a string like "fallID GesuchstellerName". The name of the GS comes from the name of the
     * first Gesuchsteller of the given Gesuch. This method should be used if possible instead of getGesuchNameFromFall
     * because the name of the Gesuchsteller1 is suppoused to be more actual than the name of the owner.
     */
    public getGesuchNameFromGesuch(gesuch: TSGesuch): string {
        let text = '';
        if (gesuch && gesuch.dossier) {
            if (gesuch.dossier.fall) {
                text = EbeguUtil.addZerosToFallNummer(gesuch.dossier.fall.fallNummer);
            }
            if (gesuch.gesuchsteller1 && gesuch.gesuchsteller1.extractNachname()) {
                text = text + ' ' + gesuch.gesuchsteller1.extractNachname();
            }
        }
        return text;
    }

    /**
     * Returns a string like "fallID GesuchstellerName". The name of the GS comes from the name of the
     * owner of the given fall. Use this method instead of getGesuchNameFromGesuch only when there is no Gesuch but a fall
     */
    public getGesuchNameFromDossier(dossier: TSDossier): string {
        let text = '';
        if (dossier && dossier.fall) {
            text = EbeguUtil.addZerosToFallNummer(dossier.fall.fallNummer);
            if (dossier.fall.besitzer && dossier.fall.besitzer.getFullName()) {
                text = text + ' ' + dossier.fall.besitzer.getFullName();
            }
        }
        return text;
    }

    public replaceElementInList(element: TSAbstractEntity, list: TSAbstractEntity[], wasNew: boolean) {
        if (wasNew) {
            list.push(element);
        } else {
            const index = EbeguUtil.getIndexOfElementwithID(element, list);
            if (index > -1) {
                list[index] = element;
                EbeguUtil.handleSmarttablesUpdateBug(list);
            }
        }
    }

    public getKontaktJugendamt(): string {
        return '<span>Jugendamt</span><br>'
            + '<span>Effingerstrasse 21</span><br>'
            + '<span>3008 Bern</span><br>'
            + '<a href="tel:0313215115"><span>031 321 51 15</span></a><br>'
            + '<a href="mailto:kinderbetreuung@bern.ch"><span>kinderbetreuung@bern.ch</span></a>';
    }

    public getKontaktSchulamt(): string {
        return '<span>Schulamt</span><br>'
            + '<span>Effingerstrasse 21</span><br>'
            + '<span>3008 Bern</span><br>'
            + '<a href="tel:0313216469"><span>031 321 64 69</span></a><br>'
            + '<a href="mailto:tagesschulen@bern.ch"><span>tagesschulen@bern.ch</span></a>';
    }
}
