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

    beforeEach(() => destroyPlatform());
    afterEach(() => destroyPlatform());

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

            bootstrap(platformBrowserDynamic(), Ng2Module, element, ng1Module).then(upgrade => {
                expect(element).toBeTruthy();
            });
        });
    });
});
