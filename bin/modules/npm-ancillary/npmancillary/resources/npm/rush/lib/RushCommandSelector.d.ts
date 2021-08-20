import * as rushLib from '@microsoft/rush-lib';
/**
 * Both "rush" and "rushx" share the same src/start.ts entry point.  This makes it
 * a little easier for them to share all the same startup checks and version selector
 * logic.  RushCommandSelector looks at argv to determine whether we're doing "rush"
 * or "rushx" behavior, and then invokes the appropriate entry point in the selected
 * @microsoft/rush-lib.
 */
export declare class RushCommandSelector {
    static failIfNotInvokedAsRush(version: string): void;
    static execute(launcherVersion: string, selectedRushLib: any, options: rushLib.ILaunchOptions): void;
    private static _failWithError;
    private static _getCommandName;
}
//# sourceMappingURL=RushCommandSelector.d.ts.map