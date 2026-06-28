import React, { useMemo } from 'react';
import { Driver, Trip, TripStatus } from '../types';

interface MapMockupProps {
  drivers: Driver[];
  trips: Trip[];
  activeTripId?: string | null;
  simulatingDriverId?: string | null;
}

export default function MapMockup({ drivers, trips, activeTripId, simulatingDriverId }: MapMockupProps) {
  // Let's establish some landmarks and coordinate mapping for our Bangalore blueprint grid
  // Latitude boundaries approximately 12.9000 to 13.0000 -> Y mapping from 450 to 50
  // Longitude boundaries approximately 77.5500 to 77.7200 -> X mapping from 50 to 450
  
  const mapWidth = 500;
  const mapHeight = 500;

  const landmarks = useMemo(() => [
    { name: 'Kemp. Int. Airport', x: 380, y: 40 },
    { name: 'MG Road Central', x: 220, y: 220 },
    { name: 'Indiranagar Metro', x: 290, y: 200 },
    { name: 'Koramangala 4th Block', x: 260, y: 310 },
    { name: 'Whitefield IT Park', x: 420, y: 240 },
    { name: 'Electronic City', x: 310, y: 440 },
    { name: 'Majestic Hub', x: 130, y: 210 },
  ], []);

  const roads = useMemo(() => [
    // NH 44 / Airport Road
    [{ x: 220, y: 220 }, { x: 220, y: 120 }, { x: 380, y: 40 }],
    // Ring Road (Whitefield to Electronic City)
    [{ x: 420, y: 240 }, { x: 350, y: 310 }, { x: 310, y: 440 }],
    // Koramangala inner roads
    [{ x: 130, y: 210 }, { x: 220, y: 220 }, { x: 290, y: 200 }, { x: 420, y: 240 }],
    // Sarjapur road link
    [{ x: 260, y: 310 }, { x: 310, y: 440 }],
  ], []);

  // Helper to get driver's visual SVG coordinates
  const getDriverCoords = (driver: Driver) => {
    // Standard mapping:
    // Lat: ~12.9000 (bottom) to ~13.0000 (top)
    // Lng: ~77.5500 (left) to ~77.7200 (right)
    const latMin = 12.9000;
    const latMax = 13.0000;
    const lngMin = 77.5500;
    const lngMax = 77.7200;

    const x = 50 + ((driver.currentLng - lngMin) / (lngMax - lngMin)) * 400;
    const y = 450 - ((driver.currentLat - latMin) / (latMax - latMin)) * 400;
    
    // Bounds check to avoid drawing off-screen
    return {
      x: Math.max(20, Math.min(mapWidth - 20, x)),
      y: Math.max(20, Math.min(mapHeight - 20, y)),
    };
  };

  return (
    <div className="relative w-full aspect-square rounded-3xl overflow-hidden bg-slate-950 border border-slate-800 shadow-2xl">
      {/* Blueprint Grid Background */}
      <div className="absolute inset-0 opacity-15 pointer-events-none" style={{
        backgroundImage: 'radial-gradient(#10b981 1px, transparent 1px), linear-gradient(to right, #1e293b 1px, transparent 1px), linear-gradient(to bottom, #1e293b 1px, transparent 1px)',
        backgroundSize: '24px 24px',
      }} />

      {/* Map Header */}
      <div className="absolute top-4 left-4 z-10 bg-slate-900/90 backdrop-blur-md px-3 py-1.5 rounded-full border border-slate-800 flex items-center gap-2">
        <span className="relative flex h-2 w-2">
          <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
          <span className="relative inline-flex rounded-full h-2 w-2 bg-emerald-500"></span>
        </span>
        <span className="font-mono text-[10px] uppercase tracking-wider text-emerald-400 font-bold">
          LIVE TELEMETRY STATION
        </span>
      </div>

      <svg viewBox={`0 0 ${mapWidth} ${mapHeight}`} className="w-full h-full select-none">
        {/* Connection Roads */}
        {roads.map((path, idx) => (
          <path
            key={idx}
            d={`M ${path.map(p => `${p.x},${p.y}`).join(' L ')}`}
            fill="none"
            stroke="#1e293b"
            strokeWidth={4}
            strokeLinecap="round"
            strokeLinejoin="round"
          />
        ))}
        {roads.map((path, idx) => (
          <path
            key={`glow-${idx}`}
            d={`M ${path.map(p => `${p.x},${p.y}`).join(' L ')}`}
            fill="none"
            stroke="#0f766e"
            strokeWidth={1.5}
            strokeOpacity={0.4}
            strokeLinecap="round"
            strokeLinejoin="round"
          />
        ))}

        {/* Landmarks */}
        {landmarks.map((l, idx) => (
          <g key={idx}>
            <circle cx={l.x} cy={l.y} r={3} fill="#475569" />
            <text
              x={l.x}
              y={l.y - 8}
              textAnchor="middle"
              className="fill-slate-500 font-sans text-[9px] font-medium tracking-tight"
            >
              {l.name}
            </text>
          </g>
        ))}

        {/* Active Trips Paths and Pins */}
        {trips
          .filter(t => t.status === TripStatus.STARTED)
          .map(t => {
            const driver = drivers.find(d => d.id === t.driverId);
            if (!driver) return null;
            const driverPos = getDriverCoords(driver);

            // Simple visual pickup coordinate map
            let pX = 220, pY = 220, dX = 380, dY = 40;
            if (t.pickupLocation.includes('Koramangala')) { pX = 260; pY = 310; }
            if (t.pickupLocation.includes('Indiranagar')) { pX = 290; pY = 200; }
            if (t.pickupLocation.includes('MG Road')) { pX = 220; pY = 220; }
            
            if (t.dropLocation.includes('Airport')) { dX = 380; dY = 40; }
            if (t.dropLocation.includes('Whitefield')) { dX = 420; dY = 240; }
            if (t.dropLocation.includes('Electronic City')) { dX = 310; dY = 440; }

            const isActive = t.id === activeTripId;

            return (
              <g key={`trip-${t.id}`}>
                {/* Visual Dotted path from pickup to drop */}
                <line
                  x1={pX}
                  y1={pY}
                  x2={dX}
                  y2={dY}
                  stroke={isActive ? '#eab308' : '#334155'}
                  strokeWidth={2}
                  strokeDasharray="4 4"
                  opacity={0.6}
                />

                {/* Pickup Pin */}
                <g transform={`translate(${pX}, ${pY})`}>
                  <circle r={7} fill="#10b981" fillOpacity={0.2} className="animate-ping" style={{ animationDuration: '3s' }} />
                  <circle r={4} fill="#10b981" />
                  <text y={12} textAnchor="middle" className="fill-emerald-400 font-mono text-[8px] font-bold">
                    P-{t.id.slice(-3)}
                  </text>
                </g>

                {/* Drop Pin */}
                <g transform={`translate(${dX}, ${dY})`}>
                  <circle r={4} fill="#ef4444" />
                  <text y={12} textAnchor="middle" className="fill-rose-400 font-mono text-[8px] font-bold">
                    D-{t.id.slice(-3)}
                  </text>
                </g>
              </g>
            );
          })}

        {/* Drivers Visual representation */}
        {drivers.map(driver => {
          const coords = getDriverCoords(driver);
          const isSimulating = driver.id === simulatingDriverId;
          const hasActiveTrip = trips.some(t => t.driverId === driver.id && t.status === TripStatus.STARTED);

          let color = '#64748b'; // Offline / Disabled
          if (driver.isEnabled) {
            if (driver.isOnline) {
              color = hasActiveTrip ? '#eab308' : '#10b981'; // Yellow if active trip, green if free
            } else {
              color = '#f8fafc';
            }
          }

          return (
            <g key={driver.id} className="transition-all duration-1000 ease-out">
              {/* Ping Ring for simulation */}
              {(isSimulating || hasActiveTrip) && (
                <circle
                  cx={coords.x}
                  cy={coords.y}
                  r={15}
                  fill="none"
                  stroke={color}
                  strokeWidth={1}
                  strokeOpacity={0.5}
                  className="animate-ping"
                  style={{ animationDuration: '2s' }}
                />
              )}

              {/* Driver Dot / Car representation */}
              <g
                transform={`translate(${coords.x}, ${coords.y})`}
                className="cursor-pointer"
              >
                {/* Back Plate */}
                <rect
                  x={-8}
                  y={-8}
                  width={16}
                  height={16}
                  rx={4}
                  fill="#020617"
                  stroke={color}
                  strokeWidth={2}
                />
                
                {/* Taxi Sign tiny dot */}
                <circle cx={0} cy={0} r={2.5} fill={color} />

                {/* Driver Tag */}
                <rect
                  x={-28}
                  y={10}
                  width={56}
                  height={12}
                  rx={3}
                  fill="#0f172a"
                  fillOpacity={0.85}
                  stroke="#334155"
                  strokeWidth={0.5}
                />
                <text
                  y={18}
                  textAnchor="middle"
                  className="fill-slate-200 font-mono text-[7px] font-bold"
                >
                  {driver.name.split(' ')[0]}
                </text>
              </g>
            </g>
          );
        })}
      </svg>
    </div>
  );
}
