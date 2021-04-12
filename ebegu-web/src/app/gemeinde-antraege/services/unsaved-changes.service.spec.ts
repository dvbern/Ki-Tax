import {TestBed} from '@angular/core/testing';
import {MatDialogModule} from '@angular/material/dialog';
import {TransitionService} from '@uirouter/core';

import {UnsavedChangesService} from './unsaved-changes.service';

const transitionServiceSpy = jasmine.createSpyObj<TransitionService>(TransitionService.name,
    ['onStart']);

describe('UnsavedChangesService', () => {
    let service: UnsavedChangesService;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [
                MatDialogModule
            ],
            providers: [
                {provide: TransitionService, useValue: transitionServiceSpy}
            ]
        });
        service = TestBed.inject(UnsavedChangesService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });
});
