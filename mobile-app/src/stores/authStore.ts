import { create } from 'zustand';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { User, LoginRequest, RegisterRequest, ApiError } from '../types';
import apiService from '../services/api';

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;

  // Actions
  login: (credentials: LoginRequest) => Promise<void>;
  register: (userData: RegisterRequest) => Promise<void>;
  logout: () => Promise<void>;
  initializeAuth: () => Promise<void>;
  clearError: () => void;
  updateProfile: (userData: Partial<User>) => Promise<void>;
  changePassword: (oldPassword: string, newPassword: string) => Promise<void>;
}

export const useAuthStore = create<AuthState>((set, get) => ({
  user: null,
  isAuthenticated: false,
  isLoading: false,
  error: null,

  login: async (credentials: LoginRequest) => {
    set({ isLoading: true, error: null });

    try {
      const response = await apiService.login(credentials);

      // Store token and user data
      await AsyncStorage.setItem('access_token', response.accessToken);
      await AsyncStorage.setItem('user_data', JSON.stringify(response.user));

      set({
        user: response.user,
        isAuthenticated: true,
        isLoading: false,
        error: null,
      });
    } catch (error: any) {
      const apiError = error as ApiError;
      set({
        error: apiError.message,
        isLoading: false,
        isAuthenticated: false,
        user: null,
      });
      throw error;
    }
  },

  register: async (userData: RegisterRequest) => {
    set({ isLoading: true, error: null });

    try {
      const user = await apiService.register(userData);

      // After successful registration, automatically log in
      await get().login({
        username: userData.username,
        password: userData.password,
      });
    } catch (error: any) {
      const apiError = error as ApiError;
      set({
        error: apiError.message,
        isLoading: false,
      });
      throw error;
    }
  },

  logout: async () => {
    set({ isLoading: true });

    try {
      await apiService.logout();
    } catch (error) {
      // Continue with logout even if API call fails
      console.warn('Logout API call failed:', error);
    } finally {
      // Clear local storage and state
      await AsyncStorage.multiRemove(['access_token', 'user_data']);
      set({
        user: null,
        isAuthenticated: false,
        isLoading: false,
        error: null,
      });
    }
  },

  initializeAuth: async () => {
    set({ isLoading: true });

    try {
      const token = await AsyncStorage.getItem('access_token');
      const userData = await AsyncStorage.getItem('user_data');

      if (token && userData) {
        const user = JSON.parse(userData);

        // Verify token is still valid by fetching current user
        try {
          const currentUser = await apiService.getCurrentUser();
          set({
            user: currentUser,
            isAuthenticated: true,
            isLoading: false,
          });
        } catch (error) {
          // Token is invalid, clear storage
          await AsyncStorage.multiRemove(['access_token', 'user_data']);
          set({
            user: null,
            isAuthenticated: false,
            isLoading: false,
          });
        }
      } else {
        set({
          user: null,
          isAuthenticated: false,
          isLoading: false,
        });
      }
    } catch (error) {
      console.error('Auth initialization failed:', error);
      set({
        user: null,
        isAuthenticated: false,
        isLoading: false,
        error: 'Failed to initialize authentication',
      });
    }
  },

  clearError: () => {
    set({ error: null });
  },

  updateProfile: async (userData: Partial<User>) => {
    const currentUser = get().user;
    if (!currentUser) throw new Error('No user logged in');

    set({ isLoading: true, error: null });

    try {
      const updatedUser = await apiService.updateProfile(userData);

      // Update stored user data
      await AsyncStorage.setItem('user_data', JSON.stringify(updatedUser));

      set({
        user: updatedUser,
        isLoading: false,
      });
    } catch (error: any) {
      const apiError = error as ApiError;
      set({
        error: apiError.message,
        isLoading: false,
      });
      throw error;
    }
  },

  changePassword: async (oldPassword: string, newPassword: string) => {
    set({ isLoading: true, error: null });

    try {
      await apiService.changePassword(oldPassword, newPassword);
      set({ isLoading: false });
    } catch (error: any) {
      const apiError = error as ApiError;
      set({
        error: apiError.message,
        isLoading: false,
      });
      throw error;
    }
  },
}));