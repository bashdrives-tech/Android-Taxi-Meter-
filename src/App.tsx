import React, { useState, useEffect } from 'react';
import {
  UserRole,
  TripType,
  TripStatus,
  Trip,
  Driver,
  Tariff,
  RentalRules,
} from './types';
import PhoneMockup from './components/PhoneMockup';
import DispatcherDashboard from './components/DispatcherDashboard';
import { Navigation, Compass, Layers, ShieldCheck, HelpCircle } from 'lucide-react';
import { db, auth } from './firebase';
import { signInAnonymously } from 'firebase/auth';
import {
  collection,
  doc,
  setDoc,
  onSnapshot,
} from 'firebase/firestore';

enum OperationType {
  CREATE = 'create',
  UPDATE = 'update',
  DELETE = 'delete',
  LIST = 'list',
  GET = 'get',
  WRITE = 'write',
}

interface FirestoreErrorInfo {
  error: string;
  operationType: OperationType;
  path: string | null;
  authInfo: {
    userId?: string | null;
    email?: string | null;
    emailVerified?: boolean | null;
    isAnonymous?: boolean | null;
    tenantId?: string | null;
  }
}

function handleFirestoreError(error: unknown, operationType: OperationType, path: string | null) {
  const errInfo: FirestoreErrorInfo = {
    error: error instanceof Error ? error.message : String(error),
    authInfo: {
      userId: auth.currentUser?.uid,
      email: auth.currentUser?.email,
      emailVerified: auth.currentUser?.emailVerified,
      isAnonymous: auth.currentUser?.isAnonymous,
      tenantId: auth.currentUser?.tenantId,
    },
    operationType,
    path
  };
  console.error('Firestore Error: ', JSON.stringify(errInfo));
  throw new Error(JSON.stringify(errInfo));
}

const initialDrivers: Driver[] = [
  {
    id: 'DRV001',
    name: 'Rajesh Kumar',
    mobile: '+91 98765 43210',
    isEnabled: true,
    isOnline: false,
    currentLat: 12.9716,
    currentLng: 77.5946,
    lastUpdated: Date.now(),
  },
  {
    id: 'DRV002',
    name: 'Amit Sharma',
    mobile: '+91 98765 43211',
    isEnabled: true,
    isOnline: true,
    currentLat: 12.9279,
    currentLng: 77.6271,
    lastUpdated: Date.now(),
  },
  {
    id: 'DRV003',
    name: 'Suresh Patel',
    mobile: '+91 98765 43212',
    isEnabled: false,
    isOnline: false,
    currentLat: 12.9562,
    currentLng: 77.7011,
    lastUpdated: Date.now(),
  },
];

const initialTrips: Trip[] = [
  {
    id: 'TRIP101',
    customerName: 'Arjun Mehra',
    customerMobile: '+91 99000 11223',
    pickupLocation: 'Indiranagar Metro Station',
    dropLocation: 'Kempegowda Int. Airport',
    tripType: TripType.AIRPORT_TRANSFER,
    status: TripStatus.ENDED,
    driverId: 'DRV001',
    driverName: 'Rajesh Kumar',
    otp: '5821',
    startTimestamp: Date.now() - 7200000,
    endTimestamp: Date.now() - 3600000,
    baseFare: 100.0,
    farePerKm: 30.0,
    waitingChargePerMin: 2.25,
    isHillChargeEnabled: false,
    isNightChargeEnabled: false,
    rentalHoursIncluded: 4,
    rentalKmIncluded: 40,
    rentalExtraKmRate: 20.0,
    rentalExtraHourRate: 150.0,
    totalKm: 38.5,
    waitingMinutes: 12.0,
    calculatedFare: 100.0 + (38.5 * 30.0) + (12.0 * 2.25),
    gstAmount: (100.0 + (38.5 * 30.0) + (12.0 * 2.25)) * 0.05,
    totalWithGst: (100.0 + (38.5 * 30.0) + (12.0 * 2.25)) * 1.05,
    isSynced: true,
    lastUpdated: Date.now(),
  },
  {
    id: 'TRIP102',
    customerName: 'Priya Sen',
    customerMobile: '+91 88776 65544',
    pickupLocation: 'Koramangala 4th Block',
    dropLocation: 'Whitefield IT Park',
    tripType: TripType.RUNNING_METER,
    status: TripStatus.ASSIGNED,
    driverId: 'DRV001',
    driverName: 'Rajesh Kumar',
    otp: '9430',
    baseFare: 80.0,
    farePerKm: 28.0,
    waitingChargePerMin: 2.25,
    isHillChargeEnabled: false,
    isNightChargeEnabled: false,
    rentalHoursIncluded: 4,
    rentalKmIncluded: 40,
    rentalExtraKmRate: 20.0,
    rentalExtraHourRate: 150.0,
    totalKm: 0,
    waitingMinutes: 0,
    calculatedFare: 0,
    gstAmount: 0,
    totalWithGst: 0,
    isSynced: false,
    lastUpdated: Date.now(),
  },
];

