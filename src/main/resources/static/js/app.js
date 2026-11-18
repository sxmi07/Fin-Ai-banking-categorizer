/**
 * FinAI — Frontend Application Logic
 * Modular SPA architecture managing Dashboard, Transactions, AI Categorizer, Analytics, and Settings.
 */

// Global Application State
const AppState = {
    currentView: 'dashboard',
    transactionsPage: 0,
    transactionsPageSize: 10,
    transactionsTotalPages: 1,
    searchQuery: '',
    categoryFilter: '',
    typeFilter: '',
    charts: {}
};

// Category Colors for Visual Consistency
const CategoryColors = {
    FOOD: { bg: '#ffedd5', text: '#c2410c', chart: '#f97316' },
    TRAVEL: { bg: '#dbeafe', text: '#1d4ed8', chart: '#3b82f6' },
    UTILITIES: { bg: '#fef9c3', text: '#a16207', chart: '#eab308' },
    SHOPPING: { bg: '#fce7f3', text: '#be185d', chart: '#ec4899' },
    ENTERTAINMENT: { bg: '#ede9fe', text: '#6d28d9', chart: '#8b5cf6' },
    HEALTHCARE: { bg: '#d1fae5', text: '#047857', chart: '#10b981' },
    EDUCATION: { bg: '#cffafe', text: '#0e7490', chart: '#06b6d4' },
    RENT: { bg: '#e0e7ff', text: '#4338ca', chart: '#6366f1' },
    TRANSFERS: { bg: '#ccfbf1', text: '#0f766e', chart: '#14b8a6' },
    OTHER: { bg: '#f1f5f9', text: '#475569', chart: '#64748b' }
};

// Initialize Application on DOM Ready
document.addEventListener('DOMContentLoaded', () => {
    initNavigation();
    initEventListeners();
    fetchSystemHealth();
    loadDashboardData();
});

/* ==============================================================================
   1. NAVIGATION & ROUTING
   ============================================================================== */
function initNavigation() {
    const navLinks = document.querySelectorAll('.nav-link, [data-view]');
    navLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const viewId = link.getAttribute('data-view');
            if (viewId) {
                switchView(viewId);
            }
        });
    });

    // Mobile Sidebar Toggle
    const sidebarToggle = document.getElementById('sidebarToggle');
    const sidebar = document.getElementById('sidebar');
    if (sidebarToggle && sidebar) {
        sidebarToggle.addEventListener('click', () => {
            sidebar.classList.toggle('show');
        });
    }

    // Default route from hash or dashboard
    const initialView = window.location.hash.replace('#', '') || 'dashboard';
    switchView(initialView);
}

function switchView(viewName) {
    const validViews = ['dashboard', 'transactions', 'categorize', 'analytics', 'settings'];
    if (!validViews.includes(viewName)) {
        viewName = 'dashboard';
    }

    AppState.currentView = viewName;
    window.location.hash = viewName;

    // Hide all views
    document.querySelectorAll('.app-view').forEach(view => view.classList.add('d-none'));

    // Show target view
    const target = document.getElementById(`view-${viewName}`);
    if (target) {
        target.classList.remove('d-none');
    }

    // Update active nav link
    document.querySelectorAll('.nav-link').forEach(link => {
        if (link.getAttribute('data-view') === viewName) {
            link.classList.add('active');
        } else {
            link.classList.remove('active');
        }
    });

    // Update Topbar Titles
    const titles = {
        dashboard: { title: 'Financial Overview', sub: 'Real-time banking analytics and AI transaction intelligence' },
        transactions: { title: 'Transaction Records', sub: 'Search, filter, and inspect banking statement line items' },
        categorize: { title: 'AI Categorization Studio', sub: 'Transform raw statement descriptions into intelligent insights' },
        analytics: { title: 'Spending Analytics', sub: 'Visual distributions and cash flow breakdown' },
        settings: { title: 'Application Settings', sub: 'System telemetry, AI provider status, and demo data controls' }
    };

    if (titles[viewName]) {
        document.getElementById('pageTitle').textContent = titles[viewName].title;
        document.getElementById('pageSubtitle').textContent = titles[viewName].sub;
    }

    // View-specific data fetching
    if (viewName === 'dashboard') loadDashboardData();
    if (viewName === 'transactions') loadTransactionsData();
    if (viewName === 'analytics') loadAnalyticsData();
    if (viewName === 'settings') fetchSystemHealth();
}

