import { ComponentFixture, TestBed } from '@angular/core/testing';
import {MatDialog} from '@angular/material/dialog';
import {ApplicationPropertyRS} from '../../../app/core/rest-services/applicationPropertyRS.rest';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {SearchRS} from '../../../gesuch/service/searchRS.rest';
import {ReindexRS} from '../../service/reindexRS.rest';

import { AdminViewXComponent } from './admin-view-x.component';

describe('AdminViewXComponent', () => {
  let component: AdminViewXComponent;
  let fixture: ComponentFixture<AdminViewXComponent>;

  const applicationPropertyRSSpy = jasmine.createSpyObj<ApplicationPropertyRS>(ApplicationPropertyRS.name,
      ['update', 'create', 'getAllApplicationProperties']);
  applicationPropertyRSSpy.getAllApplicationProperties.and.resolveTo({} as any);

  const reindexRSSpy = jasmine.createSpyObj<ReindexRS>(ReindexRS.name,
      ['reindex']);
  const matDialogSpy = jasmine.createSpyObj<MatDialog>(MatDialog.name, ['open']);

  const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name, ['isOneOfRoles']);

  const searchRSSpy = jasmine.createSpyObj<SearchRS>(SearchRS.name, ['recreateAlleFaelleView']);

    beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AdminViewXComponent ],
      providers: [
          {provide: ApplicationPropertyRS, useValue: applicationPropertyRSSpy},
          {provide: ReindexRS, useValue: reindexRSSpy},
          {provide: SearchRS, useValue: searchRSSpy},
          {provide: MatDialog, useValue: matDialogSpy},
          {provide: AuthServiceRS, useValue: authServiceSpy}
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AdminViewXComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
