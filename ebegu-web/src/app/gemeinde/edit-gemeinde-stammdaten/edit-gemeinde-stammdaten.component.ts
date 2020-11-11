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

import {ChangeDetectionStrategy, Component, Input, OnDestroy, OnInit} from '@angular/core';
import {ControlContainer, NgForm} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import * as moment from 'moment';
import {Observable, Subject} from 'rxjs';
import {takeUntil} from 'rxjs/operators';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSRole} from '../../../models/enums/TSRole';
import {TSBenutzer} from '../../../models/TSBenutzer';
import {TSGemeindeStammdaten} from '../../../models/TSGemeindeStammdaten';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {CONSTANTS} from '../../core/constants/CONSTANTS';
import {LogFactory} from '../../core/logging/LogFactory';

const LOG = LogFactory.createLog('EditGemeindeComponentStammdaten');

@Component({
    selector: 'dv-edit-gemeinde-stammdaten',
    templateUrl: './edit-gemeinde-stammdaten.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}],
})
export class EditGemeindeComponentStammdaten implements OnInit, OnDestroy {

    @Input() public stammdaten$: Observable<TSGemeindeStammdaten>;
    @Input() private readonly gemeindeId: string;
    @Input() public editMode: boolean;
    @Input() public tageschuleEnabledForMandant: boolean;

    public readonly CONSTANTS = CONSTANTS;

    public korrespondenzsprache: string;
    public benutzerListe: Array<TSBenutzer>;
    public showMessageKeinAngebotSelected: boolean = false;
    public minDateTSFI = moment('20200801', 'YYYYMMDD');

    private readonly unsubscribe$ = new Subject<void>();
    public ebeguUtil = EbeguUtil;

    public constructor(
        private readonly translate: TranslateService,
        private readonly authServiceRS: AuthServiceRS,
    ) {
    }

    public ngOnInit(): void {
        if (!this.gemeindeId) {
            return;
        }
        this.stammdaten$
            .pipe(takeUntil(this.unsubscribe$))
            .subscribe(
                stammdaten => this.initValues(stammdaten),
                err => LOG.error(err)
            );
    }

    public ngOnDestroy(): void {
        this.unsubscribe$.next();
        this.unsubscribe$.complete();
    }

    private initValues(stammdaten: TSGemeindeStammdaten): void {
        const languages: string[] = [];
        if (stammdaten.korrespondenzspracheDe) {
            languages.push(this.translate.instant('DEUTSCH'));
        }
        if (stammdaten.korrespondenzspracheFr) {
            languages.push(this.translate.instant('FRANZOESISCH'));
        }
        this.korrespondenzsprache = languages.join(', ');

        // Für den "normalen" Defaultbenutzer sollen alle Benutzer der BG oder TS Rolle vorgeschlagen werden
        // Duplikate müssen aber vermieden werden
        this.benutzerListe = stammdaten.benutzerListeBG;
        stammdaten.benutzerListeTS.forEach(tsBen => {
            if (!this.benutzerListe.find(value => value.username === tsBen.username)) {
                this.benutzerListe.push(tsBen);
            }
        });
        this.showMessageKeinAngebotSelected = !stammdaten.gemeinde.angebotBG
            && !stammdaten.gemeinde.angebotTS
            && !stammdaten.gemeinde.angebotFI;
    }

    public isSuperadminOrMandant(): boolean {
        return this.authServiceRS.isOneOfRoles([TSRole.SUPER_ADMIN, TSRole.ADMIN_MANDANT,
            TSRole.SACHBEARBEITER_MANDANT]);
    }

    public isSuperadmin(): boolean {
        return this.authServiceRS.isRole(TSRole.SUPER_ADMIN);
    }

    public compareBenutzer(b1: TSBenutzer, b2: TSBenutzer): boolean {
        return b1 && b2 ? b1.username === b2.username : b1 === b2;
    }

    public angeboteChanged(stammdaten: TSGemeindeStammdaten): void {
        const hasAngebot =
            stammdaten.gemeinde.angebotBG || stammdaten.gemeinde.angebotTS || stammdaten.gemeinde.angebotFI;
        this.showMessageKeinAngebotSelected = !hasAngebot;
    }
}
