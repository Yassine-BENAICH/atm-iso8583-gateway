import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { TrafficEvent, TrafficMetrics } from './monitoring-api.models';

@Injectable({ providedIn: 'root' })
export class MonitoringApiService {
  private readonly http = inject(HttpClient);

  getMetrics(): Observable<TrafficMetrics> {
    return this.http.get<TrafficMetrics>('/api/monitoring/metrics');
  }

  getEvents(limit = 40): Observable<TrafficEvent[]> {
    return this.http.get<TrafficEvent[]>(`/api/monitoring/events?limit=${limit}`);
  }

  getErrors(limit = 20): Observable<TrafficEvent[]> {
    return this.http.get<TrafficEvent[]>(`/api/monitoring/errors?limit=${limit}`);
  }
}
