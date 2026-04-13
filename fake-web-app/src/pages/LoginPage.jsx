import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const DEMO_USERNAME = 'admin';
const DEMO_PASSWORD = 'password123';

function LoginPage() {
  const navigate = useNavigate();
  const [formValues, setFormValues] = useState({ username: '', password: '' });
  const [submitted, setSubmitted] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Validation errors only appear after submit, which is useful for UI tests.
  const usernameError = submitted && !formValues.username.trim() ? 'Username is required.' : '';
  const passwordError = submitted && !formValues.password.trim() ? 'Password is required.' : '';

  const handleChange = (event) => {
    const { name, value } = event.target;
    setFormValues((current) => ({ ...current, [name]: value }));
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    setSubmitted(true);
    setErrorMessage('');

    if (!formValues.username.trim() || !formValues.password.trim()) {
      return;
    }

    if (
      formValues.username !== DEMO_USERNAME ||
      formValues.password !== DEMO_PASSWORD
    ) {
      setErrorMessage('Invalid username or password.');
      return;
    }

    // This timeout adds a deliberate delayed UI state before redirecting.
    setIsSubmitting(true);
    window.setTimeout(() => {
      localStorage.setItem('demo-auth', 'true');
      navigate('/dashboard');
    }, 900);
  };

  return (
    <div className="login-page" data-testid="login-page">
      <div className="login-card card">
        <div className="login-copy">
          <p className="brand-eyebrow">Automation Practice App</p>
          <h1>Sign in to OpsBoard</h1>
          <p>
            Demo credentials: <strong>admin</strong> / <strong>password123</strong>
          </p>
        </div>

        <form onSubmit={handleSubmit} noValidate data-testid="login-form">
          <div className="field-group">
            <label htmlFor="username">Username</label>
            <input
              id="username"
              name="username"
              type="text"
              value={formValues.username}
              onChange={handleChange}
              placeholder="Enter username"
              data-testid="username-input"
            />
            {usernameError ? (
              <p className="field-error" data-testid="username-error">
                {usernameError}
              </p>
            ) : null}
          </div>

          <div className="field-group">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              name="password"
              type="password"
              value={formValues.password}
              onChange={handleChange}
              placeholder="Enter password"
              data-testid="password-input"
            />
            {passwordError ? (
              <p className="field-error" data-testid="password-error">
                {passwordError}
              </p>
            ) : null}
          </div>

          {errorMessage ? (
            <div className="alert-error" data-testid="login-error">
              {errorMessage}
            </div>
          ) : null}

          <button
            type="submit"
            className="primary-button full-width"
            disabled={isSubmitting}
            data-testid="login-button"
          >
            {isSubmitting ? 'Signing in...' : 'Login'}
          </button>
        </form>
      </div>
    </div>
  );
}

export default LoginPage;
