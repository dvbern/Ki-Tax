import {Environment} from './IEnvironment';
import {LogLevel} from '../app/core/logging/log-level';

export const environment: Environment = {
    production: false,
    test: true,
    hmr: false,
    logLevel: LogLevel.WARN,
    logColorsEnabled: false
};