/* ==============================================================================
   2. SYSTEM HEALTH & TELEMETRY
   ============================================================================== */
async function fetchSystemHealth() {
    try {
        const response = await fetch('/api/health');
        if (!response.ok) return;
        const data = await response.json();

        const isAi = data.aiKeyConfigured;
        const modeBadge = document.getElementById('topbarAiMode');
        const sidebarAiProvider = document.getElementById('sidebarAiProvider');
        const settingsAiBadge = document.getElementById('settingsAiBadge');

        if (modeBadge) {
            modeBadge.textContent = isAi ? 'AI Active (Gemini)' : 'Demo / Fallback Mode';
        }
        if (sidebarAiProvider) {
            sidebarAiProvider.textContent = isAi ? 'Cloud AI: Gemini 1.5' : 'Engine: Rule-Based Fallback';
        }
        if (settingsAiBadge) {
            settingsAiBadge.innerHTML = isAi
                ? '<i class="fa-solid fa-robot me-1"></i> Google Gemini 1.5 Flash (Online)'
                : '<i class="fa-solid fa-shield-halved me-1"></i> Rule-Based Fallback Mode';
        }
    } catch (err) {
        console.warn('Health check unavailable:', err);
    }
}

/* ==============================================================================
   3. DASHBOARD VIEW CONTROLLER
   ============================================================================== */
async function loadDashboardData(year, month) {
    try {
        let summaryUrl = '/api/transactions/summary';
        if (year && month) {
            summaryUrl += `?year=${year}&month=${month}`;
        }
        const summaryRes = await fetch(summaryUrl);
        if (summaryRes.ok) {
            const json = await summaryRes.json();
            if (json.success && json.data) {
                renderDashboardSummary(json.data);
            }
        }

        const recentRes = await fetch('/api/transactions?size=5&sortDir=DESC');
        if (recentRes.ok) {
            const json = await recentRes.json();
            if (json.success && json.data && json.data.content) {
                renderDashboardRecentTable(json.data.content);
            }
        }
    } catch (err) {
        console.error('Error loading dashboard:', err);
    }
}

function renderDashboardSummary(summary) {
    document.getElementById('kpiTotalExpenses').textContent = formatCurrency(summary.totalExpenses);
    document.getElementById('kpiTotalIncome').textContent = formatCurrency(summary.totalIncome);
    document.getElementById('kpiAiCategorized').textContent = summary.aiCategorizedCount || 0;
    document.getElementById('kpiTopCategory').textContent = summary.topSpendingCategory || 'N/A';
    document.getElementById('kpiTopCategoryAmount').textContent = `${formatCurrency(summary.topCategoryAmount || 0)} total spend`;
    document.getElementById('kpiExpenseCount').textContent = `${summary.totalTransactions || 0} total transactions recorded`;

    renderDashboardCharts(summary);
}

