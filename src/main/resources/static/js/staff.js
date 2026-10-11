/**
 * Staff Department Dashboard Controller
 * Powers Accounts, Transport, Lab Assistant, and HOD workflows.
 */

let currentStaff = null;
let pendingList = [];
let activeAppForReview = null;

document.addEventListener('DOMContentLoaded', async () => {
    try {
        currentStaff = await API.getCurrentUser();
        if (!currentStaff) {
            window.location.href = '/login';
            return;
        }

        if (currentStaff.role === 'STUDENT') {
            window.location.href = '/student-dashboard';
            return;
        }

        API.renderNavbar('dashboard');
        renderStaffHeader();
        await Promise.all([loadStats(), loadPendingQueue()]);
    } catch (err) {
        console.error('Initialization error:', err);
        API.showToast('Failed to initialize staff portal', 'error');
    }
});

function renderStaffHeader() {
    const roleTitles = {
        'ACCOUNTS': {
            title: 'Accounts Department Portal',
            subtitle: 'Tuition Fee Verification & Financial Dues Clearance',
            badge: 'Accounts Desk',
            task: 'Verify student fee receipts, bank reference numbers, and approve tuition clearance.'
        },
        'TRANSPORT': {
            title: 'Transport Department Portal',
            subtitle: 'College Bus Pass & Transport Clearance',
            badge: 'Transport Desk',
            task: 'Verify student bus routes, boarding points, and transport fee receipts.'
        },
        'LAB_ASSISTANT': {
            title: 'Laboratory Assistant Portal',
            subtitle: 'Lab Equipment, Hardware Kit & Library Dues Clearance',
            badge: 'Lab In-Charge',
            task: 'Confirm returned lab components, breakages settlement, and issue lab clearance.'
        },
        'HOD': {
            title: 'Head of Department (HOD) Portal',
            subtitle: 'Final Academic Verification & Semester Registration Sign-off',
            badge: 'Department Head',
            task: 'Review student academic standing, prerequisites, department clearances, and grant final enrollment approval.'
        }
    };

    const config = roleTitles[currentStaff.role] || {
        title: 'Staff Portal',
        subtitle: 'Clearance Management System',
        badge: 'Staff',
        task: 'Review and approve student registration applications.'
    };

    document.getElementById('portal-role-title').textContent = config.title;
    document.getElementById('portal-role-subtitle').textContent = config.subtitle;
    document.getElementById('portal-role-badge').textContent = config.badge;
    document.getElementById('portal-role-task').textContent = config.task;
    document.getElementById('staff-officer-name').textContent = currentStaff.fullName;
    document.getElementById('staff-department-name').textContent = currentStaff.department || 'Administrative Office';
}

async function loadStats() {
    try {
        const res = await API.request('/api/approvals/stats');
        const stats = res.data;
        if (!stats) return;

        document.getElementById('stat-total-apps').textContent = stats.totalApplications;
        document.getElementById('stat-my-pending').textContent = getMyPendingCount(stats);
        document.getElementById('stat-approved').textContent = stats.totalApproved;
        document.getElementById('stat-rejected').textContent = stats.totalRejected;
    } catch (err) {
        console.error('Stats error:', err);
    }
}

function getMyPendingCount(stats) {
    switch (currentStaff.role) {
        case 'ACCOUNTS': return stats.pendingAccounts;
        case 'TRANSPORT': return stats.pendingTransport;
        case 'LAB_ASSISTANT': return stats.pendingLab;
        case 'HOD': return stats.pendingHod;
        default: return 0;
    }
}

async function loadPendingQueue() {
    try {
        const res = await API.request('/api/approvals/pending');
        pendingList = res.data || [];
        renderPendingTable();
    } catch (err) {
        API.showToast(err.message, 'error');
    }
}

