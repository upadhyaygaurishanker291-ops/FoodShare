/**
 * FoodShare Web Application
 * Smart Food Donation Network with Live Proximity Matching & Firebase Backend
 */

// Firebase Configuration from provisioned backend
const firebaseConfig = {
  apiKey: "AIzaSyDzvyLB8-pzU2tVN5vlaY6bCm-dcYdFI9o",
  authDomain: "gen-lang-client-0803718721.firebaseapp.com",
  projectId: "gen-lang-client-0803718721",
  storageBucket: "gen-lang-client-0803718721.firebasestorage.app",
  messagingSenderId: "67702983399",
  appId: "1:67702983399:android:e1c36698638fec9865b042"
};

let db = null;
try {
  if (typeof firebase !== 'undefined') {
    firebase.initializeApp(firebaseConfig);
    db = firebase.firestore();
  }
} catch (e) {
  console.warn("Firebase initialized in offline/fallback mode:", e);
}

// Initial Sample Users / Partners
const INITIAL_USERS = [
  {
    id: "donor_hyatt",
    name: "Chef Marco",
    role: "DONOR",
    organization: "Grand Hyatt Regency",
    phone: "+1 (555) 234-5678",
    address: "120 Market St, Downtown",
    lat: 37.7937,
    lon: -122.3965
  },
  {
    id: "donor_bistro",
    name: "Elena Rostova",
    role: "DONOR",
    organization: "Green Leaf Artisan Bistro",
    phone: "+1 (555) 345-6789",
    address: "450 Valencia St, Mission",
    lat: 37.7648,
    lon: -122.4219
  },
  {
    id: "ngo_hope",
    name: "Hope Community Food Bank",
    role: "NGO",
    organization: "Hope Food Rescue & Shelter",
    phone: "+1 (555) 890-1234",
    address: "950 Howard St, SoMa",
    lat: 37.7816,
    lon: -122.4058
  },
  {
    id: "vol_sarah",
    name: "Sarah Jenkins",
    role: "VOLUNTEER",
    organization: "EcoBike Deliveries",
    phone: "+1 (555) 678-9012",
    address: "Mission District Courier Hub",
    lat: 37.7680,
    lon: -122.4190
  }
];

