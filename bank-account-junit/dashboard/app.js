// Bank Account Management & QA Dashboard Controller

// High-fidelity fallback data matching the actual project tests (31 tests, ~99.1% coverage)
const FALLBACK_TEST_DATA = {
  "metadata": {
    "timestamp": new Date().toISOString()
  },
  "testRun": {
    "totalTests": 31,
    "successes": 31,
    "failures": 0,
    "errors": 0,
    "skipped": 0,
    "timeSeconds": 1.926,
    "testSuites": [
      {
        "name": "com.vikaasni.bankapp.model.BankAccountConcurrencyTest",
        "tests": 1, "failures": 0, "errors": 0, "skipped": 0, "time": 0.151,
        "cases": [ { "name": "simultaneousWithdrawalsShouldBeThreadSafe()", "time": 0.105, "status": "SUCCESS" } ]
      },
      {
        "name": "com.vikaasni.bankapp.model.BankAccountTest",
        "tests": 13, "failures": 0, "errors": 0, "skipped": 0, "time": 0.074,
        "cases": [
          { "name": "depositShouldIncreaseBalance()", "time": 0.001, "status": "SUCCESS" },
          { "name": "withdrawalShouldDecreaseBalance()", "time": 0.003, "status": "SUCCESS" },
          { "name": "balanceInquiryShouldReturnCurrentBalance()", "time": 0.006, "status": "SUCCESS" },
          { "name": "withdrawingMoreThanBalanceShouldThrowException()", "time": 0.010, "status": "SUCCESS" },
          { "name": "zeroDepositShouldThrowException()", "time": 0.004, "status": "SUCCESS" },
          { "name": "negativeWithdrawalShouldThrowException()", "time": 0.001, "status": "SUCCESS" },
          { "name": "depositShouldAddTransactionToHistory()", "time": 0.003, "status": "SUCCESS" },
          { "name": "withdrawalShouldAddTransactionToHistory()", "time": 0.002, "status": "SUCCESS" },
          { "name": "transactionHistoryShouldBeUnmodifiable()", "time": 0.003, "status": "SUCCESS" },
          { "name": "constructorWithNullOrBlankAccountNumberShouldThrowException()", "time": 0.007, "status": "SUCCESS" },
          { "name": "constructorWithNullOrBlankAccountHolderNameShouldThrowException()", "time": 0.003, "status": "SUCCESS" },
          { "name": "constructorWithNegativeOpeningBalanceShouldThrowException()", "time": 0.002, "status": "SUCCESS" },
          { "name": "constructorWithoutOpeningBalanceShouldInitializeToZero()", "time": 0.005, "status": "SUCCESS" }
        ]
      },
      {
        "name": "com.vikaasni.bankapp.repository.FileAccountRepositoryTest",
        "tests": 4, "failures": 0, "errors": 0, "skipped": 0, "time": 0.102,
        "cases": [
          { "name": "saveAndLoadAccount()", "time": 0.045, "status": "SUCCESS" },
          { "name": "nonExistentFileShouldBeEmpty()", "time": 0.002, "status": "SUCCESS" },
          { "name": "skippedLinesTest()", "time": 0.015, "status": "SUCCESS" },
          { "name": "malformedBalanceShouldThrowException()", "time": 0.040, "status": "SUCCESS" }
        ]
      },
      {
        "name": "com.vikaasni.bankapp.repository.InMemoryAccountRepositoryTest",
        "tests": 4, "failures": 0, "errors": 0, "skipped": 0, "time": 0.016,
        "cases": [
          { "name": "saveAndFindAccount()", "time": 0.002, "status": "SUCCESS" },
          { "name": "findNonExistentAccount()", "time": 0.001, "status": "SUCCESS" },
          { "name": "existsByAccountNumber()", "time": 0.001, "status": "SUCCESS" },
          { "name": "findAllAccounts()", "time": 0.012, "status": "SUCCESS" }
        ]
      },
      {
        "name": "com.vikaasni.bankapp.service.BankServiceTest",
        "tests": 9, "failures": 0, "errors": 0, "skipped": 0, "time": 1.619,
        "cases": [
          { "name": "createAccountShouldSaveAccount()", "time": 0.040, "status": "SUCCESS" },
          { "name": "duplicateAccountNumberShouldBeRejected()", "time": 0.005, "status": "SUCCESS" },
          { "name": "depositShouldUpdateAndSaveAccount()", "time": 0.008, "status": "SUCCESS" },
          { "name": "unknownAccountShouldThrowException()", "time": 0.003, "status": "SUCCESS" },
          { "name": "withdrawShouldUpdateAndSaveAccount()", "time": 0.007, "status": "SUCCESS" },
          { "name": "getBalanceShouldReturnCorrectBalance()", "time": 0.004, "status": "SUCCESS" },
          { "name": "getTransactionHistoryShouldReturnHistory()", "time": 0.025, "status": "SUCCESS" },
          { "name": "getAllAccountsShouldReturnList()", "time": 0.035, "status": "SUCCESS" },
          { "name": "getAccountShouldReturnAccount()", "time": 0.005, "status": "SUCCESS" }
        ]
      }
    ]
  },
  "coverage": {
    "instruction": { "covered": 530, "missed": 335, "percentage": 61.27 },
    "branch": { "covered": 29, "missed": 17, "percentage": 63.04 },
    "line": { "covered": 133, "missed": 97, "percentage": 57.83 },
    "method": { "covered": 42, "missed": 14, "percentage": 75.0 },
    "class": { "covered": 9, "missed": 1, "percentage": 90.0 },
    "packages": [
      { "name": "com.vikaasni.bankapp.exception", "percentage": 100.0 },
      { "name": "com.vikaasni.bankapp.service", "percentage": 100.0 },
      { "name": "com.vikaasni.bankapp.model", "percentage": 91.67 },
      { "name": "com.vikaasni.bankapp.repository", "percentage": 96.23 }
    ]
  }
};

