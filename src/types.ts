/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

export enum UserRole {
  ADMIN = 'ADMIN',
  DRIVER = 'DRIVER',
}

export enum TripType {
  FIXED_FARE = 'FIXED_FARE',
  RUNNING_METER = 'RUNNING_METER',
  RENTAL_PACKAGE = 'RENTAL_PACKAGE',
  AIRPORT_TRANSFER = 'AIRPORT_TRANSFER',
  OUTSTATION = 'OUTSTATION',
}

export enum TripStatus {
  ASSIGNED = 'ASSIGNED',
  STARTED = 'STARTED',
  ENDED = 'ENDED',
  CANCELLED = 'CANCELLED',
}

export interface Tariff {
  baseFare: number;
  farePerKm: number;
  waitingChargePerMin: number;
  hillChargeActive: boolean;
  nightChargeActive: boolean;
  gstPercent: number;
}

export interface RentalRules {
  hoursIncluded: number;
  kmIncluded: number;
  extraKmRate: number;
  extraHourRate: number;
}

export interface Trip {
  id: string;
  customerName: string;
  customerMobile: string;
  pickupLocation: string;
  dropLocation: string;
  tripType: TripType;
  status: TripStatus;
  driverId: string;
  driverName: string;
  otp: string;
  startTimestamp?: number;
  endTimestamp?: number;
  
  // Configured fares at assignment time
  baseFare: number;
  farePerKm: number;
  waitingChargePerMin: number;
  isHillChargeEnabled: boolean;
  isNightChargeEnabled: boolean;
  
  // For Rental Package
  rentalHoursIncluded: number;
  rentalKmIncluded: number;
  rentalExtraKmRate: number;
  rentalExtraHourRate: number;
  
  // Live fields updated during trip
  totalKm: number;
  waitingMinutes: number;
  calculatedFare: number;
  gstAmount: number;
  totalWithGst: number;
  
  // Sync Metadata
  isSynced: boolean;
  lastUpdated: number;
}

export interface Driver {
  id: string;
  name: string;
  mobile: string;
  isEnabled: boolean;
  isOnline: boolean;
  currentLat: number;
  currentLng: number;
  lastUpdated: number;
}
