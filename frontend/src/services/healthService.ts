// src/services/healthService.ts
import { API_BASE_URL } from "../config/api";

interface ServiceStatus {
  status: string;
  serviceName: string;
  timestamp: number;
  responseTime?: number;
  error?: string;
  backendStatus?: ServiceStatus; // For gateway to include backend status
}

interface ServiceHealthResponse {
  mainBackend: ServiceStatus;
  gateway: ServiceStatus;
  frontend: ServiceStatus;
  database: ServiceStatus;
  cashFlowService: ServiceStatus;
  positionsService: ServiceStatus;
  systemHealth: number; // percentage
  servicesOnline: number;
  totalServices: number;
  avgResponse: number;
  timestamp: number;
}

const healthService = {
  // Helper to create mock service status for demonstration
  mockServiceStatus(serviceName: string, isUp: boolean): ServiceStatus {
    // For services that should always be up, generate response times in a good range (20-60ms)
    const responseTime = isUp ? Math.floor(Math.random() * 40) + 20 : 0;
    return {
      status: isUp ? 'UP' : 'DOWN',
      serviceName,
      timestamp: Date.now(),
      responseTime,
      error: isUp ? undefined : 'Service unavailable'
    };
  },
  
  async checkServiceHealth(url: string, serviceName: string): Promise<ServiceStatus> {
    const startTime = performance.now();
    try {
      // Try multiple health endpoints in case one is available
      const endpoints = [
        '/actuator/health',
        '/health',
        '/api/health',
        '/api/health/status'
      ];
      
      let response = null;
      let error = null;
      
      // Try each endpoint until one works
      for (const endpoint of endpoints) {
        try {
          console.log(`Trying health endpoint for ${serviceName}: ${url}${endpoint}`);
          
          // Add a timeout to the fetch to avoid hanging
          const controller = new AbortController();
          const timeoutId = setTimeout(() => controller.abort(), 3000); // 3 second timeout
          
          response = await fetch(`${url}${endpoint}`, {
            method: 'GET',
            headers: {
              'Content-Type': 'application/json',
            },
            signal: controller.signal
          });
          
          clearTimeout(timeoutId);
          
          if (response.ok) {
            console.log(`Health endpoint ${endpoint} for ${serviceName} successful`);
            break;
          } else {
            console.log(`Health endpoint ${endpoint} for ${serviceName} returned ${response.status}`);
          }
        } catch (e) {
          error = e;
          console.log(`Health endpoint ${endpoint} for ${serviceName} failed:`, e);
        }
      }

      if (!response || !response.ok) {
        throw error || new Error(`${serviceName} returned ${response?.status || 'unknown error'}`);
      }

      const data = await response.json();
      const endTime = performance.now();
      
      return {
        ...data,
        status: data.status || 'UP', // Use status from response if available, otherwise default to UP
        serviceName: data.serviceName || serviceName, // Use service name from response if available
        timestamp: data.timestamp || Date.now(),
        responseTime: Math.round(endTime - startTime),
      };
    } catch (error) {
      console.error(`Health check failed for ${serviceName} (${url}):`, error);
      const endTime = performance.now();
      
      return {
        status: 'DOWN',
        serviceName,
        timestamp: Date.now(),
        responseTime: Math.round(endTime - startTime),
        error: error instanceof Error ? error.message : String(error),
      };
    }
  },

  async getSystemHealth(): Promise<ServiceHealthResponse> {
    // Extract base URL from API_BASE_URL (removing /api if present)
    const apiBaseWithoutApi = API_BASE_URL.replace(/\/api$/, '');
    const gatewayUrl = apiBaseWithoutApi; // Use the configured API base URL for gateway
    const frontendUrl = window.location.origin;

    console.log(`Checking health using gateway: ${gatewayUrl}`);

    // Only check gateway health - the gateway will internally check backend health
    // and return a combined status that includes all services
    const gatewayStatus = await this.checkServiceHealth(gatewayUrl, 'gateway').catch(err => {
      console.error('Gateway health check failed:', err);
      return {
        status: 'DOWN',
        serviceName: 'gateway',
        timestamp: Date.now(),
        responseTime: 0,
        error: err instanceof Error ? err.message : String(err)
      } as ServiceStatus;
    });

    // For frontend, we know it's running if we're executing this code
    const frontendStatus: ServiceStatus = {
      status: 'UP',
      serviceName: 'frontend',
      timestamp: Date.now(),
      responseTime: 0
    };
    
    // Extract backend status from gateway response if available, otherwise create a mock
    let backendStatus: ServiceStatus;
    
    if (gatewayStatus.backendStatus) {
      // Use the backend status provided by the gateway
      backendStatus = gatewayStatus.backendStatus as ServiceStatus;
    } else {
      // If gateway doesn't provide backend status, create a mock based on gateway status
      // If gateway is down, backend is likely down too
      backendStatus = this.mockServiceStatus('backend', gatewayStatus.status === 'UP');
    }
    
    // Mock services - in a real application these would be actual health checks from gateway
    // PostgreSQL Database - always UP as requested
    const databaseStatus: ServiceStatus = this.mockServiceStatus('database', true);
    
    // Cash Flow Service - always UP as requested
    const cashFlowStatus: ServiceStatus = this.mockServiceStatus('cash-flow-service', true);
    
    // Positions Service - always UP as requested
    const positionsStatus: ServiceStatus = this.mockServiceStatus('positions-service', true);    // List of all services to calculate metrics
    const allServices = [
      backendStatus, 
      gatewayStatus, 
      frontendStatus,
      databaseStatus,
      cashFlowStatus,
      positionsStatus
    ];
    
    // Calculate system health metrics
    const servicesUp = allServices.filter(s => s.status === 'UP').length;
    const totalServices = allServices.length;
    const systemHealth = Math.round((servicesUp / totalServices) * 100);
    
    // Calculate average response time (only for services that have response times)
    const responseTimes = allServices
      .filter(s => s.responseTime !== undefined && s.responseTime > 0)
      .map(s => s.responseTime as number);
      
    const avgResponse = responseTimes.length > 0
      ? Math.round(responseTimes.reduce((sum, time) => sum + time, 0) / responseTimes.length)
      : 0;

    return {
      mainBackend: backendStatus,
      gateway: gatewayStatus,
      frontend: frontendStatus,
      database: databaseStatus,
      cashFlowService: cashFlowStatus,
      positionsService: positionsStatus,
      systemHealth,
      servicesOnline: servicesUp,
      totalServices,
      avgResponse,
      timestamp: Date.now(),
    };
  },
};

export { healthService, type ServiceHealthResponse, type ServiceStatus };