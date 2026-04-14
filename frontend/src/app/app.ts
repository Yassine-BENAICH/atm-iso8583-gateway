import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, forkJoin, interval, of, startWith, switchMap } from 'rxjs';
import { MonitoringApiService } from './monitoring-api.service';
import { TrafficEvent, TrafficMetrics } from './monitoring-api.models';

type StatusBreakdown = { key: string; count: number; ratio: number };
type MtiBreakdown = { mti: string; count: number; ratio: number };
type FailureBreakdown = { label: string; count: number; ratio: number; cssClass: string };

@Component({
  selector: 'app-root',
  imports: [CommonModule, DecimalPipe],
  providers: [DatePipe],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  private readonly monitoringApi = inject(MonitoringApiService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly datePipe = inject(DatePipe);

  readonly metrics = signal<TrafficMetrics | null>(null);
  readonly events = signal<TrafficEvent[]>([]);
  readonly errorEvents = signal<TrafficEvent[]>([]);
  readonly lastUpdated = signal<Date | null>(null);
  readonly loading = signal(true);
  readonly connectionError = signal<string | null>(null);

  readonly statusBreakdown = computed<StatusBreakdown[]>(() => {
    const source = this.events();
    if (source.length === 0) {
      return [];
    }

    const counts = source.reduce<Map<string, number>>((acc, event) => {
      const key = event.status || 'UNKNOWN';
      acc.set(key, (acc.get(key) ?? 0) + 1);
      return acc;
    }, new Map<string, number>());

    return [...counts.entries()]
      .map(([key, count]) => ({ key, count, ratio: (count / source.length) * 100 }))
      .sort((left, right) => right.count - left.count);
  });

  readonly mtiBreakdown = computed<MtiBreakdown[]>(() => {
    const source = this.events();
    if (source.length === 0) {
      return [];
    }

    const counts = source.reduce<Map<string, number>>((acc, event) => {
      const key = event.requestMti || 'UNKNOWN';
      acc.set(key, (acc.get(key) ?? 0) + 1);
      return acc;
    }, new Map<string, number>());

    return [...counts.entries()]
      .map(([mti, count]) => ({ mti, count, ratio: (count / source.length) * 100 }))
      .sort((left, right) => right.count - left.count)
      .slice(0, 6);
  });

  readonly latencyBars = computed<number[]>(() => {
    const sample = this.events()
      .slice(0, 24)
      .map((event) => event.latencyMs)
      .reverse();

    const peak = sample.length > 0 ? Math.max(...sample, 1) : 1;
    return sample.map((value) => Math.max(6, Math.round((value / peak) * 100)));
  });

  readonly failureBreakdown = computed<FailureBreakdown[]>(() => {
    const snapshot = this.metrics();
    if (!snapshot || snapshot.totalTransactions <= 0) {
      return [];
    }

    const total = snapshot.totalTransactions;
    return [
      {
        label: 'Declined',
        count: snapshot.declinedTransactions,
        ratio: (snapshot.declinedTransactions / total) * 100,
        cssClass: 'declined'
      },
      {
        label: 'Errors',
        count: snapshot.errorTransactions,
        ratio: (snapshot.errorTransactions / total) * 100,
        cssClass: 'error'
      }
    ].filter((item) => item.count > 0);
  });

  readonly impactedTransactions = computed<number>(() => {
    const snapshot = this.metrics();
    if (!snapshot) {
      return 0;
    }
    return snapshot.declinedTransactions + snapshot.errorTransactions;
  });

  readonly slowestEvent = computed<TrafficEvent | null>(() => {
    return this.events().reduce<TrafficEvent | null>((slowest, current) => {
      if (!slowest || current.latencyMs > slowest.latencyMs) {
        return current;
      }
      return slowest;
    }, null);
  });

  readonly performanceLabel = computed<string>(() => {
    const p95 = this.metrics()?.p95LatencyMs ?? 0;
    if (p95 <= 120) {
      return 'Fast lane';
    }
    if (p95 <= 250) {
      return 'Stable throughput';
    }
    return 'Latency pressure';
  });

  readonly p95Pressure = computed<number>(() => {
    const p95 = this.metrics()?.p95LatencyMs ?? 0;
    return Math.max(0, Math.min(100, Math.round((p95 / 500) * 100)));
  });

  readonly successTier = computed<string>(() => {
    const successRate = this.metrics()?.successRatePercent ?? 0;
    if (successRate >= 98) {
      return 'excellent';
    }
    if (successRate >= 92) {
      return 'steady';
    }
    return 'watch';
  });

  constructor() {
    interval(4000)
      .pipe(
        startWith(0),
        switchMap(() =>
          forkJoin({
            metrics: this.monitoringApi.getMetrics(),
            events: this.monitoringApi.getEvents(40),
            errors: this.monitoringApi.getErrors(20)
          })
        ),
        catchError((error: unknown) => {
          this.connectionError.set(this.resolveErrorMessage(error));
          this.loading.set(false);
          return of(null);
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((payload) => {
        if (!payload) {
          return;
        }

        this.metrics.set(payload.metrics);
        this.events.set(payload.events);
        this.errorEvents.set(payload.errors);
        this.lastUpdated.set(new Date());
        this.connectionError.set(null);
        this.loading.set(false);
      });
  }

  statusClass(status: string | null | undefined): string {
    const normalized = (status ?? '').toUpperCase();
    if (normalized === 'SUCCESS') {
      return 'pill pill-success';
    }
    if (normalized === 'DECLINED') {
      return 'pill pill-declined';
    }
    return 'pill pill-error';
  }

  toDisplayTime(value: string | Date | null | undefined): string {
    if (!value) {
      return '-';
    }
    return this.datePipe.transform(value, 'HH:mm:ss') ?? '-';
  }

  uptimeText(): string {
    const total = this.metrics()?.uptimeSeconds ?? 0;
    const hours = Math.floor(total / 3600);
    const minutes = Math.floor((total % 3600) / 60);
    const seconds = total % 60;
    return `${hours.toString().padStart(2, '0')}:${minutes
      .toString()
      .padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
  }

  successTierLabel(): string {
    const tier = this.successTier();
    if (tier === 'excellent') {
      return 'Excellent';
    }
    if (tier === 'steady') {
      return 'Steady';
    }
    return 'Watch';
  }

  private resolveErrorMessage(error: unknown): string {
    if (typeof error === 'string') {
      return error;
    }
    if (error && typeof error === 'object' && 'message' in error) {
      const message = (error as { message?: unknown }).message;
      if (typeof message === 'string' && message.trim().length > 0) {
        return message;
      }
    }
    return 'Cannot reach monitoring API';
  }
}