function renderDashboardRecentTable(transactions) {
    const tbody = document.getElementById('dashboardRecentTbody');
    if (!tbody) return;

    if (!transactions || transactions.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="text-center py-4 text-muted">No transactions found</td></tr>`;
        return;
    }

    tbody.innerHTML = transactions.map(tx => `
        <tr>
            <td class="text-nowrap text-muted">${formatDate(tx.transactionDate)}</td>
            <td>
                <div class="fw-semibold text-dark">${escapeHtml(tx.merchantName)}</div>
                <div class="small text-muted text-truncate" style="max-width: 250px;">${escapeHtml(tx.rawDescription)}</div>
            </td>
            <td>${renderCategoryBadge(tx.category, tx.categoryDisplayName)}</td>
            <td><span class="type-tag ${tx.transactionType === 'CREDIT' ? 'type-credit' : 'type-debit'}">${tx.transactionType}</span></td>
            <td>${renderConfidenceMeter(tx.aiConfidence)}</td>
            <td class="text-end tx-amount ${tx.transactionType === 'CREDIT' ? 'amount-credit' : 'amount-debit'}">
                ${tx.transactionType === 'CREDIT' ? '+' : '-'}${formatCurrency(tx.amount)}
            </td>
        </tr>
    `).join('');
}

function renderDashboardCharts(summary) {
    // 1. Spending Trends Chart
    const ctxTrend = document.getElementById('chartExpenseTrends');
    if (ctxTrend) {
        if (AppState.charts.trend) AppState.charts.trend.destroy();

        AppState.charts.trend = new Chart(ctxTrend, {
            type: 'bar',
            data: {
                labels: ['Outgoing Expenses', 'Incoming Credits', 'Net Balance'],
                datasets: [{
                    label: 'Amount (₹)',
                    data: [
                        summary.totalExpenses || 0,
                        summary.totalIncome || 0,
                        summary.netSavings || 0
                    ],
                    backgroundColor: ['#2563eb', '#10b981', '#7c3aed'],
                    borderRadius: 8
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { display: false } },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: { callback: val => '₹' + val.toLocaleString() }
                    }
                }
            }
        });
    }

    // 2. Category Doughnut Chart
    const ctxDoughnut = document.getElementById('chartCategoryDoughnut');
    if (ctxDoughnut && summary.categoryExpenses) {
        if (AppState.charts.doughnut) AppState.charts.doughnut.destroy();

        const labels = Object.keys(summary.categoryExpenses);
        const data = Object.values(summary.categoryExpenses);
        const colors = labels.map(l => (CategoryColors[l.toUpperCase()] ? CategoryColors[l.toUpperCase()].chart : '#94a3b8'));

        AppState.charts.doughnut = new Chart(ctxDoughnut, {
            type: 'doughnut',
            data: {
                labels: labels,
                datasets: [{
                    data: data,
                    backgroundColor: colors,
                    borderWidth: 2,
                    borderColor: '#ffffff'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { position: 'bottom', labels: { boxWidth: 12, font: { size: 11 } } }
                },
                cutout: '68%'
            }
        });
    }
}

/* ==============================================================================
   4. TRANSACTIONS VIEW CONTROLLER
   ============================================================================== */
async function loadTransactionsData() {
    const tbody = document.getElementById('transactionsMasterTbody');
    if (!tbody) return;

    tbody.innerHTML = `<tr><td colspan="9" class="text-center py-4 text-muted"><div class="spinner-border spinner-border-sm text-primary me-2"></div>Loading records...</td></tr>`;

    const params = new URLSearchParams({
        page: AppState.transactionsPage,
        size: AppState.transactionsPageSize,
        sortBy: 'transactionDate',
        sortDir: 'DESC'
    });

    if (AppState.searchQuery) params.append('search', AppState.searchQuery);
    if (AppState.categoryFilter) params.append('category', AppState.categoryFilter);
    if (AppState.typeFilter) params.append('type', AppState.typeFilter);

    try {
        const response = await fetch(`/api/transactions?${params.toString()}`);
        if (!response.ok) throw new Error('Failed to load transactions');
        const json = await response.json();

        if (json.success && json.data) {
            const pageData = json.data;
            AppState.transactionsTotalPages = pageData.totalPages || 1;
            renderTransactionsTable(pageData.content || []);
            renderPaginationControls(pageData);
        }
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="9" class="text-center py-4 text-danger">Failed to load transactions: ${err.message}</td></tr>`;
    }
}

