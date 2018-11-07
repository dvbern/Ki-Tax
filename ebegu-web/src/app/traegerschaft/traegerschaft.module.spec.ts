import { TraegerschaftModule } from './traegerschaft.module';

describe('TraegerschaftModule', () => {
  let traegerschaftModule: TraegerschaftModule;

  beforeEach(() => {
    traegerschaftModule = new TraegerschaftModule();
  });

  it('should create an instance', () => {
    expect(traegerschaftModule).toBeTruthy();
  });
});
