/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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
import {Component, destroyPlatform, NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {platformBrowserDynamic} from '@angular/platform-browser-dynamic';
import {downgradeComponent, UpgradeModule} from '@angular/upgrade/static';
import {TranslateModule} from '@ngx-translate/core';
import {of} from 'rxjs';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {DvUserSelectConfig} from '../directive/dv-userselect/dv-userselect';
import {BenutzerRS} from '../service/benutzerRS.rest';
import {NewUserSelectDirective} from './new-user-select.directive';
import {bootstrap, html} from './test_helpers';

@Component({
    selector: 'ng2',
    template: `
        <div dvNewUserSelect></div>`,
})
class TestComponent {
}

const benutzerRSSpy = jasmine.createSpyObj<BenutzerRS>(BenutzerRS.name, ['getBenutzerBgOrGemeindeForGemeinde', 'getAllBenutzerBgOrGemeinde']);
const authRSSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name, ['loginRequest']);

benutzerRSSpy.getAllBenutzerBgOrGemeinde.and.returnValue(Promise.resolve([]));
authRSSpy.principal$ = of(null);

describe('NewUserSelectDirective', () => {

    let element: Element;

    beforeEach(destroyPlatform);
    afterEach(destroyPlatform);

    describe('', () => {
        it('should create an instance', () => {
            const ng1Module = angular.module('ng1Module', [])
                .component('dvUserselect', new DvUserSelectConfig())
                .factory('BenutzerRS', () => benutzerRSSpy)
                .factory('AuthServiceRS', () => authRSSpy)
                .directive('ng2', downgradeComponent({component: TestComponent}));

            @NgModule({
                declarations: [NewUserSelectDirective, TestComponent],
                entryComponents: [TestComponent],
                imports: [BrowserModule, UpgradeModule, TranslateModule.forRoot()],
            })
            class Ng2Module {
                public ngDoBootstrap(): void {
                }
            }

            element = html(`<ng2></ng2>`);

            // tslint:disable-next-line:no-unused
            bootstrap(platformBrowserDynamic(), Ng2Module, element, ng1Module).then(upgrade => {
                expect(element).toBeTruthy();
            });
        });
    });
});
