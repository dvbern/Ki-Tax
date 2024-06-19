import {ElementRef} from '@angular/core';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {EnableElementDirective} from './enable-element.directive';

export class MockElementRef extends ElementRef {
    public nativeElement: {};
}

describe('DvEnableAllowedRolesDirective', () => {
    const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(
        AuthServiceRS.name,
        ['isOneOfRoles']
    );
    authServiceSpy.isOneOfRoles.and.returnValue(true);
    it('should create an instance', () => {
        const directive = new EnableElementDirective(
            new MockElementRef({}),
            authServiceSpy,
            null
        );
        expect(directive).toBeTruthy();
    });
});