function renderTransactionsTable(transactions) {
    const tbody = document.getElementById('transactionsMasterTbody');
    if (!tbody) return;

    if (transactions.length === 0) {
        tbody.innerHTML = `<tr><td colspan="9" class="text-center py-4 text-muted">No matching transactions found</td></tr>`;
        return;
    }

    tbody.innerHTML = transactions.map(tx => `
        <tr>
            <td class="text-muted small">#${tx.id}</td>
            <td class="text-nowrap">${formatDate(tx.transactionDate)}</td>
            <td class="fw-semibold text-dark">${escapeHtml(tx.merchantName)}</td>
            <td class="text-truncate" style="max-width: 200px;" title="${escapeHtml(tx.rawDescription)}">
                ${escapeHtml(tx.rawDescription)}
            </td>
            <td>
                ${renderCategoryBadge(tx.category, tx.categoryDisplayName)}
                ${tx.manuallyEdited ? '<span class="badge bg-light text-muted border ms-1" title="Manually edited">Edited</span>' : ''}
            </td>
            <td>${renderConfidenceMeter(tx.aiConfidence)}</td>
            <td><span class="type-tag ${tx.transactionType === 'CREDIT' ? 'type-credit' : 'type-debit'}">${tx.transactionType}</span></td>
            <td class="text-end tx-amount ${tx.transactionType === 'CREDIT' ? 'amount-credit' : 'amount-debit'}">
                ${tx.transactionType === 'CREDIT' ? '+' : '-'}${formatCurrency(tx.amount)}
            </td>
            <td class="text-center">
                <button class="btn btn-sm btn-outline-secondary py-0 px-2 me-1" onclick="openEditModal(${tx.id}, '${escapeHtml(tx.rawDescription)}', '${tx.category}', '${escapeHtml(tx.subcategory || '')}')" title="Change Category">
                    <i class="fa-solid fa-pen"></i>
                </button>
                <button class="btn btn-sm btn-outline-danger py-0 px-2" onclick="deleteTransaction(${tx.id})" title="Delete">
                    <i class="fa-solid fa-trash"></i>
                </button>
            </td>
        </tr>
    `).join('');
}

function renderPaginationControls(pageData) {
    const info = document.getElementById('txPaginationInfo');
    const prevBtn = document.getElementById('txPrevPageBtn');
    const nextBtn = document.getElementById('txNextPageBtn');

    const totalElements = pageData.totalElements || 0;
    const start = totalElements === 0 ? 0 : (pageData.number * pageData.size) + 1;
    const end = Math.min((pageData.number + 1) * pageData.size, totalElements);

    if (info) info.textContent = `Showing ${start} to ${end} of ${totalElements} transactions`;
    if (prevBtn) prevBtn.disabled = pageData.first;
    if (nextBtn) nextBtn.disabled = pageData.last;
}

/* ==============================================================================
   5. AI CATEGORIZATION CONTROLLER
   ============================================================================== */
