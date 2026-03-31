export interface TrafficMetrics {
  startedAt: string;
  snapshotAt: string;
  uptimeSeconds: number;
  totalTransactions: number;
  successfulTransactions: number;
  declinedTransactions: number;
  errorTransactions: number;
  successRatePercent: number;
  averageLatencyMs: number;
  p95LatencyMs: number;
  minLatencyMs: number;
  maxLatencyMs: number;
  transactionsLastMinute: number;
  recentEventsCount: number;
}

export interface TrafficEvent {
  timestamp: string;
  requestMti: string | null;
  responseMti: string | null;
  stan: string | null;
  responseCode: string | null;
  status: string;
  latencyMs: number;
  errorMessage: string | null;
}
