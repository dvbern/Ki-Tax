import {Environment} from './IEnvironment';
import {LogLevel} from '../app/core/logging/log-level';

// tslint:disable-next-line:naming-convention
export const environment: Environment = {
    production: false,
    test: true,
    hmr: false,
    logLevel: LogLevel.WARN,
    logColorsEnabled: false
};
