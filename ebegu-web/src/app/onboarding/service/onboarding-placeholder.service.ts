import {Injectable} from '@angular/core';
import {BehaviorSubject} from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class OnboardingPlaceholderService {
    public description1$: BehaviorSubject<string> = new BehaviorSubject(
        'ONBOARDING_MAIN_DESC1'
    );
    public description2$: BehaviorSubject<string> = new BehaviorSubject(
        'ONBOARDING_MAIN_DESC2'
    );
    public description3$: BehaviorSubject<string> = new BehaviorSubject(
        'ONBOARDING_MAIN_DESC3'
    );
    public description4$: BehaviorSubject<string> = new BehaviorSubject(
        'ONBOARDING_MAIN_DESC4'
    );
    public splittedScreen$: BehaviorSubject<boolean> =
        new BehaviorSubject<boolean>(true);

    public setDescription1(description1: string): void {
        this.description1$.next(description1);
    }

    public setDescription2(description2: string): void {
        this.description2$.next(description2);
    }

    public setDescription3(description3: string): void {
        this.description3$.next(description3);
    }

    public setDescription4(description4: string): void {
        this.description4$.next(description4);
    }

    public setSplittedScreen(splittedScreen: boolean): void {
        this.splittedScreen$.next(splittedScreen);
    }
}