async function runAiCategorization() {
    const input = document.getElementById('rawTransactionsInput');
    const btn = document.getElementById('btnRunAiCategorization');
    const btnText = document.getElementById('btnRunAiText');
    const spinner = document.getElementById('categorizeSpinner');
    const autoSave = document.getElementById('categorizeAutoSave').checked;
    const defaultAmount = document.getElementById('categorizeDefaultAmount').value;
    const defaultDate = document.getElementById('categorizeDefaultDate').value;

    if (!input || !input.value.trim()) {
        showToast('Please enter at least one transaction description to categorize.', 'warning');
        return;
    }

    const lines = input.value.split('\n')
        .map(l => l.trim())
        .filter(l => l.length > 0);

    if (lines.length === 0) {
        showToast('No valid lines entered', 'warning');
        return;
    }

    // Enter loading state
    btn.disabled = true;
    btnText.textContent = 'Processing with AI...';
    spinner.classList.remove('d-none');

    const payload = {
        descriptions: lines,
        saveToDatabase: autoSave,
        defaultAmount: defaultAmount ? parseFloat(defaultAmount) : null,
        defaultDate: defaultDate ? defaultDate : null
    };

    try {
        const response = await fetch('/api/transactions/categorize', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const json = await response.json();

        if (response.ok && json.success) {
            renderAiResults(json.data || []);
            showToast(`Categorized ${lines.length} transaction(s) successfully!`, 'success');
            if (autoSave) {
                loadDashboardData(); // Refresh metrics in background
            }
        } else {
            showToast(json.message || 'Categorization failed', 'danger');
        }
    } catch (err) {
        showToast('Network error during AI categorization: ' + err.message, 'danger');
    } finally {
        btn.disabled = false;
        btnText.textContent = 'Categorize with AI';
        spinner.classList.add('d-none');
    }
}

function renderAiResults(results) {
    const container = document.getElementById('aiResultsContainer');
    const badge = document.getElementById('aiResultsCountBadge');
    if (!container) return;

    if (!results || results.length === 0) {
        container.innerHTML = `<div class="text-center py-5 text-muted">No results returned</div>`;
        return;
    }

    if (badge) badge.textContent = `${results.length} Categorized`;

    container.innerHTML = results.map(res => `
        <div class="ai-result-card">
            <div class="d-flex justify-content-between align-items-start mb-2">
                <div>
                    <span class="fw-bold text-dark fs-6">${escapeHtml(res.merchantName || 'Unknown')}</span>
                    <div class="small text-muted text-break mt-1">"${escapeHtml(res.originalDescription)}"</div>
                </div>
                <div>
                    ${renderCategoryBadge(res.category, res.categoryDisplayName)}
                </div>
            </div>
            
            <div class="d-flex flex-wrap align-items-center justify-content-between gap-2 mt-3 pt-2 border-top">
                <div class="d-flex align-items-center gap-2">
                    <span class="small text-muted">Confidence:</span>
                    ${renderConfidenceMeter(res.confidence)}
                </div>
                <span class="badge bg-light text-secondary border small">
                    <i class="fa-solid fa-microchip me-1 text-primary"></i>${escapeHtml(res.provider || 'AI Engine')}
                </span>
            </div>

            <div class="small text-secondary bg-light p-2 rounded mt-2">
                <i class="fa-solid fa-circle-info text-primary me-1"></i> ${escapeHtml(res.explanation || 'Classified based on merchant identity.')}
            </div>
        </div>
    `).join('');
}

/* ==============================================================================
   6. ANALYTICS VIEW CONTROLLER
   ============================================================================== */
async function loadAnalyticsData() {
    try {
        const response = await fetch('/api/transactions/summary');
        if (!response.ok) return;
        const json = await response.json();
        if (!json.success || !json.data) return;

        const data = json.data;
        document.getElementById('analyticsAvgDebit').textContent = formatCurrency(data.averageExpenseAmount || 0);
        document.getElementById('analyticsNetSavings').textContent = formatCurrency(data.netSavings || 0);
        document.getElementById('analyticsManualCount').textContent = data.manuallyEditedCount || 0;

        renderAnalyticsCharts(data);
    } catch (err) {
        console.error('Error loading analytics:', err);
    }
}

function renderAnalyticsCharts(summary) {
    if (!summary.categoryExpenses) return;

    const labels = Object.keys(summary.categoryExpenses);
    const amounts = Object.values(summary.categoryExpenses);
    const colors = labels.map(l => (CategoryColors[l.toUpperCase()] ? CategoryColors[l.toUpperCase()].chart : '#3b82f6'));

    // 1. Bar Chart
    const ctxBar = document.getElementById('chartAnalyticsBar');
    if (ctxBar) {
        if (AppState.charts.analyticsBar) AppState.charts.analyticsBar.destroy();

        AppState.charts.analyticsBar = new Chart(ctxBar, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Expense (₹)',
                    data: amounts,
                    backgroundColor: colors,
                    borderRadius: 6
                }]
            },
            options: {
                indexAxis: 'y',
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { display: false } },
                scales: {
                    x: { ticks: { callback: v => '₹' + v.toLocaleString() } }
                }
            }
        });
    }

    // 2. Polar Area Chart (Transaction Counts)
    const ctxPolar = document.getElementById('chartAnalyticsPolar');
    if (ctxPolar && summary.categoryCounts) {
        if (AppState.charts.analyticsPolar) AppState.charts.analyticsPolar.destroy();

        const countLabels = Object.keys(summary.categoryCounts);
        const countValues = Object.values(summary.categoryCounts);

        AppState.charts.analyticsPolar = new Chart(ctxPolar, {
            type: 'polarArea',
            data: {
                labels: countLabels,
                datasets: [{
                    data: countValues,
                    backgroundColor: countLabels.map(l => (CategoryColors[l.toUpperCase()] ? CategoryColors[l.toUpperCase()].chart + 'B3' : '#94a3b8B3'))
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { position: 'bottom', labels: { boxWidth: 10, font: { size: 10 } } }
                }
            }
        });
    }
}

