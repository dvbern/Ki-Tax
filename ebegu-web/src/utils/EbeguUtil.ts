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

import {IFilterService, ILogService} from 'angular';
import * as moment from 'moment';
import {CONSTANTS} from '../app/core/constants/CONSTANTS';
import {LogFactory} from '../app/core/logging/LogFactory';
import {Displayable} from '../app/shared/interfaces/displayable';
import {TSBetreuungsnummerParts} from '../models/dto/TSBetreuungsnummerParts';
import {TSAntragTyp} from '../models/enums/TSAntragTyp';
import {TSAbstractEntity} from '../models/TSAbstractEntity';
import {TSAdresse} from '../models/TSAdresse';
import {TSBetreuung} from '../models/TSBetreuung';
import {TSDossier} from '../models/TSDossier';
import {TSFall} from '../models/TSFall';
import {TSGemeinde} from '../models/TSGemeinde';
import {TSGemeindeStammdaten} from '../models/TSGemeindeStammdaten';
import {TSGesuch} from '../models/TSGesuch';
import {TSGesuchsperiode} from '../models/TSGesuchsperiode';
import {TSDateRange} from '../models/types/TSDateRange';
import {DateUtil} from './DateUtil';
import ITranslateService = angular.translate.ITranslateService;

const LOG = LogFactory.createLog('EbeguUtil');

const defaultDateFormat = 'DD.MM.YYYY';

/**
 * Klasse die allgemeine utils Methoden implementiert
 */
export class EbeguUtil {

    public static $inject = ['$filter', '$translate', '$log'];

    public constructor(
        private readonly $filter: IFilterService,
        private readonly $translate: ITranslateService,
        private readonly $log: ILogService,
    ) {
    }

    public static hasTextCaseInsensitive(obj: any, text: any): boolean {
        const result = String(text).toLowerCase();

        return String(obj).toLowerCase().indexOf(result) > -1;
    }

    public static compareDates(actual: any, expected: any): boolean {
        return moment(actual).format(defaultDateFormat) === expected;
    }

    public static handleDownloadError(win: Window, error: any): void {
        win.close();
        LOG.error('An error occurred downloading the document, closing download window.', error);
    }

    public static compareByName<T extends Displayable>(a: T, b: T): number {
        return a.name.localeCompare(b.name);
    }

    public static compareById<T extends TSAbstractEntity>(a: T, b: T): boolean {
        return a && b ? a.id === b.id : a === b;
    }

    /**
     * Compares two array and returns TRUE when both arrays contain objects with the same IDs (but not necessarily the
     * same references)
     */
    public static isSameById<T extends TSAbstractEntity>(a: T[], b: T[]): boolean {
        if (a.length !== b.length) {
            return false;
        }

        const compareId = (value1: T, value2: T) => value1.id.localeCompare(value2.id);

        const aSorted = a.concat().sort(compareId);
        const bSorted = b.concat().sort(compareId);

        return aSorted.every((value, index) => bSorted[index].id === value.id);
    }

    /**
     * Compares two array and returns TRUE when both arrays contain the same objects
     * but not necessary the same values!
     */
    public static isSame<T>(a: T[], b: T[]): boolean {
        if (a.length !== b.length) {
            return false;
        }

        const aSorted = a.concat().sort();
        const bSorted = b.concat().sort();

        return aSorted.every((value, index) => bSorted[index] === value);
    }

    public static copyArrayWithoutReference<T>(a: T[]): T[] {
        const newArray = [];
        for (let i = 0; i < a.length; i++) {
            newArray[i] = angular.copy(a[i]);
        }
        return newArray;
    }

