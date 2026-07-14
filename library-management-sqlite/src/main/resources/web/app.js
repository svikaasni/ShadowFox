// State Management
let currentUser = null;
let allBooks = [];

// DOM Elements
const authContainer = document.getElementById('auth-container');
const mainContainer = document.getElementById('main-container');
const adminSection = document.getElementById('admin-section');
const memberSection = document.getElementById('member-section');
const headerUserName = document.getElementById('header-user-name');
const headerUserRole = document.getElementById('header-user-role');
const catalogGrid = document.getElementById('catalog-grid');
const activeLoansBody = document.getElementById('active-loans-body');
const recommendationsContainer = document.getElementById('recommendations-container');
const searchInput = document.getElementById('search-input');

// Initialize App
document.addEventListener('DOMContentLoaded', () => {
    // Check if session already exists
    const session = localStorage.getItem('athena_session');
    if (session) {
        try {
            currentUser = JSON.parse(session);
            showDashboard();
        } catch (e) {
            localStorage.removeItem('athena_session');
        }
    }
});

// Toast Notifications
function showToast(message, type = 'success') {
    const container = document.getElementById('notification-container');
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `
        <span>${type === 'success' ? '✅' : type === 'danger' ? '❌' : '⚠️'}</span>
        <span>${message}</span>
    `;
    container.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = '0';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

// Switch Login/Register Tabs
function switchAuthTab(tab) {
    const loginForm = document.getElementById('login-form');
    const registerForm = document.getElementById('register-form');
    const tabLogin = document.getElementById('tab-login');
    const tabRegister = document.getElementById('tab-register');

    if (tab === 'login') {
        loginForm.classList.remove('hidden');
        registerForm.classList.add('hidden');
        tabLogin.classList.add('active');
        tabRegister.classList.remove('active');
    } else {
        loginForm.classList.add('hidden');
        registerForm.classList.remove('hidden');
        tabLogin.classList.remove('active');
        tabRegister.classList.add('active');
    }
}

// Handle Login
async function handleLogin(e) {
    e.preventDefault();
    const username = document.getElementById('login-username').value;
    const password = document.getElementById('login-password').value;

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        const data = await response.json();
        
        if (!response.ok) {
            throw new Error(data.message || 'Login failed');
        }

        currentUser = data;
        localStorage.setItem('athena_session', JSON.stringify(currentUser));
        showToast(`Welcome back, ${currentUser.fullName}!`, 'success');
        showDashboard();
    } catch (err) {
        showToast(err.message, 'danger');
    }
}

// Handle Register
async function handleRegister(e) {
    e.preventDefault();
    const username = document.getElementById('reg-username').value;
    const password = document.getElementById('reg-password').value;
    const fullName = document.getElementById('reg-fullname').value;
    const favoriteGenre = document.getElementById('reg-genre').value;

    try {
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password, fullName, favoriteGenre })
        });
        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || 'Registration failed');
        }

        showToast('Registration successful! Please log in.', 'success');
        switchAuthTab('login');
        
        // Populate username/password automatically for convenience
        document.getElementById('login-username').value = username;
        document.getElementById('login-password').value = password;
    } catch (err) {
        showToast(err.message, 'danger');
    }
}

// Show Dashboard
function showDashboard() {
    authContainer.classList.add('hidden');
    mainContainer.classList.remove('hidden');
    
    headerUserName.textContent = currentUser.fullName;
    headerUserRole.textContent = currentUser.role;

    if (currentUser.role === 'ADMIN') {
        adminSection.classList.remove('hidden');
        memberSection.classList.add('hidden');
        loadAdminStats();
    } else {
        adminSection.classList.add('hidden');
        memberSection.classList.remove('hidden');
        loadRecommendations();
        loadActiveLoans();
    }

    loadCatalog();
}

// Handle Logout
function handleLogout() {
    currentUser = null;
    localStorage.removeItem('athena_session');
    
    // Clear forms
    document.getElementById('login-username').value = '';
    document.getElementById('login-password').value = '';
    
    mainContainer.classList.add('hidden');
    authContainer.classList.remove('hidden');
    showToast('Logged out successfully', 'success');
}

