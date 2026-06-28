import React, { useState } from 'react';
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
  Shield,
  MapPin,
  Clock,
  User,
  Phone,
  DollarSign,
  PlusCircle,
  TrendingUp,
  Sliders,
  Award,
  Terminal,
  Activity,
  UserCheck,
  CheckCircle,
  HelpCircle,
  AlertCircle,
  Wifi,
  Map,
  BookOpen,
} from 'lucide-react';
import MapMockup from './MapMockup';

interface DispatcherDashboardProps {
  drivers: Driver[];
  trips: Trip[];
  tariff: Tariff;
  rentalRules: RentalRules;
  onUpdateTariff: (updatedTariff: Tariff) => void;
  onUpdateRentalRules: (updatedRules: RentalRules) => void;
  onAddDriver: (driver: Driver) => void;
  onToggleDriver: (driverId: string, isEnabled: boolean) => void;
  onUpdateTrips: (updatedTrips: Trip[]) => void;
  onAddTrip: (trip: Trip) => void;
  activeSimulatedTrip: Trip | null;
}

export default function DispatcherDashboard({
  drivers,
  trips,
  tariff,
  rentalRules,
  onUpdateTariff,
  onUpdateRentalRules,
  onAddDriver,
  onToggleDriver,
  onUpdateTrips,
  onAddTrip,
  activeSimulatedTrip,
}: DispatcherDashboardProps) {
  // Local Forms
  const [newDriverName, setNewDriverName] = useState('');
  const [newDriverMobile, setNewDriverMobile] = useState('');

  // Dispatch form states
  const [dispatchCustName, setDispatchCustName] = useState('');
  const [dispatchCustMobile, setDispatchCustMobile] = useState('');
  const [dispatchPickup, setDispatchPickup] = useState('');
  const [dispatchDrop, setDispatchDrop] = useState('');
  const [dispatchTripType, setDispatchTripType] = useState<TripType>(TripType.RUNNING_METER);
  const [dispatchDriverId, setDispatchDriverId] = useState('');
  const [dispatchSuccessMsg, setDispatchSuccessMsg] = useState<string | null>(null);

  // Tariff inputs
  const [baseFare, setBaseFare] = useState(tariff.baseFare);
  const [farePerKm, setFarePerKm] = useState(tariff.farePerKm);
  const [waitingRate, setWaitingRate] = useState(tariff.waitingChargePerMin);
  const [hillActive, setHillActive] = useState(tariff.hillChargeActive);
  const [nightActive, setNightActive] = useState(tariff.nightChargeActive);

  // Rental Package inputs
  const [rentalHours, setRentalHours] = useState(rentalRules.hoursIncluded);
  const [rentalKm, setRentalKm] = useState(rentalRules.kmIncluded);
  const [extraKmRate, setExtraKmRate] = useState(rentalRules.extraKmRate);
  const [extraHourRate, setExtraHourRate] = useState(rentalRules.extraHourRate);

  const [activeTab, setActiveTab] = useState<'fleet' | 'tariffs' | 'developer'>('fleet');

  const handleSaveTariff = (e: React.FormEvent) => {
    e.preventDefault();
    onUpdateTariff({
      baseFare: parseFloat(baseFare.toString()),
      farePerKm: parseFloat(farePerKm.toString()),
      waitingChargePerMin: parseFloat(waitingRate.toString()),
      hillChargeActive: hillActive,
      nightChargeActive: nightActive,
      gstPercent: tariff.gstPercent,
    });
  };

  const handleSaveRentalRules = (e: React.FormEvent) => {
    e.preventDefault();
    onUpdateRentalRules({
      hoursIncluded: parseInt(rentalHours.toString(), 10),
      kmIncluded: parseInt(rentalKm.toString(), 10),
      extraKmRate: parseFloat(extraKmRate.toString()),
      extraHourRate: parseFloat(extraHourRate.toString()),
    });
  };

  const handleRegisterDriver = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newDriverName || !newDriverMobile) return;

    const newDriverId = `DRV${Math.floor(100 + Math.random() * 900)}`;
    const newDriverObj: Driver = {
      id: newDriverId,
      name: newDriverName,
      mobile: newDriverMobile,
      isEnabled: true,
      isOnline: false,
      currentLat: 12.95 + (Math.random() - 0.5) * 0.08,
      currentLng: 77.62 + (Math.random() - 0.5) * 0.08,
      lastUpdated: Date.now(),
    };

    onAddDriver(newDriverObj);
    setNewDriverName('');
    setNewDriverMobile('');
  };

  const handleDispatchTrip = (e: React.FormEvent) => {
    e.preventDefault();
    if (!dispatchCustName || !dispatchCustMobile || !dispatchPickup || !dispatchDrop) return;

    const driverId = dispatchDriverId || drivers.find(d => d.isEnabled)?.id || 'DRV001';
    const driverObj = drivers.find(d => d.id === driverId);

    const tripId = `TRIP${Math.floor(100 + Math.random() * 900)}`;
    const otp = Math.floor(1000 + Math.random() * 9000).toString();

    const newTripObj: Trip = {
      id: tripId,
      customerName: dispatchCustName,
      customerMobile: dispatchCustMobile,
      pickupLocation: dispatchPickup,
      dropLocation: dispatchDrop,
      tripType: dispatchTripType,
      status: TripStatus.ASSIGNED,
      driverId,
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
    setDispatchSuccessMsg(`Trip #${tripId} assigned to ${driverObj?.name || 'Driver'}! OTP: ${otp}`);
    setTimeout(() => setDispatchSuccessMsg(null), 12000);

    // Reset forms
    setDispatchCustName('');
    setDispatchCustMobile('');
    setDispatchPickup('');
    setDispatchDrop('');
  };

  return (
    <div className="flex-1 flex flex-col min-w-0 bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-2xl space-y-6">
      {/* Top Banner / Stats */}
      <div className="flex flex-col md:flex-row items-start md:items-center justify-between border-b border-slate-800 pb-5 gap-4">
        <div>
          <div className="flex items-center gap-2">
            <span className="p-1.5 rounded-lg bg-amber-500/10 text-amber-500">
              <Shield className="w-5 h-5" />
            </span>
            <h1 className="text-xl font-black text-slate-100 tracking-tight leading-none italic uppercase">
              Enterprise Dispatch Portal
            </h1>
          </div>
          <p className="text-slate-400 text-xs font-semibold mt-1">
            Centralized telemetry, bookings management, and remote tariff configuration controls.
          </p>
        </div>

        {/* Port Status info */}
        <div className="flex gap-3 text-right">
          <div className="bg-slate-950/60 border border-slate-800 px-3 py-1.5 rounded-xl text-center min-w-[70px]">
            <span className="text-[9px] text-slate-500 font-bold block uppercase tracking-wider">FLEET</span>
            <span className="text-xs font-black text-slate-200">{drivers.length}</span>
          </div>
          <div className="bg-slate-950/60 border border-slate-800 px-3 py-1.5 rounded-xl text-center min-w-[70px]">
            <span className="text-[9px] text-slate-500 font-bold block uppercase tracking-wider">ONLINE</span>
            <span className="text-xs font-black text-emerald-400">{drivers.filter(d => d.isOnline).length}</span>
          </div>
          <div className="bg-slate-950/60 border border-slate-800 px-3 py-1.5 rounded-xl text-center min-w-[70px]">
            <span className="text-[9px] text-slate-500 font-bold block uppercase tracking-wider">JOBS</span>
            <span className="text-xs font-black text-amber-500">
              {trips.filter(t => t.status !== TripStatus.ENDED).length}
            </span>
          </div>
        </div>
      </div>

      {/* Main split grid */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 flex-1 items-start">
        {/* Left Side: Live Map */}
        <div className="lg:col-span-7 flex flex-col space-y-4">
          <MapMockup
            drivers={drivers}
            trips={trips}
            activeTripId={activeSimulatedTrip?.id}
            simulatingDriverId={activeSimulatedTrip?.driverId}
          />

          {/* Quick Active trip HUD if active */}
          {activeSimulatedTrip && (
            <div className="bg-amber-400/10 border border-amber-500/20 rounded-2xl p-4 flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
              <div className="flex items-center gap-3">
                <span className="relative flex h-3 w-3 shrink-0">
                  <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-amber-400 opacity-75" />
                  <span className="relative inline-flex rounded-full h-3 w-3 bg-amber-500" />
                </span>
                <div>
                  <h4 className="text-xs font-black text-amber-400 uppercase tracking-wider">
                    SIMULATED TRIP RUNNING IN FOREGROUND
                  </h4>
                  <p className="text-slate-300 text-[11px] font-semibold">
                    Client: {activeSimulatedTrip.customerName} • Driver: {activeSimulatedTrip.driverName}
                  </p>
                </div>
              </div>
              <div className="font-mono text-xs text-right shrink-0">
                <div className="text-[10px] text-slate-500 font-bold">LIVE METER</div>
                <div className="text-amber-500 font-black text-base">₹{activeSimulatedTrip.totalWithGst.toFixed(2)}</div>
              </div>
            </div>
          )}
        </div>

        {/* Right Side: Operations / Config controls */}
        <div className="lg:col-span-5 flex flex-col space-y-4">
          {/* Tab buttons */}
          <div className="flex border-b border-slate-800">
            <button
              onClick={() => setActiveTab('fleet')}
              className={`flex-1 py-2 text-xs font-black uppercase tracking-wider border-b-2 transition-all ${
                activeTab === 'fleet'
                  ? 'border-amber-500 text-amber-500'
                  : 'border-transparent text-slate-400 hover:text-slate-200'
              }`}
            >
              Fleet & Jobs
            </button>
            <button
              onClick={() => setActiveTab('tariffs')}
              className={`flex-1 py-2 text-xs font-black uppercase tracking-wider border-b-2 transition-all ${
                activeTab === 'tariffs'
                  ? 'border-amber-500 text-amber-500'
                  : 'border-transparent text-slate-400 hover:text-slate-200'
              }`}
            >
              Tariff Settings
            </button>
            <button
              onClick={() => setActiveTab('developer')}
              className={`flex-1 py-2 text-xs font-black uppercase tracking-wider border-b-2 transition-all ${
                activeTab === 'developer'
                  ? 'border-amber-500 text-amber-500'
                  : 'border-transparent text-slate-400 hover:text-slate-200'
              }`}
            >
              Developer Hub
            </button>
          </div>

          {/* TAB: FLEET & ACTIVE JOBS */}
          {activeTab === 'fleet' && (
            <div className="space-y-4">
              {/* Online Driver Roster */}
              <div className="space-y-2">
                <h3 className="text-xs font-black text-slate-400 uppercase tracking-widest flex items-center gap-1.5">
                  <Activity className="w-3.5 h-3.5 text-amber-500" />
                  Active Driver Roster
                </h3>
                
                <div className="space-y-2 max-h-[160px] overflow-y-auto">
                  {drivers.map(driver => (
                    <div
                      key={driver.id}
                      className="bg-slate-950/60 border border-slate-800 p-3 rounded-2xl flex items-center justify-between"
                    >
                      <div className="flex items-center gap-3">
                        <div className="relative">
                          <div className="w-8 h-8 rounded-full bg-slate-800 flex items-center justify-center text-slate-300 font-bold text-xs uppercase">
                            {driver.name.split(' ').map(n => n[0]).join('')}
                          </div>
                          <span className={`absolute bottom-0 right-0 w-2.5 h-2.5 rounded-full border-2 border-slate-900 ${
                            driver.isOnline ? 'bg-emerald-400' : 'bg-slate-600'
                          }`} />
                        </div>
                        <div>
                          <div className="flex items-center gap-1">
                            <span className="text-slate-200 font-bold text-xs">{driver.name}</span>
                            <span className="text-[10px] text-slate-500 font-mono">({driver.id})</span>
                          </div>
                          <span className="text-[10px] text-slate-400 font-medium">{driver.mobile}</span>
                        </div>
                      </div>

                      <div className="flex items-center gap-2">
                        <button
                          onClick={() => onToggleDriver(driver.id, !driver.isEnabled)}
                          className={`px-2 py-1 rounded text-[9px] font-black uppercase tracking-wider transition-all ${
                            driver.isEnabled
                              ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 hover:bg-emerald-500/20'
                              : 'bg-slate-800 text-slate-400 border border-slate-700 hover:bg-slate-700'
                          }`}
                        >
                          {driver.isEnabled ? 'ENABLED' : 'DISABLED'}
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Quick Driver Addition */}
              <form onSubmit={handleRegisterDriver} className="bg-slate-950/40 border border-slate-800 p-3 rounded-2xl space-y-2">
                <h4 className="text-[10px] font-black text-slate-400 uppercase tracking-widest flex items-center gap-1">
                  <UserCheck className="w-3.5 h-3.5 text-amber-500" />
                  Quick Register Driver
                </h4>
                <div className="grid grid-cols-2 gap-2">
                  <input
                    type="text"
                    required
                    placeholder="Full Name"
                    value={newDriverName}
                    onChange={e => setNewDriverName(e.target.value)}
                    className="px-2.5 py-1.5 text-xs bg-slate-950 border border-slate-800 rounded-lg text-slate-200 focus:outline-none focus:ring-1 focus:ring-amber-500"
                  />
                  <input
                    type="tel"
                    required
                    placeholder="Mobile (+91...)"
                    value={newDriverMobile}
                    onChange={e => setNewDriverMobile(e.target.value)}
                    className="px-2.5 py-1.5 text-xs bg-slate-950 border border-slate-800 rounded-lg text-slate-200 focus:outline-none focus:ring-1 focus:ring-amber-500"
                  />
                </div>
                <button
                  type="submit"
                  className="w-full bg-slate-800 hover:bg-slate-700 text-slate-200 text-[10px] py-1.5 rounded-lg font-black uppercase tracking-wider transition-all"
                >
                  Register in Fleet
                </button>
              </form>

              {/* Booking Dispatch Desk */}
              <form onSubmit={handleDispatchTrip} className="bg-slate-950/40 border border-slate-800 p-3 rounded-2xl space-y-2">
                <h4 className="text-[10px] font-black text-slate-400 uppercase tracking-widest flex items-center gap-1">
                  <MapPin className="w-3.5 h-3.5 text-amber-500" />
                  Instant Booking Dispatch Desk
                </h4>
                
                {dispatchSuccessMsg && (
                  <div className="bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-[11px] p-2 rounded-lg font-bold">
                    {dispatchSuccessMsg}
                  </div>
                )}

                <div className="grid grid-cols-2 gap-2">
                  <input
                    type="text"
                    required
                    placeholder="Customer Name"
                    value={dispatchCustName}
                    onChange={e => setDispatchCustName(e.target.value)}
                    className="px-2.5 py-1.5 text-xs bg-slate-950 border border-slate-800 rounded-lg text-slate-200 focus:outline-none focus:ring-1 focus:ring-amber-500"
                  />
                  <input
                    type="tel"
                    required
                    placeholder="Mobile (+91...)"
                    value={dispatchCustMobile}
                    onChange={e => setDispatchCustMobile(e.target.value)}
                    className="px-2.5 py-1.5 text-xs bg-slate-950 border border-slate-800 rounded-lg text-slate-200 focus:outline-none focus:ring-1 focus:ring-amber-500"
                  />
                  <input
                    type="text"
                    required
                    placeholder="Pickup Address"
                    value={dispatchPickup}
                    onChange={e => setDispatchPickup(e.target.value)}
                    className="px-2.5 py-1.5 text-xs bg-slate-950 border border-slate-800 rounded-lg text-slate-200 focus:outline-none focus:ring-1 focus:ring-amber-500"
                  />
                  <input
                    type="text"
                    required
                    placeholder="Drop Address"
                    value={dispatchDrop}
                    onChange={e => setDispatchDrop(e.target.value)}
                    className="px-2.5 py-1.5 text-xs bg-slate-950 border border-slate-800 rounded-lg text-slate-200 focus:outline-none focus:ring-1 focus:ring-amber-500"
                  />
                </div>

                <div className="grid grid-cols-2 gap-2">
                  <select
                    value={dispatchTripType}
                    onChange={e => setDispatchTripType(e.target.value as TripType)}
                    className="px-2.5 py-1.5 text-xs bg-slate-950 border border-slate-800 rounded-lg text-slate-200 focus:outline-none focus:ring-1 focus:ring-amber-500 font-semibold"
                  >
                    <option value={TripType.RUNNING_METER}>Running Meter</option>
                    <option value={TripType.FIXED_FARE}>Fixed Fare</option>
                    <option value={TripType.AIRPORT_TRANSFER}>Airport Transfer</option>
                    <option value={TripType.RENTAL_PACKAGE}>Rental Package</option>
                    <option value={TripType.OUTSTATION}>Outstation</option>
                  </select>

                  <select
                    value={dispatchDriverId}
                    onChange={e => setDispatchDriverId(e.target.value)}
                    className="px-2.5 py-1.5 text-xs bg-slate-950 border border-slate-800 rounded-lg text-slate-200 focus:outline-none focus:ring-1 focus:ring-amber-500 font-semibold"
                  >
                    <option value="">Select Driver...</option>
                    {drivers.filter(d => d.isEnabled).map(d => (
                      <option key={d.id} value={d.id}>
                        {d.name} ({d.id}) {d.isOnline ? '🟢' : '⚫'}
                      </option>
                    ))}
                  </select>
                </div>

                <button
                  type="submit"
                  className="w-full bg-amber-500 hover:bg-amber-400 text-slate-950 text-[10px] py-1.5 rounded-lg font-black uppercase tracking-wider transition-all"
                >
                  DISPATCH TO DRIVER
                </button>
              </form>

              {/* Active Booking & Dispatched Logs */}
              <div className="space-y-2 pt-2 border-t border-slate-800">
                <h3 className="text-xs font-black text-slate-400 uppercase tracking-widest flex items-center gap-1.5">
                  <BookOpen className="w-3.5 h-3.5 text-amber-500" />
                  Live Dispatch Logs
                </h3>
                
                {trips.length === 0 ? (
                  <div className="bg-slate-950/40 border border-slate-800 p-4 rounded-2xl text-center text-xs text-slate-500">
                    No active trips logged. Use the phone's Dispatcher control to create one!
                  </div>
                ) : (
                  <div className="space-y-2 max-h-[220px] overflow-y-auto">
                    {trips.map(trip => (
                      <div
                        key={trip.id}
                        className="bg-slate-950/60 border border-slate-800 p-3 rounded-2xl space-y-2"
                      >
                        <div className="flex items-center justify-between">
                          <div className="flex items-center gap-2">
                            <span className="font-bold text-xs text-amber-500">{trip.id}</span>
                            <span className="text-[10px] bg-slate-800 text-slate-300 px-1.5 py-0.5 rounded font-bold uppercase">
                              {trip.tripType.replace('_', ' ')}
                            </span>
                          </div>
                          <span className={`px-2 py-0.5 rounded text-[9px] font-black uppercase tracking-wider ${
                            trip.status === TripStatus.ENDED ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20' :
                            trip.status === TripStatus.STARTED ? 'bg-amber-500/10 text-amber-400 border border-amber-500/20 animate-pulse' :
                            'bg-slate-800 text-slate-400 border border-slate-700'
                          }`}>
                            {trip.status}
                          </span>
                        </div>

                        <div className="grid grid-cols-2 gap-2 text-[11px] text-slate-400 font-semibold">
                          <div>
                            <span className="text-[9px] text-slate-500 block">CUSTOMER</span>
                            <span className="text-slate-200">{trip.customerName}</span>
                            <span className="text-slate-500 text-[10px] block">{trip.customerMobile}</span>
                          </div>
                          <div>
                            <span className="text-[9px] text-slate-500 block">ASSIGNED DRIVER</span>
                            <span className="text-slate-200">{trip.driverName}</span>
                            <span className="text-slate-500 text-[10px] block">({trip.driverId})</span>
                          </div>
                        </div>

                        <div className="border-t border-slate-900 pt-2 flex items-center justify-between text-[11px]">
                          <div className="flex items-center gap-1 text-slate-500">
                            <MapPin className="w-3 h-3 text-emerald-500" />
                            <span className="truncate max-w-[120px]">{trip.pickupLocation}</span>
                            <span>→</span>
                            <span className="truncate max-w-[120px]">{trip.dropLocation}</span>
                          </div>

                          <div className="bg-amber-500/10 text-amber-400 border border-amber-500/20 px-2 py-1 rounded font-mono font-bold text-xs">
                            OTP: {trip.otp}
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          )}

          {/* TAB: TARIFFS & RULES SETTINGS */}
          {activeTab === 'tariffs' && (
            <div className="space-y-4">
              {/* Distance Tariff */}
              <form onSubmit={handleSaveTariff} className="bg-slate-950/60 border border-slate-800 p-4 rounded-2xl space-y-3">
                <div className="flex items-center justify-between">
                  <h3 className="text-xs font-black text-slate-300 uppercase tracking-wider flex items-center gap-1.5">
                    <Sliders className="w-3.5 h-3.5 text-amber-500" />
                    Distance tariff rules
                  </h3>
                  <button
                    type="submit"
                    className="bg-amber-500 hover:bg-amber-600 text-slate-950 text-[10px] font-black uppercase px-2.5 py-1 rounded transition-all"
                  >
                    Save Tariff
                  </button>
                </div>

                <div className="grid grid-cols-3 gap-2">
                  <div>
                    <label className="block text-[9px] text-slate-500 font-bold uppercase mb-0.5">BASE MINIMUM</label>
                    <div className="relative">
                      <span className="absolute left-2.5 top-2 text-slate-500 font-bold text-xs">₹</span>
                      <input
                        type="number"
                        value={baseFare}
                        onChange={e => setBaseFare(parseFloat(e.target.value))}
                        className="w-full pl-6 pr-2 py-1.5 text-xs bg-slate-950 border border-slate-800 rounded-lg text-slate-200 focus:outline-none"
                      />
                    </div>
                  </div>
                  <div>
                    <label className="block text-[9px] text-slate-500 font-bold uppercase mb-0.5">RATE / KM</label>
                    <div className="relative">
                      <span className="absolute left-2.5 top-2 text-slate-500 font-bold text-xs">₹</span>
                      <input
                        type="number"
                        value={farePerKm}
                        onChange={e => setFarePerKm(parseFloat(e.target.value))}
                        className="w-full pl-6 pr-2 py-1.5 text-xs bg-slate-950 border border-slate-800 rounded-lg text-slate-200 focus:outline-none"
                      />
                    </div>
                  </div>
                  <div>
                    <label className="block text-[9px] text-slate-500 font-bold uppercase mb-0.5">WAITING RATE / MIN</label>
                    <div className="relative">
                      <span className="absolute left-2.5 top-2 text-slate-500 font-bold text-xs">₹</span>
                      <input
                        type="number"
                        step={0.05}
                        value={waitingRate}
                        onChange={e => setWaitingRate(parseFloat(e.target.value))}
                        className="w-full pl-6 pr-2 py-1.5 text-xs bg-slate-950 border border-slate-800 rounded-lg text-slate-200 focus:outline-none"
                      />
                    </div>
                  </div>
                </div>

                <div className="flex gap-4 pt-1">
                  <label className="flex items-center gap-2 cursor-pointer select-none">
                    <input
                      type="checkbox"
                      checked={hillActive}
                      onChange={e => setHillActive(e.target.checked)}
                      className="rounded border-slate-800 bg-slate-950 text-amber-500 focus:ring-0"
                    />
                    <span className="text-[10px] text-slate-400 font-bold uppercase">Hill Station Surcharge (+₹300)</span>
                  </label>

                  <label className="flex items-center gap-2 cursor-pointer select-none">
                    <input
                      type="checkbox"
                      checked={nightActive}
                      onChange={e => setNightActive(e.target.checked)}
                      className="rounded border-slate-800 bg-slate-950 text-amber-500 focus:ring-0"
                    />
                    <span className="text-[10px] text-slate-400 font-bold uppercase">Night Shift Premium (+25%)</span>
                  </label>
                </div>
              </form>

              {/* Rental Packages */}
              <form onSubmit={handleSaveRentalRules} className="bg-slate-950/60 border border-slate-800 p-4 rounded-2xl space-y-3">
                <div className="flex items-center justify-between">
                  <h3 className="text-xs font-black text-slate-300 uppercase tracking-wider flex items-center gap-1.5">
                    <Sliders className="w-3.5 h-3.5 text-amber-500" />
                    Hourly rental package rules
                  </h3>
                  <button
                    type="submit"
                    className="bg-amber-500 hover:bg-amber-600 text-slate-950 text-[10px] font-black uppercase px-2.5 py-1 rounded transition-all"
                  >
                    Save Rules
                  </button>
                </div>

                <div className="grid grid-cols-4 gap-2">
                  <div>
                    <label className="block text-[8px] text-slate-500 font-bold uppercase mb-0.5">INC. HOURS</label>
                    <input
                      type="number"
                      value={rentalHours}
                      onChange={e => setRentalHours(parseInt(e.target.value, 10))}
                      className="w-full px-2.5 py-1.5 text-xs bg-slate-950 border border-slate-800 rounded-lg text-slate-200 focus:outline-none"
                    />
                  </div>
                  <div>
                    <label className="block text-[8px] text-slate-500 font-bold uppercase mb-0.5">INC. DISTANCE</label>
                    <input
                      type="number"
                      value={rentalKm}
                      onChange={e => setRentalKm(parseInt(e.target.value, 10))}
                      className="w-full px-2.5 py-1.5 text-xs bg-slate-950 border border-slate-800 rounded-lg text-slate-200 focus:outline-none"
                    />
                  </div>
                  <div>
                    <label className="block text-[8px] text-slate-500 font-bold uppercase mb-0.5">EXTRA KM RATE</label>
                    <input
                      type="number"
                      value={extraKmRate}
                      onChange={e => setExtraKmRate(parseFloat(e.target.value))}
                      className="w-full px-2.5 py-1.5 text-xs bg-slate-950 border border-slate-800 rounded-lg text-slate-200 focus:outline-none"
                    />
                  </div>
                  <div>
                    <label className="block text-[8px] text-slate-500 font-bold uppercase mb-0.5">EXTRA HOUR RATE</label>
                    <input
                      type="number"
                      value={extraHourRate}
                      onChange={e => setExtraHourRate(parseFloat(e.target.value))}
                      className="w-full px-2.5 py-1.5 text-xs bg-slate-950 border border-slate-800 rounded-lg text-slate-200 focus:outline-none"
                    />
                  </div>
                </div>
              </form>
            </div>
          )}

          {/* TAB: DEVELOPER INSTRUCTION MANUAL */}
          {activeTab === 'developer' && (
            <div className="bg-slate-950/60 border border-slate-800 p-4 rounded-2xl space-y-3 max-h-[300px] overflow-y-auto">
              <h3 className="text-xs font-black text-slate-300 uppercase tracking-wider flex items-center gap-1.5">
                <Terminal className="w-3.5 h-3.5 text-amber-500" />
                Native Android Build Guides
              </h3>

              <div className="space-y-3 font-sans text-xs text-slate-400">
                <div className="bg-slate-900 border border-slate-800 p-3 rounded-xl space-y-1.5">
                  <span className="text-[10px] bg-amber-500/10 text-amber-400 px-2 py-0.5 rounded font-bold uppercase font-mono">
                    1. BUILD APK
                  </span>
                  <p className="text-[11px] leading-relaxed">
                    The native Android application targets **SDK 34 (Jetpack Compose)**.
                    AI Studio compiles the app using native Gradle build systems.
                  </p>
                </div>

                <div className="bg-slate-900 border border-slate-800 p-3 rounded-xl space-y-1.5">
                  <span className="text-[10px] bg-amber-500/10 text-amber-400 px-2 py-0.5 rounded font-bold uppercase font-mono">
                    2. FIRESTORE BRIDGE
                  </span>
                  <p className="text-[11px] leading-relaxed">
                    Both the Android Kotlin app and the web portal support Firebase integration.
                    When Firestore is connected, trips dispatched on this web portal sync directly to active drivers' Android phones.
                  </p>
                </div>

                <div className="bg-slate-900 border border-slate-800 p-3 rounded-xl space-y-1.5">
                  <span className="text-[10px] bg-amber-500/10 text-amber-400 px-2 py-0.5 rounded font-bold uppercase font-mono">
                    3. SOURCE FILES
                  </span>
                  <p className="text-[11px] leading-relaxed">
                    - Kotlin Applet codebase: `/app/src/main/`
                    - Web Dispatcher Client: `/src/`
                  </p>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
