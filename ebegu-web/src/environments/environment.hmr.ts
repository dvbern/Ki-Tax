import {LogLevel} from '../app/core/logging/log-level';
import {Environment} from './IEnvironment';

export const environment: Environment = {
    production: false,
    test: false,
    hmr: true,
    logLevel: LogLevel.INFO,
    logModules: {},
};
