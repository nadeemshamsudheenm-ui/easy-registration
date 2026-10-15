/**
 * Student Dashboard Controller
 */

let currentStudent = null;
let studentApplications = [];

document.addEventListener('DOMContentLoaded', async () => {
    try {
        currentStudent = await API.getCurrentUser();
        if (!currentStudent) {
            window.location.href = '/login';
            return;
        }

        if (currentStudent.role !== 'STUDENT') {
            window.location.href = '/staff-dashboard';
            return;
        }

        API.renderNavbar('dashboard');
        renderStudentProfile();
        setupFormListeners();
        await loadApplications();
    } catch (err) {
        console.error('Initialization error:', err);
        API.showToast('Failed to initialize student dashboard', 'error');
    }
});

function renderStudentProfile() {
    document.getElementById('student-name-display').textContent = currentStudent.fullName;
    document.getElementById('student-reg-display').textContent = currentStudent.username;
    document.getElementById('student-dept-display').textContent = currentStudent.department || 'Not Assigned';
    document.getElementById('student-sem-display').textContent = 'Semester ' + (currentStudent.currentSemester || 'N/A');
    document.getElementById('student-roll-display').textContent = currentStudent.rollNumber || currentStudent.username;
    document.getElementById('student-email-display').textContent = currentStudent.email || '—';
    document.getElementById('student-phone-display').textContent = currentStudent.phone || '—';

    // Pre-populate submit form defaults
    const deptSelect = document.getElementById('app-department');
    if (deptSelect && currentStudent.department) {
        deptSelect.value = currentStudent.department;
    }

    const semSelect = document.getElementById('app-semester');
    if (semSelect && currentStudent.currentSemester) {
        semSelect.value = currentStudent.currentSemester;
        updateCourseSuggestions(currentStudent.currentSemester);
    }
}

async function loadApplications() {
    try {
        const res = await API.request('/api/registration/my-applications');
        studentApplications = res.data || [];
        renderApplications();
    } catch (err) {
        API.showToast(err.message, 'error');
    }
}

function renderApplications() {
    const activeContainer = document.getElementById('active-application-view');
    const historyTableBody = document.getElementById('history-table-body');

    if (studentApplications.length === 0) {
        activeContainer.innerHTML = `
            <div style="text-align: center; padding: 3rem 1rem;">
                <div style="font-size: 3rem; margin-bottom: 1rem;">📋</div>
                <h3 style="color: var(--primary-dark); margin-bottom: 0.5rem;">No Active Semester Registration</h3>
                <p style="color: var(--text-secondary); max-width: 500px; margin: 0 auto 1.5rem;">
                    You have not submitted a registration request for the current academic session. Click the button below to submit your details.
                </p>
                <button onclick="openSubmissionModal()" class="btn btn-primary">
                    + Submit New Registration
                </button>
            </div>
        `;
        historyTableBody.innerHTML = `
            <tr>
                <td colspan="7" style="text-align: center; color: var(--text-muted); padding: 2rem;">
                    No previous registration applications on record.
                </td>
            </tr>
        `;
        return;
    }

    // Latest application
    const latestApp = studentApplications[0];
    renderActiveApplication(latestApp);

    // History Table
    historyTableBody.innerHTML = studentApplications.map(app => {
        let badgeClass = 'badge-pending';
        if (app.status === 'APPROVED') badgeClass = 'badge-approved';
        else if (app.status === 'REJECTED') badgeClass = 'badge-rejected';

        return `
            <tr>
                <td><strong>${API.escapeHtml(app.applicationNumber)}</strong></td>
                <td>Semester ${app.targetSemester} (${API.escapeHtml(app.academicYear)})</td>
                <td>${API.escapeHtml(app.department)}</td>
                <td>${API.formatCurrency(app.feeAmountPaid)}</td>
                <td><span class="badge ${badgeClass}">${app.status.replace('_', ' ')}</span></td>
                <td>${API.formatDate(app.submittedAt)}</td>
                <td>
                    <button onclick="viewApplicationDetails(${app.id})" class="btn btn-secondary btn-sm">
                        View
                    </button>
                    ${app.status === 'APPROVED' ? `
                        <a href="/print-slip?appNum=${encodeURIComponent(app.applicationNumber)}" target="_blank" class="btn btn-outline btn-sm">
                            🖨️ Print Slip
                        </a>
                    ` : ''}
                    ${app.status === 'REJECTED' ? `
                        <button onclick="openResubmitModal(${app.id})" class="btn btn-warning btn-sm" style="margin-left: 0.35rem;" title="Resubmit rejected application">
                            🔄 Resubmit
                        </button>
                    ` : ''}
                </td>
            </tr>
        `;
    }).join('');
}

