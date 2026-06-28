import React, { useState, useEffect } from 'react';
import {
  UserRole,
  TripType,
  TripStatus,
  Trip,
  Driver,
  Tariff,
  RentalRules,
} from '../types';
import {
  Smartphone,
  Wifi,
  Battery,
  MapPin,
  Clock,
  User,
  Phone,
  Lock,
  ArrowRight,
  Shield,
  DollarSign,
  Play,
  Square,
  Navigation,
  CheckCircle,
  Share2,
  FileText,
  AlertCircle,
  Settings,
  PlusCircle,
  LogOut,
} from 'lucide-react';

interface PhoneMockupProps {
  drivers: Driver[];
  trips: Trip[];
  tariff: Tariff;
  rentalRules: RentalRules;
  onUpdateTrips: (updatedTrips: Trip[]) => void;
  onUpdateDrivers: (updatedDrivers: Driver[]) => void;
  onAddTrip: (trip: Trip) => void;
  activeSimulatedTrip: Trip | null;
  setActiveSimulatedTrip: (trip: Trip | null) => void;
}

export default function PhoneMockup({
  drivers,
  trips,
  tariff,
  rentalRules,
  onUpdateTrips,
  onUpdateDrivers,
  onAddTrip,
  activeSimulatedTrip,
  setActiveSimulatedTrip,
}: PhoneMockupProps) {
  // Mobile UI States
  const [currentScreen, setCurrentScreen] = useState<string>('role_select');
  const [currentRole, setCurrentRole] = useState<UserRole | null>(null);
  const [currentUser, setCurrentUser] = useState<{ id: string; name: string } | null>(null);
  const [toast, setToast] = useState<string | null>(null);

  // Forms
  const [phoneInput, setPhoneInput] = useState('');
  const [otpInput, setOtpInput] = useState('');
  const [isOtpSent, setIsOtpSent] = useState(false);

  // Mobile dispatch form
  const [customerName, setCustomerName] = useState('');
  const [customerMobile, setCustomerMobile] = useState('');
  const [pickup, setPickup] = useState('');
  const [drop, setDrop] = useState('');
  const [selectedType, setSelectedType] = useState<TripType>(TripType.RUNNING_METER);
  const [assignedDriverId, setAssignedDriverId] = useState('DRV001');

  // Interactive travel simulator values
  const [simInterval, setSimInterval] = useState<NodeJS.Timeout | null>(null);
  const [isSimulating, setIsSimulating] = useState(false);

  // Trigger Toast helper
  const showToast = (msg: string) => {
    setToast(msg);
    setTimeout(() => setToast(null), 3000);
  };

  // Helper to normalize phone numbers
  const cleanPhone = (p: string) => p.replace(/\D/g, '');

  const findDriverByPhone = (phone: string) => {
    const cleanInput = cleanPhone(phone);
    if (!cleanInput) return undefined;
    return drivers.find(d => {
      const cleanDrv = cleanPhone(d.mobile);
      return cleanInput.endsWith(cleanDrv) || cleanDrv.endsWith(cleanInput);
    });
  };

  // Login handler matching Kotlin repository behaviour
  const handleLogin = () => {
    const cleanPhoneInput = cleanPhone(phoneInput);
    if (!isOtpSent) {
      if (currentRole === UserRole.ADMIN) {
        if (cleanPhoneInput === '9999999999' || cleanPhoneInput.endsWith('99999')) {
          setIsOtpSent(true);
          showToast('Verification Code "123456" dispatched via SMS!');
        } else {
          showToast('Mobile number not registered as Dispatcher!');
        }
      } else {
        const matchedDriver = findDriverByPhone(phoneInput);
        if (matchedDriver) {
          setIsOtpSent(true);
          showToast(`Verification Code "123456" dispatched to ${matchedDriver.name}!`);
        } else {
          showToast('Mobile number not registered as a driver!');
        }
      }
    } else {
      if (otpInput === '123456' || otpInput === '1234') {
        const role = currentRole!;
        showToast(`Authorization successful as ${role}`);
        if (role === UserRole.ADMIN) {
          setCurrentUser({ id: 'ADMIN_USER', name: 'Central Dispatcher' });
          setCurrentScreen('admin_dashboard');
        } else {
          const matchedDriver = findDriverByPhone(phoneInput);
          if (matchedDriver) {
            setCurrentUser({ id: matchedDriver.id, name: matchedDriver.name });
            setCurrentScreen('driver_home');
            // Make sure the matched driver goes online
            onUpdateDrivers(drivers.map(d => d.id === matchedDriver.id ? { ...d, isOnline: true } : d));
          } else {
            showToast('Error: Driver record missing.');
          }
        }
      } else {
        showToast('Incorrect verification code. Try "123456"');
      }
    }
  };

  // Logout handler
  const handleLogout = () => {
    if (isSimulating) {
      stopSimulation();
    }
    if (currentUser) {
      onUpdateDrivers(drivers.map(d => d.id === currentUser.id ? { ...d, isOnline: false } : d));
    }
    setCurrentUser(null);
    setCurrentRole(null);
    setIsOtpSent(false);
    setPhoneInput('');
    setOtpInput('');
    setCurrentScreen('role_select');
    showToast('Signed out of session');
  };

  // Start trip with Otp matching TaxiViewModel.kt
  const handleStartTrip = (tripId: string, enteredOtp: string) => {
    const trip = trips.find(t => t.id === tripId);
    if (!trip) return;

    if (enteredOtp === trip.otp || enteredOtp === '1234') {
      const updated = {
        ...trip,
        status: TripStatus.STARTED,
        startTimestamp: Date.now(),
        lastUpdated: Date.now(),
      };
      
      onUpdateTrips(trips.map(t => t.id === tripId ? updated : t));
      setActiveSimulatedTrip(updated);
      setCurrentScreen('live_meter');
      showToast('Trip started! Live GPS tracking activated.');

      // Update driver coordinate to start of Bangalore
      onUpdateDrivers(
        drivers.map(d => d.id === trip.driverId ? { ...d, isOnline: true, currentLat: 12.9716, currentLng: 77.5946 } : d)
      );
    } else {
      showToast('Incorrect OTP code. Try matching dispatcher.');
    }
  };

  // Live Fare engine calculation (matching Tariff Engine from Models.kt)
  const calculateFare = (trip: Trip, km: number, waitMins: number): { fare: number, gst: number, total: number } => {
    let baseCalculated = 0;
    
    switch (trip.tripType) {
      case TripType.FIXED_FARE:
        baseCalculated = trip.baseFare;
        break;
      case TripType.RUNNING_METER:
      case TripType.AIRPORT_TRANSFER:
      case TripType.OUTSTATION:
        baseCalculated = trip.baseFare + (km * trip.farePerKm) + (waitMins * trip.waitingChargePerMin);
        if (trip.isHillChargeEnabled) {
          baseCalculated += 300.0; // premium hill charge
        }
        if (trip.isNightChargeEnabled) {
          baseCalculated *= 1.25; // 25% night shift surcharge
        }
        break;
      case TripType.RENTAL_PACKAGE:
        const basePackagePrice = trip.baseFare;
        const elapsedMs = Date.now() - (trip.startTimestamp || Date.now());
        const elapsedHours = Math.max(1, Math.floor(elapsedMs / 10000)); // fast simulated hours
        const extraHours = Math.max(0, elapsedHours - trip.rentalHoursIncluded);
        const extraKm = Math.max(0, km - trip.rentalKmIncluded);
        baseCalculated = basePackagePrice + (extraHours * trip.rentalExtraHourRate) + (extraKm * trip.rentalExtraKmRate);
        break;
    }

    const gst = baseCalculated * (tariff.gstPercent / 100.0);
    return {
      fare: parseFloat(baseCalculated.toFixed(2)),
      gst: parseFloat(gst.toFixed(2)),
      total: parseFloat((baseCalculated + gst).toFixed(2)),
    };
  };

  // Start Travel Simulation (Car movement 45 km/h)
  const startSimulation = () => {
    if (!activeSimulatedTrip) return;
    setIsSimulating(true);
    showToast('GPS Travel Simulator engaged: Speed 45 km/h');

    let currentKm = activeSimulatedTrip.totalKm;
    let currentWait = activeSimulatedTrip.waitingMinutes;
    let tickCount = 0;

    const interval = setInterval(() => {
      tickCount++;
      // Randomize traffic stops (every 3rd tick is a stop)
      const isStopped = tickCount % 3 === 0;

      if (isStopped) {
        currentWait += 0.5; // add 30s waiting time
      } else {
        currentKm += 0.25; // add 250m distance
      }

      // Calculate new pricing
      const fares = calculateFare(activeSimulatedTrip, currentKm, currentWait);

      // Bangalore driver GPS drift simulations
      let newLat = 12.9716;
      let newLng = 77.5946;
      const driver = drivers.find(d => d.id === activeSimulatedTrip.driverId);
      if (driver) {
        // Drifting northeast towards Whitefield / Airport
        newLat = driver.currentLat + (isStopped ? 0 : 0.0012);
        newLng = driver.currentLng + (isStopped ? 0 : 0.0019);
        onUpdateDrivers(
          drivers.map(d => d.id === driver.id ? { ...d, currentLat: parseFloat(newLat.toFixed(5)), currentLng: parseFloat(newLng.toFixed(5)) } : d)
        );
      }

      const updatedTrip: Trip = {
        ...activeSimulatedTrip,
        totalKm: parseFloat(currentKm.toFixed(2)),
        waitingMinutes: parseFloat(currentWait.toFixed(1)),
        calculatedFare: fares.fare,
        gstAmount: fares.gst,
        totalWithGst: fares.total,
        lastUpdated: Date.now(),
      };

      setActiveSimulatedTrip(updatedTrip);
      onUpdateTrips(trips.map(t => t.id === activeSimulatedTrip.id ? updatedTrip : t));
    }, 2000);

    setSimInterval(interval);
  };

  const stopSimulation = () => {
    if (simInterval) {
      clearInterval(simInterval);
      setSimInterval(null);
    }
    setIsSimulating(false);
    showToast('GPS Travel Simulator paused');
  };

  // End Active Trip matching TaxiViewModel.kt
  const handleEndTrip = () => {
    if (!activeSimulatedTrip) return;
    stopSimulation();

    const endedTrip: Trip = {
      ...activeSimulatedTrip,
      status: TripStatus.ENDED,
      endTimestamp: Date.now(),
      lastUpdated: Date.now(),
    };

    onUpdateTrips(trips.map(t => t.id === endedTrip.id ? endedTrip : t));
    setActiveSimulatedTrip(endedTrip);
    setCurrentScreen('receipt_screen');
    showToast('Trip ended successfully. Dispatch synchronized.');
  };

  // Admin creating and dispatching new trip matching Kotlin AdminScreens.kt
  const handleCreateTrip = (e: React.FormEvent) => {
    e.preventDefault();
    if (!customerName || !customerMobile || !pickup || !drop) {
      showToast('Please complete all dispatch details');
      return;
    }

    const tripId = `TRIP${Math.floor(100 + Math.random() * 900)}`;
    const otp = Math.floor(1000 + Math.random() * 9000).toString();
    const driverObj = drivers.find(d => d.id === assignedDriverId);

    const newTripObj: Trip = {
      id: tripId,
      customerName,
      customerMobile,
      pickupLocation: pickup,
      dropLocation: drop,
      tripType: selectedType,
      status: TripStatus.ASSIGNED,
      driverId: assignedDriverId,
      driverName: driverObj?.name || 'Unknown',
      otp,
      baseFare: tariff.baseFare,
      farePerKm: tariff.farePerKm,
      waitingChargePerMin: tariff.waitingChargePerMin,
      isHillChargeEnabled: tariff.hillChargeActive,
      isNightChargeEnabled: tariff.nightChargeActive,
      rentalHoursIncluded: rentalRules.hoursIncluded,
      rentalKmIncluded: rentalRules.kmIncluded,
      rentalExtraKmRate: rentalRules.extraKmRate,
      rentalExtraHourRate: rentalRules.extraHourRate,
      totalKm: 0,
      waitingMinutes: 0,
      calculatedFare: 0,
      gstAmount: 0,
      totalWithGst: 0,
      isSynced: true,
      lastUpdated: Date.now(),
    };

    onAddTrip(newTripObj);
    showToast(`Trip ${tripId} successfully dispatched! Customer OTP: ${otp}`);
    setCurrentScreen('admin_dashboard');

    // Reset forms
    setCustomerName('');
    setCustomerMobile('');
    setPickup('');
    setDrop('');
  };

  // Clean interval on unmount
  useEffect(() => {
    return () => {
      if (simInterval) clearInterval(simInterval);
    };
  }, [simInterval]);

  return (
    <div className="flex flex-col items-center">
      {/* Phone Shell wrapper */}
      <div className="relative w-[340px] h-[680px] bg-slate-900 rounded-[48px] border-[10px] border-slate-950 p-3 shadow-2xl flex flex-col overflow-hidden">
        {/* Dynamic status/camera island */}
        <div className="absolute top-1 left-1/2 -translate-x-1/2 w-28 h-4 bg-slate-950 rounded-full z-30 flex items-center justify-center">
          <div className="w-2.5 h-2.5 rounded-full bg-slate-800 border border-slate-900" />
        </div>

        {/* Status Bar */}
        <div className="flex items-center justify-between px-5 pt-1.5 pb-2 text-slate-400 text-xs font-mono select-none z-20">
          <span>12:30 PM</span>
          <div className="flex items-center gap-1.5">
            <Wifi className="w-3.5 h-3.5" />
            <Battery className="w-4 h-4" />
          </div>
        </div>

        {/* Sandbox Indicator */}
        <div className="text-[9px] text-center uppercase tracking-widest font-bold bg-amber-500/20 text-amber-300 py-0.5 font-mono">
          Interactive Android App Emulator
        </div>

        {/* Content Box */}
        <div className="flex-1 rounded-[32px] overflow-hidden bg-slate-50 relative flex flex-col text-slate-800">
          
          {/* TOAST OVERLAY */}
          {toast && (
            <div className="absolute bottom-16 left-4 right-4 bg-slate-900/90 text-slate-100 text-[11px] font-medium py-2 px-3 rounded-xl z-50 text-center shadow-lg border border-slate-800 animate-bounce">
              {toast}
            </div>
          )}

          {/* SCREEN: ROLE SELECT */}
          {currentScreen === 'role_select' && (
            <div className="flex-1 flex flex-col bg-slate-100 p-5 justify-between">
              <div className="flex-1 flex flex-col items-center justify-center text-center">
                <div className="w-16 h-16 rounded-2xl bg-amber-400 border-2 border-slate-900 flex items-center justify-center shadow-md mb-4">
                  <Navigation className="w-10 h-10 text-slate-900 fill-slate-900" />
                </div>
                <h2 className="text-xl font-black text-slate-900 tracking-tight leading-none italic uppercase">
                  GET TAXI METER
                </h2>
                <p className="text-[10px] text-slate-500 font-bold uppercase tracking-wider mb-8">
                  Live Tariff & Dispatch Engine
                </p>

                <p className="text-[11px] font-black tracking-widest text-slate-400 uppercase mb-4">
                  Choose Sandbox Role
                </p>

                {/* Dispatch Button */}
                <button
                  onClick={() => {
                    setCurrentRole(UserRole.ADMIN);
                    setCurrentScreen('login_admin');
                  }}
                  className="w-full flex items-center justify-between p-4 bg-amber-100 border border-amber-300 hover:bg-amber-200 text-slate-900 rounded-2xl transition-all mb-3 text-left shadow-sm"
                >
                  <div className="flex items-center gap-3">
                    <div className="w-9 h-9 rounded-lg bg-amber-400 flex items-center justify-center">
                      <Shield className="w-5 h-5 text-slate-900" />
                    </div>
                    <div>
                      <h4 className="font-bold text-xs">Dispatcher Control</h4>
                      <p className="text-[10px] text-slate-500">Assign trips & tweak tariffs</p>
                    </div>
                  </div>
                  <ArrowRight className="w-4 h-4 text-slate-600" />
                </button>

                {/* Driver Button */}
                <button
                  onClick={() => {
                    setCurrentRole(UserRole.DRIVER);
                    setCurrentScreen('login_driver');
                  }}
                  className="w-full flex items-center justify-between p-4 bg-white border border-slate-200 hover:bg-slate-50 text-slate-900 rounded-2xl transition-all text-left shadow-sm"
                >
                  <div className="flex items-center gap-3">
                    <div className="w-9 h-9 rounded-lg bg-emerald-100 flex items-center justify-center">
                      <Smartphone className="w-5 h-5 text-emerald-600" />
                    </div>
                    <div>
                      <h4 className="font-bold text-xs">Taxi Driver App</h4>
                      <p className="text-[10px] text-slate-500">Live GPS & meter simulator</p>
                    </div>
                  </div>
                  <ArrowRight className="w-4 h-4 text-slate-600" />
                </button>
              </div>

              <div className="text-[9px] text-slate-400 text-center font-mono font-semibold">
                Kotlin Native Framework v1.0.0
              </div>
            </div>
          )}

          {/* SCREEN: LOGIN SCREEN */}
          {(currentScreen === 'login_admin' || currentScreen === 'login_driver') && (
            <div className="flex-1 flex flex-col justify-between bg-white p-5">
              <div className="pt-4">
                <div className="w-12 h-12 rounded-xl bg-amber-100 flex items-center justify-center mb-4">
                  {currentRole === UserRole.ADMIN ? (
                    <Shield className="w-6 h-6 text-amber-500" />
                  ) : (
                    <User className="w-6 h-6 text-amber-500" />
                  )}
                </div>
                <h3 className="text-md font-black italic text-slate-900 uppercase tracking-tight">
                  {currentRole === UserRole.ADMIN ? 'Dispatcher Portal' : 'Driver Registration'}
                </h3>
                <p className="text-xs text-slate-500 mb-6">
                  Log in securely using a mobile number & verification OTP.
                </p>

                {/* Mobile input */}
                <div className="space-y-3">
                  <div>
                    <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1">
                      Mobile Number
                    </label>
                    <div className="relative">
                      <Phone className="absolute left-3 top-3 w-4 h-4 text-slate-400" />
                      <input
                        type="tel"
                        placeholder="+91 XXXXX XXXXX"
                        disabled={isOtpSent}
                        value={phoneInput}
                        onChange={e => setPhoneInput(e.target.value)}
                        className="w-full pl-9 pr-3 py-2.5 text-xs border border-slate-200 bg-slate-50 rounded-xl focus:ring-1 focus:ring-amber-400 focus:outline-none text-slate-800"
                      />
                    </div>
                  </div>

                  {isOtpSent && (
                    <div>
                      <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1">
                        Enter 6-digit OTP
                      </label>
                      <div className="relative">
                        <Lock className="absolute left-3 top-3 w-4 h-4 text-slate-400" />
                        <input
                          type="text"
                          placeholder="123456"
                          value={otpInput}
                          onChange={e => setOtpInput(e.target.value)}
                          className="w-full pl-9 pr-3 py-2.5 text-xs border border-slate-200 bg-slate-50 rounded-xl focus:ring-1 focus:ring-amber-400 focus:outline-none text-slate-800 font-mono tracking-widest"
                        />
                      </div>
                      <span className="text-[10px] text-amber-600 font-semibold mt-1 block">
                        SMS Sent: Enter standard bypass code "123456"
                      </span>
                    </div>
                  )}
                </div>
              </div>

              <div className="space-y-2">
                <button
                  onClick={handleLogin}
                  className="w-full bg-slate-900 hover:bg-slate-800 text-slate-100 py-3 rounded-xl text-xs font-black uppercase tracking-wider transition-all"
                >
                  {isOtpSent ? 'Verify & Continue' : 'Send Activation SMS'}
                </button>
                <button
                  onClick={() => {
                    setCurrentScreen('role_select');
                    setIsOtpSent(false);
                  }}
                  className="w-full text-slate-500 hover:text-slate-800 py-2 text-[11px] font-bold transition-all"
                >
                  Back to Roles
                </button>
              </div>
            </div>
          )}

          {/* SCREEN: DISPATCHER ADMIN DASHBOARD */}
          {currentScreen === 'admin_dashboard' && (
            <div className="flex-1 flex flex-col justify-between bg-slate-50">
              {/* Header */}
              <div className="bg-slate-900 text-slate-100 p-4 flex items-center justify-between select-none">
                <div className="flex items-center gap-2">
                  <Shield className="w-4 h-4 text-amber-400" />
                  <span className="font-black italic text-xs tracking-wider">DISPATCH ADMIN</span>
                </div>
                <button onClick={handleLogout} className="text-slate-400 hover:text-rose-400 transition-all">
                  <LogOut className="w-4 h-4" />
                </button>
              </div>

              {/* Form Scroll Area */}
              <div className="flex-1 overflow-y-auto p-4 space-y-4">
                <div className="flex items-center justify-between">
                  <h4 className="text-xs font-black text-slate-500 uppercase tracking-wider">
                    Dispatch Trip Board
                  </h4>
                  <span className="text-[9px] bg-slate-200 text-slate-600 px-2 py-0.5 rounded-full font-bold">
                    Active Drivers: {drivers.filter(d => d.isOnline).length}
                  </span>
                </div>

                <form onSubmit={handleCreateTrip} className="space-y-3 bg-white p-3 rounded-2xl border border-slate-200 shadow-sm">
                  <div>
                    <label className="block text-[9px] font-bold text-slate-400 uppercase mb-0.5">
                      Customer Name
                    </label>
                    <input
                      type="text"
                      required
                      placeholder="e.g. Priya Sen"
                      value={customerName}
                      onChange={e => setCustomerName(e.target.value)}
                      className="w-full px-2.5 py-1.5 text-[11px] border border-slate-200 rounded-lg focus:ring-1 focus:ring-amber-400 focus:outline-none"
                    />
                  </div>

                  <div>
                    <label className="block text-[9px] font-bold text-slate-400 uppercase mb-0.5">
                      Customer Mobile
                    </label>
                    <input
                      type="tel"
                      required
                      placeholder="+91 99000 11223"
                      value={customerMobile}
                      onChange={e => setCustomerMobile(e.target.value)}
                      className="w-full px-2.5 py-1.5 text-[11px] border border-slate-200 rounded-lg focus:ring-1 focus:ring-amber-400 focus:outline-none"
                    />
                  </div>

                  <div className="grid grid-cols-2 gap-2">
                    <div>
                      <label className="block text-[9px] font-bold text-slate-400 uppercase mb-0.5">
                        Pickup Spot
                      </label>
                      <select
                        value={pickup}
                        onChange={e => setPickup(e.target.value)}
                        className="w-full px-2 py-1.5 text-[10px] border border-slate-200 rounded-lg bg-slate-50"
                      >
                        <option value="">Choose pickup</option>
                        <option value="Koramangala 4th Block">Koramangala</option>
                        <option value="Indiranagar Metro Station">Indiranagar Metro</option>
                        <option value="MG Road Central">MG Road</option>
                      </select>
                    </div>
                    <div>
                      <label className="block text-[9px] font-bold text-slate-400 uppercase mb-0.5">
                        Dropoff Destination
                      </label>
                      <select
                        value={drop}
                        onChange={e => setDrop(e.target.value)}
                        className="w-full px-2 py-1.5 text-[10px] border border-slate-200 rounded-lg bg-slate-50"
                      >
                        <option value="">Choose destination</option>
                        <option value="Kempegowda Int. Airport">Kemp. Airport</option>
                        <option value="Whitefield IT Park">Whitefield IT</option>
                        <option value="Electronic City">Electronic City</option>
                      </select>
                    </div>
                  </div>

                  <div className="grid grid-cols-2 gap-2">
                    <div>
                      <label className="block text-[9px] font-bold text-slate-400 uppercase mb-0.5">
                        Tariff Type
                      </label>
                      <select
                        value={selectedType}
                        onChange={e => setSelectedType(e.target.value as TripType)}
                        className="w-full px-2 py-1.5 text-[10px] border border-slate-200 rounded-lg bg-slate-50"
                      >
                        <option value={TripType.RUNNING_METER}>Running Meter</option>
                        <option value={TripType.AIRPORT_TRANSFER}>Airport Drop</option>
                        <option value={TripType.RENTAL_PACKAGE}>Rental Package</option>
                        <option value={TripType.FIXED_FARE}>Fixed Tariff</option>
                        <option value={TripType.OUTSTATION}>Outstation</option>
                      </select>
                    </div>

                    <div>
                      <label className="block text-[9px] font-bold text-slate-400 uppercase mb-0.5">
                        Assign Driver
                      </label>
                      <select
                        value={assignedDriverId}
                        onChange={e => setAssignedDriverId(e.target.value)}
                        className="w-full px-2 py-1.5 text-[10px] border border-slate-200 rounded-lg bg-slate-50"
                      >
                        {drivers.map(d => (
                          <option key={d.id} value={d.id} disabled={!d.isEnabled}>
                            {d.name} {d.isEnabled ? '(Online)' : '(Disabled)'}
                          </option>
                        ))}
                      </select>
                    </div>
                  </div>

                  <button
                    type="submit"
                    className="w-full bg-amber-400 hover:bg-amber-500 text-slate-900 py-2 rounded-xl text-xs font-black uppercase tracking-wider mt-2 transition-all flex items-center justify-center gap-1"
                  >
                    <PlusCircle className="w-4 h-4" />
                    Dispatch New Trip
                  </button>
                </form>

                {/* Simulated database list */}
                <div className="space-y-2">
                  <h5 className="text-[10px] font-black text-slate-500 uppercase tracking-wider">
                    Recent Trips Logs
                  </h5>
                  <div className="space-y-1.5 max-h-32 overflow-y-auto">
                    {trips.map(t => (
                      <div key={t.id} className="bg-white p-2.5 rounded-xl border border-slate-200 flex items-center justify-between text-[10px]">
                        <div className="flex-1 min-w-0 pr-2">
                          <div className="flex items-center gap-1">
                            <span className="font-bold text-slate-900">{t.id}</span>
                            <span className="text-slate-400 font-medium text-[9px]">({t.tripType.replace('_', ' ')})</span>
                          </div>
                          <div className="text-slate-500 text-[9px] truncate">Driver: {t.driverName}</div>
                          {t.status === TripStatus.ASSIGNED && (
                            <div className="mt-1 flex items-center gap-1">
                              <span className="bg-amber-100 text-amber-800 px-1.5 py-0.5 rounded text-[9px] font-mono font-bold">
                                OTP: {t.otp}
                              </span>
                            </div>
                          )}
                        </div>
                        <span className={`px-1.5 py-0.5 rounded text-[8px] font-black ${
                          t.status === TripStatus.ENDED ? 'bg-emerald-100 text-emerald-800' :
                          t.status === TripStatus.STARTED ? 'bg-amber-100 text-amber-800 animate-pulse' :
                          'bg-slate-100 text-slate-700'
                        }`}>
                          {t.status}
                        </span>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* SCREEN: DRIVER HOME SCREEN */}
          {currentScreen === 'driver_home' && (
            <div className="flex-1 flex flex-col justify-between bg-slate-100">
              {/* Header */}
              <div className="bg-slate-900 text-slate-100 p-4 flex items-center justify-between select-none">
                <div className="flex items-center gap-2">
                  <span className="w-2 h-2 rounded-full bg-emerald-400 animate-ping" />
                  <span className="font-black italic text-xs tracking-wider">DRIVER BOARD</span>
                </div>
                <button onClick={handleLogout} className="text-slate-400 hover:text-rose-400 transition-all">
                  <LogOut className="w-4 h-4" />
                </button>
              </div>

              {/* Main List */}
              <div className="flex-1 overflow-y-auto p-4 space-y-4">
                <div className="flex items-center justify-between">
                  <div>
                    <h4 className="font-black text-xs text-slate-800">
                      Welcome, Rajesh Kumar
                    </h4>
                    <p className="text-[9px] text-slate-400 font-bold uppercase">
                      Driver ID: DRV001 • online
                    </p>
                  </div>
                  <Badge text="ACTIVE" color="green" />
                </div>

                {/* Assigned Trips Column */}
                <div className="space-y-3">
                  <h5 className="text-[10px] font-black text-slate-400 uppercase tracking-widest">
                    Assigned Job Orders
                  </h5>

                  {trips.filter(t => t.driverId === 'DRV001' && t.status === TripStatus.ASSIGNED).length === 0 ? (
                    <div className="bg-white p-6 rounded-2xl border border-slate-200 text-center space-y-2">
                      <CheckCircle className="w-8 h-8 text-emerald-500 mx-auto" />
                      <p className="text-xs text-slate-600 font-bold">All clear! No assignments.</p>
                      <p className="text-[9px] text-slate-400">Dispatcher will broadcast a route here.</p>
                    </div>
                  ) : (
                    trips
                      .filter(t => t.driverId === 'DRV001' && t.status === TripStatus.ASSIGNED)
                      .map(t => (
                        <div key={t.id} className="bg-white p-4 rounded-2xl border border-slate-200 shadow-sm space-y-3">
                          <div className="flex items-center justify-between">
                            <span className="font-black text-xs text-amber-500 bg-amber-100 px-2 py-0.5 rounded">
                              {t.id}
                            </span>
                            <span className="text-[9px] font-bold text-slate-400 uppercase tracking-wider">
                              OTP Verification Req.
                            </span>
                          </div>

                          <div className="space-y-1">
                            <div className="flex items-start gap-2">
                              <MapPin className="w-3.5 h-3.5 text-emerald-500 shrink-0 mt-0.5" />
                              <span className="text-[11px] font-bold text-slate-700">{t.pickupLocation}</span>
                            </div>
                            <div className="flex items-start gap-2">
                              <MapPin className="w-3.5 h-3.5 text-rose-500 shrink-0 mt-0.5" />
                              <span className="text-[11px] text-slate-500 font-semibold">{t.dropLocation}</span>
                            </div>
                          </div>

                          <div className="flex items-center justify-between border-t border-slate-100 pt-2 text-[10px] font-semibold text-slate-500">
                            <span>Client: {t.customerName}</span>
                            <span>Type: {t.tripType.replace('_', ' ')}</span>
                          </div>

                          {/* Enter OTP Field */}
                          <div className="flex gap-2 pt-1">
                            <input
                              type="text"
                              id={`otp-input-${t.id}`}
                              placeholder="Enter 4-digit OTP"
                              maxLength={4}
                              className="flex-1 px-3 py-1.5 text-xs bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-1 focus:ring-amber-400 font-mono tracking-widest text-center"
                            />
                            <button
                              onClick={() => {
                                const input = document.getElementById(`otp-input-${t.id}`) as HTMLInputElement;
                                if (input) {
                                  handleStartTrip(t.id, input.value);
                                }
                              }}
                              className="bg-emerald-500 hover:bg-emerald-600 text-white font-bold px-4 rounded-xl text-[10px] uppercase tracking-wider"
                            >
                              START
                            </button>
                          </div>
                          <p className="text-[8px] text-amber-600 font-mono text-center">
                            * dispatcher code is: <b className="font-black text-[9px]">{t.otp}</b>
                          </p>
                        </div>
                      ))
                  )}
                </div>
              </div>
            </div>
          )}

          {/* SCREEN: LIVE TRAVEL METER SCREEN */}
          {currentScreen === 'live_meter' && activeSimulatedTrip && (
            <div className="flex-1 flex flex-col bg-slate-950 text-slate-100 justify-between">
              {/* Header */}
              <div className="p-4 border-b border-slate-900 flex items-center justify-between">
                <div className="flex items-center gap-1.5">
                  <div className="w-2.5 h-2.5 rounded-full bg-amber-500 animate-pulse" />
                  <span className="font-black text-xs tracking-widest text-amber-500">LIVE TRAVEL METER</span>
                </div>
                <span className="font-mono text-[9px] text-slate-500">#{activeSimulatedTrip.id}</span>
              </div>

              {/* Main Meter Readout */}
              <div className="p-5 flex-1 flex flex-col justify-center items-center text-center space-y-6">
                <div>
                  <span className="text-[10px] tracking-widest text-slate-500 font-mono uppercase block mb-1">
                    LIVE METER FARE DUE
                  </span>
                  <div className="flex items-center justify-center text-slate-100">
                    <span className="text-2xl font-bold text-amber-500">₹</span>
                    <span className="text-4xl font-black tracking-tight text-amber-500 font-mono">
                      {activeSimulatedTrip.totalWithGst.toFixed(2)}
                    </span>
                  </div>
                  <span className="text-[9px] text-slate-500 font-bold block mt-1">
                    Calculated subtotal ₹{activeSimulatedTrip.calculatedFare.toFixed(2)} + 5% GST
                  </span>
                </div>

                {/* Telemetry Stats Rows */}
                <div className="w-full bg-slate-900/50 rounded-2xl border border-slate-900 p-4 grid grid-cols-3 gap-2">
                  <div className="text-center">
                    <span className="text-[9px] text-slate-500 font-bold uppercase block">DISTANCE</span>
                    <span className="text-md font-black text-slate-200 font-mono">{activeSimulatedTrip.totalKm.toFixed(2)}</span>
                    <span className="text-[8px] text-slate-500 block">KM</span>
                  </div>
                  <div className="text-center border-x border-slate-800">
                    <span className="text-[9px] text-slate-500 font-bold uppercase block">SPEED</span>
                    <span className="text-md font-black text-slate-200 font-mono">{isSimulating ? '45' : '0'}</span>
                    <span className="text-[8px] text-slate-500 block">KM/H</span>
                  </div>
                  <div className="text-center">
                    <span className="text-[9px] text-slate-500 font-bold uppercase block">WAITING</span>
                    <span className="text-md font-black text-slate-200 font-mono">{activeSimulatedTrip.waitingMinutes.toFixed(1)}</span>
                    <span className="text-[8px] text-slate-500 block">MINS</span>
                  </div>
                </div>

                {/* Simulator Engine controls */}
                <div className="w-full space-y-2">
                  <span className="text-[9px] font-bold text-slate-600 uppercase tracking-widest block text-center">
                    SIMULATION CONTROLS
                  </span>
                  
                  <div className="flex gap-2 justify-center">
                    {!isSimulating ? (
                      <button
                        onClick={startSimulation}
                        className="flex-1 bg-emerald-600 hover:bg-emerald-700 text-white font-bold py-2.5 px-4 rounded-xl text-[10px] uppercase tracking-wider flex items-center justify-center gap-1.5 transition-all"
                      >
                        <Play className="w-3.5 h-3.5" /> Start Travel
                      </button>
                    ) : (
                      <button
                        onClick={stopSimulation}
                        className="flex-1 bg-slate-800 hover:bg-slate-700 text-amber-500 font-bold py-2.5 px-4 rounded-xl text-[10px] uppercase tracking-wider flex items-center justify-center gap-1.5 transition-all"
                      >
                        <Square className="w-3.5 h-3.5" /> Stop / Standby
                      </button>
                    )}
                  </div>
                  <p className="text-[8px] text-slate-500 leading-tight">
                    * Travel simulator mimics GPS coordinate advancement and automatic Live Tariff engine updates every 2 seconds.
                  </p>
                </div>
              </div>

              {/* End Active Trip Button */}
              <div className="p-4 border-t border-slate-900 bg-slate-950/80">
                <button
                  onClick={handleEndTrip}
                  className="w-full bg-rose-600 hover:bg-rose-700 text-white py-3 rounded-xl text-xs font-black uppercase tracking-wider transition-all flex items-center justify-center gap-1.5"
                >
                  <AlertCircle className="w-4 h-4" />
                  END TRIP & ISSUE RECEIPT
                </button>
              </div>
            </div>
          )}

          {/* SCREEN: DIGITAL RECEIPT SCREEN */}
          {currentScreen === 'receipt_screen' && activeSimulatedTrip && (
            <div className="flex-1 flex flex-col justify-between bg-slate-50">
              {/* Scroll Content */}
              <div className="flex-1 overflow-y-auto p-4 space-y-4">
                <div className="text-center py-4 space-y-1">
                  <div className="w-10 h-10 rounded-full bg-emerald-100 flex items-center justify-center mx-auto">
                    <CheckCircle className="w-6 h-6 text-emerald-600" />
                  </div>
                  <h4 className="text-xs font-black text-emerald-600 uppercase tracking-widest">
                    TRIP COMPLETED
                  </h4>
                  <p className="text-[9px] text-slate-400">Digital receipt successfully shared</p>
                </div>

                {/* Printable receipt card */}
                <div className="bg-white rounded-2xl border border-slate-200 p-4 shadow-sm text-slate-800 space-y-3 font-mono text-[10px]">
                  <div className="flex justify-between items-center border-b border-dashed border-slate-200 pb-2">
                    <span className="font-bold text-slate-900">GET TAXI METER</span>
                    <span className="bg-slate-100 text-slate-600 px-1.5 py-0.5 rounded text-[8px] font-bold">
                      #{activeSimulatedTrip.id}
                    </span>
                  </div>

                  <div className="space-y-1">
                    <div className="flex justify-between">
                      <span className="text-slate-400 font-semibold">Client Name:</span>
                      <span className="font-bold text-slate-700">{activeSimulatedTrip.customerName}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-slate-400 font-semibold">Assigned Driver:</span>
                      <span className="font-bold text-slate-700">{activeSimulatedTrip.driverName}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-slate-400 font-semibold">Tariff Route:</span>
                      <span className="font-bold text-slate-700">{activeSimulatedTrip.tripType.replace('_', ' ')}</span>
                    </div>
                  </div>

                  <hr className="border-dashed border-slate-200" />

                  <div className="space-y-1">
                    <div className="flex justify-between">
                      <span className="text-slate-400 font-semibold">Distance Traveled:</span>
                      <span className="font-bold text-slate-700">{activeSimulatedTrip.totalKm.toFixed(2)} KM</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-slate-400 font-semibold">Speed Wait Stops:</span>
                      <span className="font-bold text-slate-700">{activeSimulatedTrip.waitingMinutes.toFixed(1)} Mins</span>
                    </div>
                  </div>

                  <hr className="border-dashed border-slate-200" />

                  <div className="space-y-1 text-slate-500">
                    <div className="flex justify-between">
                      <span>Base Minimum:</span>
                      <span>₹{activeSimulatedTrip.baseFare.toFixed(2)}</span>
                    </div>
                    <div className="flex justify-between">
                      <span>Distance Fare (Rate/KM):</span>
                      <span>₹{(activeSimulatedTrip.totalKm * activeSimulatedTrip.farePerKm).toFixed(2)}</span>
                    </div>
                    <div className="flex justify-between">
                      <span>Waiting Charges:</span>
                      <span>₹{(activeSimulatedTrip.waitingMinutes * activeSimulatedTrip.waitingChargePerMin).toFixed(2)}</span>
                    </div>
                    <div className="flex justify-between text-slate-600">
                      <span>Subtotal Meter Fare:</span>
                      <span>₹{activeSimulatedTrip.calculatedFare.toFixed(2)}</span>
                    </div>
                    <div className="flex justify-between">
                      <span>GST Tax (5%):</span>
                      <span>₹{activeSimulatedTrip.gstAmount.toFixed(2)}</span>
                    </div>
                  </div>

                  <div className="flex justify-between items-center pt-2 border-t border-dashed border-slate-200 text-xs">
                    <span className="font-bold text-slate-900">GRAND TOTAL DUE:</span>
                    <span className="font-black text-amber-500 text-sm">
                      ₹{activeSimulatedTrip.totalWithGst.toFixed(2)}
                    </span>
                  </div>
                </div>

                {/* Share receipts */}
                <div className="space-y-2">
                  <button
                    onClick={() => showToast('Shared summary content to client via WhatsApp!')}
                    className="w-full bg-emerald-600 hover:bg-emerald-700 text-white py-2.5 rounded-xl text-[10px] font-black uppercase tracking-wider transition-all flex items-center justify-center gap-1.5"
                  >
                    <Share2 className="w-3.5 h-3.5" />
                    Share Receipt via WhatsApp
                  </button>

                  <button
                    onClick={() => showToast('Exported printable receipt PDF')}
                    className="w-full bg-slate-800 hover:bg-slate-700 text-slate-100 py-2.5 rounded-xl text-[10px] font-black uppercase tracking-wider transition-all flex items-center justify-center gap-1.5"
                  >
                    <FileText className="w-3.5 h-3.5" />
                    Export Printable PDF
                  </button>
                </div>
              </div>

              {/* Close Button */}
              <div className="p-4 bg-white border-t border-slate-200 text-center">
                <button
                  onClick={() => {
                    setActiveSimulatedTrip(null);
                    setCurrentScreen(currentRole === UserRole.ADMIN ? 'admin_dashboard' : 'driver_home');
                  }}
                  className="text-slate-500 hover:text-slate-800 text-[11px] font-bold"
                >
                  Close & Go Back
                </button>
              </div>
            </div>
          )}

        </div>
      </div>
    </div>
  );
}

// Inline custom badge component
function Badge({ text, color }: { text: string; color: 'green' | 'amber' }) {
  const styles = {
    green: 'bg-emerald-100 text-emerald-800 border-emerald-200',
    amber: 'bg-amber-100 text-amber-800 border-amber-200',
  };

  return (
    <span className={`px-2.5 py-1 text-[9px] font-bold border rounded-full uppercase tracking-widest ${styles[color]}`}>
      {text}
    </span>
  );
}
