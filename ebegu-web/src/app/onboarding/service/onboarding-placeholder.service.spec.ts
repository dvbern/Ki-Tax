import { TestBed } from '@angular/core/testing';

import { OnboardingPlaceholderService } from './onboarding-placeholder.service';

describe('OnboardingPlaceholderService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service = TestBed.inject<OnboardingPlaceholderService>(OnboardingPlaceholderService);
    expect(service).toBeTruthy();
  });
});
