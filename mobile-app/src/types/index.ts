// User Types
export interface User {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  phoneNumber?: string;
  emailNotificationsEnabled: boolean;
  smsNotificationsEnabled: boolean;
  pushNotificationsEnabled: boolean;
  deviceToken?: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

// Vehicle Types
export interface Vehicle {
  id: number;
  licensePlate: string;
  brand: string;
  model: string;
  year: number;
  color: string;
  mileage: number;
  status: VehicleStatus;
  dailyRate: number;
  category: string;
  seats: number;
  transmission: string;
  fuelType: string;
  description?: string;
  images?: string[];
}

export enum VehicleStatus {
  AVAILABLE = 'AVAILABLE',
  RESERVED = 'RESERVED',
  RENTED = 'RENTED',
  MAINTENANCE = 'MAINTENANCE',
  OUT_OF_SERVICE = 'OUT_OF_SERVICE'
}

// Reservation Types
export interface Reservation {
  id: number;
  reservationCode: string;
  user: User;
  vehicle: Vehicle;
  startDate: string;
  endDate: string;
  pickupLocation: string;
  returnLocation: string;
  status: ReservationStatus;
  dailyRate: number;
  totalDays: number;
  totalAmount: number;
  depositAmount?: number;
  specialRequests?: string;
  createdAt: string;
  updatedAt: string;
}

export enum ReservationStatus {
  PENDING = 'PENDING',
  CONFIRMED = 'CONFIRMED',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
  NO_SHOW = 'NO_SHOW'
}

// Payment Types
export interface Payment {
  id: number;
  paymentCode: string;
  reservation: Reservation;
  user: User;
  amount: number;
  subtotal: number;
  taxAmount: number;
  discountAmount?: number;
  processingFee: number;
  status: PaymentStatus;
  paymentMethod: PaymentMethod;
  stripePaymentIntentId?: string;
  paidAt?: string;
  refundAmount?: number;
  createdAt: string;
}

export enum PaymentStatus {
  PENDING = 'PENDING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  REFUNDED = 'REFUNDED',
  PARTIALLY_REFUNDED = 'PARTIALLY_REFUNDED'
}

export enum PaymentMethod {
  CREDIT_CARD = 'CREDIT_CARD',
  DEBIT_CARD = 'DEBIT_CARD',
  CASH = 'CASH',
  BANK_TRANSFER = 'BANK_TRANSFER'
}

// API Request/Response Types
export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  user: User;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phoneNumber?: string;
}

export interface CreateReservationRequest {
  vehicleId: number;
  startDate: string;
  endDate: string;
  pickupLocation: string;
  returnLocation: string;
  specialRequests?: string;
}

export interface PaymentRequest {
  reservationId: number;
  userId: number;
  amount: number;
  discountAmount?: number;
  promoCode?: string;
  paymentMethodId: string;
  currency?: string;
  description?: string;
}

// Filter Types
export interface VehicleFilters {
  category?: string;
  minPrice?: number;
  maxPrice?: number;
  transmission?: string;
  fuelType?: string;
  minSeats?: number;
  availability?: boolean;
  sortBy?: 'price' | 'year' | 'rating';
  sortOrder?: 'asc' | 'desc';
}

// Notification Types
export interface Notification {
  id: string;
  title: string;
  message: string;
  type: NotificationType;
  read: boolean;
  data?: any;
  createdAt: string;
}

export enum NotificationType {
  RESERVATION_CONFIRMATION = 'RESERVATION_CONFIRMATION',
  PAYMENT_CONFIRMATION = 'PAYMENT_CONFIRMATION',
  PICKUP_REMINDER = 'PICKUP_REMINDER',
  RETURN_REMINDER = 'RETURN_REMINDER',
  PROMOTIONAL = 'PROMOTIONAL',
  SYSTEM = 'SYSTEM'
}

// Error Types
export interface ApiError {
  message: string;
  code?: string;
  details?: any;
}

// Navigation Types
export type RootStackParamList = {
  Login: undefined;
  Register: undefined;
  Home: undefined;
  VehicleList: { filters?: VehicleFilters };
  VehicleDetail: { vehicleId: number };
  CreateReservation: { vehicleId: number };
  ReservationList: undefined;
  ReservationDetail: { reservationId: number };
  Payment: { reservationId: number };
  Profile: undefined;
  Notifications: undefined;
};

export type TabParamList = {
  Home: undefined;
  Vehicles: undefined;
  Reservations: undefined;
  Profile: undefined;
};