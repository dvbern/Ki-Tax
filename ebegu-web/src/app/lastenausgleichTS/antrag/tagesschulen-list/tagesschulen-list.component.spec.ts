import {HttpClientModule} from '@angular/common/http';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {TranslateModule} from '@ngx-translate/core';
import {ErrorService} from '../../../core/errors/service/ErrorService';

import {TagesschulenListComponent} from './tagesschulen-list.component';

describe('TagesschulenListComponent', () => {
    let component: TagesschulenListComponent;
    let fixture: ComponentFixture<TagesschulenListComponent>;

    const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, ['getErrors']);

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [HttpClientModule, TranslateModule.forRoot()],
            providers: [
                {
                    provide: ErrorService,
                    useValue: errorServiceSpy
                }
            ],
            declarations: [TagesschulenListComponent],
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(TagesschulenListComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