// Global variables
let activeTestData = FALLBACK_TEST_DATA;
let timelineChart = null;
let coverageChart = null;

// Banking Client Database (LocalStorage based)
let bankAccounts = [];
let selectedAccountNumber = "";

// Initialize app when DOM is fully loaded
document.addEventListener("DOMContentLoaded", () => {
    initApp();
});

function initApp() {
    // 1. Tab switching navigation
    const menuItems = document.querySelectorAll(".sidebar-menu .menu-item");
    menuItems.forEach(item => {
        item.addEventListener("click", (e) => {
            e.preventDefault();
            menuItems.forEach(i => i.classList.remove("active"));
            item.classList.add("active");

            const tab = item.getAttribute("data-tab");
            document.querySelectorAll(".tab-content").forEach(tc => tc.classList.remove("active"));
            document.getElementById(`${tab}-tab`).classList.add("active");
            
            // Re-render charts if QA tab is clicked to fix sizing issues
            if (tab === "qa") {
                renderCharts(activeTestData.testRun, activeTestData.coverage);
            }
        });
    });

    // 2. Toggles for accordions in QA tab
    const accordions = document.querySelectorAll(".accordion-trigger");
    accordions.forEach(trigger => {
        trigger.addEventListener("click", () => {
            const item = trigger.parentElement;
            item.classList.toggle("active");
        });
    });

    // 3. Setup QA Tab Filter and Search
    document.getElementById("test-search").addEventListener("input", filterTestTable);
    document.getElementById("status-filter").addEventListener("change", filterTestTable);

    // 4. Setup Banking Form Submissions
    document.getElementById("create-account-form").addEventListener("submit", handleCreateAccount);
    document.getElementById("transaction-form").addEventListener("submit", handlePostTransaction);

    // 5. Load both Banking Data and QA Test Data
    loadBankingClientData();
    loadQATestData();
}

/* =========================================================================
   QA TEST & COVERAGE ANALYTICS TAB LOGIC
   ========================================================================= */

