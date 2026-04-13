// Fake user data for the Users page.
export const initialUsers = [
  { id: 1, name: 'Olivia Carter', email: 'olivia.carter@demoapp.test', role: 'Admin', status: 'Active' },
  { id: 2, name: 'Ethan Brooks', email: 'ethan.brooks@demoapp.test', role: 'Manager', status: 'Active' },
  { id: 3, name: 'Mia Turner', email: 'mia.turner@demoapp.test', role: 'Support', status: 'Inactive' },
  { id: 4, name: 'Noah Bennett', email: 'noah.bennett@demoapp.test', role: 'Viewer', status: 'Active' },
  { id: 5, name: 'Sophia Reed', email: 'sophia.reed@demoapp.test', role: 'Manager', status: 'Pending' },
  { id: 6, name: 'Liam Foster', email: 'liam.foster@demoapp.test', role: 'Support', status: 'Active' },
  { id: 7, name: 'Ava Morgan', email: 'ava.morgan@demoapp.test', role: 'Viewer', status: 'Inactive' },
  { id: 8, name: 'James Cooper', email: 'james.cooper@demoapp.test', role: 'Admin', status: 'Active' },
];

// Fake order data for the Orders page.
export const initialOrders = [
  { id: 101, orderNumber: 'ORD-1001', customer: 'Northwind Traders', status: 'Processing', amount: 425.0, items: 4, date: '2026-04-02' },
  { id: 102, orderNumber: 'ORD-1002', customer: 'BluePeak Solutions', status: 'Delivered', amount: 1299.99, items: 8, date: '2026-04-04' },
  { id: 103, orderNumber: 'ORD-1003', customer: 'Harbor Retail', status: 'Pending', amount: 220.5, items: 2, date: '2026-04-05' },
  { id: 104, orderNumber: 'ORD-1004', customer: 'Summit Health', status: 'Cancelled', amount: 95.75, items: 1, date: '2026-04-06' },
  { id: 105, orderNumber: 'ORD-1005', customer: 'Brightline Media', status: 'Delivered', amount: 780.0, items: 5, date: '2026-04-07' },
  { id: 106, orderNumber: 'ORD-1006', customer: 'Apex Logistics', status: 'Processing', amount: 1510.2, items: 10, date: '2026-04-08' },
  { id: 107, orderNumber: 'ORD-1007', customer: 'Redwood Foods', status: 'Pending', amount: 310.4, items: 3, date: '2026-04-08' },
  { id: 108, orderNumber: 'ORD-1008', customer: 'Silver Oak Labs', status: 'Delivered', amount: 680.8, items: 6, date: '2026-04-09' },
  { id: 109, orderNumber: 'ORD-1009', customer: 'Evergreen Partners', status: 'Processing', amount: 2040.0, items: 11, date: '2026-04-10' },
  { id: 110, orderNumber: 'ORD-1010', customer: 'Sunrise Telecom', status: 'Pending', amount: 499.0, items: 4, date: '2026-04-11' },
];

// Small dashboard metrics for the top cards.
export const dashboardMetrics = [
  { id: 'revenue', label: 'Monthly Revenue', value: '$48,920', trend: '+12.4%' },
  { id: 'users', label: 'Active Users', value: '1,284', trend: '+6.1%' },
  { id: 'orders', label: 'Open Orders', value: '37', trend: '-2.8%' },
];

// Default settings state for the Settings page.
export const defaultSettings = {
  emailNotifications: true,
  darkMode: false,
  autoRefresh: true,
};
