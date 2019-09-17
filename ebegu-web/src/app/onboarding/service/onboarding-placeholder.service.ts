import {Injectable} from '@angular/core';
import {BehaviorSubject} from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class OnboardingPlaceholderService {

    public placeholder1$: BehaviorSubject<string> = new BehaviorSubject('ONBOARDING_MAIN_PH1');
    public description1$: BehaviorSubject<string> = new BehaviorSubject('ONBOARDING_MAIN_DESC1');
    public placeholder2$: BehaviorSubject<string> = new BehaviorSubject('ONBOARDING_MAIN_PH2');
    public description2$: BehaviorSubject<string> = new BehaviorSubject('ONBOARDING_MAIN_DESC2');
    public placeholder3$: BehaviorSubject<string> = new BehaviorSubject('ONBOARDING_MAIN_PH3');
    public description3$: BehaviorSubject<string> = new BehaviorSubject('ONBOARDING_MAIN_DESC3');
    public placeholder4$: BehaviorSubject<string> = new BehaviorSubject('ONBOARDING_MAIN_PH4');
    public description4$: BehaviorSubject<string> = new BehaviorSubject('ONBOARDING_MAIN_DESC4');
    public splittedScreen$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(true);

    public setPlaceholder1(placeholder1: string): void {
        this.placeholder1$.next(placeholder1);
    }

    public setPlaceholder2(placeholder2: string): void {
        this.placeholder2$.next(placeholder2);
    }

    public setPlaceholder3(placeholder3: string): void {
        this.placeholder3$.next(placeholder3);
    }

    public setPlaceholder4(placeholder4: string): void {
        this.placeholder4$.next(placeholder4);
    }

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
