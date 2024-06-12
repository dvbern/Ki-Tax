import {LogLevel} from '../app/core/logging/log-level';
import {Environment} from './IEnvironment';

export const environment: Environment = {
    production: false,
    test: true,
    hmr: false,
    logLevel: LogLevel.WARN,
    logColorsEnabled: false
};