function renderActiveApplication(app) {
    const container = document.getElementById('active-application-view');
    const isApproved = app.status === 'APPROVED';
    const isRejected = app.status === 'REJECTED';

    // Stepper State Logic
    // Stages: 1: Submitted, 2: Accounts, 3: Transport, 4: Lab, 5: HOD
    let step1Class = 'completed'; // Submitted is always completed
    let step2Class = app.accountsCleared ? 'completed' : (app.status === 'PENDING_ACCOUNTS' ? 'current' : (isRejected && app.rejectionStage === 'ACCOUNTS' ? 'rejected' : ''));
    
    // Transport step
    let step3Class = '';
    let step3Text = 'Transport Clearance';
    let step3Subtext = 'Bus Fee Verification';
    if (!app.transportOpted) {
        step3Class = 'skipped';
        step3Text = 'Transport (Not Opted)';
        step3Subtext = 'Bypassed automatically';
    } else if (app.transportCleared) {
        step3Class = 'completed';
        step3Subtext = 'Verified by ' + (app.transportApprovedBy || 'Transport');
    } else if (app.status === 'PENDING_TRANSPORT') {
        step3Class = 'current';
        step3Subtext = 'In Progress';
    } else if (isRejected && app.rejectionStage === 'TRANSPORT') {
        step3Class = 'rejected';
        step3Subtext = 'Rejected';
    }

    // Lab step
    let step4Class = app.labCleared ? 'completed' : (app.status === 'PENDING_LAB' ? 'current' : (isRejected && app.rejectionStage === 'LAB' ? 'rejected' : ''));
    
    // HOD step
    let step5Class = app.hodApproved ? 'completed' : (app.status === 'PENDING_HOD' ? 'current' : (isRejected && app.rejectionStage === 'HOD' ? 'rejected' : ''));

    let statusBadge = ``;
    if (isApproved) {
        statusBadge = `<span class="badge badge-approved" style="font-size: 0.95rem; padding: 0.4rem 1rem;">✓ Final Registration Approved</span>`;
    } else if (isRejected) {
        statusBadge = `<span class="badge badge-rejected" style="font-size: 0.95rem; padding: 0.4rem 1rem;">✕ Application Rejected at ${app.rejectionStage || 'Verification'}</span>`;
    } else {
        statusBadge = `<span class="badge badge-pending" style="font-size: 0.95rem; padding: 0.4rem 1rem;">⏳ ${app.status.replace('_', ' ')}</span>`;
    }

    container.innerHTML = `
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; flex-wrap: wrap; gap: 1rem;">
            <div>
                <div style="font-size: 0.85rem; color: var(--text-secondary); text-transform: uppercase; font-weight: 600;">Active Application Reference</div>
                <h2 style="color: var(--primary); font-size: 1.6rem; margin: 0.2rem 0;">${API.escapeHtml(app.applicationNumber)}</h2>
                <div style="font-size: 0.9rem; color: var(--text-secondary);">
                    Semester ${app.targetSemester} (${API.escapeHtml(app.academicYear)}) • Submitted on ${API.formatDateTime(app.submittedAt)}
                </div>
            </div>
            <div style="display: flex; align-items: center; gap: 1rem;">
                ${statusBadge}
                ${isApproved ? `
                    <a href="/print-slip?appNum=${encodeURIComponent(app.applicationNumber)}" target="_blank" class="btn btn-success" style="box-shadow: 0 4px 15px rgba(5,150,105,0.3);">
                        🖨️ Download & Print Clearance Slip
                    </a>
                ` : isRejected ? `
                    <button onclick="openResubmitModal(${app.id})" class="btn btn-warning" id="btn-header-resubmit" style="display: inline-flex; align-items: center; gap: 0.4rem; font-weight: 600;">
                        🔄 Resubmit Application
                    </button>
                ` : `
                    <button onclick="openSubmissionModal()" class="btn btn-secondary btn-sm" title="Submit another application if needed">
                        + New Form
                    </button>
                `}
            </div>
        </div>

        <!-- Visual Multi-Stage Workflow Stepper -->
        <div class="stepper-container">
            <div class="stepper">
                <div class="step-item ${step1Class}">
                    <div class="step-circle">1</div>
                    <div class="step-title">Submitted</div>
                    <div class="step-subtext">Application Lodged</div>
                </div>
                <div class="step-item ${step2Class}">
                    <div class="step-circle">2</div>
                    <div class="step-title">Accounts</div>
                    <div class="step-subtext">${app.accountsCleared ? 'Fee Verified' : (app.status === 'PENDING_ACCOUNTS' ? 'Reviewing Payment' : 'Pending')}</div>
                </div>
                <div class="step-item ${step3Class}">
                    <div class="step-circle">3</div>
                    <div class="step-title">${step3Text}</div>
                    <div class="step-subtext">${step3Subtext}</div>
                </div>
                <div class="step-item ${step4Class}">
                    <div class="step-circle">4</div>
                    <div class="step-title">Laboratory</div>
                    <div class="step-subtext">${app.labCleared ? 'No Dues Cleared' : (app.status === 'PENDING_LAB' ? 'Awaiting Clearance' : 'Pending')}</div>
                </div>
                <div class="step-item ${step5Class}">
                    <div class="step-circle">5</div>
                    <div class="step-title">HOD Final Approval</div>
                    <div class="step-subtext">${app.hodApproved ? 'Final Sign-off Done' : (app.status === 'PENDING_HOD' ? 'Ready for HOD' : 'Pending')}</div>
                </div>
            </div>
        </div>

        ${isRejected ? `
            <div style="background: var(--danger-light); border: 1.5px solid var(--danger); border-radius: var(--radius-md); padding: 1.25rem; margin-bottom: 1.5rem;">
                <div style="display: flex; gap: 0.75rem; align-items: flex-start; justify-content: space-between; flex-wrap: wrap;">
                    <div style="display: flex; gap: 0.75rem; align-items: flex-start; max-width: 650px;">
                        <div style="font-size: 1.5rem; color: var(--danger); line-height: 1;">⚠️</div>
                        <div>
                            <h4 style="color: var(--danger); margin-bottom: 0.25rem;">Application Returned / Rejected</h4>
                            <p style="color: #7f1d1d; font-size: 0.92rem; margin-bottom: 0.5rem;">
                                <strong>Stage:</strong> ${app.rejectionStage || 'Verification'} Department<br>
                                <strong>Remarks:</strong> ${API.escapeHtml(app.rejectionReason || 'No specific remarks recorded.')}
                            </p>
                            <p style="font-size: 0.85rem; color: #991b1b; margin-bottom: 0;">
                                Please review the remarks above, make any required updates to your fee receipts or information, and click <strong>Resubmit Application</strong> to send it back for departmental clearance.
                            </p>
                        </div>
                    </div>
                    <div style="align-self: center; margin-top: 0.5rem;">
                        <button onclick="openResubmitModal(${app.id})" class="btn btn-primary" id="btn-resubmit-banner" style="display: inline-flex; align-items: center; gap: 0.45rem; padding: 0.65rem 1.25rem; font-weight: 600; box-shadow: 0 4px 12px rgba(37,99,235,0.25);">
                            🔄 Resubmit Application
                        </button>
                    </div>
                </div>
            </div>
        ` : ''}

        <!-- Department Clearances Detailed Cards -->
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 1rem; margin-top: 1.5rem;">
            <!-- Accounts Card -->
            <div style="border: 1px solid var(--border); border-radius: var(--radius-md); padding: 1.2rem; background: ${app.accountsCleared ? '#f0fdf4' : '#ffffff'};">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem;">
                    <span style="font-weight: 700; font-size: 0.95rem; color: var(--primary-dark);">1. Accounts Department</span>
                    <span class="badge ${app.accountsCleared ? 'badge-approved' : 'badge-pending'}">${app.accountsCleared ? 'Cleared' : 'Pending'}</span>
                </div>
                <div style="font-size: 0.85rem; color: var(--text-secondary); line-height: 1.5;">
                    <div><strong>Receipt No:</strong> ${API.escapeHtml(app.feeReceiptNumber || '—')}</div>
                    <div><strong>Amount:</strong> ${API.formatCurrency(app.feeAmountPaid)}</div>
                    <div><strong>Remarks:</strong> ${API.escapeHtml(app.accountsRemarks || 'Awaiting verification')}</div>
                    ${app.accountsClearedAt ? `<div style="font-size: 0.75rem; color: var(--text-muted); margin-top: 0.35rem;">Cleared: ${API.formatDateTime(app.accountsClearedAt)}</div>` : ''}
                </div>
            </div>

            <!-- Transport Card -->
            <div style="border: 1px solid var(--border); border-radius: var(--radius-md); padding: 1.2rem; background: ${!app.transportOpted ? '#f8fafc' : (app.transportCleared ? '#f0fdf4' : '#ffffff')};">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem;">
                    <span style="font-weight: 700; font-size: 0.95rem; color: var(--primary-dark);">2. Transport Department</span>
                    <span class="badge ${!app.transportOpted ? 'badge-info' : (app.transportCleared ? 'badge-approved' : 'badge-pending')}">
                        ${!app.transportOpted ? 'Not Opted' : (app.transportCleared ? 'Cleared' : 'Pending')}
                    </span>
                </div>
                <div style="font-size: 0.85rem; color: var(--text-secondary); line-height: 1.5;">
                    <div><strong>Service:</strong> ${app.transportOpted ? 'College Bus Opted' : 'Not Availing Transport'}</div>
                    ${app.transportOpted ? `
                        <div><strong>Route:</strong> ${API.escapeHtml(app.busRoute || '—')}</div>
                        <div><strong>Remarks:</strong> ${API.escapeHtml(app.transportRemarks || 'Awaiting bus pass verification')}</div>
                        ${app.transportClearedAt ? `<div style="font-size: 0.75rem; color: var(--text-muted); margin-top: 0.35rem;">Cleared: ${API.formatDateTime(app.transportClearedAt)}</div>` : ''}
                    ` : '<div style="color: var(--text-muted);">No clearance required.</div>'}
                </div>
            </div>

            <!-- Lab Assistant Card -->
            <div style="border: 1px solid var(--border); border-radius: var(--radius-md); padding: 1.2rem; background: ${app.labCleared ? '#f0fdf4' : '#ffffff'};">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem;">
                    <span style="font-weight: 700; font-size: 0.95rem; color: var(--primary-dark);">3. Laboratory Assistant</span>
                    <span class="badge ${app.labCleared ? 'badge-approved' : 'badge-pending'}">${app.labCleared ? 'Cleared' : 'Pending'}</span>
                </div>
                <div style="font-size: 0.85rem; color: var(--text-secondary); line-height: 1.5;">
                    <div><strong>Lab Dues:</strong> ${app.labCleared ? 'No Pending Dues / Kit Returned' : 'Verification Pending'}</div>
                    <div><strong>Remarks:</strong> ${API.escapeHtml(app.labRemarks || 'Awaiting lab assistant clearance')}</div>
                    ${app.labClearedAt ? `<div style="font-size: 0.75rem; color: var(--text-muted); margin-top: 0.35rem;">Cleared: ${API.formatDateTime(app.labClearedAt)}</div>` : ''}
                </div>
            </div>

            <!-- HOD Card -->
            <div style="border: 1px solid var(--border); border-radius: var(--radius-md); padding: 1.2rem; background: ${app.hodApproved ? '#f0fdf4' : '#ffffff'};">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem;">
                    <span style="font-weight: 700; font-size: 0.95rem; color: var(--primary-dark);">4. Head of Department</span>
                    <span class="badge ${app.hodApproved ? 'badge-approved' : 'badge-pending'}">${app.hodApproved ? 'Approved' : 'Pending'}</span>
                </div>
                <div style="font-size: 0.85rem; color: var(--text-secondary); line-height: 1.5;">
                    <div><strong>HOD Approval:</strong> ${app.hodApproved ? 'Approved for Semester Enrollment' : 'Awaiting Final Sign-off'}</div>
                    <div><strong>Remarks:</strong> ${API.escapeHtml(app.hodRemarks || 'Pending all prerequisite clearances')}</div>
                    ${app.hodApprovedAt ? `<div style="font-size: 0.75rem; color: var(--text-muted); margin-top: 0.35rem;">Signed: ${API.formatDateTime(app.hodApprovedAt)}</div>` : ''}
                </div>
            </div>
        </div>
    `;
}

