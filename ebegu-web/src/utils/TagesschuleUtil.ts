/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

import {getWeekdaysValues, TSDayOfWeek} from '../models/enums/TSDayOfWeek';
import {TSModulTagesschuleName} from '../models/enums/TSModulTagesschuleName';
import {TSBelegungTagesschuleModul} from '../models/TSBelegungTagesschuleModul';
import {TSBelegungTagesschuleModulGroup} from '../models/TSBelegungTagesschuleModulGroup';
import {TSBetreuung} from '../models/TSBetreuung';
import {TSEinstellungenTagesschule} from '../models/TSEinstellungenTagesschule';
import {TSGesuchsperiode} from '../models/TSGesuchsperiode';
import {TSInstitutionStammdatenSummary} from '../models/TSInstitutionStammdatenSummary';
import {TSModulTagesschule} from '../models/TSModulTagesschule';
import {TSModulTagesschuleGroup} from '../models/TSModulTagesschuleGroup';
import {EbeguUtil} from './EbeguUtil';

export class TagesschuleUtil {

    public static initModuleTagesschule(
        betreuung: TSBetreuung,
        gesuchsPeriode: TSGesuchsperiode,
        verfuegungView: boolean,
    ): TSBelegungTagesschuleModulGroup[] {
        if (!(betreuung.institutionStammdaten
            && betreuung.institutionStammdaten.institutionStammdatenTagesschule)
        ) {
            return [];
        }
        let moduleAngemeldet: TSBelegungTagesschuleModul[];
        (betreuung.belegungTagesschule?.belegungTagesschuleModule)
            ? moduleAngemeldet = betreuung.belegungTagesschule.belegungTagesschuleModule
            : moduleAngemeldet = [];
        const moduleAngeboten = this.loadAngeboteneModuleForTagesschule(betreuung, gesuchsPeriode);

        return TagesschuleUtil.initModulGroups(moduleAngemeldet, moduleAngeboten, verfuegungView, false);
    }

    public static initModuleTagesschuleAfterInstitutionChange(
        betreuung: TSBetreuung,
        oldInstitutionStammdaten: TSInstitutionStammdatenSummary,
        gesuchsPeriode: TSGesuchsperiode,
        verfuegungView: boolean,
    ): TSBelegungTagesschuleModulGroup[] {
        if (!(betreuung.institutionStammdaten
            && betreuung.institutionStammdaten.institutionStammdatenTagesschule)
        ) {
            return [];
        }
        const moduleAngemeldet = betreuung.belegungTagesschule.belegungTagesschuleModule;
        // hier wir setzen die ModuleGroupName um wieder finden zu koennen welche mussen auf der neue Institution
        // pre-selektiert werden
        this.setModuleGroupNameAndBezeichnungForAngemeldeteModule(moduleAngemeldet, oldInstitutionStammdaten, gesuchsPeriode);
        const moduleAngeboten = this.loadAngeboteneModuleForTagesschule(betreuung, gesuchsPeriode);

        return TagesschuleUtil.initModulGroups(moduleAngemeldet, moduleAngeboten, verfuegungView, true);
    }

    private static initModulGroups(
        moduleAngemeldet: TSBelegungTagesschuleModul[],
        moduleAngeboten: TSModulTagesschuleGroup[],
        verfuegungView: boolean,
        copyFromOtherInstitution: boolean,
    ): TSBelegungTagesschuleModulGroup[] {
        const modulGroups: TSBelegungTagesschuleModulGroup[] = [];
        const moduleAngebotenSorted = this.sortModulTagesschuleGroups(moduleAngeboten);
        for (const groupTagesschule of moduleAngebotenSorted) {
            TagesschuleUtil.initializeGroup(groupTagesschule);
            const moduleOfGroup = groupTagesschule.getModuleOrdered();
            const group = new TSBelegungTagesschuleModulGroup();
            group.group = groupTagesschule;
            let groupFoundInAngemeldete = false;
            for (const modulOfGroup of moduleOfGroup) {
                const foundInAngemeldete = copyFromOtherInstitution
                    ? TagesschuleUtil.copyAlreadyAngemeldetModule(group, moduleAngemeldet,
                        groupTagesschule, modulOfGroup)
                    : TagesschuleUtil.setAlreadyAngemeldetModule(group, moduleAngemeldet, modulOfGroup.id);
                if (foundInAngemeldete) {
                    groupFoundInAngemeldete = true;
                    continue;
                }
                // Das Modul war bisher nicht ausgewählt, muss aber trotzdem angeboten werden
                const tsBelegungTagesschuleModul = new TSBelegungTagesschuleModul();
                modulOfGroup.angemeldet = false;
                tsBelegungTagesschuleModul.modulTagesschule = modulOfGroup;
                group.module.push(tsBelegungTagesschuleModul);
            }
            if (groupFoundInAngemeldete || !verfuegungView) {
                modulGroups.push(group);
            }
        }
        return modulGroups;
    }

