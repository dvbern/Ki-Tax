import {TraegerschaftRoutingModule} from './traegerschaft-routing.module';

describe('TraegerschaftRoutingModule', () => {
    let traegerschaftRoutingModule: TraegerschaftRoutingModule;

    beforeEach(() => {
        traegerschaftRoutingModule = new TraegerschaftRoutingModule();
    });

    it('should create an instance', () => {
        expect(traegerschaftRoutingModule).toBeTruthy();
    });
});