// Load Catalog Books
async function loadCatalog(query = '') {
    try {
        const url = query ? `/api/books?q=${encodeURIComponent(query)}` : '/api/books';
        const response = await fetch(url);
        if (!response.ok) throw new Error('Could not load catalog');
        allBooks = await response.json();
        renderCatalog();
    } catch (err) {
        showToast(err.message, 'danger');
    }
}

// Search Catalog
function handleSearch() {
    const query = searchInput.value.trim();
    loadCatalog(query);
}

// Render Catalog
function renderCatalog() {
    catalogGrid.innerHTML = '';
    if (allBooks.length === 0) {
        catalogGrid.innerHTML = '<p class="placeholder-text text-center">No books found in the catalog.</p>';
        return;
    }

    allBooks.forEach(book => {
        const isAvailable = book.availableCopies > 0;
        const card = document.createElement('div');
        card.className = 'book-card glass-panel';
        card.innerHTML = `
            <div class="book-header">
                <div class="book-tag-row">
                    <span class="book-genre-tag">${book.genre || 'General'}</span>
                    <span class="book-copies-tag ${isAvailable ? 'copies-available' : 'copies-out'}">
                        ${book.availableCopies}/${book.totalCopies} Available
                    </span>
                </div>
                <h3>${book.title}</h3>
                <p class="book-author">by ${book.authors.join(', ')}</p>
            </div>
            <p class="book-desc">${book.description || 'No description available.'}</p>
            <div class="book-footer">
                <span class="book-meta">ISBN: ${book.isbn || 'N/A'}</span>
                ${currentUser && currentUser.role === 'MEMBER' && isAvailable ? 
                  `<button class="btn btn-primary btn-sm" onclick="issueBook(${book.id})">Borrow</button>` : ''}
            </div>
        `;
        catalogGrid.appendChild(card);
    });
}

// Load Recommendations for Member
async function loadRecommendations() {
    try {
        const response = await fetch(`/api/books/recommendations?userId=${currentUser.id}`);
        if (!response.ok) throw new Error('Could not load recommendations');
        const books = await response.json();
        
        recommendationsContainer.innerHTML = '';
        if (books.length === 0) {
            recommendationsContainer.innerHTML = '<p class="placeholder-text">Borrow books to build recommendations or set your favorite genre!</p>';
            return;
        }

        books.forEach(book => {
            const item = document.createElement('div');
            item.className = 'rec-item';
            item.onclick = () => {
                searchInput.value = book.title;
                loadCatalog(book.title);
            };
            item.innerHTML = `
                <h4>${book.title}</h4>
                <p>by ${book.authors[0]}</p>
                <span class="rec-genre">${book.genre || 'General'}</span>
            `;
            recommendationsContainer.appendChild(item);
        });
    } catch (err) {
        recommendationsContainer.innerHTML = `<p class="placeholder-text text-danger">${err.message}</p>`;
    }
}

