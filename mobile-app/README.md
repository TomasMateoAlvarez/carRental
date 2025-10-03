# CarRental Mobile App

React Native mobile application for the CarRental SaaS platform.

## Features

- **Authentication**: JWT-based login and registration
- **Vehicle Browsing**: Browse available vehicles with filters
- **Reservations**: Create and manage vehicle reservations
- **Payments**: Stripe integration for secure payments
- **Notifications**: Push notifications for booking updates
- **Profile Management**: User profile and preferences
- **Multi-tenant Support**: Support for different company accounts

## Getting Started

### Prerequisites

- Node.js 18+
- React Native CLI
- Android Studio (for Android development)
- Xcode (for iOS development - macOS only)

### Installation

```bash
# Install dependencies
npm install

# iOS specific (macOS only)
cd ios && pod install && cd ..

# Start Metro bundler
npm start

# Run on Android
npm run android

# Run on iOS
npm run ios
```

## Project Structure

```
src/
├── components/        # Reusable UI components
├── screens/          # Application screens
├── navigation/       # Navigation configuration
├── services/         # API services and HTTP client
├── stores/           # Zustand state management
├── types/            # TypeScript type definitions
├── utils/            # Utility functions
└── assets/           # Images, fonts, and other assets
```

## API Integration

The mobile app connects to the CarRental backend API:

- **Base URL**: `http://localhost:8083/api/v1` (development)
- **Authentication**: JWT Bearer tokens
- **Endpoints**:
  - `/auth/*` - Authentication
  - `/vehicles/*` - Vehicle management
  - `/reservations/*` - Reservation management
  - `/payments/*` - Payment processing
  - `/analytics/*` - Analytics data

## State Management

Using Zustand for state management:

- **authStore**: User authentication state
- **vehicleStore**: Vehicle data and filters
- **reservationStore**: Reservation management
- **notificationStore**: Push notification handling

## Development Notes

- **Hot Reload**: Both Metro bundler and backend support hot reload
- **Type Safety**: Full TypeScript integration
- **Testing**: Jest testing framework included
- **Linting**: ESLint configuration for code quality

## Building for Production

```bash
# Android
cd android && ./gradlew assembleRelease

# iOS
cd ios && xcodebuild -scheme CarRentalMobile -configuration Release
```

## Environment Configuration

Create `.env` file with:

```
API_BASE_URL=https://api.carrental.com/api/v1
STRIPE_PUBLISHABLE_KEY=pk_live_...
GOOGLE_MAPS_API_KEY=AIza...
```