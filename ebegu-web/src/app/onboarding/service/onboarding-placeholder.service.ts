import { Injectable } from '@angular/core';
import {BehaviorSubject} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class OnboardingPlaceholderService {
    public placeholder1: BehaviorSubject<string> = new BehaviorSubject('ONBOARDING_MAIN_PH1');
    public description1: BehaviorSubject<string> = new BehaviorSubject('ONBOARDING_MAIN_DESC1');
    public placeholder2: BehaviorSubject<string> = new BehaviorSubject('ONBOARDING_MAIN_PH2');
    public description2: BehaviorSubject<string> = new BehaviorSubject('ONBOARDING_MAIN_DESC2');
    public placeholder3: BehaviorSubject<string> = new BehaviorSubject('ONBOARDING_MAIN_PH3');
    public description3: BehaviorSubject<string> = new BehaviorSubject('ONBOARDING_MAIN_DESC3');
    public placeholder4: BehaviorSubject<string> = new BehaviorSubject('ONBOARDING_MAIN_PH4');
    public description4: BehaviorSubject<string> = new BehaviorSubject('ONBOARDING_MAIN_DESC4');

    public setPlaceholder1(placeholder1: string){
        this.placeholder1.next(placeholder1);
    }

    public setPlaceholder2(placeholder2: string){
        this.placeholder2.next(placeholder2);
    }

    public setPlaceholder3(placeholder3: string){
        this.placeholder3.next(placeholder3);
    }

    public setPlaceholder4(placeholder4: string){
        this.placeholder4.next(placeholder4);
    }

    public setDescription1(description1: string){
        this.description1.next(description1);
    }

    public setDescription2(description2: string){
        this.description2.next(description2);
    }

    public setDescription3(description3: string){
        this.description3.next(description3);
    }

    public setDescription4(description4: string){
        this.description4.next(description4);
    }
}
