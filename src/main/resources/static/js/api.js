/**
 * Core API & Session Utility for Easy Registration System
 */

const API = {
    // Current user in memory
    currentUser: null,

    async request(url, options = {}) {
        const defaultHeaders = {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        };

        options.headers = { ...defaultHeaders, ...options.headers };

        try {
            const response = await fetch(url, options);
            const data = await response.json();

            if (!response.ok || !data.success) {
                const errorMsg = data.message || `Request failed with status ${response.status}`;
                throw new Error(errorMsg);
            }

            return data;
        } catch (err) {
            console.error('API Error:', err);
            throw err;
        }
    },

    async getCurrentUser() {
        if (this.currentUser) return this.currentUser;
        try {
            const res = await this.request('/api/auth/me');
            if (res.success && res.data) {
                this.currentUser = res.data;
                return this.currentUser;
            }
        } catch (e) {
            // Not authenticated
        }
        return null;
    },

    async logout() {
        try {
            await this.request('/api/auth/logout', { method: 'POST' });
        } finally {
            this.currentUser = null;
            window.location.href = '/login';
        }
    },

    renderNavbar(activePage = '') {
        const user = this.currentUser;
        const navRight = document.getElementById('nav-user-area');
        if (!navRight) return;

        if (user) {
            let dashboardUrl = user.role === 'STUDENT' ? '/student-dashboard' : '/staff-dashboard';
            navRight.innerHTML = `
                <a href="${dashboardUrl}" class="nav-link ${activePage === 'dashboard' ? 'active' : ''}">Dashboard</a>
                <div class="nav-user">
                    <div class="user-badge">
                        <div class="user-name">${this.escapeHtml(user.fullName)}</div>
                        <span class="user-role-tag">${user.role.replace('_', ' ')}</span>
                    </div>
                    <button onclick="API.logout()" class="btn btn-secondary btn-sm" title="Log out">
                        Logout
                    </button>
                </div>
            `;
        } else {
            navRight.innerHTML = `
                <a href="/login" class="btn btn-primary btn-sm">Portal Login</a>
            `;
        }
    },

    showToast(message, type = 'info') {
        let container = document.getElementById('toast-container');
        if (!container) {
            container = document.createElement('div');
            container.id = 'toast-container';
            container.className = 'toast-container';
            document.body.appendChild(container);
        }

        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        
        let icon = type === 'success' ? '✓' : type === 'error' ? '✕' : 'ℹ';
        toast.innerHTML = `
            <div style="font-weight: bold; font-size: 1.1rem;">${icon}</div>
            <div style="flex: 1;">${this.escapeHtml(message)}</div>
        `;

        container.appendChild(toast);

        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(100%)';
            toast.style.transition = 'all 0.3s ease';
            setTimeout(() => toast.remove(), 300);
        }, 4000);
    },

    formatCurrency(amount) {
        if (amount == null) return '₹ 0.00';
        return '₹ ' + Number(amount).toLocaleString('en-IN', { minimumFractionDigits: 2 });
    },

    formatDate(dateStr) {
        if (!dateStr) return '—';
        const d = new Date(dateStr);
        return d.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
    },

    formatDateTime(dateStr) {
        if (!dateStr) return '—';
        const d = new Date(dateStr);
        return d.toLocaleDateString('en-IN', {
            day: '2-digit', month: 'short', year: 'numeric',
            hour: '2-digit', minute: '2-digit'
        });
    },

    escapeHtml(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }
};
