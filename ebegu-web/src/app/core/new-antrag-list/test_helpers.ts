import {NgZone, PlatformRef, Type} from '@angular/core';
import {UpgradeModule} from '@angular/upgrade/static';

// tslint:disable-next-line:no-shadowed-variable
export function html(html: string): Element {
    // Don't return `body` itself, because using it as a `$rootElement` for ng1
    // will attach `$injector` to it and that will affect subsequent tests.
    const body = document.body;
    body.innerHTML = `<div>${html.trim()}</div>`;
    const div = document.body.firstChild as Element;

    if (div.childNodes.length === 1 && div.firstChild instanceof HTMLElement) {
        return div.firstChild;
    }

    return div;
}

// tslint:disable-next-line:typedef
export function bootstrap(
    platform: PlatformRef, ng2Module: Type<{}>, element: Element, ng1Module: angular.IModule) {
    // We bootstrap the Angular module first; then when it is ready (async) we bootstrap the AngularJS
    // module on the bootstrap element (also ensuring that AngularJS errors will fail the test).
    return platform.bootstrapModule(ng2Module).then(ref => {
        const ngZone = ref.injector.get<NgZone>(NgZone);
        const upgrade = ref.injector.get(UpgradeModule);
        const failHardModule: any = ($provide: any) => {
            $provide.value('$exceptionHandler', (err: any) => { throw err; });
        };

        // The `bootstrap()` helper is used for convenience in tests, so that we don't have to inject
        // and call `upgrade.bootstrap()` on every Angular module.
        // In order to closer emulate what happens in real application, ensure AngularJS is bootstrapped
        // inside the Angular zone.
        //
        ngZone.run(() => upgrade.bootstrap(element, [failHardModule, ng1Module.name]));

        return upgrade;
    });
}
