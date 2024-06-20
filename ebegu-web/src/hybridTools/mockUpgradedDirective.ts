/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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
    Directive,
    EventEmitter,
    Injectable,
    Input,
    Output
} from '@angular/core';
import {Observable, of} from 'rxjs';
import {MANDANTS, KiBonMandant} from '../app/core/constants/MANDANTS';
import {NewUserSelectDirective} from '../app/core/new-antrag-list/new-user-select.directive';
import {WindowRef} from '../app/core/service/windowRef.service';
import {LoadingButtonDirective} from '../app/shared/directive/loading-button.directive';
import {TooltipDirective} from '../app/shared/directive/TooltipDirective';
import {MandantService} from '../app/shared/services/mandant.service';
import {TSBenutzerNoDetails} from '../models/TSBenutzerNoDetails';

/**
 * This mock should be used when testing an angular component that uses LoadingButtonDirective
 * to avoid conflicts with dependencies
 */
@Directive({
    selector: 'dv-loading-button'
})
export class MockDvLoadingButtonDirective {
    @Input() public type: string;
    @Input() public delay: string;
    @Input() public buttonClass: string;
    @Input() public forceWaitService: string;
    @Input() public buttonDisabled: boolean;
    @Input() public ariaLabel: string;
    @Input() public inputId: string;

    @Output() public readonly buttonClick: EventEmitter<void> =
        new EventEmitter<void>();
}

@Directive({
    selector: 'dv-tooltip'
})
export class MockTooltipDirective {
    @Input() public inputId: string;
    @Input() public text: string;
}

@Directive({
    selector: 'dv-tooltip'
})
export class MockNewUserSelectDirective {
    @Input()
    public showSelectionAll: boolean;
    @Input()
    public angular2: boolean;
    @Input()
    public inputId: string;
    @Input()
    public dvUsersearch: string;
    @Input()
    public initialAll: boolean;
    @Input()
    public selectedUser: TSBenutzerNoDetails;
    @Input()
    public sachbearbeiterGemeinde: boolean;
    @Input()
    public schulamt: boolean;
    @Input()
    public userList: TSBenutzerNoDetails[];
    @Input()
    public useDefaultUserLists: boolean = true;
    // eslint-disable-next-line @angular-eslint/no-output-on-prefix
    @Output()
    public readonly userChanged: EventEmitter<{user: TSBenutzerNoDetails}> =
        new EventEmitter<{user: TSBenutzerNoDetails}>();
}

@Injectable()
class MockMandantService {
    public async initMandantCookie(): Promise<void> {
        return Promise.resolve();
    }

    public get mandant$(): Observable<KiBonMandant> {
        return of(MANDANTS.BERN);
    }
}

export const SHARED_MODULE_OVERRIDES = {
    remove: {
        declarations: [
            LoadingButtonDirective,
            TooltipDirective,
            NewUserSelectDirective
        ],
        exports: [
            LoadingButtonDirective,
            TooltipDirective,
            NewUserSelectDirective
        ],
        providers: [MandantService]
    },
    add: {
        declarations: [
            MockDvLoadingButtonDirective,
            MockTooltipDirective,
            MockNewUserSelectDirective
        ],
        exports: [
            MockDvLoadingButtonDirective,
            MockTooltipDirective,
            MockNewUserSelectDirective
        ],
        providers: [
            WindowRef,
            {
                provide: MandantService,
                useClass: MockMandantService
            }
        ]
    }
};