/* ==============================================================================
   7. MODALS & MUTATION ACTIONS
   ============================================================================== */
window.openEditModal = function(id, rawDesc, category, subcategory) {
    document.getElementById('editTxId').value = id;
    document.getElementById('editTxDescription').textContent = rawDesc;
    document.getElementById('editTxCategory').value = category;
    document.getElementById('editTxSubcategory').value = subcategory || '';

    const modal = new bootstrap.Modal(document.getElementById('editCategoryModal'));
    modal.show();
};

async function saveCategoryEdit() {
    const id = document.getElementById('editTxId').value;
    const category = document.getElementById('editTxCategory').value;
    const subcategory = document.getElementById('editTxSubcategory').value;

    try {
        const res = await fetch(`/api/transactions/${id}/category`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ category, subcategory })
        });
        const json = await res.json();
        if (res.ok && json.success) {
            bootstrap.Modal.getInstance(document.getElementById('editCategoryModal')).hide();
            showToast('Category updated successfully!', 'success');
            loadTransactionsData();
            loadDashboardData();
        } else {
            showToast(json.message || 'Failed to update category', 'danger');
        }
    } catch (err) {
        showToast('Error updating category: ' + err.message, 'danger');
    }
}

window.deleteTransaction = async function(id) {
    if (!confirm('Are you sure you want to delete this transaction record?')) return;

    try {
        const res = await fetch(`/api/transactions/${id}`, { method: 'DELETE' });
        if (res.ok) {
            showToast('Transaction deleted successfully', 'success');
            loadTransactionsData();
            loadDashboardData();
        } else {
            showToast('Failed to delete transaction', 'danger');
        }
    } catch (err) {
        showToast('Error: ' + err.message, 'danger');
    }
};

async function submitNewTransaction() {
    const desc = document.getElementById('newTxDescription').value;
    const amount = document.getElementById('newTxAmount').value;
    const date = document.getElementById('newTxDate').value;
    const type = document.getElementById('newTxType').value;

    if (!desc || !amount) {
        showToast('Please provide both description and amount', 'warning');
        return;
    }

    try {
        const res = await fetch('/api/transactions', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                description: desc,
                amount: parseFloat(amount),
                date: date || null,
                type: type
            })
        });

        const json = await res.json();
        if (res.ok && json.success) {
            bootstrap.Modal.getInstance(document.getElementById('newTransactionModal')).hide();
            document.getElementById('newTransactionForm').reset();
            showToast('Transaction created successfully!', 'success');
            loadTransactionsData();
            loadDashboardData();
        } else {
            showToast(json.message || 'Failed to create transaction', 'danger');
        }
    } catch (err) {
        showToast('Error: ' + err.message, 'danger');
    }
}

/* ==============================================================================
   8. EVENT LISTENERS
   ============================================================================== */
