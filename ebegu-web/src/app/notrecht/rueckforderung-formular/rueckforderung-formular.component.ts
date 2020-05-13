/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {ChangeDetectionStrategy, Component, OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {Transition} from '@uirouter/core';
import {from, Observable} from 'rxjs';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import {TSRole} from '../../../models/enums/TSRole';
import {isNeuOrEingeladenStatus, TSRueckforderungStatus} from '../../../models/enums/TSRueckforderungStatus';
import {TSDownloadFile} from '../../../models/TSDownloadFile';
import {TSRueckforderungFormular} from '../../../models/TSRueckforderungFormular';
import {TSRueckforderungZahlung} from '../../../models/TSRueckforderungZahlung';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {DownloadRS} from '../../core/service/downloadRS.rest';
import {NotrechtRS} from '../../core/service/notrechtRS.rest';
import {I18nServiceRSRest} from '../../i18n/services/i18nServiceRS.rest';

@Component({
    selector: 'dv-rueckforderung-formular',
    templateUrl: './rueckforderung-formular.component.html',
    styleUrls: ['./rueckforderung-formular.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class RueckforderungFormularComponent implements OnInit {
    public get rueckforderungZahlungenList(): TSRueckforderungZahlung[] {
        return this._rueckforderungZahlungenList;
    }

    @ViewChild(NgForm) private readonly form: NgForm;

    public rueckforderungFormular$: Observable<TSRueckforderungFormular>;

    // Checkbox for Institution:
    public betreuungKorrektAusgewiesen: boolean;
    public gutscheinPlaetzenReduziert: boolean;
    public erstattungGemaessKanton: boolean;
    public mahlzeitenBGSubventionenGebuehrensystem: boolean;
    public belegeEinreichenBetrageKantonZurueckfordern: boolean;
    private _rueckforderungZahlungenList: TSRueckforderungZahlung[];
    private _stufe1ProvBetrag: number;

    private readonly TAGEMULTIPLYFACTOR: number = 25;

    public constructor(
        private readonly $transition$: Transition,
        private readonly translate: TranslateService,
        private readonly notrechtRS: NotrechtRS,
        private readonly authServiceRS: AuthServiceRS,
        private readonly downloadRS: DownloadRS,
        private readonly i18nServiceRS: I18nServiceRSRest,
    ) {
    }

    public ngOnInit(): void {
        const rueckforederungFormId: string = this.$transition$.params().rueckforderungId;

        if (!rueckforederungFormId) {
            return;
        }
        this.rueckforderungFormular$ = from(
            this.notrechtRS.findRueckforderungFormular(rueckforederungFormId).then(
                (response: TSRueckforderungFormular) => {
                    this.initRueckforderungZahlungen(response);
                    if (this.isPruefungKantonStufe1(response)) {
                        this.calculateKantonProvBetrag(response);
                    }
                    return response;
                }));
    }

    public saveRueckforderungFormular(rueckforderungFormular: TSRueckforderungFormular): void {
        if (!this.form.valid && rueckforderungFormular.status !== TSRueckforderungStatus.ABGESCHLOSSEN_OHNE_GESUCH) {
            EbeguUtil.selectFirstInvalid();
            return;
        }
        if (isNeuOrEingeladenStatus(rueckforderungFormular.status)) {
            return;
        }

        // Status wechseln:
        if (rueckforderungFormular.status === TSRueckforderungStatus.IN_BEARBEITUNG_INSTITUTION_STUFE_1) {
            if (this.authServiceRS.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles())) {
                rueckforderungFormular.status = TSRueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_1;
                rueckforderungFormular.stufe1KantonKostenuebernahmeAnzahlStunden = rueckforderungFormular.stufe1InstitutionKostenuebernahmeAnzahlStunden;
                rueckforderungFormular.stufe1KantonKostenuebernahmeAnzahlTage = rueckforderungFormular.stufe1InstitutionKostenuebernahmeAnzahlTage;
                rueckforderungFormular.stufe1KantonKostenuebernahmeBetreuung = rueckforderungFormular.stufe1InstitutionKostenuebernahmeBetreuung;
            } else {
                // ERROR transition not accepted
                return;
            }
        } else if (rueckforderungFormular.status === TSRueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_1) {
            if (this.authServiceRS.isOneOfRoles(
                [TSRole.SUPER_ADMIN, TSRole.ADMIN_MANDANT, TSRole.SACHBEARBEITER_MANDANT])) {
                rueckforderungFormular.status = TSRueckforderungStatus.GEPRUEFT_STUFE_1;
            } else {
                // ERROR transition not accepted
                return;
            }
        }

        this.rueckforderungFormular$ = from(this.notrechtRS.saveRueckforderungFormular(rueckforderungFormular)
            .then((response: TSRueckforderungFormular) => {
                this.initRueckforderungZahlungen(response);
                return response;
            }));
    }

    public rueckforderungAbschliessen(rueckforderungFormular: TSRueckforderungFormular): void {
        if (rueckforderungFormular.status === TSRueckforderungStatus.IN_BEARBEITUNG_INSTITUTION_STUFE_1
            && this.authServiceRS.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles())) {
            rueckforderungFormular.status = TSRueckforderungStatus.ABGESCHLOSSEN_OHNE_GESUCH;
        } else {
            // ERROR transition not accepted
            return;
        }

        this.saveRueckforderungFormular(rueckforderungFormular);
    }

    public initRueckforderungZahlungen(rueckfordeungFormular: TSRueckforderungFormular): void {
        this._rueckforderungZahlungenList = [];
        if (EbeguUtil.isNotNullOrUndefined(rueckfordeungFormular.stufe1FreigabeBetrag)) {
            const rueckforderungZahlungStufe1 = new TSRueckforderungZahlung();
            rueckforderungZahlungStufe1.betrag = rueckfordeungFormular.stufe1FreigabeBetrag;
            rueckforderungZahlungStufe1.datumErstellt = rueckfordeungFormular.stufe1FreigabeDatum;
            rueckforderungZahlungStufe1.stufe = 'RUECKFORDERUNG_ZAHLUNGEN_STUFE_1';
            rueckforderungZahlungStufe1.ausgeloest =
                EbeguUtil.isNotNullOrUndefined(rueckfordeungFormular.stufe1FreigabeAusbezahltAm);
            this.rueckforderungZahlungenList.push(rueckforderungZahlungStufe1);
        }
        if (EbeguUtil.isNullOrUndefined(rueckfordeungFormular.stufe2VerfuegungBetrag)) {
            return;
        }
        const rueckforderungZahlungStufe2 = new TSRueckforderungZahlung();
        rueckforderungZahlungStufe2.betrag = rueckfordeungFormular.stufe2VerfuegungBetrag;
        rueckforderungZahlungStufe2.datumErstellt = rueckfordeungFormular.stufe2VerfuegungDatum;
        rueckforderungZahlungStufe2.stufe = 'RUECKFORDERUNG_ZAHLUNGEN_STUFE_2';
        rueckforderungZahlungStufe2.ausgeloest =
            EbeguUtil.isNotNullOrUndefined(rueckfordeungFormular.stufe2VerfuegungAusbezahltAm);
        this.rueckforderungZahlungenList.push(rueckforderungZahlungStufe2);

    }

    public enableRueckforderungAbschliessen(): boolean {
        return this.betreuungKorrektAusgewiesen
            && this.gutscheinPlaetzenReduziert
            && this.erstattungGemaessKanton
            && this.mahlzeitenBGSubventionenGebuehrensystem
            && this.belegeEinreichenBetrageKantonZurueckfordern;
    }

    public isInstitutionStufe1(rueckforderungFormular: TSRueckforderungFormular): boolean {
        if (rueckforderungFormular.status === TSRueckforderungStatus.IN_BEARBEITUNG_INSTITUTION_STUFE_1
            && this.authServiceRS.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles())) {
            return true;
        }
        return false;
    }

    public isPruefungKantonStufe1(rueckforderungFormular: TSRueckforderungFormular): boolean {
        if (rueckforderungFormular.status === TSRueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_1
            && this.authServiceRS.isOneOfRoles(
                [TSRole.SUPER_ADMIN, TSRole.ADMIN_MANDANT, TSRole.SACHBEARBEITER_MANDANT])) {
            return true;
        }
        return false;
    }

    public isGeprueftKantonStufe1(rueckforderungFormular: TSRueckforderungFormular): boolean {
        if (rueckforderungFormular.status === TSRueckforderungStatus.GEPRUEFT_STUFE_1
            && this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getAllRolesForNotrecht())) {
            return true;
        }
        return false;
    }

    public showAbsendenText(rueckforderungFormular: TSRueckforderungFormular): boolean {
        if (rueckforderungFormular.status === TSRueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_1
            && this.authServiceRS.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles())) {
            return true;
        }
        return false;
    }

    public translateStatus(status: string): string {
        return this.translate.instant(`RUECKFORDERUNG_STATUS_${status}`);
    }

    public isKitaAngebot(rueckforderungFormular: TSRueckforderungFormular): boolean {
        return rueckforderungFormular.institutionStammdaten.betreuungsangebotTyp === TSBetreuungsangebotTyp.KITA;
    }

    public calculateInstiProvBetrag(rueckforderungFormular: TSRueckforderungFormular): void {
        this.stufe1ProvBetrag = undefined;
        if (EbeguUtil.isNullOrUndefined(rueckforderungFormular.stufe1InstitutionKostenuebernahmeBetreuung)) {
            return;
        }
        if (this.isKitaAngebot(rueckforderungFormular)
            && EbeguUtil.isNotNullOrUndefined(rueckforderungFormular.stufe1InstitutionKostenuebernahmeAnzahlTage)) {
            this.stufe1ProvBetrag =
                rueckforderungFormular.stufe1InstitutionKostenuebernahmeAnzahlTage * this.TAGEMULTIPLYFACTOR
                + rueckforderungFormular.stufe1InstitutionKostenuebernahmeBetreuung;
            return;
        }
        if (EbeguUtil.isNullOrUndefined(rueckforderungFormular.stufe1InstitutionKostenuebernahmeAnzahlStunden)) {
            return;
        }
        this.stufe1ProvBetrag =
            rueckforderungFormular.stufe1InstitutionKostenuebernahmeAnzahlStunden * 1
            + rueckforderungFormular.stufe1InstitutionKostenuebernahmeBetreuung;
    }

    public calculateKantonProvBetrag(rueckforderungFormular: TSRueckforderungFormular): void {
        this.stufe1ProvBetrag = undefined;
        if (EbeguUtil.isNullOrUndefined(rueckforderungFormular.stufe1KantonKostenuebernahmeBetreuung)) {
            return;
        }
        if (this.isKitaAngebot(rueckforderungFormular)
            && EbeguUtil.isNotNullOrUndefined(rueckforderungFormular.stufe1KantonKostenuebernahmeAnzahlTage)) {
            this.stufe1ProvBetrag =
                rueckforderungFormular.stufe1KantonKostenuebernahmeAnzahlTage * this.TAGEMULTIPLYFACTOR
                + rueckforderungFormular.stufe1KantonKostenuebernahmeBetreuung;
            return;
        }
        if (EbeguUtil.isNullOrUndefined(rueckforderungFormular.stufe1KantonKostenuebernahmeAnzahlStunden)) {
            return;
        }
        this.stufe1ProvBetrag =
            rueckforderungFormular.stufe1KantonKostenuebernahmeAnzahlStunden * 1
            + rueckforderungFormular.stufe1KantonKostenuebernahmeBetreuung;
    }

    public get stufe1ProvBetrag(): number {
        return this._stufe1ProvBetrag;
    }

    public set stufe1ProvBetrag(stufe1ProvBetrag: number) {
        this._stufe1ProvBetrag = stufe1ProvBetrag;
    }

    public downloadVorlage(rueckforderungFormular: TSRueckforderungFormular): void {
        const win = this.downloadRS.prepareDownloadWindow();
        const language = this.i18nServiceRS.currentLanguage();
        const angebotTyp = rueckforderungFormular.institutionStammdaten.betreuungsangebotTyp;
        this.downloadRS.getAccessTokenNotrechtvorlage(language, angebotTyp)
            .then((downloadFile: TSDownloadFile) => {
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, true, win);
            })
            .catch(() => {
                win.close();
            });
    }
}