const initialTariff: Tariff = {
  baseFare: 80.0,
  farePerKm: 28.0,
  waitingChargePerMin: 2.25,
  hillChargeActive: false,
  nightChargeActive: false,
  gstPercent: 5.0,
};

const initialRentalRules: RentalRules = {
  hoursIncluded: 4,
  kmIncluded: 40,
  extraKmRate: 20.0,
  extraHourRate: 150.0,
};

export default function App() {
  const [drivers, setDrivers] = useState<Driver[]>([]);
  const [trips, setTrips] = useState<Trip[]>([]);
  const [tariff, setTariff] = useState<Tariff>(initialTariff);
  const [rentalRules, setRentalRules] = useState<RentalRules>(initialRentalRules);
  const [firebaseReady, setFirebaseReady] = useState(false);
  const [activeSimulatedTrip, setActiveSimulatedTrip] = useState<Trip | null>(null);

  useEffect(() => {
    signInAnonymously(auth)
      .then(() => {
        setFirebaseReady(true);
      })
      .catch((err) => {
        console.error("Firebase auth failed:", err);
      });
  }, []);

  useEffect(() => {
    if (!firebaseReady) return;

    // Listen to drivers
    const unsubDrivers = onSnapshot(collection(db, 'drivers'), (snapshot) => {
      if (snapshot.empty) {
        initialDrivers.forEach(async (driver) => {
          try {
            await setDoc(doc(db, 'drivers', driver.id), driver);
          } catch (err) {
            handleFirestoreError(err, OperationType.WRITE, `drivers/${driver.id}`);
          }
        });
      } else {
        const driversList: Driver[] = [];
        snapshot.forEach((doc) => {
          driversList.push(doc.data() as Driver);
        });
        driversList.sort((a, b) => a.id.localeCompare(b.id));
        setDrivers(driversList);
      }
    }, (error) => {
      handleFirestoreError(error, OperationType.GET, 'drivers');
    });

    // Listen to trips
    const unsubTrips = onSnapshot(collection(db, 'trips'), (snapshot) => {
      if (snapshot.empty) {
        initialTrips.forEach(async (trip) => {
          try {
            await setDoc(doc(db, 'trips', trip.id), trip);
          } catch (err) {
            handleFirestoreError(err, OperationType.WRITE, `trips/${trip.id}`);
          }
        });
      } else {
        const tripsList: Trip[] = [];
        snapshot.forEach((doc) => {
          tripsList.push(doc.data() as Trip);
        });
        tripsList.sort((a, b) => b.lastUpdated - a.lastUpdated);
        setTrips(tripsList);
      }
    }, (error) => {
      handleFirestoreError(error, OperationType.GET, 'trips');
    });

    // Listen to tariff
    const unsubTariff = onSnapshot(doc(db, 'settings', 'tariff'), (docSnap) => {
      if (!docSnap.exists()) {
        setDoc(doc(db, 'settings', 'tariff'), initialTariff).catch((err) => {
          handleFirestoreError(err, OperationType.WRITE, 'settings/tariff');
        });
      } else {
        setTariff(docSnap.data() as Tariff);
      }
    }, (error) => {
      handleFirestoreError(error, OperationType.GET, 'settings/tariff');
    });

    // Listen to rental rules
    const unsubRentalRules = onSnapshot(doc(db, 'settings', 'rental_rules'), (docSnap) => {
      if (!docSnap.exists()) {
        setDoc(doc(db, 'settings', 'rental_rules'), initialRentalRules).catch((err) => {
          handleFirestoreError(err, OperationType.WRITE, 'settings/rental_rules');
        });
      } else {
        setRentalRules(docSnap.data() as RentalRules);
      }
    }, (error) => {
      handleFirestoreError(error, OperationType.GET, 'settings/rental_rules');
    });

    return () => {
      unsubDrivers();
      unsubTrips();
      unsubTariff();
      unsubRentalRules();
    };
  }, [firebaseReady]);

  const handleUpdateTrips = async (updatedTrips: Trip[]) => {
    for (const trip of updatedTrips) {
      try {
        await setDoc(doc(db, 'trips', trip.id), { ...trip, isSynced: true, lastUpdated: Date.now() });
      } catch (err) {
        handleFirestoreError(err, OperationType.WRITE, `trips/${trip.id}`);
      }
    }
  };

  const handleUpdateDrivers = async (updatedDrivers: Driver[]) => {
    for (const driver of updatedDrivers) {
      try {
        await setDoc(doc(db, 'drivers', driver.id), { ...driver, lastUpdated: Date.now() });
      } catch (err) {
        handleFirestoreError(err, OperationType.WRITE, `drivers/${driver.id}`);
      }
    }
  };

  const handleAddTrip = async (newTrip: Trip) => {
    try {
      await setDoc(doc(db, 'trips', newTrip.id), { ...newTrip, isSynced: true, lastUpdated: Date.now() });
    } catch (err) {
      handleFirestoreError(err, OperationType.WRITE, `trips/${newTrip.id}`);
    }
  };

  const handleAddDriver = async (newDriver: Driver) => {
    try {
      await setDoc(doc(db, 'drivers', newDriver.id), { ...newDriver, lastUpdated: Date.now() });
    } catch (err) {
      handleFirestoreError(err, OperationType.WRITE, `drivers/${newDriver.id}`);
    }
  };

  const handleToggleDriver = async (driverId: string, isEnabled: boolean) => {
    const driver = drivers.find(d => d.id === driverId);
    if (driver) {
      try {
        await setDoc(doc(db, 'drivers', driverId), { ...driver, isEnabled, lastUpdated: Date.now() });
      } catch (err) {
        handleFirestoreError(err, OperationType.WRITE, `drivers/${driverId}`);
      }
    }
  };

  const handleUpdateTariff = async (updatedTariff: Tariff) => {
    try {
      await setDoc(doc(db, 'settings', 'tariff'), updatedTariff);
    } catch (err) {
      handleFirestoreError(err, OperationType.WRITE, 'settings/tariff');
    }
  };

  const handleUpdateRentalRules = async (updatedRules: RentalRules) => {
    try {
      await setDoc(doc(db, 'settings', 'rental_rules'), updatedRules);
    } catch (err) {
      handleFirestoreError(err, OperationType.WRITE, 'settings/rental_rules');
    }
  };


  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col font-sans select-none antialiased">
      {/* Top Header */}
      <header className="border-b border-slate-900 bg-slate-950/80 backdrop-blur-md sticky top-0 z-40 px-6 py-4 flex flex-col sm:flex-row items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-amber-400 border border-slate-900 flex items-center justify-center shadow-lg">
            <Navigation className="w-6 h-6 text-slate-950 fill-slate-950" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-md font-black tracking-tight leading-none italic text-slate-100 uppercase">
                GET TAXI METER
              </h1>
              <span className="text-[10px] bg-emerald-500/10 text-emerald-400 font-black px-2 py-0.5 rounded border border-emerald-500/20">
                ACTIVE WORKSPACE
              </span>
            </div>
            <p className="text-[11px] text-slate-500 font-semibold uppercase tracking-wider mt-1">
              Dual Screen Sandbox Simulation: Dispatcher Control Portal & Android Client Applet
            </p>
          </div>
        </div>

        {/* Quick Information pills */}
        <div className="flex items-center gap-3">
          <div className="hidden md:flex items-center gap-1.5 text-[11px] bg-slate-900/60 border border-slate-800 px-3 py-1 rounded-full text-slate-400">
            <Compass className="w-3.5 h-3.5 text-amber-400" />
            <span>Bangalore, Karnataka Hub</span>
          </div>
          <div className="flex items-center gap-1.5 text-[11px] bg-slate-900/60 border border-slate-800 px-3 py-1 rounded-full text-slate-400">
            <Layers className="w-3.5 h-3.5 text-amber-400" />
            <span>Tariff Engine v1.0.4</span>
          </div>
        </div>
      </header>

      {/* Main split work area */}
      <main className="flex-1 max-w-7xl w-full mx-auto p-4 md:p-6 lg:p-8 grid grid-cols-1 xl:grid-cols-12 gap-8 items-start">
        
        {/* Left column: Physical Android Simulator (3 cols) */}
        <div className="xl:col-span-4 flex flex-col items-center justify-center">
          <div className="w-full text-center mb-4">
            <h3 className="text-xs font-black uppercase tracking-widest text-amber-400 mb-1 flex items-center justify-center gap-1">
              <Compass className="w-3.5 h-3.5" />
              1. Active Driver Handset
            </h3>
            <p className="text-[11px] text-slate-400 font-medium">
              Interact with the Kotlin-equivalent Android Application, verify OTP, and test the dynamic travel meter.
            </p>
          </div>
          
          <PhoneMockup
            drivers={drivers}
            trips={trips}
            tariff={tariff}
            rentalRules={rentalRules}
            onUpdateTrips={handleUpdateTrips}
            onUpdateDrivers={handleUpdateDrivers}
            onAddTrip={handleAddTrip}
            activeSimulatedTrip={activeSimulatedTrip}
            setActiveSimulatedTrip={setActiveSimulatedTrip}
          />
        </div>

        {/* Right column: Dispatcher Web Panel (9 cols) */}
        <div className="xl:col-span-8 flex flex-col space-y-4">
          <div className="mb-1">
            <h3 className="text-xs font-black uppercase tracking-widest text-amber-400 mb-1 flex items-center gap-1">
              <ShieldCheck className="w-3.5 h-3.5" />
              2. Corporate Telemetries Dashboard
            </h3>
            <p className="text-[11px] text-slate-400 font-medium">
              Command station for dispatchers: map-level fleet coordinates, live bookings desks, and instant tariff updates.
            </p>
          </div>

          <DispatcherDashboard
            drivers={drivers}
            trips={trips}
            tariff={tariff}
            rentalRules={rentalRules}
            onUpdateTariff={handleUpdateTariff}
            onUpdateRentalRules={handleUpdateRentalRules}
            onAddDriver={handleAddDriver}
            onToggleDriver={handleToggleDriver}
            onUpdateTrips={handleUpdateTrips}
            onAddTrip={handleAddTrip}
            activeSimulatedTrip={activeSimulatedTrip}
          />
        </div>

      </main>

      {/* Bottom status rail */}
      <footer className="border-t border-slate-900 bg-slate-950 py-3 px-6 flex flex-col sm:flex-row items-center justify-between text-[11px] text-slate-500 font-mono gap-2 mt-auto">
        <span>© 2026 Get Taxi Meter System • Bengaluru, IN.</span>
        <div className="flex gap-4">
          <span className="flex items-center gap-1">
            <span className="w-1.5 h-1.5 rounded-full bg-emerald-400" />
            Full-Stack Compiler Active
          </span>
          <span>Port: 3000 (Vite)</span>
        </div>
      </footer>
    </div>
  );
}