// Initial Food Donations Data
const INITIAL_DONATIONS = [
  {
    id: "don_101",
    donorId: "donor_hyatt",
    donorName: "Chef Marco",
    donorOrg: "Grand Hyatt Regency",
    donorPhone: "+1 (555) 234-5678",
    title: "Executive Banquet: Mediterranean Rice & Veggies",
    description: "Steam-table surplus in insulated stainless containers. Kept warm at 145°F.",
    quantityMeals: 45,
    quantityWeightKg: 18.0,
    foodType: "VEG",
    cookedTime: Date.now() - 3600000,
    expiryTime: Date.now() + 14400000,
    pickupAddress: "120 Market St, Loading Dock Bay 2",
    pickupInstructions: "Ring service bell; ask for Kitchen Steward Marco",
    lat: 37.7937,
    lon: -122.3965,
    status: "PENDING",
    verificationOtp: "4912"
  },
  {
    id: "don_102",
    donorId: "donor_bistro",
    donorName: "Elena Rostova",
    donorOrg: "Green Leaf Artisan Bistro",
    donorPhone: "+1 (555) 345-6789",
    title: "Herb Roasted Chicken & Steamed Green Beans",
    description: "Rapidly chilled to 38°F after lunch service. Packaged in aluminum trays.",
    quantityMeals: 35,
    quantityWeightKg: 14.5,
    foodType: "NON_VEG",
    cookedTime: Date.now() - 7200000,
    expiryTime: Date.now() + 10800000,
    pickupAddress: "450 Valencia St, Mission",
    pickupInstructions: "Back alley kitchen door, ring Bistro bell",
    lat: 37.7648,
    lon: -122.4219,
    status: "PENDING",
    verificationOtp: "7820"
  },
  {
    id: "don_103",
    donorId: "donor_hyatt",
    donorName: "Chef Marco",
    donorOrg: "Grand Hyatt Regency",
    donorPhone: "+1 (555) 234-5678",
    title: "Fresh Baked Artisan Sourdough & Brioche",
    description: "Surplus baked goods packed cleanly in food-grade kraft bags.",
    quantityMeals: 30,
    quantityWeightKg: 12.0,
    foodType: "BAKERY",
    cookedTime: Date.now() - 5400000,
    expiryTime: Date.now() + 28800000,
    pickupAddress: "120 Market St, Service Bay 2",
    pickupInstructions: "Thermal bags recommended",
    lat: 37.7937,
    lon: -122.3965,
    status: "CLAIMED",
    claimedByNgoId: "ngo_hope",
    claimedByNgoName: "Hope Food Rescue & Shelter",
    claimedAt: Date.now() - 1800000,
    verificationOtp: "6321"
  },
  {
    id: "don_104",
    donorId: "donor_bistro",
    donorName: "Elena Rostova",
    donorOrg: "Green Leaf Artisan Bistro",
    donorPhone: "+1 (555) 345-6789",
    title: "Organic Quinoa Salad & Lentil Bowls",
    description: "Individually packed compostable bowls. En route with courier.",
    quantityMeals: 40,
    quantityWeightKg: 16.0,
    foodType: "VEGAN",
    cookedTime: Date.now() - 10800000,
    expiryTime: Date.now() + 7200000,
    pickupAddress: "450 Valencia St, Mission",
    lat: 37.7648,
    lon: -122.4219,
    status: "OUT_FOR_DELIVERY",
    claimedByNgoId: "ngo_hope",
    claimedByNgoName: "Hope Food Rescue & Shelter",
    assignedVolunteerId: "vol_sarah",
    assignedVolunteerName: "Sarah Jenkins",
    outForDeliveryAt: Date.now() - 900000,
    verificationOtp: "9143"
  },
  {
    id: "don_105",
    donorId: "donor_hyatt",
    donorName: "Chef Marco",
    donorOrg: "Grand Hyatt Regency",
    donorPhone: "+1 (555) 234-5678",
    title: "Morning Pastries & Breakfast Frittatas",
    description: "Morning breakfast buffet surplus delivered to shelter.",
    quantityMeals: 60,
    quantityWeightKg = 25.0,
    foodType: "VEG",
    cookedTime: Date.now() - 86400000,
    expiryTime: Date.now() - 72000000,
    pickupAddress: "120 Market St, Downtown",
    lat: 37.7937,
    lon: -122.3965,
    status: "DELIVERED",
    claimedByNgoId: "ngo_hope",
    claimedByNgoName: "Hope Food Rescue & Shelter",
    assignedVolunteerId: "vol_sarah",
    assignedVolunteerName: "Sarah Jenkins",
    deliveredAt: Date.now() - 70000000,
    verificationOtp: "1190"
  }
];

// App State
let users = JSON.parse(localStorage.getItem('foodshare_users')) || INITIAL_USERS;
let donations = JSON.parse(localStorage.getItem('foodshare_donations')) || INITIAL_DONATIONS;
let currentUser = users[0];
let currentCategoryFilter = 'ALL';
let claimingDonationId = null;

// User coordinates (defaults to SoMa / Downtown SF)
let userLat = currentUser.lat || 37.7816;
let userLon = currentUser.lon || -122.4058;

// Haversine Distance Formula in Kilometers
function calculateDistanceKm(lat1, lon1, lat2, lon2) {
  const R = 6371; // Earth radius in km
  const dLat = (lat2 - lat1) * Math.PI / 180;
  const dLon = (lon2 - lon1) * Math.PI / 180;
  const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
}

function formatDistance(distKm) {
  if (distKm < 1) return `${Math.round(distKm * 1000)} m away`;
  return `${distKm.toFixed(1)} km away`;
}

function formatExpiryCountdown(expiryMillis) {
  const diff = expiryMillis - Date.now();
  if (diff <= 0) return 'Expired';
  const hours = Math.floor(diff / 3600000);
  const minutes = Math.floor((diff % 3600000) / 60000);
  return hours > 0 ? `Expires in ${hours}h ${minutes}m` : `Expires in ${minutes}m`;
}

// Save state
function persistData() {
  localStorage.setItem('foodshare_donations', JSON.stringify(donations));
  localStorage.setItem('foodshare_users', JSON.stringify(users));
}