// Load Active Loans for Member
async function loadActiveLoans() {
    try {
        const response = await fetch(`/api/loans?userId=${currentUser.id}`);
        if (!response.ok) throw new Error('Could not load loans');
        const loans = await response.json();
        
        activeLoansBody.innerHTML = '';
        
        // Only show active (ISSUED) loans
        const activeLoans = loans.filter(l => l.status === 'ISSUED');
        
        if (activeLoans.length === 0) {
            activeLoansBody.innerHTML = `
                <tr>
                    <td colspan="5" class="placeholder-text text-center">You have no active loans. Borrow a book from the catalog!</td>
                </tr>
            `;
            return;
        }

        activeLoans.forEach(loan => {
            // Check if overdue to show fine calculation estimation (daily fine is ₹5.0)
            const dueDate = new Date(loan.dueDate);
            const today = new Date();
            today.setHours(0,0,0,0);
            
            let fineEst = 0;
            if (today > dueDate) {
                const diffTime = Math.abs(today - dueDate);
                const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
                fineEst = diffDays * 5;
            }

            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><strong>${loan.bookTitle}</strong></td>
                <td>${loan.issueDate}</td>
                <td>${loan.dueDate}</td>
                <td>${fineEst > 0 ? `<span class="fine-badge">₹${fineEst.toFixed(2)} (Overdue)</span>` : '<span class="fine-none">₹0.00</span>'}</td>
                <td><button class="btn btn-secondary btn-sm" onclick="returnBook(${loan.id})">Return</button></td>
            `;
            activeLoansBody.appendChild(tr);
        });
    } catch (err) {
        showToast(err.message, 'danger');
    }
}

// Load Stats for Admin
async function loadAdminStats() {
    try {
        const response = await fetch('/api/books');
        if (!response.ok) throw new Error();
        const books = await response.json();
        
        let totalBooks = 0;
        let totalItems = books.length;
        books.forEach(b => totalBooks += b.totalCopies);
        
        // Fetch active loans by querying all loans (admin can query database overview)
        const activeLoansCount = books.reduce((acc, b) => acc + (b.totalCopies - b.availableCopies), 0);

        document.getElementById('stat-total-books').textContent = totalBooks;
        document.getElementById('stat-active-loans').textContent = activeLoansCount;
        document.getElementById('stat-catalog-items').textContent = totalItems;
    } catch (e) {
        // Fallback placeholders
        document.getElementById('stat-total-books').textContent = 'N/A';
        document.getElementById('stat-active-loans').textContent = 'N/A';
        document.getElementById('stat-catalog-items').textContent = 'N/A';
    }
}

// Issue Book Action
async function issueBook(bookId) {
    try {
        const response = await fetch('/api/loans/issue', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ userId: currentUser.id, bookId })
        });
        const data = await response.json();

        if (!response.ok) throw new Error(data.message || 'Issue failed');

        showToast('Book checked out successfully! Enjoy reading.', 'success');
        loadCatalog();
        loadActiveLoans();
    } catch (err) {
        showToast(err.message, 'danger');
    }
}

// Return Book Action
async function returnBook(loanId) {
    try {
        const response = await fetch('/api/loans/return', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ loanId })
        });
        const data = await response.json();

        if (!response.ok) throw new Error(data.message || 'Return failed');

        if (data.fine > 0) {
            showToast(`Book returned. Overdue fine collected: ₹${data.fine.toFixed(2)}`, 'warning');
        } else {
            showToast('Book returned successfully! Thank you.', 'success');
        }
        
        loadCatalog();
        loadActiveLoans();
    } catch (err) {
        showToast(err.message, 'danger');
    }
}

// Handle Add Book Manually
async function handleManualAdd(e) {
    e.preventDefault();
    const title = document.getElementById('book-title').value;
    const isbn = document.getElementById('book-isbn').value;
    const authors = document.getElementById('book-authors').value.split(',').map(s => s.trim());
    const genre = document.getElementById('book-genre').value;
    const publishedDate = document.getElementById('book-published').value;
    const totalCopies = parseInt(document.getElementById('book-copies').value);
    const description = document.getElementById('book-description').value;

    try {
        const response = await fetch('/api/books/add', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                method: 'MANUAL',
                title, isbn, authors, genre, publishedDate, totalCopies, description
            })
        });
        const data = await response.json();

        if (!response.ok) throw new Error(data.message || 'Add book failed');

        showToast(`Manually added "${title}" to the catalog!`, 'success');
        e.target.reset();
        loadCatalog();
        loadAdminStats();
    } catch (err) {
        showToast(err.message, 'danger');
    }
}

// Handle Add Book via ISBN Import (Google Books API integration)
async function handleIsbnImport(e) {
    e.preventDefault();
    const isbn = document.getElementById('isbn-input').value.trim();
    const totalCopies = parseInt(document.getElementById('isbn-copies').value);
    
    // Disable form button to show loading
    const btn = e.target.querySelector('button');
    const originalText = btn.textContent;
    btn.disabled = true;
    btn.textContent = 'Contacting Google API...';

    try {
        const response = await fetch('/api/books/add', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ method: 'ISBN', isbn, totalCopies })
        });
        const data = await response.json();

        if (!response.ok) throw new Error(data.message || 'Import failed');

        showToast(`Successfully imported: "${data.title}" (${totalCopies} copies)`, 'success');
        e.target.reset();
        document.getElementById('isbn-copies').value = 1;
        loadCatalog();
        loadAdminStats();
    } catch (err) {
        showToast(err.message, 'danger');
    } finally {
        btn.disabled = false;
        btn.textContent = originalText;
    }
}
