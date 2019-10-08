import { TestBed } from '@angular/core/testing';

import { OnboardingPlaceholderService } from './onboarding-placeholder.service';

describe('OnboardingPlaceholderService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: OnboardingPlaceholderService = TestBed.get(OnboardingPlaceholderService);
    expect(service).toBeTruthy();
  });
});