// UI Tabs
function switchTab(tabId) {
  document.querySelectorAll('main > section').forEach(sec => sec.classList.add('hidden'));
  document.querySelectorAll('header nav button').forEach(btn => btn.classList.remove('tab-active', 'text-emerald-600'));

  const view = document.getElementById(`tab-${tabId}-view`);
  if (view) view.classList.remove('hidden');

  const navBtn = document.getElementById(`nav-${tabId}`);
  if (navBtn) navBtn.classList.add('tab-active', 'text-emerald-600');

  if (tabId === 'donor') renderDonorHub();
  if (tabId === 'feed') renderFeed();
  if (tabId === 'logistics') renderLogistics();
  if (tabId === 'impact') renderImpact();

  if (window.lucide) lucide.createIcons();
}

// Update Active Role UI
function updateRoleDisplay() {
  const badge = document.getElementById('current-role-badge');
  badge.textContent = `${currentUser.role}: ${currentUser.organization}`;
  userLat = currentUser.lat;
  userLon = currentUser.lon;
}

// Render Donor Hub
function renderDonorHub() {
  const myDonations = donations.filter(d => d.donorId === currentUser.id);
  const container = document.getElementById('donor-listings-container');

  document.getElementById('donor-total-meals').textContent = myDonations.reduce((acc, d) => acc + d.quantityMeals, 0);
  document.getElementById('donor-active-listings').textContent = myDonations.filter(d => d.status === 'PENDING' || d.status === 'CLAIMED').length;
  document.getElementById('donor-delivered-count').textContent = myDonations.filter(d => d.status === 'DELIVERED').length;

  if (myDonations.length === 0) {
    container.innerHTML = `
      <div class="col-span-full py-12 text-center text-slate-400">
        <i data-lucide="package" class="w-12 h-12 mx-auto mb-2 opacity-50"></i>
        <p class="font-medium">No food listings posted yet.</p>
        <p class="text-xs">Click 'Post Excess Food' to list your kitchen's surplus meals.</p>
      </div>`;
    return;
  }

  container.innerHTML = myDonations.map(item => `
    <div class="bg-white rounded-2xl border border-slate-200 p-5 shadow-sm space-y-4 hover:shadow-md transition">
      <div class="flex justify-between items-start">
        <span class="inline-flex px-2.5 py-1 rounded-full text-xs font-bold ${getStatusBadgeClass(item.status)}">
          ${item.status}
        </span>
        <span class="text-xs text-amber-600 font-semibold flex items-center gap-1">
          <i data-lucide="clock" class="w-3.5 h-3.5"></i> ${formatExpiryCountdown(item.expiryTime)}
        </span>
      </div>

      <div>
        <h3 class="font-bold text-slate-900 text-base leading-snug">${item.title}</h3>
        <p class="text-xs text-slate-500 mt-1 line-clamp-2">${item.description || 'No additional packaging notes'}</p>
      </div>

      <div class="flex flex-wrap gap-2 text-xs">
        <span class="bg-emerald-50 text-emerald-700 px-2.5 py-1 rounded-lg font-semibold">${item.quantityMeals} Meals</span>
        <span class="bg-slate-100 text-slate-700 px-2.5 py-1 rounded-lg font-semibold">${item.quantityWeightKg} kg</span>
        <span class="badge-${item.foodType.toLowerCase().replace('_', '-')} px-2.5 py-1 rounded-lg font-semibold">${item.foodType}</span>
      </div>

      <!-- Stepper Progress -->
      <div class="pt-2">
        ${renderMiniStepper(item.status)}
      </div>

      <div class="bg-slate-50 rounded-xl p-3 text-xs space-y-1.5 border border-slate-100">
        <div class="flex items-center gap-1.5 text-slate-700">
          <i data-lucide="map-pin" class="w-3.5 h-3.5 text-emerald-600"></i>
          <span>${item.pickupAddress}</span>
        </div>
        ${item.claimedByNgoName ? `
          <div class="font-semibold text-emerald-700">Claimed by: ${item.claimedByNgoName}</div>
        ` : '<div class="text-slate-400 italic">Waiting for nearby NGO claim...</div>'}
        <div class="font-bold text-slate-900 pt-1">Handoff OTP: <span class="text-emerald-700">${item.verificationOtp}</span></div>
      </div>
    </div>
  `).join('');

  if (window.lucide) lucide.createIcons();
}

