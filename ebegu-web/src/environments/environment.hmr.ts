import {LogLevel} from '../app/core/logging/log-level';
import {Environment} from './IEnvironment';

// tslint:disable-next-line:naming-convention
export const environment: Environment = {
    production: false,
    test: false,
    hmr: true,
    logLevel: LogLevel.INFO,
    logModules: {},
};