function setupFormListeners() {
    const transportCheckbox = document.getElementById('app-transport-opted');
    const transportDetails = document.getElementById('transport-details-section');
    if (transportCheckbox && transportDetails) {
        transportCheckbox.addEventListener('change', () => {
            transportDetails.style.display = transportCheckbox.checked ? 'block' : 'none';
        });
    }

    const semSelect = document.getElementById('app-semester');
    if (semSelect) {
        semSelect.addEventListener('change', () => {
            updateCourseSuggestions(semSelect.value);
        });
    }

    const form = document.getElementById('registration-submission-form');
    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            await handleFormSubmission();
        });
    }
}

function updateCourseSuggestions(semester) {
    const coursesInput = document.getElementById('app-courses');
    if (!coursesInput) return;

    const sem = parseInt(semester) || 4;
    const sampleCurriculum = {
        1: 'MA101: Engineering Mathematics I, PH101: Engineering Physics, CS101: Problem Solving with Python, CY101: Engineering Chemistry, ME101: Basic Mechanical',
        2: 'MA102: Engineering Mathematics II, CS102: Object Oriented Programming in Java, EE101: Basic Electrical, CS103: Digital Logic Design, CE101: Engineering Mechanics',
        3: 'CS301: Discrete Mathematics, CS302: Data Structures & Algorithms, CS303: Computer Organization, CS304: Object Oriented Software Engineering, CS331: Data Structures Lab',
        4: 'CS401: Database Management Systems, CS402: Operating Systems, CS403: Design & Analysis of Algorithms, CS404: Web Technologies, CS405: Software Engineering, CS431: DBMS Lab, CS432: OS Lab',
        5: 'CS501: Computer Networks, CS502: Theory of Computation, CS503: Microprocessors & Microcontrollers, CS504: Cloud Computing, CS531: Networks Lab',
        6: 'CS601: Compiler Design, CS602: Cryptography & Network Security, CS603: Artificial Intelligence, CS604: Full Stack Development, CS631: AI & Web Lab',
        7: 'CS701: Machine Learning, CS702: Distributed Systems, CS703: Internet of Things, CS704: Elective IV, CS731: Project Phase I',
        8: 'CS801: Cyber Forensics, CS802: Open Elective II, CS831: Major Capstone Project'
    };

    if (sampleCurriculum[sem]) {
        coursesInput.value = sampleCurriculum[sem];
    }
}

