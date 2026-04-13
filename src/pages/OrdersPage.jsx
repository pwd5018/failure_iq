import { useMemo, useState } from 'react';
import EmptyState from '../components/EmptyState';
import Modal from '../components/Modal';
import { initialOrders } from '../data/mockData';

function OrdersPage() {
  const [statusFilter, setStatusFilter] = useState('All');
  const [sortDirection, setSortDirection] = useState('asc');
  const [selectedOrder, setSelectedOrder] = useState(null);

  const visibleOrders = useMemo(() => {
    const filtered = initialOrders.filter((order) =>
      statusFilter === 'All' ? true : order.status === statusFilter
    );

    return [...filtered].sort((firstOrder, secondOrder) => {
      return sortDirection === 'asc'
        ? firstOrder.amount - secondOrder.amount
        : secondOrder.amount - firstOrder.amount;
    });
  }, [statusFilter, sortDirection]);

  return (
    <div className="page-section" data-testid="orders-page">
      <div className="page-header">
        <div>
          <p className="section-kicker">Sales</p>
          <h2>Orders</h2>
          <p className="page-subtitle">Review fake orders and open a details modal from a row click or button.</p>
        </div>
      </div>

      <section className="card table-panel">
        <div className="toolbar" data-testid="orders-toolbar">
          <div className="field-group compact-field">
            <label htmlFor="order-status-filter">Status filter</label>
            <select
              id="order-status-filter"
              value={statusFilter}
              onChange={(event) => setStatusFilter(event.target.value)}
              data-testid="orders-status-filter"
            >
              <option value="All">All</option>
              <option value="Pending">Pending</option>
              <option value="Processing">Processing</option>
              <option value="Delivered">Delivered</option>
              <option value="Cancelled">Cancelled</option>
            </select>
          </div>

          <div className="field-group compact-field">
            <label htmlFor="amount-sort">Sort amount</label>
            <select
              id="amount-sort"
              value={sortDirection}
              onChange={(event) => setSortDirection(event.target.value)}
              data-testid="orders-amount-sort"
            >
              <option value="asc">Low to high</option>
              <option value="desc">High to low</option>
            </select>
          </div>
        </div>

        {visibleOrders.length === 0 ? (
          <EmptyState
            title="No orders match this filter"
            message="Change the order status filter to see more results."
            testId="orders-empty-state"
          />
        ) : (
          <div className="table-wrapper">
            <table className="data-table" data-testid="orders-table">
              <thead>
                <tr>
                  <th>Order Number</th>
                  <th>Customer</th>
                  <th>Status</th>
                  <th>Amount</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {visibleOrders.map((order) => (
                  <tr
                    key={order.id}
                    className="clickable-row"
                    onClick={() => setSelectedOrder(order)}
                    data-testid={`order-row-${order.id}`}
                  >
                    <td>{order.orderNumber}</td>
                    <td>{order.customer}</td>
                    <td>
                      <span className={`status-badge ${order.status.toLowerCase()}`}>
                        {order.status}
                      </span>
                    </td>
                    <td>${order.amount.toFixed(2)}</td>
                    <td
                      className="actions-cell"
                      onClick={(event) => event.stopPropagation()}
                    >
                      <button
                        type="button"
                        className="secondary-button"
                        onClick={() => setSelectedOrder(order)}
                        data-testid={`view-order-${order.id}`}
                      >
                        View
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <Modal
        isOpen={Boolean(selectedOrder)}
        title="Order Details"
        onClose={() => setSelectedOrder(null)}
        testId="order-details-modal"
      >
        {selectedOrder ? (
          <div className="details-grid" data-testid="order-details-content">
            <div>
              <span className="details-label">Order Number</span>
              <p>{selectedOrder.orderNumber}</p>
            </div>
            <div>
              <span className="details-label">Customer</span>
              <p>{selectedOrder.customer}</p>
            </div>
            <div>
              <span className="details-label">Status</span>
              <p>{selectedOrder.status}</p>
            </div>
            <div>
              <span className="details-label">Amount</span>
              <p>${selectedOrder.amount.toFixed(2)}</p>
            </div>
            <div>
              <span className="details-label">Items</span>
              <p>{selectedOrder.items}</p>
            </div>
            <div>
              <span className="details-label">Order Date</span>
              <p>{selectedOrder.date}</p>
            </div>
          </div>
        ) : null}
      </Modal>
    </div>
  );
}

export default OrdersPage;
