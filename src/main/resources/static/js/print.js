/**
 * Official Printable Registration Slip Controller
 */

document.addEventListener('DOMContentLoaded', async () => {
    try {
        const urlParams = new URLSearchParams(window.location.search);
        const appNum = urlParams.get('appNum');
        const appId = urlParams.get('id');

        if (!appNum && !appId) {
            alert('Missing application identifier in URL.');
            window.location.href = '/student-dashboard';
            return;
        }

        const user = await API.getCurrentUser();
        if (!user) {
            window.location.href = '/login';
            return;
        }

        let appData = null;
        if (appNum) {
            const res = await API.request('/api/registration/by-number/' + encodeURIComponent(appNum));
            appData = res.data;
        } else {
            const res = await API.request('/api/registration/' + appId);
            appData = res.data;
        }

        if (!appData) {
            alert('Application not found.');
            return;
        }

        renderPrintableSlip(appData);
    } catch (err) {
        console.error('Error rendering registration slip:', err);
        alert('Error loading registration slip: ' + err.message);
    }
});

function renderPrintableSlip(app) {
    const student = app.student || {};

    // Check if approved
    const isApproved = app.status === 'APPROVED';
    const warningBanner = document.getElementById('non-approved-warning');
    if (!isApproved && warningBanner) {
        warningBanner.style.display = 'block';
        warningBanner.innerHTML = `
            <strong>Notice:</strong> This registration application has not yet received final HOD approval (Current status: ${app.status.replace('_', ' ')}).
            This document is a provisional preview and does NOT constitute an official semester clearance slip.
        `;
    }

    // Populate Fields
    document.getElementById('slip-app-number').textContent = app.applicationNumber;
    document.getElementById('slip-date').textContent = API.formatDate(app.completedAt || app.updatedAt);
    document.getElementById('slip-academic-year').textContent = app.academicYear;
    document.getElementById('slip-semester').textContent = 'Semester ' + app.targetSemester;

    document.getElementById('slip-student-name').textContent = student.fullName || '—';
    document.getElementById('slip-reg-number').textContent = student.username || '—';
    document.getElementById('slip-department').textContent = app.department || '—';
    document.getElementById('slip-roll-number').textContent = student.rollNumber || student.username || '—';
    document.getElementById('slip-cgpa').textContent = app.cgpa != null ? app.cgpa : '—';
    document.getElementById('slip-backlogs').textContent = app.backlogsCount != null ? app.backlogsCount : 0;
    document.getElementById('slip-phone').textContent = student.phone || '—';
    document.getElementById('slip-email').textContent = student.email || '—';

    // Fee Details
    document.getElementById('slip-fee-rcpt').textContent = app.feeReceiptNumber || '—';
    document.getElementById('slip-fee-amount').textContent = API.formatCurrency(app.feeAmountPaid);
    document.getElementById('slip-fee-date').textContent = API.formatDate(app.feePaymentDate);

    // Transport Details
    const transRow = document.getElementById('slip-transport-row');
    if (app.transportOpted) {
        transRow.innerHTML = `
            <strong>College Bus:</strong> Route ${API.escapeHtml(app.busRoute || 'Assigned')} 
            (Stop: ${API.escapeHtml(app.busStop || 'Main')}) • Receipt: ${API.escapeHtml(app.transportFeeReceipt || 'Paid')}
        `;
    } else {
        transRow.innerHTML = `<strong>Transport:</strong> Not Availed (Own Arrangement)`;
    }

    // Registered Courses
    const coursesList = (app.selectedSubjects || '').split(',').map(s => s.trim()).filter(Boolean);
    const coursesContainer = document.getElementById('slip-courses-table-body');
    if (coursesList.length > 0) {
        coursesContainer.innerHTML = coursesList.map((course, idx) => `
            <tr>
                <td style="width: 50px; text-align: center;">${idx + 1}</td>
                <td><strong>${API.escapeHtml(course)}</strong></td>
                <td style="text-align: center; width: 100px;">Core / Reg</td>
                <td style="text-align: center; width: 90px; color: var(--success); font-weight: 700;">CONFIRMED</td>
            </tr>
        `).join('');
    } else {
        coursesContainer.innerHTML = `
            <tr><td colspan="4" style="text-align: center; color: var(--text-muted);">Standard prescribed curriculum.</td></tr>
        `;
    }

    // Department Clearances
    const stampsGrid = document.getElementById('clearance-stamps-container');
    stampsGrid.innerHTML = `
        <div class="clearance-stamp ${app.accountsCleared ? 'verified' : ''}">
            <div class="stamp-dept">1. Accounts Clearance</div>
            <div class="stamp-seal">${app.accountsCleared ? '✓ VERIFIED' : 'PENDING'}</div>
            <div class="stamp-date">${app.accountsCleared ? API.formatDate(app.accountsClearedAt) : '—'}</div>
            <div style="font-size: 0.68rem; color: var(--text-secondary); margin-top: 2px;">
                ${API.escapeHtml(app.accountsApprovedBy || 'Accounts Dept')}
            </div>
        </div>

        <div class="clearance-stamp ${!app.transportOpted ? 'verified' : (app.transportCleared ? 'verified' : '')}">
            <div class="stamp-dept">2. Transport Clearance</div>
            <div class="stamp-seal">${!app.transportOpted ? 'NOT OPTED' : (app.transportCleared ? '✓ VERIFIED' : 'PENDING')}</div>
            <div class="stamp-date">${app.transportCleared ? API.formatDate(app.transportClearedAt) : (app.transportOpted ? '—' : 'N/A')}</div>
            <div style="font-size: 0.68rem; color: var(--text-secondary); margin-top: 2px;">
                ${API.escapeHtml(app.transportApprovedBy || (app.transportOpted ? 'Transport Desk' : 'Bypassed'))}
            </div>
        </div>

        <div class="clearance-stamp ${app.labCleared ? 'verified' : ''}">
            <div class="stamp-dept">3. Laboratory Clearance</div>
            <div class="stamp-seal">${app.labCleared ? '✓ NO DUES' : 'PENDING'}</div>
            <div class="stamp-date">${app.labCleared ? API.formatDate(app.labClearedAt) : '—'}</div>
            <div style="font-size: 0.68rem; color: var(--text-secondary); margin-top: 2px;">
                ${API.escapeHtml(app.labApprovedBy || 'Lab In-Charge')}
            </div>
        </div>

        <div class="clearance-stamp ${app.hodApproved ? 'verified' : ''}">
            <div class="stamp-dept">4. HOD Final Approval</div>
            <div class="stamp-seal">${app.hodApproved ? '✓ APPROVED' : 'PENDING'}</div>
            <div class="stamp-date">${app.hodApproved ? API.formatDate(app.hodApprovedAt) : '—'}</div>
            <div style="font-size: 0.68rem; color: var(--text-secondary); margin-top: 2px;">
                ${API.escapeHtml(app.hodApprovedBy || 'Head of Dept')}
            </div>
        </div>
    `;

    // Security Verification Code
    const verifyCode = 'REG' + app.targetSemester + '-' + Math.abs(app.applicationNumber.hashCode ? app.applicationNumber.hashCode() : 984128) + '-SEC';
    document.getElementById('slip-verification-code').textContent = verifyCode;
}