    private static setAlreadyAngemeldetModule(
        group: TSBelegungTagesschuleModulGroup,
        moduleAngemeldet: TSBelegungTagesschuleModul[],
        moduleOfGroupId: string,
    ): boolean {
        let foundInAngemeldete = false;
        for (const angMod of moduleAngemeldet) {
            if (angMod.modulTagesschule.id !== moduleOfGroupId) {
                continue;
            }
            angMod.modulTagesschule.angemeldet = true; // transientes Feld, muss neu gesetzt werden!
            angMod.modulTagesschule.angeboten = true;
            group.module.push(angMod);
            foundInAngemeldete = true;
        }
        return foundInAngemeldete;
    }

    private static copyAlreadyAngemeldetModule(
        group: TSBelegungTagesschuleModulGroup,
        moduleAngemeldet: TSBelegungTagesschuleModul[],
        newTagesschuleGroup: TSModulTagesschuleGroup,
        moduleOfGroup: TSModulTagesschule,
    ): boolean {
        let foundInAngemeldete = false;
        for (const angMod of moduleAngemeldet) {
            if (angMod.modulTagesschule.wochentag !== moduleOfGroup.wochentag ||
                !this.isModuleGroupSimilar(angMod.modulTagesschule, newTagesschuleGroup)  ||
                !moduleOfGroup.angeboten) {
                continue;
            }
            angMod.modulTagesschule.angemeldet = true; // transientes Feld, muss neu gesetzt werden!
            angMod.modulTagesschule.angeboten = true;
            angMod.modulTagesschule.id = moduleOfGroup.id; // wir muessen der id von der neue Modul setzen
            group.module.push(angMod);
            foundInAngemeldete = true;
        }
        return foundInAngemeldete;
    }

    private static isModuleGroupSimilar(
        module: TSModulTagesschule,
        group: TSModulTagesschuleGroup): boolean {
        // SCOLARIS Module
        if (EbeguUtil.isNotNullOrUndefined(module.moduleGroupName) &&
            EbeguUtil.isNotNullOrUndefined(group.modulTagesschuleName) &&
            group.modulTagesschuleName !== TSModulTagesschuleName.DYNAMISCH) {
            return module.moduleGroupName === group.modulTagesschuleName;
        }
        // Dynamische Module
        if (EbeguUtil.isNotNullOrUndefined(module.moduleGroupBezeichnung) &&
            EbeguUtil.isNotNullOrUndefined(group.bezeichnung)) {
            return this.similarString(module.moduleGroupBezeichnung.textDeutsch, group.bezeichnung.textDeutsch) &&
                this.similarString(module.moduleGroupBezeichnung.textFranzoesisch, group.bezeichnung.textFranzoesisch);
        }
        return false;
    }

    private static similarString(a: string, b: string): boolean {
        return a.toLowerCase().trim() === b.toLowerCase().trim();
    }

    private static loadAngeboteneModuleForTagesschule(
        betreuung: TSBetreuung,
        gesuchsPeriode: TSGesuchsperiode,
    ): TSModulTagesschuleGroup[] {
        const tsEinstellungenTagesschule =
            betreuung.institutionStammdaten.institutionStammdatenTagesschule.einstellungenTagesschule
                .filter((einstellung: TSEinstellungenTagesschule) =>
                    einstellung.gesuchsperiode.id === gesuchsPeriode.id)
                .pop();
        if (!tsEinstellungenTagesschule) {
            return [];
        }
        return tsEinstellungenTagesschule.modulTagesschuleGroups;
    }

