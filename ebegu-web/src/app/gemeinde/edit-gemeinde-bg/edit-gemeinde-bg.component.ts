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

import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    EventEmitter,
    Input,
    OnInit,
    Output,
    ViewChild
} from '@angular/core';
import {ControlContainer, NgForm, NgModelGroup} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {StateDeclaration, Transition} from '@uirouter/core';
import {Moment} from 'moment';
import {Observable} from 'rxjs';
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSAnspruchBeschaeftigungAbhaengigkeitTyp} from '../../../models/enums/TSAnspruchBeschaeftigungAbhaengigkeitTyp';
import {TSEinschulungTyp} from '../../../models/enums/TSEinschulungTyp';
import {getGemeindspezifischeBGConfigKeys, TSEinstellungKey} from '../../../models/enums/TSEinstellungKey';
import {TSGemeindeStatus} from '../../../models/enums/TSGemeindeStatus';
import {TSGesuchsperiodeStatus} from '../../../models/enums/TSGesuchsperiodeStatus';
import {TSRole} from '../../../models/enums/TSRole';
import {TSBenutzer} from '../../../models/TSBenutzer';
import {TSEinstellung} from '../../../models/TSEinstellung';
import {TSGemeinde} from '../../../models/TSGemeinde';
import {TSGemeindeKonfiguration} from '../../../models/TSGemeindeKonfiguration';
import {TSGemeindeStammdaten} from '../../../models/TSGemeindeStammdaten';
import {TSGesuchsperiode} from '../../../models/TSGesuchsperiode';
import {TSInstitution} from '../../../models/TSInstitution';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {CONSTANTS} from '../../core/constants/CONSTANTS';
import {EinschulungTypesGemeindeVisitor} from '../../core/constants/EinschulungTypesGemeindeVisitor';
import {LogFactory} from '../../core/logging/LogFactory';
import {ApplicationPropertyRS} from '../../core/rest-services/applicationPropertyRS.rest';
import {InstitutionRS} from '../../core/service/institutionRS.rest';
import {MandantService} from '../../shared/services/mandant.service';

const LOG = LogFactory.createLog('EditGemeindeBGComponent');