async function handleFormSubmission() {
    const resubmitIdInput = document.getElementById('app-resubmit-id');
    const resubmitId = resubmitIdInput ? resubmitIdInput.value.trim() : '';
    const isResubmit = !!resubmitId;

    const submitBtn = document.getElementById('btn-submit-registration');
    submitBtn.disabled = true;
    submitBtn.textContent = isResubmit ? 'Resubmitting Application...' : 'Submitting Application...';

    try {
        const payload = {
            academicYear: document.getElementById('app-academic-year').value.trim(),
            targetSemester: parseInt(document.getElementById('app-semester').value),
            department: document.getElementById('app-department').value.trim(),
            cgpa: parseFloat(document.getElementById('app-cgpa').value),
            backlogsCount: parseInt(document.getElementById('app-backlogs').value) || 0,
            feeReceiptNumber: document.getElementById('app-fee-receipt').value.trim(),
            feeAmountPaid: parseFloat(document.getElementById('app-fee-amount').value),
            feePaymentDate: document.getElementById('app-fee-date').value || null,
            transportOpted: document.getElementById('app-transport-opted').checked,
            busRoute: document.getElementById('app-bus-route').value.trim(),
            busStop: document.getElementById('app-bus-stop').value.trim(),
            transportFeeReceipt: document.getElementById('app-transport-receipt').value.trim(),
            transportFeeAmount: parseFloat(document.getElementById('app-transport-amount').value) || 0.0,
            selectedSubjects: document.getElementById('app-courses').value.trim()
        };

        const url = isResubmit ? `/api/registration/resubmit/${resubmitId}` : '/api/registration/submit';
        const res = await API.request(url, {
            method: 'POST',
            body: JSON.stringify(payload)
        });

        API.showToast(res.message, 'success');
        closeSubmissionModal();
        await loadApplications();
    } catch (err) {
        API.showToast(err.message, 'error');
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = isResubmit ? '🔄 Resubmit Application for Approval' : 'Submit Registration Request';
    }
}