// Render NGO Live Feed
function renderFeed() {
  const container = document.getElementById('feed-items-container');
  const query = (document.getElementById('feed-search-input')?.value || '').toLowerCase();
  const maxDistance = parseFloat(document.getElementById('feed-distance-filter')?.value || '25');

  const pendingListings = donations
    .filter(d => d.status === 'PENDING')
    .map(d => ({
      ...d,
      distanceKm: calculateDistanceKm(userLat, userLon, d.lat, d.lon)
    }))
    .filter(d => {
      const matchCat = currentCategoryFilter === 'ALL' || d.foodType === currentCategoryFilter;
      const matchDist = d.distanceKm <= maxDistance;
      const matchQuery = query === '' ||
        d.title.toLowerCase().includes(query) ||
        d.donorOrg.toLowerCase().includes(query) ||
        d.pickupAddress.toLowerCase().includes(query);
      return matchCat && matchDist && matchQuery;
    })
    .sort((a, b) => a.distanceKm - b.distanceKm);

  if (pendingListings.length === 0) {
    container.innerHTML = `
      <div class="col-span-full py-16 text-center text-slate-400">
        <i data-lucide="utensils" class="w-12 h-12 mx-auto mb-2 opacity-50"></i>
        <p class="font-bold text-slate-700 text-base">No matching food donations</p>
        <p class="text-xs text-slate-500">Try expanding the distance radius or selecting another category.</p>
      </div>`;
    return;
  }

  container.innerHTML = pendingListings.map(item => `
    <div class="bg-white rounded-2xl border border-slate-200 p-5 shadow-sm space-y-4 hover:shadow-md transition">
      <div class="flex justify-between items-center">
        <span class="inline-flex items-center gap-1 bg-emerald-100 text-emerald-800 text-xs font-bold px-2.5 py-1 rounded-full">
          <i data-lucide="navigation" class="w-3 h-3"></i> ${formatDistance(item.distanceKm)}
        </span>
        <span class="text-xs text-amber-600 font-semibold flex items-center gap-1">
          <i data-lucide="clock" class="w-3.5 h-3.5"></i> ${formatExpiryCountdown(item.expiryTime)}
        </span>
      </div>

      <div>
        <div class="text-xs font-semibold text-slate-500 mb-0.5">${item.donorOrg}</div>
        <h3 class="font-bold text-slate-900 text-base leading-snug">${item.title}</h3>
        <p class="text-xs text-slate-500 mt-1 line-clamp-2">${item.description}</p>
      </div>

      <div class="flex flex-wrap gap-2 text-xs">
        <span class="bg-emerald-50 text-emerald-700 px-2.5 py-1 rounded-lg font-semibold">${item.quantityMeals} Meals</span>
        <span class="bg-slate-100 text-slate-700 px-2.5 py-1 rounded-lg font-semibold">${item.quantityWeightKg} kg</span>
        <span class="badge-${item.foodType.toLowerCase().replace('_', '-')} px-2.5 py-1 rounded-lg font-semibold">${item.foodType}</span>
      </div>

      <div class="pt-2 border-t border-slate-100 flex items-center justify-between">
        <div class="text-xs text-slate-500 truncate max-w-[180px]">
          <i data-lucide="map-pin" class="w-3.5 h-3.5 inline mr-1 text-slate-400"></i> ${item.pickupAddress}
        </div>
        <button onclick="openClaimModal('${item.id}')" class="bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold px-4 py-2 rounded-xl shadow-sm transition">
          Claim / Accept
        </button>
      </div>
    </div>
  `).join('');

  if (window.lucide) lucide.createIcons();
}

function filterFeed() {
  renderFeed();
}

function setCategoryFilter(cat) {
  currentCategoryFilter = cat;
  document.querySelectorAll('.category-pill').forEach(btn => {
    if (btn.dataset.category === cat) {
      btn.className = 'category-pill active bg-emerald-600 text-white px-3 py-1 rounded-lg text-xs font-semibold transition';
    } else {
      btn.className = 'category-pill bg-slate-100 text-slate-700 hover:bg-slate-200 px-3 py-1 rounded-lg text-xs font-semibold transition';
    }
  });
  renderFeed();
}

