import { API_BASE_URL } from '../config/api';

interface DemoStatus {
  running: boolean;
  mode: string;
  tradeCount: number;
}

class DemoService {
  /**
   * Get the current status of the demo trade generator
   */
  async getStatus(): Promise<DemoStatus> {
    try {
      console.log(`Calling ${API_BASE_URL}/admin/auto-trades/status`);
      const response = await fetch(`${API_BASE_URL}/admin/auto-trades/status`);
      
      console.log('Status response:', response.status);
      
      if (!response.ok) {
        const errorText = await response.text();
        console.error('Error response from status:', errorText);
        throw new Error(`HTTP error! status: ${response.status}, message: ${errorText}`);
      }
      
      const data = await response.json();
      console.log('Status data:', data);
      return data;
    } catch (error) {
      console.error('Error fetching demo status:', error);
      throw error;
    }
  }

  /**
   * Set the demo trade generator mode
   * @param mode - The mode to set: 'start', 'stop', 'backdated-mode', or 'regular-mode'
   */
  async setMode(mode: string): Promise<string> {
    try {
      console.log(`Calling ${API_BASE_URL}/admin/auto-trades/${mode}`);
      const response = await fetch(`${API_BASE_URL}/admin/auto-trades/${mode}`, {
        method: 'POST'
      });
      
      console.log('Response status:', response.status);
      
      if (!response.ok) {
        const errorText = await response.text();
        console.error('Error response:', errorText);
        throw new Error(`Failed to set demo mode to ${mode}: ${response.status} ${errorText}`);
      }
      
      const responseText = await response.text();
      console.log('Success response:', responseText);
      return responseText;
    } catch (error) {
      console.error(`Error setting demo mode to ${mode}:`, error);
      const errorMessage = error instanceof Error ? error.message : String(error);
      alert(`Failed to set demo mode: ${errorMessage}. Check console for details.`);
      throw error;
    }
  }
  
  /**
   * Start the demo trade generator
   */
  async start(): Promise<string> {
    return this.setMode('start');
  }
  
  /**
   * Stop the demo trade generator
   */
  async stop(): Promise<string> {
    return this.setMode('stop');
  }
  
  /**
   * Set the demo trade generator to backdated mode (trades settle today)
   */
  async enableBackdatedMode(): Promise<string> {
    return this.setMode('backdated-mode');
  }
  
  /**
   * Set the demo trade generator to regular mode (trades settle T+2)
   */
  async enableRegularMode(): Promise<string> {
    return this.setMode('regular-mode');
  }

  /**
   * Set the demo trade generator to stopped mode (no trade generation)
   */
  async enableStoppedMode(): Promise<string> {
    return this.setMode('stopped-mode');
  }
}

export const demoService = new DemoService();