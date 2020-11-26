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

import {ChangeDetectionStrategy, Component, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {ControlContainer, NgForm, NgModelGroup} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {StateDeclaration, Transition} from '@uirouter/core';
import {Moment} from 'moment';
import {Observable} from 'rxjs';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {getTSEinschulungTypGemeindeValues, TSEinschulungTyp} from '../../../models/enums/TSEinschulungTyp';
import {TSEinstellungKey} from '../../../models/enums/TSEinstellungKey';
import {TSGemeindeStatus} from '../../../models/enums/TSGemeindeStatus';
import {TSGesuchsperiodeStatus} from '../../../models/enums/TSGesuchsperiodeStatus';
import {TSRole} from '../../../models/enums/TSRole';
import {TSBenutzer} from '../../../models/TSBenutzer';
import {TSGemeindeKonfiguration} from '../../../models/TSGemeindeKonfiguration';
import {TSGemeindeStammdaten} from '../../../models/TSGemeindeStammdaten';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {LogFactory} from '../../core/logging/LogFactory';
import {CONSTANTS} from '../../core/constants/CONSTANTS';

const LOG = LogFactory.createLog('EditGemeindeComponentBG');

@Component({
    selector: 'dv-edit-gemeinde-bg',
    templateUrl: './edit-gemeinde-bg.component.html',
    styleUrls: ['./edit-gemeinde-bg.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}],
})
export class EditGemeindeComponentBG implements OnInit {
    @Input() public stammdaten$: Observable<TSGemeindeStammdaten>;
    @Input() public beguStartStr: string;
    @Input() private readonly gemeindeId: string;
    @Input() public editMode: boolean;
    @Input() public altBGAdresse: boolean;
    @Input() public beguStartDatum: Moment;
    @Input() public keineBeschwerdeAdresse: boolean;

    @Output() public readonly altBGAdresseChange: EventEmitter<boolean> = new EventEmitter();
    @Output() public readonly keineBeschwerdeAdresseChange: EventEmitter<boolean> = new EventEmitter();

    @ViewChild(NgModelGroup) private readonly group: NgModelGroup;

    public readonly CONSTANTS = CONSTANTS;

    public konfigurationsListe: TSGemeindeKonfiguration[];
    public gemeindeStatus: TSGemeindeStatus;
    public einschulungTypGemeindeValues: Array<TSEinschulungTyp>;
    private navigationDest: StateDeclaration;

    public constructor(
        private readonly $transition$: Transition,
        private readonly translate: TranslateService,
        private readonly authServiceRs: AuthServiceRS,
    ) {

    }

    public ngOnInit(): void {
        if (!this.gemeindeId) {
            return;
        }
        this.stammdaten$.subscribe(stammdaten => {
                this.konfigurationsListe = stammdaten.konfigurationsListe;
                this.gemeindeStatus = stammdaten.gemeinde.status;
                this.initProperties();
            },
            err => LOG.error(err));

        this.navigationDest = this.$transition$.to();
        this.einschulungTypGemeindeValues = getTSEinschulungTypGemeindeValues();
    }

    public compareBenutzer(b1: TSBenutzer, b2: TSBenutzer): boolean {
        return b1 && b2 ? b1.username === b2.username : b1 === b2;
    }

    public altBGAdresseHasChange(newVal: boolean): void {
        this.altBGAdresseChange.emit(newVal);
    }

    public getKonfigKontingentierungString(): string {
        return this.translate.instant('KONTINGENTIERUNG');
    }

    public changeKonfigKontingentierung(gk: TSGemeindeKonfiguration): void {
        this.changeKonfig(TSEinstellungKey.GEMEINDE_KONTINGENTIERUNG_ENABLED, gk.konfigKontingentierung, gk);
    }

    public getKonfigBeguBisUndMitSchulstufeString(gk: TSGemeindeKonfiguration): string {
        const bgBisStr = this.translate.instant(gk.konfigBeguBisUndMitSchulstufe.toString());
        return bgBisStr;
    }

    public changeKonfigBeguBisUndMitSchulstufe(gk: TSGemeindeKonfiguration): void {
        this.changeKonfig(TSEinstellungKey.GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE, gk.konfigBeguBisUndMitSchulstufe, gk);
    }

    public changeKonfigMahlzeitenverguenstigungMinmalerElternanteilMahlzeit(gk: TSGemeindeKonfiguration): void {
        this.changeKonfig(TSEinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_MINIMALER_ELTERNBEITRAG_MAHLZEIT,
            gk.konfigMahlzeitenverguenstigungMinimalerElternbeitragMahlzeit, gk);
    }

    public changeKonfigErwerbspensumZuschlagOverriden(gk: TSGemeindeKonfiguration): void {
        // if the flag is unchecked, we need to restore the original value
        if (!gk.erwerbspensumZuschlagOverriden) {
            this.resetErwerbspensumZuschlag(gk);
        }
    }

    public changeErwerbspensumZuschlag(gk: TSGemeindeKonfiguration): void {
        this.changeKonfig(TSEinstellungKey.ERWERBSPENSUM_ZUSCHLAG, gk.erwerbspensumZuschlag, gk);
    }

    public changeKonfigZusaetzlicherGutscheinEnabled(gk: TSGemeindeKonfiguration): void {
        this.changeKonfig(
            TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED, gk.konfigZusaetzlicherGutscheinEnabled, gk);
        // Falls nicht mehr angewaehlt -> alle betroffenen Daten zuruecksetzen
        if (EbeguUtil.isNullOrFalse(gk.konfigZusaetzlicherGutscheinEnabled)) {
            this.resetKonfigZusaetzlicherGutschein(gk);
        }
    }

    private resetKonfigZusaetzlicherGutschein(gk: TSGemeindeKonfiguration): void {
        gk.konfigZusaetzlicherGutscheinBetragKita = 0;
        gk.konfigZusaetzlicherGutscheinBetragTfo = 0;
        gk.konfigZusaetzlicherGutscheinBisUndMitSchulstufeKita = TSEinschulungTyp.VORSCHULALTER;
        gk.konfigZusaetzlicherGutscheinBisUndMitSchulstufeTfo = TSEinschulungTyp.VORSCHULALTER;

        this.changeKonfig(
            TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA,
            gk.konfigZusaetzlicherGutscheinBetragKita, gk
        );
        this.changeKonfig(
            TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO,
            gk.konfigZusaetzlicherGutscheinBetragTfo, gk
        );
        this.changeKonfig(
            TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_KITA,
            gk.konfigZusaetzlicherGutscheinBisUndMitSchulstufeKita, gk
        );
        this.changeKonfig(
            TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO,
            gk.konfigZusaetzlicherGutscheinBisUndMitSchulstufeTfo, gk
        );
    }

    public changeKonfigZusaetzlicherGutscheinBetragKita(gk: TSGemeindeKonfiguration): void {
        this.changeKonfig(
            TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA,
            gk.konfigZusaetzlicherGutscheinBetragKita,
            gk
        );
    }

    public changeKonfigZusaetzlicherGutscheinBetragTfo(gk: TSGemeindeKonfiguration): void {
        this.changeKonfig(
            TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO,
            gk.konfigZusaetzlicherGutscheinBetragTfo,
            gk
        );
    }

    public changeKonfigZusaetzlicherGutscheinBisUndMitSchulstufeKita(gk: TSGemeindeKonfiguration): void {
        this.changeKonfig(
            TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_KITA,
            gk.konfigZusaetzlicherGutscheinBisUndMitSchulstufeKita,
            gk
        );
    }

    public changeKonfigZusaetzlicherGutscheinBisUndMitSchulstufeTfo(gk: TSGemeindeKonfiguration): void {
        this.changeKonfig(
            TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO,
            gk.konfigZusaetzlicherGutscheinBisUndMitSchulstufeTfo,
            gk
        );
    }

    public changeKonfigZusaetzlicherBabybeitragEnabled(gk: TSGemeindeKonfiguration): void {
        this.changeKonfig(
            TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED,
            gk.konfigZusaetzlicherBabybeitragEnabled, gk
        );
        // Falls nicht mehr angewaehlt -> alle betroffenen Daten zuruecksetzen
        if (EbeguUtil.isNullOrFalse(gk.konfigZusaetzlicherBabybeitragEnabled)) {
            this.resetKonfigZusaetzlicherBabybeitrag(gk);
        }
    }

    private resetKonfigZusaetzlicherBabybeitrag(gk: TSGemeindeKonfiguration): void {
        gk.konfigZusaetzlicherBabybeitragBetragKita = 0;
        gk.konfigZusaetzlicherBabybeitragBetragTfo = 0;

        this.changeKonfig(
            TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA,
            gk.konfigZusaetzlicherBabybeitragBetragKita, gk
        );
        this.changeKonfig(
            TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO,
            gk.konfigZusaetzlicherBabybeitragBetragTfo, gk
        );
    }

    public changeZusaetzlicherBabybeitragKita(gk: TSGemeindeKonfiguration): void {
        this.changeKonfig(
            TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA,
            gk.konfigZusaetzlicherBabybeitragBetragKita,
            gk
        );
    }

    public changeZusaetzlicherBabybeitragTfo(gk: TSGemeindeKonfiguration): void {
        this.changeKonfig(
            TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO,
            gk.konfigZusaetzlicherBabybeitragBetragTfo,
            gk
        );
    }

    public changeKonfigZusaetzlicherAnspruchFreiwilligenarbeitEnabled(gk: TSGemeindeKonfiguration): void {
        this.changeKonfig(
            TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED,
            gk.konfigZusaetzlicherAnspruchFreiwilligenarbeitEnabled, gk
        );
        // Falls nicht mehr angewaehlt -> alle betroffenen Daten zuruecksetzen
        if (EbeguUtil.isNullOrFalse(gk.konfigZusaetzlicherAnspruchFreiwilligenarbeitEnabled)) {
            this.resetKonfigZusaetzlicherAnspruchFreiwilligenarbeit(gk);
        }
    }

    private resetKonfigZusaetzlicherAnspruchFreiwilligenarbeit(gk: TSGemeindeKonfiguration): void {
        gk.konfigZusaetzlicherAnspruchFreiwilligenarbeitMaxprozent = 0;
        this.changeKonfig(
            TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT,
            gk.konfigZusaetzlicherAnspruchFreiwilligenarbeitMaxprozent, gk
        );
    }

    public changeKonfigZusaetzlicherAnspruchFreiwilligenarbeitMax(gk: TSGemeindeKonfiguration): void {
        this.changeKonfig(
            TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT,
            gk.konfigZusaetzlicherAnspruchFreiwilligenarbeitMaxprozent,
            gk
        );
    }

    public changeKonfigMahlzeitenverguenstigungEnabled(gk: TSGemeindeKonfiguration): void {
        this.changeKonfig(
            TSEinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED,
            gk.konfigMahlzeitenverguenstigungEnabled,
            gk
        );
    }

    public changeKonfigMahlzeitenverguenstigungEinkommensstufe1VerguenstigungMahlzeit(gk: TSGemeindeKonfiguration): void {
        this.changeKonfig(
            TSEinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_MAHLZEIT,
            gk.konfigMahlzeitenverguenstigungEinkommensstufe1VerguenstigungMahlzeit,
            gk
        );
    }

    public changeKonfigMahlzeitenverguenstigungEinkommensstufe1MaxEinkommen(gk: TSGemeindeKonfiguration, i: number): void {
        this.changeKonfig(
            TSEinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_MAX_EINKOMMEN,
            gk.konfigMahlzeitenverguenstigungEinkommensstufe1MaxEinkommen,
            gk
        );
        const stufe2MaxInput = this.group.control.controls[`mahlzeitenverguenstigung_stufe2_max_id_${i}`];
        if (stufe2MaxInput.untouched) {
            stufe2MaxInput.markAsTouched();
        }
        stufe2MaxInput.updateValueAndValidity();
    }

    public changeKonfigMahlzeitenverguenstigungEinkommensstufe2VerguenstigungMahlzeit(gk: TSGemeindeKonfiguration): void {
        this.changeKonfig(
            TSEinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_MAHLZEIT,
            gk.konfigMahlzeitenverguenstigungEinkommensstufe2VerguenstigungMahlzeit,
            gk
        );
    }

    public changeKonfigMahlzeitenverguenstigungEinkommensstufe2MaxEinkommen(gk: TSGemeindeKonfiguration): void {
        this.changeKonfig(
            TSEinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_MAX_EINKOMMEN,
            gk.konfigMahlzeitenverguenstigungEinkommensstufe2MaxEinkommen,
            gk
        );
    }

    public changeKonfigMahlzeitenverguenstigungEinkommensstufe3VerguenstigungMahlzeit(gk: TSGemeindeKonfiguration): void {
        this.changeKonfig(
            TSEinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_MAHLZEIT,
            gk.konfigMahlzeitenverguenstigungEinkommensstufe3VerguenstigungMahlzeit,
            gk
        );
    }

    public changeKonfigMahlzeitenverguenstigungFuerSozialhilfebezuegerEnabled(gk: TSGemeindeKonfiguration): void {
        this.changeKonfig(
            TSEinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_FUER_SOZIALHILFEBEZUEGER_ENABLED,
            gk.konfigMahlzeitenverguenstigungFuerSozialhilfebezuegerEnabled,
            gk
        );
    }

    public changeKonfigErwerbspensumMinimumOverriden(gk: TSGemeindeKonfiguration): void {
        // if the flag is unchecked, we need to restore the original value
        if (!gk.erwerbspensumMinimumOverriden) {
            this.resetErwerbspensenMinimum(gk);
        }
    }

    public changeErwerbspensumMinimumVorschule(gk: TSGemeindeKonfiguration): void {
        this.changeKonfig(
            TSEinstellungKey.GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT, gk.erwerbspensumMiminumVorschule, gk);
    }

    public changeErwerbspensumMinimumSchulkinder(gk: TSGemeindeKonfiguration): void {
        this.changeKonfig(
            TSEinstellungKey.GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT, gk.erwerbspensumMiminumSchulkinder, gk);
    }

    private resetErwerbspensenMinimum(gk: TSGemeindeKonfiguration): void {
        gk.erwerbspensumMiminumVorschule = gk.erwerbspensumMiminumVorschuleMax;
        gk.erwerbspensumMiminumSchulkinder = gk.erwerbspensumMiminumSchulkinderMax;
        this.changeErwerbspensumMinimumVorschule(gk);
        this.changeErwerbspensumMinimumSchulkinder(gk);
    }

    private changeKonfig(einstellungKey: TSEinstellungKey, konfig: any, gk: TSGemeindeKonfiguration): void {
        gk.konfigurationen
            .filter(property => einstellungKey === property.key)
            .forEach(property => {
                property.value = String(konfig);
            });
    }

    public resetErwerbspensumZuschlag(gk: TSGemeindeKonfiguration): void {
        gk.erwerbspensumZuschlag = gk.erwerbspensumZuschlagMax;
        this.changeErwerbspensumZuschlag(gk);
    }

    public isKonfigurationEditable(gk: TSGemeindeKonfiguration): boolean {
        return 'gemeinde.edit' === this.navigationDest.name
            && this.editMode
            && (TSGemeindeStatus.EINGELADEN === this.gemeindeStatus
                || (gk.gesuchsperiode && gk.gesuchsperiode.status &&
                    TSGesuchsperiodeStatus.GESCHLOSSEN !== gk.gesuchsperiode.status));
    }

    private initProperties(): void {
        this.konfigurationsListe.forEach(config => {
            config.initProperties();
        });
    }

    public isSuperAdmin(): boolean {
        return this.authServiceRs.isRole(TSRole.SUPER_ADMIN);
    }

    public keineBeschwerdeAdresseChanged(newVal: boolean): void {
        this.keineBeschwerdeAdresseChange.emit(newVal);
    }
}