// Logistics Screen
let activeLogisticsSubtab = 'available';

function switchLogisticsSubtab(subtab) {
  activeLogisticsSubtab = subtab;
  const btnAvail = document.getElementById('subtab-available-tasks');
  const btnActive = document.getElementById('subtab-active-deliveries');

  if (subtab === 'available') {
    btnAvail.className = 'py-2.5 px-4 text-sm font-bold border-b-2 border-emerald-600 text-emerald-600';
    btnActive.className = 'py-2.5 px-4 text-sm font-semibold text-slate-600 hover:text-emerald-600';
  } else {
    btnActive.className = 'py-2.5 px-4 text-sm font-bold border-b-2 border-emerald-600 text-emerald-600';
    btnAvail.className = 'py-2.5 px-4 text-sm font-semibold text-slate-600 hover:text-emerald-600';
  }
  renderLogistics();
}

function renderLogistics() {
  const container = document.getElementById('logistics-container');
  const claimedUnassigned = donations.filter(d => d.status === 'CLAIMED' && !d.assignedVolunteerId);
  const activeDeliveries = donations.filter(d => d.status === 'CLAIMED' || d.status === 'OUT_FOR_DELIVERY');

  document.getElementById('count-unassigned-tasks').textContent = claimedUnassigned.length;
  document.getElementById('count-active-tasks').textContent = activeDeliveries.length;

  const currentList = activeLogisticsSubtab === 'available' ? claimedUnassigned : activeDeliveries;

  if (currentList.length === 0) {
    container.innerHTML = `
      <div class="col-span-full py-16 text-center text-slate-400">
        <i data-lucide="bike" class="w-12 h-12 mx-auto mb-2 opacity-50"></i>
        <p class="font-bold text-slate-700 text-base">No tasks in this view</p>
        <p class="text-xs text-slate-500">Pickups will appear here when an NGO claims available food.</p>
      </div>`;
    return;
  }

  container.innerHTML = currentList.map(item => `
    <div class="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm space-y-4">
      <div class="flex justify-between items-start">
        <span class="inline-flex px-2.5 py-1 rounded-full text-xs font-bold ${getStatusBadgeClass(item.status)}">
          ${item.status}
        </span>
        <span class="text-xs font-bold text-emerald-700 bg-emerald-50 px-2.5 py-1 rounded-lg">
          ${item.quantityMeals} Meals • ${item.quantityWeightKg} kg
        </span>
      </div>

      <h3 class="text-base font-bold text-slate-900">${item.title}</h3>

      <!-- Route Card: Point A to Point B -->
      <div class="bg-emerald-50/70 rounded-2xl p-4 border border-emerald-100 space-y-3 text-xs">
        <div class="flex items-start gap-2.5">
          <div class="w-5 h-5 rounded-full bg-emerald-600 text-white font-bold flex items-center justify-center shrink-0 text-[10px]">A</div>
          <div>
            <div class="font-bold text-slate-900">PICKUP: ${item.donorOrg}</div>
            <div class="text-slate-500">${item.pickupAddress}</div>
            <div class="text-slate-500 text-[11px] mt-0.5">Contact: ${item.donorName} (${item.donorPhone})</div>
          </div>
        </div>

        <div class="flex items-start gap-2.5 pt-2 border-t border-emerald-100/60">
          <div class="w-5 h-5 rounded-full bg-teal-600 text-white font-bold flex items-center justify-center shrink-0 text-[10px]">B</div>
          <div>
            <div class="font-bold text-slate-900">DELIVER TO: ${item.claimedByNgoName || 'Claimed NGO'}</div>
            <div class="text-slate-500">Community Shelter Pantry Location</div>
          </div>
        </div>
      </div>

      <!-- Stepper -->
      <div>${renderMiniStepper(item.status)}</div>

      <!-- Action Buttons -->
      <div class="pt-2 flex gap-3">
        ${item.status === 'CLAIMED' && !item.assignedVolunteerId ? `
          <button onclick="acceptPickupTask('${item.id}')" class="w-full bg-emerald-600 hover:bg-emerald-700 text-white py-2.5 rounded-xl font-bold text-xs shadow-md shadow-emerald-200 transition">
            Accept Delivery Task
          </button>
        ` : ''}

        ${item.status === 'CLAIMED' && item.assignedVolunteerId ? `
          <button onclick="startDeliveryRoute('${item.id}')" class="w-full bg-emerald-600 hover:bg-emerald-700 text-white py-2.5 rounded-xl font-bold text-xs shadow-md shadow-emerald-200 transition">
            Picked Up Food • Start Route
          </button>
        ` : ''}

        ${item.status === 'OUT_FOR_DELIVERY' ? `
          <button onclick="promptCompleteDelivery('${item.id}')" class="w-full bg-emerald-600 hover:bg-emerald-700 text-white py-2.5 rounded-xl font-bold text-xs shadow-md shadow-emerald-200 transition">
            Arrived & Confirm Delivered
          </button>
        ` : ''}
      </div>
    </div>
  `).join('');

  if (window.lucide) lucide.createIcons();
}