    private static setModuleGroupNameAndBezeichnungForAngemeldeteModule(
        moduleAngemeldet: TSBelegungTagesschuleModul[],
        oldInstitutionStammdaten: TSInstitutionStammdatenSummary,
        gesuchsPeriode: TSGesuchsperiode,
    ): void {
        const tsEinstellungenTagesschule =
            oldInstitutionStammdaten.institutionStammdatenTagesschule.einstellungenTagesschule
                .filter((einstellung: TSEinstellungenTagesschule) =>
                    einstellung.gesuchsperiode.id === gesuchsPeriode.id)
                .pop();
        if (!tsEinstellungenTagesschule) {
            return;
        }
        for (const angMod of moduleAngemeldet) {
            tsEinstellungenTagesschule.modulTagesschuleGroups.forEach(
                moduleTagesschuleGroup => {
                    moduleTagesschuleGroup.module.forEach(
                        module => {
                            if (module.id === angMod.modulTagesschule.id) {
                                angMod.modulTagesschule.moduleGroupName = moduleTagesschuleGroup.modulTagesschuleName;
                                angMod.modulTagesschule.moduleGroupBezeichnung = moduleTagesschuleGroup.bezeichnung;
                            }
                        }
                    );
                }
            );
        }
    }

    public static initializeGroup(group: TSModulTagesschuleGroup): void {
        for (const day of getWeekdaysValues()) {
            if (TagesschuleUtil.getModulForDay(group, day)) {
                continue;
            }
            const modul = new TSModulTagesschule();
            modul.wochentag = day;
            modul.angeboten = false;
            group.module.push(modul);
        }
    }

    public static getModulForDay(group: TSModulTagesschuleGroup, day: TSDayOfWeek): TSModulTagesschule {
        for (const modul of group.module) {
            if (day !== modul.wochentag) {
                continue;
            }
            if (modul.id !== undefined) {
                modul.angeboten = true;
            }
            return modul;
        }
        return undefined;
    }

    public static getModulTimeAsString(modul: TSModulTagesschuleGroup): string {
        if (modul) {
            return `${modul.zeitVon} - ${modul.zeitBis}`;
        }
        return '';
    }

    public static sortModulTagesschuleGroups(modulTagesschuleGroups: TSModulTagesschuleGroup[]):
        TSModulTagesschuleGroup[] {
        if (EbeguUtil.isNotNullOrUndefined(modulTagesschuleGroups[0]) && modulTagesschuleGroups[0].modulTagesschuleName.startsWith('SCOLARIS_')) {
            return this.sortModulTagesschuleGroupsScolaris(modulTagesschuleGroups);
        }
        return modulTagesschuleGroups.sort((a: TSModulTagesschuleGroup, b: TSModulTagesschuleGroup) => {
            const referenzeDatum = '01/01/2011 ';
            const vonA = Date.parse(referenzeDatum + a.zeitVon);
            const vonB = Date.parse(referenzeDatum + b.zeitVon);
            const vergleicheVon = vonA.valueOf() - vonB.valueOf();
            if (vergleicheVon !== 0) {
                return vergleicheVon;
            }
            const bisA = Date.parse(referenzeDatum + a.zeitBis);
            const bisB = Date.parse(referenzeDatum + b.zeitBis);
            const vergleicheBis = bisA.valueOf() - bisB.valueOf();
            if (vergleicheBis !== 0) {
                return vergleicheBis;
            }
            // Falls es einen Bezeichnung gibt
            if (a.bezeichnung.textDeutsch && b.bezeichnung.textDeutsch) {
                return a.bezeichnung.textDeutsch.localeCompare(b.bezeichnung.textDeutsch);
            }
            return a.modulTagesschuleName.localeCompare(b.modulTagesschuleName);
        });
    }

    public static sortModulTagesschuleGroupsScolaris(modulTagesschuleGroups: TSModulTagesschuleGroup[]):
        TSModulTagesschuleGroup[] {
        return modulTagesschuleGroups.sort((a: TSModulTagesschuleGroup, b: TSModulTagesschuleGroup) => {
            return a.modulTagesschuleName.localeCompare(b.modulTagesschuleName);
        });
    }

    /**
     * Sortiert Tagesschuleinstellungen absteigend nach Periode
     */
    public static sortEinstellungenTagesschuleByPeriod(einstellungen: TSEinstellungenTagesschule[]):
        TSEinstellungenTagesschule[] {
        return einstellungen.sort((a, b) => {
            if (a.gesuchsperiode && b.gesuchsperiode) {
                return b.gesuchsperiode.gesuchsperiodeString.localeCompare(a.gesuchsperiode.gesuchsperiodeString);
            }
            return -1;
        });
    }

}
