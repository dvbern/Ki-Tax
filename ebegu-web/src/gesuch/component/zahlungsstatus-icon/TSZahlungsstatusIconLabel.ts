/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

import {TranslateService} from '@ngx-translate/core';
import {LogFactory} from '../../../app/core/logging/LogFactory';
import {TSVerfuegungZeitabschnittZahlungsstatus} from '../../../models/enums/TSVerfuegungZeitabschnittZahlungsstatus';
import {TSBetreuung} from '../../../models/TSBetreuung';

const LOG = LogFactory.createLog('TSZahlungsstatusIconLabel');

export class TSZahlungsstatusIconLabel {
    private _iconLabel: string;
    private _tooltipLabel: string;

    public constructor(
        private readonly translateService: TranslateService
    ) {
    }

    public zahlungsstatusToIconLabel(
        zahlungsstatus: TSVerfuegungZeitabschnittZahlungsstatus,
        betreuung: TSBetreuung
    ): void {
        switch (zahlungsstatus) {
            case TSVerfuegungZeitabschnittZahlungsstatus.VERRECHNET:
            case TSVerfuegungZeitabschnittZahlungsstatus.VERRECHNET_KEINE_BETREUUNG:
                this.setIconLabelVerrechnet();
                break;
            case TSVerfuegungZeitabschnittZahlungsstatus.IGNORIERT:
            case TSVerfuegungZeitabschnittZahlungsstatus.IGNORIEREND:
            case TSVerfuegungZeitabschnittZahlungsstatus.IGNORIERT_KORRIGIERT:
                this.setIconLabelAusserhalb();
                break;
            case TSVerfuegungZeitabschnittZahlungsstatus.VERRECHNEND:
                this.setIconLabelZukuenftig();
                break;
            case TSVerfuegungZeitabschnittZahlungsstatus.VERRECHNET_KORRIGIERT:
                this.setIconLabelKorrigiert();
                break;
            case TSVerfuegungZeitabschnittZahlungsstatus.NEU: {
                if (betreuung.gueltig) {
                    this.setIconLabelZukuenftig();
                } else {
                    this.setIconLabelKorrigiert();
                }
                break;
            }
            default:
                LOG.error(`No Icon for Zahlungsstatus ${  zahlungsstatus  } defined`);
        }
    }

    private setIconLabelVerrechnet(): void {
        this._iconLabel = this.translateService.instant('ZAHLUNGSSTATUS_VERRECHNET_ICON');
        this._tooltipLabel = this.translateService.instant('ZAHLUNGSSTATUS_VERRECHNET');
    }

    private setIconLabelAusserhalb(): void {
        this._iconLabel = this.translateService.instant('ZAHLUNGSSTATUS_AUSSERHALB_ICON');
        this._tooltipLabel = this.translateService.instant('ZAHLUNGSSTATUS_AUSSERHALB');
    }

    private setIconLabelZukuenftig(): void {
        this._iconLabel = this.translateService.instant('ZAHLUNGSSTATUS_ZUKUENFTIG_ICON');
        this._tooltipLabel = this.translateService.instant('ZAHLUNGSSTATUS_ZUKUENFTIG');
    }

    private setIconLabelKorrigiert(): void {
        this._iconLabel = this.translateService.instant('ZAHLUNGSSTATUS_KORRIGIERT_ICON');
        this._tooltipLabel = this.translateService.instant('ZAHLUNGSSTATUS_KORRIGIERT');
    }

    public get iconLabel(): string {
        return this._iconLabel;
    }

    public get tooltipLabel(): string {
        return this._tooltipLabel;
    }
}