// Render Impact Metrics & Leaderboard
function renderImpact() {
  const delivered = donations.filter(d => d.status === 'DELIVERED');
  const totalMeals = donations.filter(d => d.status !== 'CANCELLED').reduce((acc, d) => acc + d.quantityMeals, 0);
  const totalKg = donations.filter(d => d.status !== 'CANCELLED').reduce((acc, d) => acc + d.quantityWeightKg, 0);
  const co2 = (totalKg * 2.5).toFixed(0);

  document.getElementById('stat-meals-saved').textContent = totalMeals;
  document.getElementById('stat-kg-diverted').textContent = `${totalKg.toFixed(0)} kg`;
  document.getElementById('stat-co2-avoided').textContent = `${co2} kg`;
  document.getElementById('stat-partner-count').textContent = users.length;

  const milestonePct = Math.min(100, Math.round((totalMeals / 1000) * 100));
  document.getElementById('milestone-percentage').textContent = `${milestonePct}%`;
  document.getElementById('milestone-progress-bar').style.width = `${milestonePct}%`;

  // Top Donors
  const donorStats = {};
  donations.forEach(d => {
    if (!donorStats[d.donorOrg]) donorStats[d.donorOrg] = { meals: 0, donor: d.donorName };
    donorStats[d.donorOrg].meals += d.quantityMeals;
  });

  const sortedDonors = Object.entries(donorStats).sort((a, b) => b[1].meals - a[1].meals);
  document.getElementById('leaderboard-donors').innerHTML = sortedDonors.map(([org, data], idx) => `
    <div class="py-3 flex items-center justify-between text-sm">
      <div class="flex items-center space-x-3">
        <span class="w-6 h-6 rounded-full flex items-center justify-center font-bold text-xs ${idx === 0 ? 'bg-amber-100 text-amber-800' : 'bg-slate-100 text-slate-700'}">
          ${idx + 1}
        </span>
        <div>
          <div class="font-bold text-slate-900">${org}</div>
          <div class="text-xs text-slate-500">${data.donor}</div>
        </div>
      </div>
      <div class="text-right">
        <div class="font-bold text-emerald-700">${data.meals} Meals</div>
        <div class="text-[11px] text-slate-400">Total shared</div>
      </div>
    </div>
  `).join('');

  // Top Volunteers
  const volunteers = [
    { name: "Sarah Jenkins", org: "EcoBike Deliveries", deliveries: 16 },
    { name: "Marcus Chen", org: "Community EV Express", deliveries: 11 },
    { name: "Amina Yusuf", org: "Pedal Pantry Courier", deliveries: 7 }
  ];

  document.getElementById('leaderboard-volunteers').innerHTML = volunteers.map((vol, idx) => `
    <div class="py-3 flex items-center justify-between text-sm">
      <div class="flex items-center space-x-3">
        <span class="w-6 h-6 rounded-full flex items-center justify-center font-bold text-xs ${idx === 0 ? 'bg-amber-100 text-amber-800' : 'bg-slate-100 text-slate-700'}">
          ${idx + 1}
        </span>
        <div>
          <div class="font-bold text-slate-900">${vol.name}</div>
          <div class="text-xs text-slate-500">${vol.org}</div>
        </div>
      </div>
      <div class="text-right">
        <div class="font-bold text-emerald-700">${vol.deliveries} Deliveries</div>
        <div class="text-[11px] text-slate-400">Completed</div>
      </div>
    </div>
  `).join('');
}