function openSubmissionModal() {
    // Reset resubmit mode
    const resubmitIdInput = document.getElementById('app-resubmit-id');
    if (resubmitIdInput) resubmitIdInput.value = '';

    const titleEl = document.getElementById('submission-modal-title');
    if (titleEl) titleEl.textContent = '📝 New Semester Registration Request';

    const descEl = document.getElementById('submission-modal-desc');
    if (descEl) descEl.textContent = 'Fill in your academic and fee clearance details for the upcoming semester. Once submitted, your application will be routed through Accounts, Transport, Laboratory, and HOD for clearance.';

    const submitBtn = document.getElementById('btn-submit-registration');
    if (submitBtn) submitBtn.textContent = 'Submit Registration Request';

    const alertBox = document.getElementById('modal-resubmit-alert');
    if (alertBox) alertBox.style.display = 'none';

    // Reset defaults from current student
    if (currentStudent) {
        renderStudentProfile();
    }
    document.getElementById('submission-modal').classList.add('active');
}

async function openResubmitModal(appId) {
    let app = studentApplications.find(a => a.id === appId);
    if (!app) {
        try {
            const res = await API.request('/api/registration/' + appId);
            app = res.data;
        } catch (err) {
            API.showToast('Failed to load application details', 'error');
            return;
        }
    }

    // Set hidden resubmit ID
    const resubmitIdInput = document.getElementById('app-resubmit-id');
    if (resubmitIdInput) resubmitIdInput.value = app.id;

    // Update modal title & text
    const titleEl = document.getElementById('submission-modal-title');
    if (titleEl) titleEl.textContent = `🔄 Resubmit Application (${app.applicationNumber})`;

    const descEl = document.getElementById('submission-modal-desc');
    if (descEl) descEl.textContent = 'Review and update your details, correct any disputed fee receipts or information, and resubmit for department approvals.';

    const submitBtn = document.getElementById('btn-submit-registration');
    if (submitBtn) submitBtn.textContent = '🔄 Resubmit Application for Approval';

    // Show rejection warning banner inside modal
    const alertBox = document.getElementById('modal-resubmit-alert');
    const badgeAppNum = document.getElementById('resubmit-badge-appnum');
    const reasonText = document.getElementById('resubmit-alert-reason');
    if (alertBox) {
        alertBox.style.display = 'block';
        if (badgeAppNum) badgeAppNum.textContent = app.applicationNumber;
        if (reasonText) {
            reasonText.innerHTML = `<strong>Rejected at:</strong> ${API.escapeHtml(app.rejectionStage || 'Verification')} Department<br><strong>Remarks:</strong> ${API.escapeHtml(app.rejectionReason || 'No specific remarks given.')}`;
        }
    }

    // Prepopulate form fields with existing data
    if (document.getElementById('app-academic-year')) document.getElementById('app-academic-year').value = app.academicYear || '2025-2026';
    if (document.getElementById('app-semester')) document.getElementById('app-semester').value = app.targetSemester || 4;
    if (document.getElementById('app-department')) document.getElementById('app-department').value = app.department || (currentStudent ? currentStudent.department : '');
    if (document.getElementById('app-cgpa')) document.getElementById('app-cgpa').value = app.cgpa != null ? app.cgpa : '';
    if (document.getElementById('app-backlogs')) document.getElementById('app-backlogs').value = app.backlogsCount !== undefined ? app.backlogsCount : 0;
    if (document.getElementById('app-fee-receipt')) document.getElementById('app-fee-receipt').value = app.feeReceiptNumber || '';
    if (document.getElementById('app-fee-amount')) document.getElementById('app-fee-amount').value = app.feeAmountPaid != null ? app.feeAmountPaid : '';
    if (document.getElementById('app-fee-date')) document.getElementById('app-fee-date').value = app.feePaymentDate || new Date().toISOString().split('T')[0];
    if (document.getElementById('app-courses')) document.getElementById('app-courses').value = app.selectedSubjects || '';

    // Transport details
    const transportCheckbox = document.getElementById('app-transport-opted');
    const transportDetails = document.getElementById('transport-details-section');
    if (transportCheckbox) {
        transportCheckbox.checked = !!app.transportOpted;
        if (transportDetails) transportDetails.style.display = transportCheckbox.checked ? 'block' : 'none';
    }
    if (document.getElementById('app-bus-route')) document.getElementById('app-bus-route').value = app.busRoute || '';
    if (document.getElementById('app-bus-stop')) document.getElementById('app-bus-stop').value = app.busStop || '';
    if (document.getElementById('app-transport-receipt')) document.getElementById('app-transport-receipt').value = app.transportFeeReceipt || '';
    if (document.getElementById('app-transport-amount')) document.getElementById('app-transport-amount').value = app.transportFeeAmount || 0;

    // Open modal
    document.getElementById('submission-modal').classList.add('active');
}

function closeSubmissionModal() {
    document.getElementById('submission-modal').classList.remove('active');
}

async function viewApplicationDetails(id) {
    try {
        const res = await API.request('/api/registration/' + id);
        renderActiveApplication(res.data);
        window.scrollTo({ top: 0, behavior: 'smooth' });
    } catch (err) {
        API.showToast(err.message, 'error');
    }
}

