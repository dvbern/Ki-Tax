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
import TSBelegungTagesschuleModul from '../models/TSBelegungTagesschuleModul';
import TSBelegungTagesschuleModulGroup from '../models/TSBelegungTagesschuleModulGroup';
import TSBetreuung from '../models/TSBetreuung';
import TSEinstellungenTagesschule from '../models/TSEinstellungenTagesschule';
import TSGesuchsperiode from '../models/TSGesuchsperiode';
import TSModulTagesschule from '../models/TSModulTagesschule';
import TSModulTagesschuleGroup from '../models/TSModulTagesschuleGroup';

export class TagesschuleUtil {

    public static initModuleTagesschule(betreuung: TSBetreuung, gesuchsPeriode: TSGesuchsperiode,
                                        verfuegungView: boolean): TSBelegungTagesschuleModulGroup[] {
        if (!(betreuung.institutionStammdaten
            && betreuung.institutionStammdaten.institutionStammdatenTagesschule)
        ) {
            return [];
        }
        const moduleAngemeldet = betreuung.belegungTagesschule.belegungTagesschuleModule;
        const moduleAngeboten = this.loadAngeboteneModuleForTagesschule(betreuung, gesuchsPeriode);

        return TagesschuleUtil.initModulGroups(moduleAngemeldet, moduleAngeboten, verfuegungView);
    }

    private static initModulGroups(moduleAngemeldet: TSBelegungTagesschuleModul[],
                                   moduleAngeboten: TSModulTagesschuleGroup[],
                                   verfuegungView: boolean): TSBelegungTagesschuleModulGroup[] {
        const modulGroups: TSBelegungTagesschuleModulGroup[] = [];
        moduleAngeboten = this.sortModulTagesschuleGroups(moduleAngeboten);
        for (const groupTagesschule of moduleAngeboten) {
            TagesschuleUtil.initializeGroup(groupTagesschule);
            const moduleOfGroup = groupTagesschule.getModuleOrdered();
            const group = new TSBelegungTagesschuleModulGroup();
            group.group = groupTagesschule;
            let groupFoundInAngemeldete = false;
            for (const modulOfGroup of moduleOfGroup) {
                const foundInAngemeldete =
                    TagesschuleUtil.setAlreadyAngemeldetModule(group, moduleAngemeldet, modulOfGroup.id);
                if (foundInAngemeldete) {
                    groupFoundInAngemeldete = true;
                    continue;
                }
                // Das Modul war bisher nicht ausgewählt, muss aber trotzdem angeboten werden
                const tsBelegungTagesschuleModul = new TSBelegungTagesschuleModul();
                tsBelegungTagesschuleModul.modulTagesschule = modulOfGroup;
                group.module.push(tsBelegungTagesschuleModul);
            }
            if (groupFoundInAngemeldete || !verfuegungView) {
                modulGroups.push(group);
            }
        }
        return modulGroups;
    }

    private static setAlreadyAngemeldetModule(group: TSBelegungTagesschuleModulGroup,
                                              moduleAngemeldet: TSBelegungTagesschuleModul[],
                                              moduleOfGroupId: string): boolean {
        let foundInAngemeldete = false;
        for (const angMod of moduleAngemeldet) {
            if (angMod.modulTagesschule.id !== moduleOfGroupId) {
                continue;
            }
            angMod.modulTagesschule.angemeldet = true; // transientes Feld, muss neu gesetzt werden!
            group.module.push(angMod);
            foundInAngemeldete = true;
        }
        return foundInAngemeldete;
    }

    private static loadAngeboteneModuleForTagesschule(betreuung: TSBetreuung,
                                                      gesuchsPeriode: TSGesuchsperiode): TSModulTagesschuleGroup[] {
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

    public static sortModulTagesschuleGroups(modulTagesschuleGroups: TSModulTagesschuleGroup[]) : TSModulTagesschuleGroup[] {
        return modulTagesschuleGroups.sort(function (a: TSModulTagesschuleGroup, b: TSModulTagesschuleGroup) {
            var vonA = Date.parse('01/01/2011 ' + a.zeitVon);
            var vonB = Date.parse('01/01/2011 ' + b.zeitVon);
            var vergleicheVon = vonA.valueOf() - vonB.valueOf();
            if (vergleicheVon != 0) {
                return vergleicheVon;
            }
            var bisA = Date.parse('01/01/2011 ' + a.zeitBis);
            var bisB = Date.parse('01/01/2011 ' + b.zeitBis);
            var vergleicheBis = bisA.valueOf() - bisB.valueOf();
            if (vergleicheBis != 0) {
                return vergleicheBis;
            }
            return a.bezeichnung.textDeutsch.localeCompare(b.bezeichnung.textDeutsch);
        });
    }

}