// Stepper Generator
function renderMiniStepper(status) {
  const steps = ["PENDING", "CLAIMED", "OUT_FOR_DELIVERY", "DELIVERED"];
  const stepLabels = ["Posted", "Claimed", "In Transit", "Delivered"];
  const currentIdx = steps.indexOf(status);

  return `
    <div class="flex items-center justify-between text-[11px] text-slate-500 font-semibold px-1">
      ${stepLabels.map((label, idx) => `
        <div class="flex items-center ${idx <= currentIdx ? 'text-emerald-700 font-bold' : 'text-slate-400'}">
          <span class="w-2 h-2 rounded-full mr-1.5 ${idx <= currentIdx ? 'bg-emerald-600 ring-2 ring-emerald-200' : 'bg-slate-300'}"></span>
          <span>${label}</span>
        </div>
      `).join('<div class="flex-1 h-0.5 mx-2 bg-slate-200"></div>')}
    </div>
  `;
}

function getStatusBadgeClass(status) {
  switch (status) {
    case 'PENDING': return 'bg-amber-100 text-amber-800';
    case 'CLAIMED': return 'bg-indigo-100 text-indigo-800';
    case 'OUT_FOR_DELIVERY': return 'bg-orange-100 text-orange-800';
    case 'DELIVERED': return 'bg-emerald-100 text-emerald-800';
    default: return 'bg-slate-100 text-slate-800';
  }
}

// User Actions: Post Food
function handlePostFood(e) {
  e.preventDefault();
  const title = document.getElementById('food-title').value.trim();
  const desc = document.getElementById('food-desc').value.trim();
  const meals = parseInt(document.getElementById('food-meals').value, 10);
  const weight = parseFloat(document.getElementById('food-weight').value);
  const category = document.getElementById('food-category').value;
  const expiryHours = parseInt(document.getElementById('food-expiry').value, 10);
  const address = document.getElementById('food-address').value.trim();
  const instructions = document.getElementById('food-instructions').value.trim();

  const newDonation = {
    id: 'don_' + Date.now().toString().slice(-6),
    donorId: currentUser.id,
    donorName: currentUser.name,
    donorOrg: currentUser.organization,
    donorPhone: currentUser.phone,
    title,
    description: desc,
    quantityMeals: meals,
    quantityWeightKg: weight,
    foodType: category,
    cookedTime: Date.now(),
    expiryTime: Date.now() + (expiryHours * 3600000),
    pickupAddress: address,
    pickupInstructions: instructions,
    lat: currentUser.lat,
    lon: currentUser.lon,
    status: 'PENDING',
    verificationOtp: Math.floor(1000 + Math.random() * 9000).toString()
  };

  donations.unshift(newDonation);
  persistData();

  if (db) {
    db.collection('food_donations').doc(newDonation.id).set(newDonation).catch(err => console.log('Firestore sync:', err));
  }

  closeModal('modal-post-food');
  showToast('Food donation published! Alert sent to local NGOs.');
  switchTab('donor');
}

// Claim Action
function openClaimModal(id) {
  claimingDonationId = id;
  const item = donations.find(d => d.id === id);
  if (!item) return;

  document.getElementById('claim-summary-card').innerHTML = `
    <div class="font-bold text-slate-900 text-sm">${item.title}</div>
    <div class="text-emerald-700 font-semibold">${item.quantityMeals} Meals (${item.quantityWeightKg} kg) from ${item.donorOrg}</div>
    <div class="text-slate-500">Pickup: ${item.pickupAddress}</div>
  `;

  document.getElementById('modal-claim').classList.remove('hidden');
  document.getElementById('modal-claim').classList.add('flex');
}