    /**
     * Die Methode fuegt 0s (links) hinzu bis die gegebene Nummer, die gegebene Laenge hat und dann gibt die nummer als
     * string zurueck
     */
    public static addZerosToNumber(num: number, length: number): string {
        if (EbeguUtil.isNotNullOrUndefined(num)) {
            let fallnummerString = `${num}`;
            while (fallnummerString.length < length) {
                fallnummerString = `0${fallnummerString}`;
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

    public static handleSmarttablesUpdateBug(aList: any[]): void {
        // Ugly Fix:
        // Because of a bug in smarttables, the table will only be refreshed if the reverence or the first element
        // changes in table. To resolve this bug, we overwrite the first element by a copy of itself.
        aList[0] = angular.copy(aList[0]);
    }

    /**
     * Erzeugt einen random String mit einer Laenge von numberOfCharacters
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
        if (!tmp) {
            return;
        }

        const ariaDescribedby = 'aria-describedby';
        let tmpAria = tmp.attr(ariaDescribedby) === undefined ? '' : `${tmp.attr(ariaDescribedby)} `;
        const h2 = angular.element('h2:not(.access-for-all-title)').first();
        const h2Id = h2.attr('id') === undefined ? 'aria-describe-form-h2' : h2.attr('id');
        h2.attr('id', h2Id);
        tmpAria += h2Id;
        const h3 = angular.element('h3:not(.access-for-all-title)').first();
        const h3Id = h3.attr('id') === undefined ? 'aria-describe-form-h3' : h3.attr('id');
        h3.attr('id', h3Id);
        tmpAria += ` ${h3Id}`;
        tmp.attr(ariaDescribedby, tmpAria);
        if (tmp.prop('tagName') === 'MD-RADIO-BUTTON') {
            tmp = tmp.parent().first();
        }
        tmp.focus();
    }

    public static selectFirstInvalid(): void {
        const tmp: any = angular.element('md-radio-group.ng-invalid,'
            + ' .ng-invalid>input,'
            + 'input.ng-invalid,'
            + 'textarea.ng-invalid,'
            + 'select.ng-invalid,'
            + 'mat-select.ng-invalid,'
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

    public static isNotNullAndTrue(data: boolean): boolean {
        return this.isNotNullOrUndefined(data) && data;
    }

    public static isNotNullAndFalse(data: boolean): boolean {
        return this.isNotNullOrUndefined(data) && !data;
    }

    public static isNullOrFalse(data: boolean): boolean {
        return this.isNullOrUndefined(data) || !data;
    }

    public static isNotNullAndPositive(data: number): boolean {
        return this.isNotNullOrUndefined(data) && data >= 0;
    }

    public static isEmptyStringNullOrUndefined(data: string): boolean {
        return !data;
    }

    public static roundToFiveRappen(betrag: number): number {
        return Number((Math.ceil(betrag * 20 - 0.5) / 20).toFixed(2));
    }

    private static getYear(gueltigkeit: TSDateRange): string {
        return gueltigkeit.gueltigAb.year().toString().substring(2);
    }

    private static toBetreuungsId(
        gueltigkeit: TSDateRange,
        fall: TSFall,
        gemeinde: TSGemeinde,
        kindNr: number,
        betreuungNumber: number,
    ): string {
        const year = EbeguUtil.getYear(gueltigkeit);
        const fallNr = EbeguUtil.addZerosToFallNummer(fall.fallNummer);
        const gemeindeNr = EbeguUtil.addZerosToGemeindeNummer(gemeinde.gemeindeNummer);

        return `${year}.${fallNr}.${gemeindeNr}.${kindNr}.${betreuungNumber}`;
    }

    /**
     * Achtung: Diese Logik befindet sich ebenfalls serverseitig hier:
     * EbeguUtil.java#isFinanzielleSituationRequired
     */
    public static isFinanzielleSituationRequiredForGesuch(gesuch: TSGesuch): boolean {
        if (!gesuch) {
            return false;
        }
        return EbeguUtil.isNotNullOrUndefined(gesuch)
            && EbeguUtil.isNotNullOrUndefined(gesuch.familiensituationContainer)
            && EbeguUtil.isNotNullOrUndefined(gesuch.familiensituationContainer.familiensituationJA)
            && EbeguUtil.isFinanzielleSituationRequired(
                gesuch.familiensituationContainer.familiensituationJA.sozialhilfeBezueger,
                gesuch.familiensituationContainer.familiensituationJA.verguenstigungGewuenscht);
    }

    /**
     * Both parameters must always be set, thuogh they are nullable in the Familiensituation because they are not set
     * while creating the object but later while filling out the finanzielle situation.
     *
     * For the finanzielle situation to be required:
     * sozialhilfeBezueger=false and verguenstigungGewuenscht=true
     */
    public static isFinanzielleSituationRequired(
        sozialhilfeBezueger: boolean,
        verguenstigungGewuenscht: boolean,
    ): boolean {
        return sozialhilfeBezueger === false && verguenstigungGewuenscht === true; // tslint:disable-line:no-boolean-literal-compare
    }

    public static getAmtsspracheAsString(
        gemeindeStammdaten: TSGemeindeStammdaten,
        translate: ITranslateService,
    ): string {

        if (!gemeindeStammdaten || !translate) {
            return '';
        }
        if (gemeindeStammdaten.korrespondenzspracheDe && gemeindeStammdaten.korrespondenzspracheFr) {
            return translate.instant('DEUTSCH_ODER_FRANZOESISCH');
        }
        if (gemeindeStammdaten.korrespondenzspracheFr) {
            return translate.instant('FRANZOESISCH');
        }
        return translate.instant('DEUTSCH');
    }

    public static replaceElementInList(element: TSAbstractEntity, list: TSAbstractEntity[]): void {
        const index = EbeguUtil.getIndexOfElementwithID(element, list);
        if (index > -1) {
            list[index] = element;
            EbeguUtil.handleSmarttablesUpdateBug(list);
        } else {
            list.push(element);
        }
    }

    public static removeElementFromList(element: TSAbstractEntity, list: TSAbstractEntity[]): void {
        const index = EbeguUtil.getIndexOfElementwithID(element, list);
        if (index > -1) {
            list.splice(index, 1);
            EbeguUtil.handleSmarttablesUpdateBug(list);
        }
    }

    public static formatHrefUrl(url: string): string {
        if (EbeguUtil.isNotNullOrUndefined(url) && url.startsWith('www.')) {
            return 'http://' + url;
        }
        return url;
    }

    public static zemisNummerToStandardZemisNummer(zemisNummer: string): string {
        if (!zemisNummer) {
            return zemisNummer;
        }

        const re1 = /^0\d{8}\.\d$/m; // format 012345678.9
        const re2 = /^\d{3}\.\d{3}\.\d{3}[.-]\d$/m; // format 012.345.678.9 | 012.345.678-9
        let standardZemisNummer;
        if (zemisNummer.match(re1)) {
            standardZemisNummer = zemisNummer.slice(1);
        } else if (zemisNummer.match(re2)) {
            const tmp = zemisNummer
                .replace(/[\\.-]/g, '')
                .slice(1);
            standardZemisNummer = `${tmp.slice(0, 8)}.${tmp.slice(-1)}`;
        } else {
            standardZemisNummer = zemisNummer;
        }
        if (!(new RegExp(CONSTANTS.PATTERN_ZEMIS_NUMMER, 'm')).test(zemisNummer)) {
            throw new Error(`Wrong Format for ZEMIS-Nummer ${zemisNummer}`);
        }
        return standardZemisNummer;
    }

    /**
     * Checks if required TS Adresse fields are set
     */
    public static adresseValid(adresse: TSAdresse): boolean {
        return !!adresse.strasse && !!adresse.plz && !!adresse.ort;
    }

    /**
     * Returns the first day of the given Period in the format DD.MM.YYYY
     */
    public getFirstDayGesuchsperiodeAsString(gesuchsperiode: TSGesuchsperiode): string {
        if (gesuchsperiode && gesuchsperiode.gueltigkeit && gesuchsperiode.gueltigkeit.gueltigAb) {
            return DateUtil.momentToLocalDateFormat(gesuchsperiode.gueltigkeit.gueltigAb, defaultDateFormat);
        }
        return '';
    }

    public getAntragTextDateAsString(
        tsAntragTyp: TSAntragTyp,
        eingangsdatum: moment.Moment,
        laufnummer: number,
    ): string {
        if (tsAntragTyp) {
            if (tsAntragTyp === TSAntragTyp.MUTATION && eingangsdatum) {
                return this.$translate.instant(`TOOLBAR_${TSAntragTyp[tsAntragTyp]}`, {
                    nummer: laufnummer,
                    date: eingangsdatum.format(defaultDateFormat),
                });
            }
            return this.$translate.instant(`TOOLBAR_${TSAntragTyp[tsAntragTyp]}_NO_DATE`);
        }
        return '';
    }

    /**
     * Translates the given string using the angular-translate filter
     */
    public translateString(toTranslate: string): string {
        return this.$filter('translate')(toTranslate).toString();
    }

    // bgNummer is also stored on betreuung when Betreuung is loaded from server! (Don't use this function if you load

    /**
     * Translates the given list using the angular translate filter
     * @param translationList list of words that will be translated
     * @returns A List of Objects with key and value, where value is the translated word.
     */
    public translateStringList(translationList: Array<any>): Array<any> {
        const listResult: Array<any> = [];
        translationList.forEach(item => {
            listResult.push({key: item, value: this.translateString(item)});
        });
        return listResult;
    }

    // bgNummer is also stored on betreuung when Betreuung is loaded from server! (Don't use this function if you load

    public addZerosToNumber(num: number, length: number): string {
        return EbeguUtil.addZerosToNumber(num, length);
    }

    // betreuung from server)
    public calculateBetreuungsId(
        gesuchsperiode: TSGesuchsperiode,
        fall: TSFall,
        gemeinde: TSGemeinde,
        kindContainerNumber: number,
        betreuungNumber: number,
    ): string {
        return gesuchsperiode && fall ?
            EbeguUtil.toBetreuungsId(gesuchsperiode.gueltigkeit, fall, gemeinde, kindContainerNumber, betreuungNumber) :
            '';
    }

    // betreuung from server)
    public calculateBetreuungsIdFromBetreuung(fall: TSFall, gemeinde: TSGemeinde, betreuung: TSBetreuung): string {
        return betreuung && fall ?
            EbeguUtil.toBetreuungsId(betreuung.gesuchsperiode.gueltigkeit,
                fall,
                gemeinde,
                betreuung.kindNummer,
                betreuung.betreuungNummer) :
            '';
    }

    /**
     * hilfsmethode um die betreuungsnummer in ihre einzelteile zu zerlegen. gibt ein objekt zurueck welches die werte
     * einzeln enthaelt
     * @param betreuungsnummer im format JJ.Fallnr.GemeindeNr.kindnr.betrnr
     */
    public splitBetreuungsnummer(betreuungsnummer: string): TSBetreuungsnummerParts {
        const parts = betreuungsnummer.split('.');
        const betrNr = CONSTANTS.PARTS_OF_BETREUUNGSNUMMER;

        if (!parts || parts.length !== betrNr) {
            this.$log.error(`A Betreuungsnummer must always have ${betrNr} parts. The given one had ${parts.length}`);
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
                text = `${text} ${gesuch.gesuchsteller1.extractNachname()}`;
            }
        }
        return text;
    }

    /**
     * Returns a string like "fallID GesuchstellerName". The name of the GS comes from the name of the
     * owner of the given fall. Use this method instead of getGesuchNameFromGesuch only when there is no Gesuch but a
     * fall
     */
    public getGesuchNameFromDossier(dossier: TSDossier): string {
        let text = '';
        if (dossier && dossier.fall) {
            text = EbeguUtil.addZerosToFallNummer(dossier.fall.fallNummer);
            if (dossier.fall.besitzer && dossier.fall.besitzer.getFullName()) {
                text = `${text} ${dossier.fall.besitzer.getFullName()}`;
            }
        }
        return text;
    }
}
