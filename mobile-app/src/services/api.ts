import axios, { AxiosInstance, AxiosResponse } from 'axios';
import AsyncStorage from '@react-native-async-storage/async-storage';
import {
  User,
  Vehicle,
  Reservation,
  Payment,
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  CreateReservationRequest,
  PaymentRequest,
  VehicleFilters,
  ApiError
} from '../types';

class ApiService {
  private client: AxiosInstance;
  private baseURL = 'http://localhost:8083/api/v1'; // Development URL

  constructor() {
    this.client = axios.create({
      baseURL: this.baseURL,
      timeout: 10000,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    this.setupInterceptors();
  }

  private setupInterceptors(): void {
    // Request interceptor to add auth token
    this.client.interceptors.request.use(
      async (config) => {
        const token = await AsyncStorage.getItem('access_token');
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      },
      (error) => Promise.reject(error)
    );

    // Response interceptor for error handling
    this.client.interceptors.response.use(
      (response) => response,
      async (error) => {
        if (error.response?.status === 401) {
          // Token expired, clear storage and redirect to login
          await AsyncStorage.multiRemove(['access_token', 'user_data']);
          // Navigation to login should be handled by the auth store
        }
        return Promise.reject(this.handleError(error));
      }
    );
  }

  private handleError(error: any): ApiError {
    if (error.response) {
      return {
        message: error.response.data?.message || 'Server error occurred',
        code: error.response.status.toString(),
        details: error.response.data,
      };
    } else if (error.request) {
      return {
        message: 'Network error - please check your connection',
        code: 'NETWORK_ERROR',
      };
    } else {
      return {
        message: error.message || 'An unexpected error occurred',
        code: 'UNKNOWN_ERROR',
      };
    }
  }

  // Authentication endpoints
  async login(credentials: LoginRequest): Promise<LoginResponse> {
    const response: AxiosResponse<LoginResponse> = await this.client.post('/auth/login', credentials);
    return response.data;
  }

  async register(userData: RegisterRequest): Promise<User> {
    const response: AxiosResponse<User> = await this.client.post('/auth/register', userData);
    return response.data;
  }

  async logout(): Promise<void> {
    await this.client.post('/auth/logout');
    await AsyncStorage.multiRemove(['access_token', 'user_data']);
  }

  async refreshToken(): Promise<LoginResponse> {
    const response: AxiosResponse<LoginResponse> = await this.client.post('/auth/refresh');
    return response.data;
  }

  // Vehicle endpoints
  async getVehicles(filters?: VehicleFilters): Promise<Vehicle[]> {
    const response: AxiosResponse<Vehicle[]> = await this.client.get('/vehicles', {
      params: filters,
    });
    return response.data;
  }

  async getVehicle(id: number): Promise<Vehicle> {
    const response: AxiosResponse<Vehicle> = await this.client.get(`/vehicles/${id}`);
    return response.data;
  }

  async getAvailableVehicles(startDate: string, endDate: string): Promise<Vehicle[]> {
    const response: AxiosResponse<Vehicle[]> = await this.client.get('/vehicles/available', {
      params: { startDate, endDate },
    });
    return response.data;
  }

  // Reservation endpoints
  async getReservations(): Promise<Reservation[]> {
    const response: AxiosResponse<Reservation[]> = await this.client.get('/reservations');
    return response.data;
  }

  async getReservation(id: number): Promise<Reservation> {
    const response: AxiosResponse<Reservation> = await this.client.get(`/reservations/${id}`);
    return response.data;
  }

  async createReservation(reservationData: CreateReservationRequest): Promise<Reservation> {
    const response: AxiosResponse<Reservation> = await this.client.post('/reservations', reservationData);
    return response.data;
  }

  async updateReservation(id: number, reservationData: Partial<CreateReservationRequest>): Promise<Reservation> {
    const response: AxiosResponse<Reservation> = await this.client.put(`/reservations/${id}`, reservationData);
    return response.data;
  }

  async cancelReservation(id: number, reason?: string): Promise<Reservation> {
    const response: AxiosResponse<Reservation> = await this.client.put(`/reservations/${id}/cancel`, { reason });
    return response.data;
  }

  async confirmReservation(id: number): Promise<Reservation> {
    const response: AxiosResponse<Reservation> = await this.client.put(`/reservations/${id}/confirm`);
    return response.data;
  }

  // Payment endpoints
  async createPaymentIntent(paymentData: PaymentRequest): Promise<any> {
    const response: AxiosResponse<any> = await this.client.post('/payments/create-payment-intent', paymentData);
    return response.data;
  }

  async confirmPayment(paymentIntentId: string): Promise<Payment> {
    const response: AxiosResponse<Payment> = await this.client.post('/payments/confirm', null, {
      params: { paymentIntentId },
    });
    return response.data;
  }

  async getPayments(): Promise<Payment[]> {
    const response: AxiosResponse<Payment[]> = await this.client.get('/payments');
    return response.data;
  }

  async getUserPayments(userId: number): Promise<Payment[]> {
    const response: AxiosResponse<Payment[]> = await this.client.get(`/payments/user/${userId}`);
    return response.data;
  }

  // User profile endpoints
  async getCurrentUser(): Promise<User> {
    const response: AxiosResponse<User> = await this.client.get('/auth/me');
    return response.data;
  }

  async updateProfile(userData: Partial<User>): Promise<User> {
    const response: AxiosResponse<User> = await this.client.put('/auth/profile', userData);
    return response.data;
  }

  async changePassword(oldPassword: string, newPassword: string): Promise<void> {
    await this.client.put('/auth/change-password', { oldPassword, newPassword });
  }

  // Dashboard/Analytics endpoints (for admin users)
  async getDashboardKPIs(): Promise<any> {
    const response: AxiosResponse<any> = await this.client.get('/dashboard/kpis');
    return response.data;
  }

  async getAnalytics(type: string, params?: any): Promise<any> {
    const response: AxiosResponse<any> = await this.client.get(`/analytics/${type}`, { params });
    return response.data;
  }

  // Utility methods
  async uploadImage(imageUri: string, type: 'profile' | 'vehicle'): Promise<string> {
    const formData = new FormData();
    formData.append('image', {
      uri: imageUri,
      type: 'image/jpeg',
      name: 'image.jpg',
    } as any);

    const response: AxiosResponse<{ url: string }> = await this.client.post(`/upload/${type}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });

    return response.data.url;
  }

  // Push notification registration
  async registerPushToken(token: string): Promise<void> {
    await this.client.post('/notifications/register-token', { token });
  }

  async updateNotificationPreferences(preferences: {
    emailNotificationsEnabled: boolean;
    smsNotificationsEnabled: boolean;
    pushNotificationsEnabled: boolean;
  }): Promise<void> {
    await this.client.put('/auth/notification-preferences', preferences);
  }
}

export const apiService = new ApiService();
export default apiService;