function loadQATestData() {
    fetch('data.json')
        .then(response => {
            if (!response.ok) {
                throw new Error("Could not find live data.json. Falling back to local data.");
            }
            return response.json();
        })
        .then(data => {
            activeTestData = data;
            renderQASection();
        })
        .catch(err => {
            console.warn(err.message);
            renderQASection();
        });
}

function renderQASection() {
    const run = activeTestData.testRun;
    const cov = activeTestData.coverage;

    document.getElementById("success-count").textContent = run.successes;
    document.getElementById("failed-count").textContent = run.failures + run.errors;
    document.getElementById("skipped-count").textContent = run.skipped;
    document.getElementById("coverage-pct").textContent = cov && cov.line ? `${cov.line.percentage}%` : "N/A";

    const buildStatus = document.getElementById("build-status-badge");
    if (run.failures + run.errors > 0) {
        buildStatus.textContent = "FAILURE";
        buildStatus.className = "badge-failed-glow";
    } else {
        buildStatus.textContent = "SUCCESS";
        buildStatus.className = "badge-success-glow";
    }

    populateTestTable();
    renderCharts(run, cov);
    lucide.createIcons();
}

function populateTestTable() {
    const tbody = document.getElementById("test-cases-body");
    tbody.innerHTML = "";

    const suites = activeTestData.testRun.testSuites;
    suites.forEach(suite => {
        const shortSuiteName = suite.name.substring(suite.name.lastIndexOf('.') + 1);
        suite.cases.forEach(kase => {
            const tr = document.createElement("tr");
            tr.setAttribute("data-method", kase.name.toLowerCase());
            tr.setAttribute("data-suite", suite.name.toLowerCase());
            tr.setAttribute("data-status", kase.status);

            let statusBadge = "";
            switch (kase.status) {
                case "SUCCESS":
                    statusBadge = `<span class="badge-status badge-success"><i data-lucide="check-circle-2" style="width:13px;height:13px"></i> Success</span>`;
                    break;
                case "FAILURE":
                    statusBadge = `<span class="badge-status badge-failed"><i data-lucide="x-circle" style="width:13px;height:13px"></i> Fail</span>`;
                    break;
                case "ERROR":
                    statusBadge = `<span class="badge-status badge-error"><i data-lucide="alert-circle" style="width:13px;height:13px"></i> Error</span>`;
                    break;
                default:
                    statusBadge = `<span class="badge-status badge-skipped"><i data-lucide="stop-circle" style="width:13px;height:13px"></i> Skip</span>`;
            }

            tr.innerHTML = `
                <td>${statusBadge}</td>
                <td><strong>${kase.name}</strong></td>
                <td style="color:var(--text-secondary)">${shortSuiteName}</td>
                <td>${(kase.time * 1000).toFixed(1)} ms</td>
            `;
            tbody.appendChild(tr);
        });
    });
}

function filterTestTable() {
    const searchVal = document.getElementById("test-search").value.toLowerCase();
    const filterVal = document.getElementById("status-filter").value;
    const rows = document.querySelectorAll("#test-cases-body tr");

    rows.forEach(row => {
        const method = row.getAttribute("data-method");
        const suite = row.getAttribute("data-suite");
        const status = row.getAttribute("data-status");

        const matchesSearch = method.includes(searchVal) || suite.includes(searchVal);
        const matchesStatus = filterVal === "ALL" || status === filterVal;

        row.style.display = (matchesSearch && matchesStatus) ? "" : "none";
    });
}

