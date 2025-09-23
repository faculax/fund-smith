import { API_BASE_URL } from '../config/api';
import { NavSnapshot } from '../types/nav';

class NavService {
  private base = `${API_BASE_URL}/nav`;

  /**
   * Trigger a NAV calculation on the backend.
   * Returns the saved snapshot (including id).
   */
  async calculate(portfolioId: string = 'DEFAULT'): Promise<NavSnapshot> {
    const url = `${this.base}/calculate?portfolioId=${encodeURIComponent(portfolioId)}`;
    const res = await fetch(url, { method: 'POST' });
    if (!res.ok) {
      const text = await res.text();
      throw new Error(`Failed to calculate NAV: ${res.status} ${text}`);
    }
    return (await res.json()) as NavSnapshot;
  }

  /**
   * Fetch the latest NAV snapshot for a portfolio.
   * If none exists the backend returns 404 which we surface as null.
   */
  async getLatest(portfolioId: string = 'DEFAULT'): Promise<NavSnapshot | null> {
    const url = `${this.base}/latest?portfolioId=${encodeURIComponent(portfolioId)}`;
    const res = await fetch(url);
    if (res.status === 404) return null;
    if (!res.ok) {
      const text = await res.text();
      throw new Error(`Failed to get latest NAV: ${res.status} ${text}`);
    }
    return (await res.json()) as NavSnapshot;
  }

  /**
   * Fetch newest-first NAV history. Limit enforced client-side; the backend
   * expects /history?limit=N to apply DB-level limit.
   */
  async getHistory(portfolioId: string = 'DEFAULT', limit = 30): Promise<NavSnapshot[]> {
    const url = `${this.base}/history?portfolioId=${encodeURIComponent(portfolioId)}&limit=${limit}`;
    const res = await fetch(url);
    if (!res.ok) {
      const text = await res.text();
      throw new Error(`Failed to get NAV history: ${res.status} ${text}`);
    }
    return (await res.json()) as NavSnapshot[];
  }
}

export const navService = new NavService();