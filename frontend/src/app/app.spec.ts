import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { App } from './app';
import { MonitoringApiService } from './monitoring-api.service';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        {
          provide: MonitoringApiService,
          useValue: {
            getMetrics: () =>
              of({
                startedAt: new Date().toISOString(),
                snapshotAt: new Date().toISOString(),
                uptimeSeconds: 12,
                totalTransactions: 10,
                successfulTransactions: 8,
                declinedTransactions: 1,
                errorTransactions: 1,
                successRatePercent: 80,
                averageLatencyMs: 110,
                p95LatencyMs: 140,
                minLatencyMs: 80,
                maxLatencyMs: 200,
                transactionsLastMinute: 4,
                recentEventsCount: 10
              }),
            getEvents: () =>
              of([
                {
                  timestamp: new Date().toISOString(),
                  requestMti: '0200',
                  responseMti: '0210',
                  stan: '000001',
                  responseCode: '00',
                  status: 'SUCCESS',
                  latencyMs: 120,
                  errorMessage: null
                }
              ]),
            getErrors: () => of([])
          }
        }
      ]
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should render dashboard title', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h1')?.textContent).toContain('Monitoring Dashboard');
  });
});