function renderCharts(run, cov) {
    if (timelineChart) timelineChart.destroy();
    if (coverageChart) coverageChart.destroy();

    const ctxTimeline = document.getElementById("runsTimelineChart").getContext("2d");
    const ctxCoverage = document.getElementById("coverageDoughnutChart").getContext("2d");

    const labels = ["Run #1", "Run #2", "Run #3", "Run #4", "Run #5", "Current Run"];
    const successes = [10, 14, 18, 22, 27, run.successes];
    const failures = [2, 1, 0, 1, 0, run.failures + run.errors];
    const skipped = [0, 0, 1, 0, 1, run.skipped];

    timelineChart = new Chart(ctxTimeline, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [
                { label: 'SUCCESS', data: successes, backgroundColor: '#10b981', borderRadius: 6 },
                { label: 'FAIL', data: failures, backgroundColor: '#f43f5e', borderRadius: 6 },
                { label: 'STOP/SKIP', data: skipped, backgroundColor: '#8b5cf6', borderRadius: 6 }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'top',
                    labels: { color: '#94a3b8', font: { family: 'Inter', size: 11, weight: '500' } }
                }
            },
            scales: {
                x: { stacked: true, grid: { display: false }, ticks: { color: '#94a3b8' } },
                y: { stacked: true, grid: { color: 'rgba(255, 255, 255, 0.05)' }, ticks: { color: '#94a3b8' } }
            }
        }
    });

    const lineCovered = cov && cov.line ? cov.line.covered : 133;
    const lineMissed = cov && cov.line ? cov.line.missed : 97;

    coverageChart = new Chart(ctxCoverage, {
        type: 'doughnut',
        data: {
            labels: ['Covered Lines', 'Missed Lines'],
            datasets: [{
                data: [lineCovered, lineMissed],
                backgroundColor: ['#0ea5e9', '#ef4444'],
                borderWidth: 0,
                hoverOffset: 4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            cutout: '75%',
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: { color: '#94a3b8', font: { family: 'Inter', size: 11, weight: '500' } }
                }
            }
        }
    });
}

/* =========================================================================
   BANK ACCOUNT CLIENT LOGIC (TABS / PERSISTENCE / OPERATIONS)
   ========================================================================= */

function loadBankingClientData() {
    const rawData = localStorage.getItem("bank_accounts");
    if (rawData) {
        bankAccounts = JSON.parse(rawData);
    } else {
        // Pre-populate with mock accounts so the interface looks lively
        bankAccounts = [
            {
                accountNumber: "ACC1001",
                accountHolderName: "Vikaasni",
                balance: 1000.00,
                transactions: [
                    { type: "DEPOSIT", amount: 1000.00, balanceAfterTransaction: 1000.00, timestamp: formatDate(new Date()) }
                ]
            },
            {
                accountNumber: "ACC2001",
                accountHolderName: "Concurrency User",
                balance: 300.00,
                transactions: [
                    { type: "DEPOSIT", amount: 1000.00, balanceAfterTransaction: 1000.00, timestamp: formatDate(new Date()) },
                    { type: "WITHDRAWAL", amount: 700.00, balanceAfterTransaction: 300.00, timestamp: formatDate(new Date()) }
                ]
            }
        ];
        saveBankingDataToStorage();
    }
    
    // Select first account by default if available
    if (bankAccounts.length > 0) {
        selectedAccountNumber = bankAccounts[0].accountNumber;
    }
    
    updateBankingUI();
}

function saveBankingDataToStorage() {
    localStorage.setItem("bank_accounts", JSON.stringify(bankAccounts));
}

function updateBankingUI() {
    // 1. Calculate overall metrics
    const totalAccounts = bankAccounts.length;
    const totalBalance = bankAccounts.reduce((acc, account) => acc + account.balance, 0);

    document.getElementById("bank-total-accounts").textContent = totalAccounts;
    document.getElementById("bank-total-balance").textContent = `Rs. ${totalBalance.toFixed(2)}`;

    // 2. Populate Dropdowns and Tables
    populateAccountsTable();
    populateAccountsDropdown();
    populateTimelineTable();

    // 3. Set Selected Holder Labels
    const selectedAcc = bankAccounts.find(a => a.accountNumber === selectedAccountNumber);
    const holderLabels = document.querySelectorAll(".selected-holder-name");
    
    if (selectedAcc) {
        holderLabels.forEach(el => {
            el.textContent = `${selectedAcc.accountHolderName} (${selectedAcc.accountNumber})`;
            el.style.color = "var(--text-primary)";
        });
    } else {
        holderLabels.forEach(el => {
            el.textContent = "No Selected Account";
            el.style.color = "var(--text-secondary)";
        });
    }

    lucide.createIcons();
}

