/**
 * Represents a minimal subset of the rush.json configuration file. It provides the information necessary to
 * decide which version of Rush should be installed/used.
 */
export declare class MinimalRushConfiguration {
    private _rushVersion;
    private _commonRushConfigFolder;
    private constructor();
    static loadFromDefaultLocation(): MinimalRushConfiguration | undefined;
    private static _loadFromConfigurationFile;
    /**
     * The version of rush specified by the rushVersion property of the rush.json configuration file. If the
     *  rushVersion property is not specified, this falls back to the rushMinimumVersion property. This should be
     *  a semver style version number like "4.0.0"
     */
    get rushVersion(): string;
    /**
     * The folder where Rush's additional config files are stored.  This folder is always a
     * subfolder called "config\rush" inside the common folder.  (The "common\config" folder
     * is reserved for configuration files used by other tools.)  To avoid confusion or mistakes,
     * Rush will report an error if this this folder contains any unrecognized files.
     *
     * Example: "C:\MyRepo\common\config\rush"
     */
    get commonRushConfigFolder(): string;
}
//# sourceMappingURL=MinimalRushConfiguration.d.ts.map