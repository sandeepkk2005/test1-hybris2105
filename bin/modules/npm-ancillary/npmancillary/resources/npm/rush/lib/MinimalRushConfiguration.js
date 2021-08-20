"use strict";
// Copyright (c) Microsoft Corporation. All rights reserved. Licensed under the MIT license.
// See LICENSE in the project root for license information.
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    Object.defineProperty(o, k2, { enumerable: true, get: function() { return m[k]; } });
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || function (mod) {
    if (mod && mod.__esModule) return mod;
    var result = {};
    if (mod != null) for (var k in mod) if (k !== "default" && Object.hasOwnProperty.call(mod, k)) __createBinding(result, mod, k);
    __setModuleDefault(result, mod);
    return result;
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.MinimalRushConfiguration = void 0;
const path = __importStar(require("path"));
const node_core_library_1 = require("@rushstack/node-core-library");
const rush_lib_1 = require("@microsoft/rush-lib");
const RushConstants_1 = require("@microsoft/rush-lib/lib/logic/RushConstants");
const Utilities_1 = require("@microsoft/rush-lib/lib/utilities/Utilities");
/**
 * Represents a minimal subset of the rush.json configuration file. It provides the information necessary to
 * decide which version of Rush should be installed/used.
 */
class MinimalRushConfiguration {
    constructor(minimalRushConfigurationJson, rushJsonFilename) {
        this._rushVersion =
            minimalRushConfigurationJson.rushVersion || minimalRushConfigurationJson.rushMinimumVersion;
        this._commonRushConfigFolder = path.join(path.dirname(rushJsonFilename), RushConstants_1.RushConstants.commonFolderName, 'config', 'rush');
    }
    static loadFromDefaultLocation() {
        const rushJsonLocation = rush_lib_1.RushConfiguration.tryFindRushJsonLocation({
            showVerbose: !Utilities_1.Utilities.shouldRestrictConsoleOutput()
        });
        if (rushJsonLocation) {
            return MinimalRushConfiguration._loadFromConfigurationFile(rushJsonLocation);
        }
        else {
            return undefined;
        }
    }
    static _loadFromConfigurationFile(rushJsonFilename) {
        try {
            const minimalRushConfigurationJson = node_core_library_1.JsonFile.load(rushJsonFilename);
            return new MinimalRushConfiguration(minimalRushConfigurationJson, rushJsonFilename);
        }
        catch (e) {
            return undefined;
        }
    }
    /**
     * The version of rush specified by the rushVersion property of the rush.json configuration file. If the
     *  rushVersion property is not specified, this falls back to the rushMinimumVersion property. This should be
     *  a semver style version number like "4.0.0"
     */
    get rushVersion() {
        return this._rushVersion;
    }
    /**
     * The folder where Rush's additional config files are stored.  This folder is always a
     * subfolder called "config\rush" inside the common folder.  (The "common\config" folder
     * is reserved for configuration files used by other tools.)  To avoid confusion or mistakes,
     * Rush will report an error if this this folder contains any unrecognized files.
     *
     * Example: "C:\MyRepo\common\config\rush"
     */
    get commonRushConfigFolder() {
        return this._commonRushConfigFolder;
    }
}
exports.MinimalRushConfiguration = MinimalRushConfiguration;
//# sourceMappingURL=MinimalRushConfiguration.js.map