function initEventListeners() {
    // Refresh dashboard button
    const refreshBtn = document.getElementById('refreshDashboardBtn');
    if (refreshBtn) refreshBtn.addEventListener('click', loadDashboardData);

    // AI Categorization Trigger
    const btnRunAi = document.getElementById('btnRunAiCategorization');
    if (btnRunAi) btnRunAi.addEventListener('click', runAiCategorization);

    // Clear input
    const btnClearAi = document.getElementById('btnClearAiInput');
    if (btnClearAi) {
        btnClearAi.addEventListener('click', () => {
            document.getElementById('rawTransactionsInput').value = '';
        });
    }

    // Sample Chips
    document.querySelectorAll('.sample-chip').forEach(chip => {
        chip.addEventListener('click', () => {
            const sample = chip.getAttribute('data-sample');
            document.getElementById('rawTransactionsInput').value = sample;
        });
    });

    // Transactions Search & Filters
    const searchInput = document.getElementById('txSearchInput');
    if (searchInput) {
        let debounceTimer;
        searchInput.addEventListener('input', (e) => {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(() => {
                AppState.searchQuery = e.target.value.trim();
                AppState.transactionsPage = 0;
                loadTransactionsData();
            }, 300);
        });
    }

    const catFilter = document.getElementById('txCategoryFilter');
    if (catFilter) {
        catFilter.addEventListener('change', (e) => {
            AppState.categoryFilter = e.target.value;
            AppState.transactionsPage = 0;
            loadTransactionsData();
        });
    }

    const typeFilter = document.getElementById('txTypeFilter');
    if (typeFilter) {
        typeFilter.addEventListener('change', (e) => {
            AppState.typeFilter = e.target.value;
            AppState.transactionsPage = 0;
            loadTransactionsData();
        });
    }

    const resetFiltersBtn = document.getElementById('txFilterResetBtn');
    if (resetFiltersBtn) {
        resetFiltersBtn.addEventListener('click', () => {
            document.getElementById('txSearchInput').value = '';
            document.getElementById('txCategoryFilter').value = '';
            document.getElementById('txTypeFilter').value = '';
            AppState.searchQuery = '';
            AppState.categoryFilter = '';
            AppState.typeFilter = '';
            AppState.transactionsPage = 0;
            loadTransactionsData();
        });
    }

    // Pagination
    const prevBtn = document.getElementById('txPrevPageBtn');
    if (prevBtn) {
        prevBtn.addEventListener('click', () => {
            if (AppState.transactionsPage > 0) {
                AppState.transactionsPage--;
                loadTransactionsData();
            }
        });
    }

    const nextBtn = document.getElementById('txNextPageBtn');
    if (nextBtn) {
        nextBtn.addEventListener('click', () => {
            if (AppState.transactionsPage < AppState.transactionsTotalPages - 1) {
                AppState.transactionsPage++;
                loadTransactionsData();
            }
        });
    }

    // CSV Export
    const exportBtn = document.getElementById('btnExportCsv');
    if (exportBtn) {
        exportBtn.addEventListener('click', () => {
            showToast('Preparing transactions CSV export...', 'info');
            window.location.href = '/api/transactions/export';
        });
    }

    // Reset Demo Data
    const resetDemoBtn = document.getElementById('btnResetDemoData');
    if (resetDemoBtn) {
        resetDemoBtn.addEventListener('click', resetDemonstrationData);
    }

    // Month Selector on Dashboard
    const monthSelector = document.getElementById('dashboardMonthSelector');
    if (monthSelector) {
        monthSelector.addEventListener('change', (e) => {
            const val = e.target.value;
            const now = new Date();
            let year = null;
            let month = null;

            if (val === 'CURRENT') {
                year = now.getFullYear();
                month = now.getMonth() + 1;
            } else if (val === 'PREVIOUS') {
                const prev = new Date(now.getFullYear(), now.getMonth() - 1, 1);
                year = prev.getFullYear();
                month = prev.getMonth() + 1;
            }
            loadDashboardData(year, month);
        });
    }

    // Modal Actions
    const saveCategoryBtn = document.getElementById('btnSaveCategoryEdit');
    if (saveCategoryBtn) saveCategoryBtn.addEventListener('click', saveCategoryEdit);

    const submitNewTxBtn = document.getElementById('btnSubmitNewTx');
    if (submitNewTxBtn) submitNewTxBtn.addEventListener('click', submitNewTransaction);

    // CSV Dropzone
    const dropzone = document.getElementById('csvDropzone');
    const fileInput = document.getElementById('csvFileInput');
    if (dropzone && fileInput) {
        dropzone.addEventListener('click', () => fileInput.click());
        dropzone.addEventListener('dragover', (e) => {
            e.preventDefault();
            dropzone.classList.add('dragover');
        });
        dropzone.addEventListener('dragleave', () => dropzone.classList.remove('dragover'));
        dropzone.addEventListener('drop', (e) => {
            e.preventDefault();
            dropzone.classList.remove('dragover');
            if (e.dataTransfer.files.length > 0) {
                handleCsvFile(e.dataTransfer.files[0]);
            }
        });
        fileInput.addEventListener('change', (e) => {
            if (e.target.files.length > 0) {
                handleCsvFile(e.target.files[0]);
            }
        });
    }
}

