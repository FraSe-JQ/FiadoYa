import { Injectable, inject } from "@angular/core";
import { HttpClient, HttpHeaders } from "@angular/common/http";
import { firstValueFrom } from "rxjs";
@Injectable({ providedIn: "root" })
export class ApiService {
  private http = inject(HttpClient);
  get<T = any>(path: string): Promise<T> {
    return firstValueFrom(this.http.get<T>("/api" + path));
  }
  post<T = any>(path: string, body: unknown, key?: string): Promise<T> {
    return firstValueFrom(
      this.http.post<T>("/api" + path, body, {
        headers: key ? new HttpHeaders({ "Idempotency-Key": key }) : undefined,
      }),
    );
  }
  put<T = any>(path: string, body: unknown): Promise<T> {
    return firstValueFrom(this.http.put<T>("/api" + path, body));
  }
}
