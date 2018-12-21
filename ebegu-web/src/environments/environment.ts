import {LogLevel} from '../app/core/logging/log-level';
import {Environment} from './IEnvironment';

// tslint:disable-next-line:naming-convention
export const environment: Environment = {
    production: false,
    test: false,
    hmr: false,
    logLevel: LogLevel.INFO,
    logModules: {},
};
