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

import {ChangeDetectionStrategy, Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ControlContainer, NgForm} from '@angular/forms';
import {MatRadioChange} from '@angular/material/radio';
import {TranslateService} from '@ngx-translate/core';
import {TSFinanzModel} from '../../../../../models/TSFinanzModel';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';

@Component({
  selector: 'dv-steuerveranlagung-gemeinsam',
  templateUrl: './steuerveranlagung-gemeinsam.component.html',
  changeDetection: ChangeDetectionStrategy.Default,
  viewProviders: [{provide: ControlContainer, useExisting: NgForm}]
})
export class SteuerveranlagungGemeinsamComponent implements OnInit {

  @Input() public model: TSFinanzModel;

  @Output() public readonly gemeinsamChanged = new EventEmitter<MatRadioChange>();

  public constructor(
      public gesuchModelManager: GesuchModelManager,
      private readonly $translate: TranslateService
  ) { }

  public ngOnInit(): void {
  }

  public change($event: MatRadioChange): void {
      this.gemeinsamChanged.emit($event);
  }

  public getLabel(): string {
    if (EbeguUtil.isNullOrUndefined(this.gesuchModelManager.getGesuch()?.gesuchsteller2)) {
      return this.$translate.instant('FINANZIELLE_SITUATION_STEK_GEMEINSAM_NO_GS2_NAME', {
        basisjahr: this.gesuchModelManager.getBasisjahr()});
    }

    return this.$translate.instant('FINANZIELLE_SITUATION_STEK_GEMEINSAM', {
      basisjahr: this.gesuchModelManager.getBasisjahr(),
      namegs2: this.gesuchModelManager.getGesuch()?.gesuchsteller2?.gesuchstellerJA?.getFullName() || ''});
  }
}