async function handleCsvFile(file) {
    if (!file.name.endsWith('.csv')) {
        showToast('Please upload a valid .csv file', 'warning');
        return;
    }

    const dropzone = document.getElementById('csvDropzone');
    const originalHtml = dropzone ? dropzone.innerHTML : '';
    if (dropzone) {
        dropzone.innerHTML = `
            <div class="spinner-border spinner-border-sm text-primary mb-2"></div>
            <div class="fw-semibold text-secondary">Uploading & Categorizing statement...</div>
            <div class="text-muted small">Spring AI / Heuristic engine is parsing rows</div>
        `;
    }

    const formData = new FormData();
    formData.append('file', file);

    try {
        const response = await fetch('/api/transactions/import', {
            method: 'POST',
            body: formData
        });

        const json = await response.json();
        if (response.ok && json.success) {
            const data = json.data || {};
            showToast(`CSV Imported! ${data.importedCount || 0} transactions categorized and saved.`, 'success');
            loadTransactionsData();
            loadDashboardData();
            loadAnalyticsData();
        } else {
            showToast(json.message || 'Failed to import CSV statement', 'danger');
        }
    } catch (err) {
        showToast('CSV Upload error: ' + err.message, 'danger');
    } finally {
        if (dropzone) dropzone.innerHTML = originalHtml;
    }
}

async function resetDemonstrationData() {
    if (!confirm('Are you sure you want to reset the database back to the 30 sample Indian banking transactions?')) {
        return;
    }

    const resetBtn = document.getElementById('btnResetDemoData');
    const originalText = resetBtn ? resetBtn.innerHTML : '';
    if (resetBtn) {
        resetBtn.disabled = true;
        resetBtn.innerHTML = '<div class="spinner-border spinner-border-sm me-1"></div> Resetting...';
    }

    try {
        const response = await fetch('/api/demo/reset', { method: 'POST' });
        const json = await response.json();

        if (response.ok && json.success) {
            showToast('Demonstration data reloaded successfully! (30 records)', 'success');
            loadDashboardData();
            loadTransactionsData();
            loadAnalyticsData();
        } else {
            showToast(json.message || 'Failed to reset demo data', 'danger');
        }
    } catch (err) {
        showToast('Reset error: ' + err.message, 'danger');
    } finally {
        if (resetBtn) {
            resetBtn.disabled = false;
            resetBtn.innerHTML = originalText;
        }
    }
}

/* ==============================================================================
   9. FORMATTERS & UI HELPERS
   ============================================================================== */
function formatCurrency(amount) {
    if (amount === null || amount === undefined) return '₹0.00';
    const num = Number(amount);
    return '₹' + num.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function formatDate(dateStr) {
    if (!dateStr) return '';
    try {
        const d = new Date(dateStr);
        return d.toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' });
    } catch {
        return dateStr;
    }
}

function renderCategoryBadge(category, displayName) {
    const key = (category || 'OTHER').toUpperCase();
    const styleClass = `badge-${key.toLowerCase()}`;
    const name = displayName || key;
    return `<span class="category-badge ${styleClass}">${escapeHtml(name)}</span>`;
}

function renderConfidenceMeter(score) {
    const val = (score !== null && score !== undefined) ? Number(score) : 0.0;
    const pct = Math.round(val * 100);
    let fillClass = 'fill-high';
    if (pct < 70) fillClass = 'fill-mid';
    if (pct < 50) fillClass = 'fill-low';

    return `
        <div class="confidence-wrapper">
            <div class="confidence-bar">
                <div class="confidence-fill ${fillClass}" style="width: ${pct}%"></div>
            </div>
            <span class="small fw-semibold text-muted" style="font-size: 0.75rem;">${pct}%</span>
        </div>
    `;
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, '&amp;')
              .replace(/</g, '&lt;')
              .replace(/>/g, '&gt;')
              .replace(/"/g, '&quot;')
              .replace(/'/g, '&#039;');
}

function showToast(message, type = 'primary') {
    const toastEl = document.getElementById('appToast');
    const msgEl = document.getElementById('toastMessage');
    if (!toastEl || !msgEl) return;

    msgEl.textContent = message;
    toastEl.className = `toast align-items-center text-white bg-${type} border-0`;
    const toast = new bootstrap.Toast(toastEl, { delay: 3500 });
    toast.show();
}
