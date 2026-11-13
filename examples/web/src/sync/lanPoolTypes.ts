export type LanPoolSub = {
  url: string;
  title: string;
  kind: string;
  tag?: string;
  updatedAt: number;
};

export type LanPoolDocument = {
  version: number;
  updatedAt: number;
  subs: LanPoolSub[];
  reads: Array<{ url: string; read: boolean; updatedAt: number }>;
  stars: Array<{ url: string; starred: boolean; updatedAt: number }>;
  playback: Array<{ url: string; positionMs: number; updatedAt: number }>;
};