@Component({
    selector: 'dv-edit-gemeinde-bg',
    templateUrl: './edit-gemeinde-bg.component.html',
    styleUrls: ['./edit-gemeinde-bg.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}]
})
export class EditGemeindeBGComponent implements OnInit {
    @Input() public stammdaten$: Observable<TSGemeindeStammdaten>;
    @Input() public beguStartStr: string;
    @Input() private readonly gemeindeId: string;
    @Input() public editMode: boolean;
    @Input() public altBGAdresse: boolean;
    @Input() public beguStartDatum: Moment;
    @Input() public keineBeschwerdeAdresse: boolean;
    @Input() public gemeindeList$: Observable<TSGemeinde[]>;
    @Input() public zusatzTextBG: boolean;
    @Input() public gemeindeVereinfachteKonfigAktiv: boolean;

    @Output() public readonly altBGAdresseChange: EventEmitter<boolean> = new EventEmitter();
    @Output() public readonly keineBeschwerdeAdresseChange: EventEmitter<boolean> = new EventEmitter();
    @Output() public readonly zusatzTextBgChange: EventEmitter<boolean> = new EventEmitter();

    @ViewChild(NgModelGroup) private readonly group: NgModelGroup;

    public readonly CONSTANTS = CONSTANTS;

    public konfigurationsListe: TSGemeindeKonfiguration[];
    public gemeindeStatus: TSGemeindeStatus;
    public einschulungTypGemeindeValues: ReadonlyArray<TSEinschulungTyp>;
    public dauerBabyTarife: TSEinstellung[];
    public erlaubenInstitutionenZuWaehlen: boolean;
    public institutionen: TSInstitution[];
    public anspruchBeschaeftigungAbhaengigkeitTypValues: Array<TSAnspruchBeschaeftigungAbhaengigkeitTyp>;
    private navigationDest: StateDeclaration;
    private gesuchsperiodeIdsGemeindespezifischeKonfigForBGMap: Map<string, boolean>;

    public constructor(
        private readonly $transition$: Transition,
        private readonly translate: TranslateService,
        private readonly authServiceRs: AuthServiceRS,
        private readonly einstellungRS: EinstellungRS,
        private readonly cd: ChangeDetectorRef,
        private readonly applicationPropertyRS: ApplicationPropertyRS,
        private readonly institutionRS: InstitutionRS,
        private readonly  mandantService: MandantService
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

        this.mandantService.mandant$
            .subscribe(mandant => {
                this.einschulungTypGemeindeValues = new EinschulungTypesGemeindeVisitor().process(mandant);
            }, err => LOG.error(err));

        this.navigationDest = this.$transition$.to();
        this.anspruchBeschaeftigungAbhaengigkeitTypValues = Object.values(TSAnspruchBeschaeftigungAbhaengigkeitTyp);
        this.initDauerBabytarifEinstellungen();
        this.initGesuchsperiodeIdsGemeindespezifischeKonfigForBGMap();
        this.initErlaubenInstitutionenZuWaehlen();
    }

    private initDauerBabytarifEinstellungen(): void {
        this.einstellungRS.findEinstellungByKey(TSEinstellungKey.DAUER_BABYTARIF)
            .subscribe(einstellungen => {
                this.dauerBabyTarife = einstellungen;
                this.cd.markForCheck();
            }, error => LOG.error(error));
    }

    private initGesuchsperiodeIdsGemeindespezifischeKonfigForBGMap(): void {
        this.gesuchsperiodeIdsGemeindespezifischeKonfigForBGMap = new Map();
        this.einstellungRS.findEinstellungByKey(TSEinstellungKey.GEMEINDESPEZIFISCHE_BG_KONFIGURATIONEN)
            .subscribe((response: TSEinstellung[]) => {
                response.forEach(config => {
                    this.gesuchsperiodeIdsGemeindespezifischeKonfigForBGMap
                        .set(config.gesuchsperiodeId, config.getValueAsBoolean());
                    if (config.getValueAsBoolean()) {
                        this.loadGemeindespezifischeBgKonfigurationen(config.gesuchsperiodeId);
                    }
                });
                this.cd.markForCheck();
            }, error => LOG.error(error));
    }

    private loadGemeindespezifischeBgKonfigurationen(gesuchsperiodeId: string): void {
        const gemeindeKonfig = this.konfigurationsListe
            .find(config => config.gesuchsperiode.id === gesuchsperiodeId);

        if (!gemeindeKonfig) {
            return;
        }

        getGemeindspezifischeBGConfigKeys().forEach(einstellungenKey => {
            this.einstellungRS.findEinstellung(einstellungenKey, this.gemeindeId, gesuchsperiodeId)
                .subscribe(einstellung => {
                    einstellung.gemeindeId = this.gemeindeId;
                    gemeindeKonfig.gemeindespezifischeBGKonfigurationen.push(einstellung);
                    gemeindeKonfig.gemeindespezifischeBGKonfigurationen
                        .sort((a, b) => this.sortGemeindespezifischeConfigs(a, b));
                    gemeindeKonfig.konfigurationen.push(einstellung);
                    this.cd.markForCheck();
                }, error => LOG.error(error));
        });
    }

    // sorts by oder of EinstellungKey defined in getGemeindspezifischeBGConfigKeys
    private sortGemeindespezifischeConfigs(a: TSEinstellung, b: TSEinstellung): number {
        return getGemeindspezifischeBGConfigKeys().indexOf(a.key) - getGemeindspezifischeBGConfigKeys().indexOf(b.key);
    }

    public compareBenutzer(b1: TSBenutzer, b2: TSBenutzer): boolean {
        return b1 && b2 ? b1.username === b2.username : b1 === b2;
    }

    public compareGemeinde(g1: TSGemeinde, g2: TSGemeinde): boolean {
        return g1 && g2 ? g1.name === g2.name : g1 === g2;
    }

    public compareInstitution(i1: TSInstitution, i2: TSInstitution): boolean {
        return i1 && i2 ? i1.id === i2.id : i1 === i2;
    }

    public altBGAdresseHasChange(newVal: boolean): void {
        this.altBGAdresseChange.emit(newVal);
    }

    public zusatzTextBgHasChange(stammdaten: TSGemeindeStammdaten): void {
        if (!stammdaten.hasZusatzTextVerfuegung) {
            stammdaten.zusatzTextVerfuegung = undefined;
        }
        this.zusatzTextBgChange.emit(stammdaten.hasZusatzTextVerfuegung);
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

    public getKonfigAbhaengigkeitAnspruchBeschaeftigungspensum(gk: TSGemeindeKonfiguration): string {
        return this.translate.instant(gk.anspruchUnabhaengingVonBeschaeftigungsPensum.toString());

    }

    public changeKonfigBeguBisUndMitSchulstufe(gk: TSGemeindeKonfiguration): void {
        this.changeKonfig(TSEinstellungKey.GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE, gk.konfigBeguBisUndMitSchulstufe, gk);
    }

    public changeKonfigMahlzeitenverguenstigungMinmalerElternanteilMahlzeit(gk: TSGemeindeKonfiguration): void {
        this.changeKonfig(TSEinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_MINIMALER_ELTERNBEITRAG_MAHLZEIT,
            gk.konfigMahlzeitenverguenstigungMinimalerElternbeitragMahlzeit, gk);
    }

    public changeKonfigKeineGutscheineFuerSozialhilfeEmpfaenger(gk: TSGemeindeKonfiguration): void {
        this.changeKonfig(TSEinstellungKey.GEMEINDE_KEIN_GUTSCHEIN_FUER_SOZIALHILFE_EMPFAENGER,
            gk.konfigKeineGutscheineFuerSozialhilfeEmpfaenger, gk);
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

    public changeKonfigMahlzeitenverguenstigungEinkommensstufe1MaxEinkommen(
        gk: TSGemeindeKonfiguration,
        i: number
    ): void {
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
            this.einstellungRS.getAllEinstellungenBySystemCached(config.gesuchsperiode.id)
                .subscribe(einstellungen => {
                    const einstellungFKJVTexte = einstellungen
                        .find(e => e.key === TSEinstellungKey.FKJV_TEXTE);
                    config.isTextForFKJV = einstellungFKJVTexte.getValueAsBoolean();
                }, error => LOG.error(error));
        });
    }

    public isSuperAdmin(): boolean {
        return this.authServiceRs.isRole(TSRole.SUPER_ADMIN);
    }

    public keineBeschwerdeAdresseChanged(newVal: boolean): void {
        this.keineBeschwerdeAdresseChange.emit(newVal);
    }

    public showGemeindespezifischeKonfigForBG(gesuchsperiode: TSGesuchsperiode): boolean {
        return this.gesuchsperiodeIdsGemeindespezifischeKonfigForBGMap.get(gesuchsperiode.id);
    }

    public getDauerBabytarif(gesuchsperiode: TSGesuchsperiode): string {
        if (EbeguUtil.isNullOrUndefined(this.dauerBabyTarife)) {
            return null;
        }
        return this.dauerBabyTarife.find(einstellung => einstellung.gesuchsperiodeId === gesuchsperiode.id)?.value;
    }

    public changeKonfigHoheEinkommensklassenAktiviert(gk: TSGemeindeKonfiguration): void {
        this.changeKonfig(
            TSEinstellungKey.GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_AKTIVIERT,
            gk.konfigHoheEinkommensklassenAktiviert, gk
        );
        // Falls nicht mehr angewaehlt -> alle betroffenen Daten zuruecksetzen
        if (EbeguUtil.isNullOrFalse(gk.konfigHoheEinkommensklassenAktiviert)) {
            this.resetKonfigHoheEinkommensklassen(gk);
        }
    }

    private resetKonfigHoheEinkommensklassen(gk: TSGemeindeKonfiguration): void {
        gk.konfigHoheEinkommensklassenBetragKita = 0;
        gk.konfigHoheEinkommensklassenBetragTfo = 0;
        gk.konfigHoheEinkommensklassenBetragTfoAbPrimarschule = 0;
        gk.konfigHoheEinkommensklassenMassgebendenEinkommen = 0;

        this.changeKonfig(
            TSEinstellungKey.GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_KITA,
            gk.konfigHoheEinkommensklassenBetragKita, gk
        );
        this.changeKonfig(
            TSEinstellungKey.GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO,
            gk.konfigHoheEinkommensklassenBetragTfo, gk
        );
        this.changeKonfig(
            TSEinstellungKey.GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO_AB_PRIMARSCHULE,
            gk.konfigHoheEinkommensklassenBetragTfoAbPrimarschule, gk
        );
        this.changeKonfig(
            TSEinstellungKey.GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_MAX_MASSGEBENDEN_EINKOMMEN_FUER_BERECHNUNG,
            gk.konfigHoheEinkommensklassenMassgebendenEinkommen, gk
        );
    }

    public changeKonfigHoheEinkommensklassenBetragKita(gk: TSGemeindeKonfiguration): void {
        this.changeKonfig(
            TSEinstellungKey.GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_KITA,
            gk.konfigHoheEinkommensklassenBetragKita,
            gk
        );
    }

    public changeKonfigHoheEinkommensklassenBetragTfo(gk: TSGemeindeKonfiguration): void {
        this.changeKonfig(
            TSEinstellungKey.GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO,
            gk.konfigHoheEinkommensklassenBetragTfo,
            gk
        );
    }

    public changeKonfigHoheEinkommensklassenBetragTfoAbPrimarschule(gk: TSGemeindeKonfiguration): void {
        this.changeKonfig(
            TSEinstellungKey.GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO_AB_PRIMARSCHULE,
            gk.konfigHoheEinkommensklassenBetragTfoAbPrimarschule,
            gk
        );
    }

    public changeKonfigHoheEinkommensklassenMassgebendenEinkommen(gk: TSGemeindeKonfiguration): void {
        this.changeKonfig(
            TSEinstellungKey.GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_MAX_MASSGEBENDEN_EINKOMMEN_FUER_BERECHNUNG,
            gk.konfigHoheEinkommensklassenMassgebendenEinkommen,
            gk
        );
    }

    public changeKonfigAbhaengigkeitAnspruchBeschaeftigung(gk: TSGemeindeKonfiguration): void {
        this.changeKonfig(
            TSEinstellungKey.ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM,
            gk.anspruchUnabhaengingVonBeschaeftigungsPensum,
            gk
        );
    }

    public isUndefined(data: any): boolean {
        return EbeguUtil.isUndefined(data);
    }

    private initErlaubenInstitutionenZuWaehlen(): void {
        this.applicationPropertyRS.getPublicPropertiesCached()
            .then(res => this.erlaubenInstitutionenZuWaehlen = res.erlaubenInstitutionenZuWaehlen)
            .then(() => this.initInstitutionen());
    }

    private initInstitutionen(): void {
        // falls die Einstellung dekativiert ist, benötigen wir die Institutionen nicht
        if (!this.erlaubenInstitutionenZuWaehlen) {
            return;
        }
        this.institutionRS.getAllBgInstitutionen()
            .subscribe(institutionen => this.institutionen = institutionen);
    }

    public zugelasseneBgInstitutionenStr(institution: TSInstitution[]): string {
        return institution.map(i => i.name).join(', ');
    }

    public zugelasseneBgInstitutionenShort(institutionen: TSInstitution[]): string {
        let postfix = '';
        let iShort = institutionen.map(i => i.name);
        if (institutionen.length > 5) {
            iShort = iShort.slice(0, 4);
            postfix = '...';
        }
        return iShort.join(', ') + postfix;
    }

    public alleBgInstitutionenZugelassenChanged(stammdaten: TSGemeindeStammdaten): void {
        if (stammdaten.alleBgInstitutionenZugelassen) {
            stammdaten.zugelasseneBgInstitutionen = [];
        }
    }
}