function executeClaim() {
  if (!claimingDonationId) return;
  const item = donations.find(d => d.id === claimingDonationId);
  if (item) {
    item.status = 'CLAIMED';
    item.claimedByNgoId = currentUser.id;
    item.claimedByNgoName = currentUser.organization;
    item.claimedAt = Date.now();
    persistData();

    if (db) {
      db.collection('food_donations').doc(item.id).update({
        status: 'CLAIMED',
        claimedByNgoId: currentUser.id,
        claimedByNgoName: currentUser.organization,
        claimedAt: Date.now()
      }).catch(e => console.log(e));
    }
  }

  closeModal('modal-claim');
  showToast('Donation secured! Ready for volunteer pickup.');
  switchTab('logistics');
}

// Delivery Lifecycle Transitions
function acceptPickupTask(id) {
  const item = donations.find(d => d.id === id);
  if (item) {
    item.assignedVolunteerId = currentUser.id;
    item.assignedVolunteerName = currentUser.name;
    persistData();
    showToast('Task assigned! Head to pickup location.');
    renderLogistics();
  }
}

function startDeliveryRoute(id) {
  const item = donations.find(d => d.id === id);
  if (item) {
    item.status = 'OUT_FOR_DELIVERY';
    item.outForDeliveryAt = Date.now();
    persistData();
    showToast('Package picked up! In transit to destination.');
    renderLogistics();
  }
}

function promptCompleteDelivery(id) {
  const item = donations.find(d => d.id === id);
  if (!item) return;

  const enteredOtp = prompt(`Enter recipient verification code (Expected: ${item.verificationOtp}):`, item.verificationOtp);
  if (enteredOtp === item.verificationOtp) {
    item.status = 'DELIVERED';
    item.deliveredAt = Date.now();
    persistData();
    showToast('Delivery completed! Impact statistics updated.');
    renderLogistics();
  } else if (enteredOtp !== null) {
    alert('Incorrect verification OTP code. Please check with recipient.');
  }
}

// Geolocation
function detectGeolocation() {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      pos => {
        userLat = pos.coords.latitude;
        userLon = pos.coords.longitude;
        document.getElementById('food-address').value = `Current Location (${userLat.toFixed(4)}, ${userLon.toFixed(4)})`;
        showToast('GPS coordinates acquired successfully.');
      },
      err => {
        showToast('Using downtown reference coordinates.');
      }
    );
  } else {
    showToast('Geolocation not supported by browser.');
  }
}

// Modals
function openNewDonationModal() {
  document.getElementById('modal-post-food').classList.remove('hidden');
  document.getElementById('modal-post-food').classList.add('flex');
}

function openRoleModal() {
  const list = document.getElementById('role-selector-list');
  list.innerHTML = users.map(u => `
    <div onclick="selectUser('${u.id}')" class="p-3.5 rounded-2xl border ${u.id === currentUser.id ? 'border-emerald-600 bg-emerald-50' : 'border-slate-200 hover:bg-slate-50'} cursor-pointer flex items-center justify-between transition">
      <div>
        <div class="font-bold text-slate-900 text-sm">${u.organization}</div>
        <div class="text-xs text-slate-500">${u.role} • ${u.name}</div>
      </div>
      ${u.id === currentUser.id ? '<i data-lucide="check" class="w-5 h-5 text-emerald-600"></i>' : ''}
    </div>
  `).join('');

  document.getElementById('modal-role').classList.remove('hidden');
  document.getElementById('modal-role').classList.add('flex');
  if (window.lucide) lucide.createIcons();
}

function selectUser(id) {
  currentUser = users.find(u => u.id === id) || users[0];
  updateRoleDisplay();
  closeModal('modal-role');
  showToast(`Active role switched to ${currentUser.organization}`);
  switchTab(currentUser.role === 'DONOR' ? 'donor' : currentUser.role === 'NGO' ? 'feed' : 'logistics');
}

function closeModal(id) {
  const el = document.getElementById(id);
  if (el) {
    el.classList.add('hidden');
    el.classList.remove('flex');
  }
}

function showToast(msg) {
  const toast = document.getElementById('toast');
  const msgEl = document.getElementById('toast-message');
  msgEl.textContent = msg;
  toast.classList.remove('translate-y-24', 'opacity-0');
  setTimeout(() => {
    toast.classList.add('translate-y-24', 'opacity-0');
  }, 3200);
}

// Initialize on Load
window.addEventListener('DOMContentLoaded', () => {
  updateRoleDisplay();
  switchTab('feed');
  if (window.lucide) lucide.createIcons();
});
