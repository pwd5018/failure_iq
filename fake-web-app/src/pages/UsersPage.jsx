import { useMemo, useState } from 'react';
import EmptyState from '../components/EmptyState';
import Modal from '../components/Modal';
import { initialUsers } from '../data/mockData';

function UsersPage() {
  const [users, setUsers] = useState(initialUsers);
  const [searchTerm, setSearchTerm] = useState('');
  const [roleFilter, setRoleFilter] = useState('All');
  const [userToDelete, setUserToDelete] = useState(null);

  const filteredUsers = useMemo(() => {
    return users.filter((user) => {
      const matchesSearch =
        user.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        user.email.toLowerCase().includes(searchTerm.toLowerCase());
      const matchesRole = roleFilter === 'All' ? true : user.role === roleFilter;
      return matchesSearch && matchesRole;
    });
  }, [users, searchTerm, roleFilter]);

  const handleConfirmDelete = () => {
    setUsers((current) => current.filter((user) => user.id !== userToDelete.id));
    setUserToDelete(null);
  };

  return (
    <div className="page-section" data-testid="users-page">
      <div className="page-header">
        <div>
          <p className="section-kicker">Directory</p>
          <h2>Users</h2>
          <p className="page-subtitle">Search, filter, and manage fake users with predictable controls.</p>
        </div>
      </div>

      <section className="card table-panel">
        <div className="toolbar" data-testid="users-toolbar">
          <div className="field-group compact-field">
            <label htmlFor="user-search">Search users</label>
            <input
              id="user-search"
              type="text"
              placeholder="Search by name or email"
              value={searchTerm}
              onChange={(event) => setSearchTerm(event.target.value)}
              data-testid="users-search-input"
            />
          </div>

          <div className="field-group compact-field">
            <label htmlFor="role-filter">Role filter</label>
            <select
              id="role-filter"
              value={roleFilter}
              onChange={(event) => setRoleFilter(event.target.value)}
              data-testid="users-role-filter"
            >
              <option value="All">All</option>
              <option value="Admin">Admin</option>
              <option value="Manager">Manager</option>
              <option value="Support">Support</option>
              <option value="Viewer">Viewer</option>
            </select>
          </div>
        </div>

        {filteredUsers.length === 0 ? (
          <EmptyState
            title="No users found"
            message="Try a different search term or role filter."
            testId="users-empty-state"
          />
        ) : (
          <div className="table-wrapper">
            <table className="data-table" data-testid="users-table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Role</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredUsers.map((user) => (
                  <tr key={user.id} data-testid={`user-row-${user.id}`}>
                    <td>{user.name}</td>
                    <td>{user.email}</td>
                    <td>{user.role}</td>
                    <td>
                      <span className={`status-badge ${user.status.toLowerCase()}`}>
                        {user.status}
                      </span>
                    </td>
                    <td className="actions-cell">
                      <button
                        type="button"
                        className="secondary-button"
                        data-testid={`edit-user-${user.id}`}
                      >
                        Edit
                      </button>
                      <button
                        type="button"
                        className="danger-button"
                        onClick={() => setUserToDelete(user)}
                        data-testid={`delete-user-${user.id}`}
                      >
                        Delete
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
        isOpen={Boolean(userToDelete)}
        title="Confirm Delete"
        onClose={() => setUserToDelete(null)}
        testId="delete-user-modal"
      >
        <p className="modal-copy">
          Are you sure you want to delete <strong>{userToDelete?.name}</strong>?
        </p>
        <div className="modal-actions">
          <button
            type="button"
            className="secondary-button"
            onClick={() => setUserToDelete(null)}
            data-testid="cancel-delete-button"
          >
            Cancel
          </button>
          <button
            type="button"
            className="danger-button"
            onClick={handleConfirmDelete}
            data-testid="confirm-delete-button"
          >
            Confirm Delete
          </button>
        </div>
      </Modal>
    </div>
  );
}

export default UsersPage;