function renderPendingTable() {
    const tableBody = document.getElementById('pending-table-body');
    const badgeCount = document.getElementById('queue-count-badge');
    badgeCount.textContent = pendingList.length + ' Pending';

    if (pendingList.length === 0) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="7" style="text-align: center; padding: 3rem; color: var(--text-secondary);">
                    <div style="font-size: 2.5rem; margin-bottom: 0.5rem;">🎉</div>
                    <h4 style="color: var(--primary);">All Caught Up!</h4>
                    <p style="font-size: 0.9rem; color: var(--text-muted); margin-top: 0.25rem;">
                        No pending applications currently require your department's clearance.
                    </p>
                </td>
            </tr>
        `;
        return;
    }

    tableBody.innerHTML = pendingList.map(app => {
        const student = app.student || {};
        return `
            <tr>
                <td><strong>${API.escapeHtml(app.applicationNumber)}</strong></td>
                <td>
                    <div style="font-weight: 600; color: var(--primary-dark);">${API.escapeHtml(student.fullName || '—')}</div>
                    <div style="font-size: 0.8rem; color: var(--text-secondary);">Reg No: ${API.escapeHtml(student.username || '—')}</div>
                </td>
                <td>${API.escapeHtml(app.department)}</td>
                <td>Semester ${app.targetSemester}</td>
                <td>
                    <div>${API.formatCurrency(app.feeAmountPaid)}</div>
                    <div style="font-size: 0.75rem; color: var(--text-muted);">Rcpt: ${API.escapeHtml(app.feeReceiptNumber || '—')}</div>
                </td>
                <td>
                    ${app.transportOpted ? `
                        <span class="badge badge-info" title="${API.escapeHtml(app.busRoute || '')}">Bus: Route Opted</span>
                    ` : '<span style="color: var(--text-muted); font-size: 0.82rem;">Not Opted</span>'}
                </td>
                <td>${API.formatDate(app.submittedAt)}</td>
                <td>
                    <button onclick="openReviewModal(${app.id})" class="btn btn-primary btn-sm">
                        Review & Clear →
                    </button>
                </td>
            </tr>
        `;
    }).join('');
}

function openReviewModal(appId) {
    activeAppForReview = pendingList.find(a => a.id === appId);
    if (!activeAppForReview) return;

    const modalBody = document.getElementById('review-modal-body');
    const student = activeAppForReview.student || {};

    let roleSpecificPrompt = '';
    if (currentStaff.role === 'ACCOUNTS') {
        roleSpecificPrompt = `
            <div style="background: var(--primary-subtle); padding: 0.85rem 1rem; border-radius: var(--radius-md); margin-bottom: 1rem; font-size: 0.88rem; color: var(--primary);">
                💳 <strong>Fee Verification:</strong> Verify the semester tuition fee payment receipt (<strong>${API.escapeHtml(activeAppForReview.feeReceiptNumber)}</strong>) for <strong>${API.formatCurrency(activeAppForReview.feeAmountPaid)}</strong>.
            </div>
        `;
    } else if (currentStaff.role === 'TRANSPORT') {
        roleSpecificPrompt = `
            <div style="background: var(--primary-subtle); padding: 0.85rem 1rem; border-radius: var(--radius-md); margin-bottom: 1rem; font-size: 0.88rem; color: var(--primary);">
                🚌 <strong>Bus Pass Verification:</strong> Student has opted for <strong>${API.escapeHtml(activeAppForReview.busRoute || 'College Bus')}</strong> at stop <strong>${API.escapeHtml(activeAppForReview.busStop || 'Main')}</strong>. Transport Receipt: <strong>${API.escapeHtml(activeAppForReview.transportFeeReceipt || 'N/A')}</strong>.
            </div>
        `;
    } else if (currentStaff.role === 'LAB_ASSISTANT') {
        roleSpecificPrompt = `
            <div style="background: var(--primary-subtle); padding: 0.85rem 1rem; border-radius: var(--radius-md); margin-bottom: 1rem; font-size: 0.88rem; color: var(--primary);">
                🔬 <strong>Lab Clearance:</strong> Verify that the student has no pending hardware kit dues, library fines, or lab damage liabilities.
            </div>
        `;
    } else if (currentStaff.role === 'HOD') {
        roleSpecificPrompt = `
            <div style="background: #f0fdf4; border: 1px solid var(--success); padding: 0.85rem 1rem; border-radius: var(--radius-md); margin-bottom: 1rem; font-size: 0.88rem; color: #166534;">
                🎓 <strong>Prerequisites Satisfied:</strong> Accounts, Transport (if applicable), and Laboratory clearances have all been granted. Review academic standing (CGPA: <strong>${activeAppForReview.cgpa}</strong>, Backlogs: <strong>${activeAppForReview.backlogsCount}</strong>) and grant final approval.
            </div>
        `;
    }

    modalBody.innerHTML = `
        ${roleSpecificPrompt}

        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1.25rem;">
            <div>
                <label class="form-label" style="font-size: 0.78rem; text-transform: uppercase; color: var(--text-muted);">Student Information</label>
                <div style="font-weight: 700; font-size: 1.05rem; color: var(--primary-dark);">${API.escapeHtml(student.fullName)}</div>
                <div style="font-size: 0.85rem; color: var(--text-secondary);">Reg No: ${API.escapeHtml(student.username)}</div>
                <div style="font-size: 0.85rem; color: var(--text-secondary);">${API.escapeHtml(activeAppForReview.department)}</div>
            </div>
            <div>
                <label class="form-label" style="font-size: 0.78rem; text-transform: uppercase; color: var(--text-muted);">Academic Profile</label>
                <div style="font-size: 0.88rem; color: var(--text-secondary);">
                    Target Semester: <strong>${activeAppForReview.targetSemester}</strong> (${API.escapeHtml(activeAppForReview.academicYear)})<br>
                    Current CGPA: <strong>${activeAppForReview.cgpa != null ? activeAppForReview.cgpa : '—'}</strong><br>
                    Active Backlogs: <strong>${activeAppForReview.backlogsCount != null ? activeAppForReview.backlogsCount : 0}</strong>
                </div>
            </div>
        </div>

        <div style="margin-bottom: 1.25rem;">
            <label class="form-label" style="font-size: 0.78rem; text-transform: uppercase; color: var(--text-muted);">Registered Courses / Curriculum</label>
            <div style="background: #f8fafc; border: 1px solid var(--border); border-radius: var(--radius-md); padding: 0.75rem; font-size: 0.85rem; color: var(--text-secondary);">
                ${API.escapeHtml(activeAppForReview.selectedSubjects || 'Standard semester curriculum.')}
            </div>
        </div>

        <!-- Clearance Summary Table -->
        <div style="margin-bottom: 1.25rem;">
            <label class="form-label" style="font-size: 0.78rem; text-transform: uppercase; color: var(--text-muted);">Prior Clearances Status</label>
            <table class="table" style="font-size: 0.82rem;">
                <thead>
                    <tr>
                        <th>Department</th>
                        <th>Status</th>
                        <th>Cleared By</th>
                        <th>Remarks</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td>Accounts Dept</td>
                        <td><span class="badge ${activeAppForReview.accountsCleared ? 'badge-approved' : 'badge-pending'}">${activeAppForReview.accountsCleared ? 'Cleared' : 'Pending'}</span></td>
                        <td>${API.escapeHtml(activeAppForReview.accountsApprovedBy || '—')}</td>
                        <td>${API.escapeHtml(activeAppForReview.accountsRemarks || '—')}</td>
                    </tr>
                    <tr>
                        <td>Transport Dept</td>
                        <td><span class="badge ${!activeAppForReview.transportOpted ? 'badge-info' : (activeAppForReview.transportCleared ? 'badge-approved' : 'badge-pending')}">${!activeAppForReview.transportOpted ? 'Not Opted' : (activeAppForReview.transportCleared ? 'Cleared' : 'Pending')}</span></td>
                        <td>${API.escapeHtml(activeAppForReview.transportApprovedBy || '—')}</td>
                        <td>${API.escapeHtml(activeAppForReview.transportRemarks || '—')}</td>
                    </tr>
                    <tr>
                        <td>Laboratory</td>
                        <td><span class="badge ${activeAppForReview.labCleared ? 'badge-approved' : 'badge-pending'}">${activeAppForReview.labCleared ? 'Cleared' : 'Pending'}</span></td>
                        <td>${API.escapeHtml(activeAppForReview.labApprovedBy || '—')}</td>
                        <td>${API.escapeHtml(activeAppForReview.labRemarks || '—')}</td>
                    </tr>
                </tbody>
            </table>
        </div>

        <div class="form-group">
            <label class="form-label" for="review-remarks">Department Clearance Remarks & Notes *</label>
            <textarea id="review-remarks" class="form-textarea" rows="2" placeholder="e.g., Verified payment UTR / All lab dues settled / Academics verified..."></textarea>
        </div>
    `;

    document.getElementById('review-modal').classList.add('active');
}

function closeReviewModal() {
    document.getElementById('review-modal').classList.remove('active');
    activeAppForReview = null;
}

async function processDecision(decision) {
    if (!activeAppForReview) return;

    const remarks = document.getElementById('review-remarks').value.trim();
    if (decision === 'REJECT' && !remarks) {
        API.showToast('Please provide rejection remarks explaining the reason.', 'error');
        return;
    }

    const confirmMsg = decision === 'APPROVE'
        ? 'Are you sure you want to approve this application and forward it to the next stage?'
        : 'Are you sure you want to reject this registration application?';

    if (!confirm(confirmMsg)) return;

    const btnApprove = document.getElementById('btn-approve-action');
    const btnReject = document.getElementById('btn-reject-action');
    if (btnApprove) btnApprove.disabled = true;
    if (btnReject) btnReject.disabled = true;

    try {
        const payload = {
            applicationId: activeAppForReview.id,
            decision: decision,
            remarks: remarks || (decision === 'APPROVE' ? 'Verified & Approved' : 'Rejected')
        };

        const res = await API.request('/api/approvals/action', {
            method: 'POST',
            body: JSON.stringify(payload)
        });

        API.showToast(res.message, decision === 'APPROVE' ? 'success' : 'info');
        closeReviewModal();
        await Promise.all([loadStats(), loadPendingQueue()]);
    } catch (err) {
        API.showToast(err.message, 'error');
    } finally {
        if (btnApprove) btnApprove.disabled = false;
        if (btnReject) btnReject.disabled = false;
    }
}