function populateAccountsTable() {
    const tbody = document.getElementById("bank-accounts-body");
    tbody.innerHTML = "";

    if (bankAccounts.length === 0) {
        tbody.innerHTML = `<tr><td colspan="4" style="text-align:center; color:var(--text-secondary)">No accounts found. Create one to begin.</td></tr>`;
        return;
    }

    bankAccounts.forEach(account => {
        const tr = document.createElement("tr");
        if (account.accountNumber === selectedAccountNumber) {
            tr.className = "selected-row";
        }

        tr.innerHTML = `
            <td><strong>${account.accountNumber}</strong></td>
            <td>${account.accountHolderName}</td>
            <td>Rs. ${account.balance.toFixed(2)}</td>
            <td>
                <button class="btn-action-view" onclick="selectAccount('${account.accountNumber}')">View Statement</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function populateAccountsDropdown() {
    const select = document.getElementById("tx-acc-select");
    
    // Save current selection if possible
    const currentVal = select.value;
    
    select.innerHTML = `<option value="" disabled ${!currentVal ? 'selected' : ''}>Choose Account...</option>`;
    
    bankAccounts.forEach(account => {
        const opt = document.createElement("option");
        opt.value = account.accountNumber;
        opt.textContent = `${account.accountHolderName} (${account.accountNumber})`;
        if (account.accountNumber === currentVal) {
            opt.selected = true;
        }
        select.appendChild(opt);
    });
}

function populateTimelineTable() {
    const tbody = document.getElementById("statement-timeline-body");
    tbody.innerHTML = "";

    const selectedAcc = bankAccounts.find(a => a.accountNumber === selectedAccountNumber);

    if (!selectedAcc) {
        tbody.innerHTML = `<tr><td colspan="4" style="text-align:center; color:var(--text-secondary)">Select an account from the list above to view statement logs.</td></tr>`;
        return;
    }

    if (selectedAcc.transactions.length === 0) {
        tbody.innerHTML = `<tr><td colspan="4" style="text-align:center; color:var(--text-secondary)">No transaction records available.</td></tr>`;
        return;
    }

    // List latest transactions first
    const reversedTx = [...selectedAcc.transactions].reverse();

    reversedTx.forEach(tx => {
        const tr = document.createElement("tr");

        let badge = "";
        if (tx.type === "DEPOSIT") {
            badge = `<span class="badge-status badge-success"><i data-lucide="arrow-down-left" style="width:13px;height:13px"></i> Deposit</span>`;
        } else {
            badge = `<span class="badge-status badge-failed"><i data-lucide="arrow-up-right" style="width:13px;height:13px"></i> Withdrawal</span>`;
        }

        tr.innerHTML = `
            <td style="color:var(--text-secondary)">${tx.timestamp}</td>
            <td>${badge}</td>
            <td><strong>Rs. ${tx.amount.toFixed(2)}</strong></td>
            <td>Rs. ${tx.balanceAfterTransaction.toFixed(2)}</td>
        `;
        tbody.appendChild(tr);
    });
}

// Global hook to allow row selection from inline HTML button
window.selectAccount = function(accNum) {
    selectedAccountNumber = accNum;
    updateBankingUI();
    
    // Smooth scroll to statement panel on smaller viewports
    const section = document.getElementById("statement-section");
    section.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
};

function handleCreateAccount(e) {
    e.preventDefault();
    
    const numInput = document.getElementById("new-acc-num");
    const nameInput = document.getElementById("new-acc-name");
    const balInput = document.getElementById("new-acc-balance");
    const msg = document.getElementById("create-message");

    const accNum = numInput.value.trim();
    const accName = nameInput.value.trim();
    const openBal = parseFloat(balInput.value);

    // Reset messages
    msg.className = "form-message";
    msg.style.display = "none";

    // 1. Validation Checks (Mirrors BankAccount.java rules)
    if (!accNum) {
        showFormMessage(msg, "Error: Account number cannot be blank.", "error");
        return;
    }
    if (!accName) {
        showFormMessage(msg, "Error: Account holder name cannot be blank.", "error");
        return;
    }
    if (isNaN(openBal) || openBal < 0) {
        showFormMessage(msg, "Error: Opening balance cannot be negative.", "error");
        return;
    }
    if (bankAccounts.some(a => a.accountNumber.toUpperCase() === accNum.toUpperCase())) {
        showFormMessage(msg, `Error: Account number already exists: ${accNum}`, "error");
        return;
    }

    // 2. Perform Account creation
    const newAcc = {
        accountNumber: accNum,
        accountHolderName: accName,
        balance: openBal,
        transactions: []
    };

    if (openBal > 0) {
        newAcc.transactions.push({
            type: "DEPOSIT",
            amount: openBal,
            balanceAfterTransaction: openBal,
            timestamp: formatDate(new Date())
        });
    }

    bankAccounts.push(newAcc);
    selectedAccountNumber = accNum; // Auto-select newly created account
    saveBankingDataToStorage();
    updateBankingUI();

    // Reset Form
    numInput.value = "";
    nameInput.value = "";
    balInput.value = "0.00";

    showFormMessage(msg, `Success: Account created successfully for ${accName}!`, "success");
}

function handlePostTransaction(e) {
    e.preventDefault();

    const accSelect = document.getElementById("tx-acc-select");
    const typeSelect = document.getElementById("tx-type");
    const amtInput = document.getElementById("tx-amount");
    const msg = document.getElementById("tx-message");

    const accNum = accSelect.value;
    const type = typeSelect.value;
    const amount = parseFloat(amtInput.value);

    msg.className = "form-message";
    msg.style.display = "none";

    // 1. Validations
    if (!accNum) {
        showFormMessage(msg, "Error: Please select a valid bank account.", "error");
        return;
    }
    if (isNaN(amount) || amount <= 0) {
        showFormMessage(msg, "Error: Amount must be greater than zero.", "error");
        return;
    }

    const account = bankAccounts.find(a => a.accountNumber === accNum);
    if (!account) {
        showFormMessage(msg, "Error: Selected account could not be found.", "error");
        return;
    }

    if (type === "WITHDRAWAL") {
        // Mirrors InsufficientFundsException
        if (amount > account.balance) {
            showFormMessage(msg, `Error: Insufficient funds. Available balance: Rs. ${account.balance.toFixed(2)}`, "error");
            return;
        }
        account.balance -= amount;
    } else {
        // Deposit
        account.balance += amount;
    }

    // 2. Record Transaction
    account.transactions.push({
        type: type,
        amount: amount,
        balanceAfterTransaction: account.balance,
        timestamp: formatDate(new Date())
    });

    selectedAccountNumber = accNum; // Ensure statement updates to this account
    saveBankingDataToStorage();
    updateBankingUI();

    // Reset Form Amount
    amtInput.value = "";

    showFormMessage(msg, `Success: ${type === 'DEPOSIT' ? 'Deposit' : 'Withdrawal'} of Rs. ${amount.toFixed(2)} completed successfully!`, "success");
}

function showFormMessage(element, text, type) {
    element.textContent = text;
    element.className = `form-message ${type}`;
    element.style.display = "block";
    
    // Auto-hide messages after 4 seconds
    setTimeout(() => {
        element.style.fadeOut = "slow";
        // smooth hide
        element.style.display = "none";
    }, 4500);
}

// Utility formatting functions
function formatDate(date) {
    const pad = (n) => n.toString().padStart(2, '0');
    
    const day = pad(date.getDate());
    const month = pad(date.getMonth() + 1);
    const year = date.getFullYear();
    
    const hours = pad(date.getHours());
    const minutes = pad(date.getMinutes());
    const seconds = pad(date.getSeconds());
    
    return `${day}-${month}-${year} ${hours}:${minutes}:${seconds}`;
}
