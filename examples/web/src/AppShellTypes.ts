import type { LaunchPrompt } from "./about/runAppUpdates";
import type { DonationConfig } from "./about/types";

export type AppMode = "news" | "podcast" | "booru";

export type AppShellState = {
  showAbout: boolean;
  showSettings: boolean;
  updateStatus: string;
  donations: DonationConfig;
  mode: AppMode;
  launchPrompt?: LaunchPrompt | null;
};

export type AppShellCallbacks = {
  onState: (next: Partial<AppShellState>) => void;
  onUpdateCheckChange?: (enabled: boolean) => void;
  onLaunchAction?: (action: "donate" | "not-now" | "install" | "later") => void;
  onApplyUpdate?: () => void;
  canApplyUpdate?: boolean;
};
