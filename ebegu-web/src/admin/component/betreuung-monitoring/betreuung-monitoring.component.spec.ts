import {ComponentFixture, TestBed} from '@angular/core/testing';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {of} from 'rxjs';
import {BetreuungMonitoringRS} from '../../service/betreuungMonitoringRS.rest';
import {WindowRef} from '../../../app/core/service/windowRef.service';
import {MaterialModule} from '../../../app/shared/material.module';
import {SharedModule} from '../../../app/shared/shared.module';

import {BetreuungMonitoringComponent} from './betreuung-monitoring.component';

describe('BetreuungMonitoringComponent', () => {
    let component: BetreuungMonitoringComponent;
    let fixture: ComponentFixture<BetreuungMonitoringComponent>;

    const betreuungMonitoringRSSpy = jasmine.createSpyObj<BetreuungMonitoringRS>(BetreuungMonitoringRS.name,
        ['getBetreuungMonitoringList']);

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [BetreuungMonitoringComponent],
            imports: [
                SharedModule,
                NoopAnimationsModule,
                MaterialModule,
            ],
            providers: [
                WindowRef,
                {provide: BetreuungMonitoringRS, useValue: betreuungMonitoringRSSpy},
            ],
        })
            .compileComponents();
        betreuungMonitoringRSSpy.getBetreuungMonitoringList.and.returnValue(of([]));
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(BetreuungMonitoringComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
