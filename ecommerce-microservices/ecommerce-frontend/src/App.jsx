import React, { useState, useEffect } from 'react';

const API_BASE = 'http://localhost:8080';

// Cute helper to resolve emoji based on product keywords
const getProductEmoji = (name = '', desc = '') => {
  const text = (name + ' ' + desc).toLowerCase();
  if (text.includes('mouse')) return '🖱️';
  if (text.includes('keyboard')) return '⌨️';
  if (text.includes('headphone') || text.includes('sound') || text.includes('music')) return '🎧';
  if (text.includes('phone') || text.includes('mobile')) return '📱';
  if (text.includes('shirt') || text.includes('cloth') || text.includes('wear')) return '👕';
  if (text.includes('shoe') || text.includes('boot') || text.includes('sneaker')) return '👟';
  if (text.includes('watch') || text.includes('smartwatch')) return '⌚';
  if (text.includes('laptop') || text.includes('computer')) return '💻';
  if (text.includes('book') || text.includes('read')) return '📚';
  if (text.includes('chocolate') || text.includes('sweet') || text.includes('candy')) return '🍫';
  if (text.includes('cake') || text.includes('cupcake') || text.includes('bakery')) return '🧁';
  if (text.includes('coffee') || text.includes('tea') || text.includes('drink')) return '🥤';
  if (text.includes('toy') || text.includes('game') || text.includes('play')) return '🧸';
  
  const sweets = ['🍬', '🍩', '🍪', '🍨', '🍿', '🧇', '🍓', '🍑', '🍇', '🎒', '🎨', '✨'];
  // Stable random selection based on name length
  return sweets[name.length % sweets.length];
};

export default function App() {
  // --- STATE ---
  const [theme, setTheme] = useState(() => localStorage.getItem('sweet-shop-theme') || 'light');
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('sweet-shop-user');
    return saved ? JSON.parse(saved) : null;
  });
  const [page, setPage] = useState('catalog'); // 'catalog', 'orders', 'auth'
  const [authMode, setAuthMode] = useState('login'); // 'login', 'register'
  const [products, setProducts] = useState([]);
  const [orders, setOrders] = useState([]);
  const [cart, setCart] = useState(() => {
    const saved = localStorage.getItem('sweet-shop-cart');
    return saved ? JSON.parse(saved) : [];
  });
  const [isCartOpen, setIsCartOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [toasts, setToasts] = useState([]);
  const [isOffline, setIsOffline] = useState(false);
  const [adminModalOpen, setAdminModalOpen] = useState(false);
  const [editingProduct, setEditingProduct] = useState(null); // null means adding a new product

  // Form states for login/register
  const [authForm, setAuthForm] = useState({ name: '', email: '', password: '' });
  // Form states for product CRUD
  const [productForm, setProductForm] = useState({ name: '', description: '', price: '', stock: '' });

  // --- MOCK DATABASE FALLBACKS ---
  const [mockProducts, setMockProducts] = useState([
    { id: 1, name: 'Berry Cotton Candy', description: 'Fluffy, clouds of sweet berry heaven that melts in your mouth!', price: 120.00, stock: 15, active: true },
    { id: 2, name: 'Unicorn Sprinkles Cupcake', description: 'Cute vanilla muffin topped with pastel buttercream and magical sprinkles.', price: 180.00, stock: 8, active: true },
    { id: 3, name: 'Glazed Rainbow Donut', description: 'Freshly baked ring donut dipped in pink strawberry glaze and sparkles.', price: 90.00, stock: 20, active: true },
    { id: 4, name: 'Choco Bubble Tea', description: 'Creamy milk tea infused with chocolate syrups and bouncy tapioca boba pearls.', price: 150.00, stock: 4, active: true },
    { id: 5, name: 'Marshmallow Pillow Pack', description: 'Super soft, pillow-like marshmallows in cute rabbit shapes.', price: 110.00, stock: 0, active: true }
  ]);
  const [mockOrders, setMockOrders] = useState([
    { id: 101, productId: 1, productName: 'Berry Cotton Candy', quantity: 2, unitPrice: 120.00, totalPrice: 240.00, status: 'CONFIRMED', createdAt: new Date(Date.now() - 3600000 * 2).toISOString() }
  ]);

  // --- THEME ---
  useEffect(() => {
    if (theme === 'dark') {
      document.body.classList.add('dark');
    } else {
      document.body.classList.remove('dark');
    }
    localStorage.setItem('sweet-shop-theme', theme);
  }, [theme]);

  // --- SAVE CART ---
  useEffect(() => {
    localStorage.setItem('sweet-shop-cart', JSON.stringify(cart));
  }, [cart]);

  // --- TOAST SYSTEM ---
  const showToast = (text, type = 'success') => {
    const id = Date.now();
    setToasts(prev => [...prev, { id, text, type }]);
    setTimeout(() => {
      setToasts(prev => prev.filter(t => t.id !== id));
    }, 3000);
  };

  // --- CONFETTI EFFECT ---
  const triggerConfetti = () => {
    const emojis = ['🎉', '✨', '🍭', '🧁', '💖', '⭐', '🎈', '🍩', '🍬', '🌈'];
    for (let i = 0; i < 35; i++) {
      const p = document.createElement('div');
      p.className = 'confetti-particle';
      p.innerText = emojis[Math.floor(Math.random() * emojis.length)];
      p.style.left = `${window.innerWidth / 2 + (Math.random() - 0.5) * 300}px`;
      p.style.top = `${window.innerHeight / 2 + (Math.random() - 0.5) * 150}px`;
      
      const xOffset = `${(Math.random() - 0.5) * 500}px`;
      const yOffset = `${-Math.random() * 400 - 150}px`;
      const rotation = `${(Math.random() - 0.5) * 720}deg`;
      
      p.style.setProperty('--x-offset', xOffset);
      p.style.setProperty('--y-offset', yOffset);
      p.style.setProperty('--rotation', rotation);
      
      document.body.appendChild(p);
      setTimeout(() => p.remove(), 1500);
    }
  };

  // --- API CALLS ---
  const fetchProducts = async () => {
    try {
      const res = await fetch(`${API_BASE}/api/products`);
      if (!res.ok) throw new Error('Failed to load products');
      const data = await res.json();
      setProducts(data);
      setIsOffline(false);
    } catch (err) {
      console.warn('Backend unavailable, using simulated sweet items database.', err);
      setProducts(mockProducts);
      setIsOffline(true);
    }
  };

  const fetchOrders = async () => {
    if (!user) return;
    try {
      const res = await fetch(`${API_BASE}/api/orders/my`, {
        headers: {
          'Authorization': `Bearer ${user.token}`
        }
      });
      if (!res.ok) throw new Error('Failed to load orders');
      const data = await res.json();
      setOrders(data);
    } catch (err) {
      console.warn('Backend unavailable, showing simulated order history.', err);
      setOrders(mockOrders);
    }
  };

  useEffect(() => {
    fetchProducts();
  }, [mockProducts]);

  useEffect(() => {
    if (user) {
      fetchOrders();
    }
  }, [user, mockOrders]);

  // --- ACTIONS ---

  // Auth Operations
  const handleAuthSubmit = async (e) => {
    e.preventDefault();
    if (!authForm.email || !authForm.password || (authMode === 'register' && !authForm.name)) {
      showToast('Please fill out all required fields 🦄', 'error');
      return;
    }
    
    if (isOffline) {
      // Simulate Auth locally
      const mockUser = {
        userId: Date.now(),
        name: authForm.name || authForm.email.split('@')[0],
        email: authForm.email,
        role: authForm.email.toLowerCase().includes('admin') ? 'ADMIN' : 'CUSTOMER',
        token: 'mock-jwt-token-' + Date.now()
      };
      setUser(mockUser);
      localStorage.setItem('sweet-shop-user', JSON.stringify(mockUser));
      showToast(`Welcome ${mockUser.name}! (Simulated Mode) ✨`, 'success');
      setPage('catalog');
      setAuthForm({ name: '', email: '', password: '' });
      return;
    }

    try {
      const endpoint = authMode === 'login' ? '/api/auth/login' : '/api/auth/register';
      const body = authMode === 'login' 
        ? { email: authForm.email, password: authForm.password }
        : { name: authForm.name, email: authForm.email, password: authForm.password };

      const res = await fetch(`${API_BASE}${endpoint}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
      });

      if (!res.ok) {
        const errorData = await res.json().catch(() => ({ error: 'Request failed' }));
        throw new Error(errorData.error || 'Authentication failed');
      }

      const userData = await res.json();
      setUser(userData);
      localStorage.setItem('sweet-shop-user', JSON.stringify(userData));
      showToast(`Welcome back, ${userData.name}! 🎉`, 'success');
      setPage('catalog');
      setAuthForm({ name: '', email: '', password: '' });
    } catch (err) {
      showToast(err.message || 'Error communicating with server 🌧️', 'error');
    }
  };

  const handleLogout = () => {
    setUser(null);
    localStorage.removeItem('sweet-shop-user');
    setCart([]);
    showToast('Logged out! See you soon! Bubble bye! 👋🍬', 'info');
    setPage('catalog');
  };

  // Cart Operations
  const handleAddToCart = (product) => {
    if (product.stock <= 0) {
      showToast('Oh no! This sweet is out of stock! 😴', 'error');
      return;
    }

    setCart(prev => {
      const existing = prev.find(item => item.product.id === product.id);
      if (existing) {
        if (existing.quantity >= product.stock) {
          showToast(`Oops! Only ${product.stock} available in stock! 🍭`, 'info');
          return prev;
        }
        showToast(`Added another ${product.name}! 🛒`, 'success');
        return prev.map(item => 
          item.product.id === product.id 
            ? { ...item, quantity: item.quantity + 1 }
            : item
        );
      }
      showToast(`Added ${product.name} to cart! 🛒`, 'success');
      
      // Trigger a wiggle on the cart icon
      const cartBtn = document.querySelector('.nav-cart-btn');
      if (cartBtn) {
        cartBtn.classList.add('wiggle');
        setTimeout(() => cartBtn.classList.remove('wiggle'), 600);
      }
      
      return [...prev, { product, quantity: 1 }];
    });
  };

  const updateCartQty = (productId, change) => {
    setCart(prev => {
      return prev.map(item => {
        if (item.product.id === productId) {
          const newQty = item.quantity + change;
          if (newQty <= 0) return null;
          // Check stock limit
          if (newQty > item.product.stock) {
            showToast('Cannot add more, stock limit reached! 🍭', 'info');
            return item;
          }
          return { ...item, quantity: newQty };
        }
        return item;
      }).filter(Boolean);
    });
  };

  // Order Placement
  const handleCheckout = async () => {
    if (!user) {
      showToast('Please login to place your order! 🔐', 'info');
      setPage('auth');
      setIsCartOpen(false);
      return;
    }

    if (cart.length === 0) return;

    let checkoutSuccess = true;
    const placedOrders = [];

    if (isOffline) {
      // Simulated checkout logic
      mockProducts.forEach(prod => {
        const cartItem = cart.find(c => c.product.id === prod.id);
        if (cartItem) {
          prod.stock -= cartItem.quantity;
        }
      });
      setMockProducts([...mockProducts]);

      cart.forEach(item => {
        const newMockOrder = {
          id: Date.now() + Math.floor(Math.random() * 100),
          productId: item.product.id,
          productName: item.product.name,
          quantity: item.quantity,
          unitPrice: item.product.price,
          totalPrice: item.product.price * item.quantity,
          status: 'CONFIRMED',
          createdAt: new Date().toISOString()
        };
        placedOrders.push(newMockOrder);
      });

      setMockOrders(prev => [...placedOrders, ...prev]);
      triggerConfetti();
      showToast('Sweet order placed successfully! (Simulated) 🎉🍭', 'success');
      setCart([]);
      setIsCartOpen(false);
      setPage('orders');
      return;
    }

    // Connect to Backend API
    showToast('Sending order to the baking deck... 🧁', 'info');
    for (const item of cart) {
      try {
        const res = await fetch(`${API_BASE}/api/orders`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${user.token}`,
            'X-User-Id': user.userId // Backup header fallback
          },
          body: JSON.stringify({
            productId: item.product.id,
            quantity: item.quantity
          })
        });

        if (!res.ok) {
          const errorData = await res.json().catch(() => ({}));
          throw new Error(errorData.error || 'Failed to place order');
        }
      } catch (err) {
        checkoutSuccess = false;
        showToast(`Baking failed for ${item.product.name}: ${err.message} 🌧️`, 'error');
      }
    }

    if (checkoutSuccess) {
      triggerConfetti();
      showToast('Order confirmed! Let us bake it! 🎉🧁', 'success');
      setCart([]);
      setIsCartOpen(false);
      fetchProducts(); // Refresh stock
      fetchOrders(); // Refresh history
      setPage('orders');
    } else {
      fetchProducts(); // Refresh stock in case some succeeded
      fetchOrders();
    }
  };

  // Admin Product Operations (CRUD)
  const openProductModal = (product = null) => {
    if (product) {
      setEditingProduct(product);
      setProductForm({
        name: product.name,
        description: product.description,
        price: product.price,
        stock: product.stock
      });
    } else {
      setEditingProduct(null);
      setProductForm({ name: '', description: '', price: '', stock: '' });
    }
    setAdminModalOpen(true);
  };

  const handleProductSubmit = async (e) => {
    e.preventDefault();
    if (!productForm.name || !productForm.price || productForm.stock === '') {
      showToast('Please fill in required product details 🧁', 'error');
      return;
    }

    const payload = {
      name: productForm.name,
      description: productForm.description,
      price: parseFloat(productForm.price),
      stock: parseInt(productForm.stock)
    };

    if (isOffline) {
      // Local Mock DB CRUD
      if (editingProduct) {
        const updated = mockProducts.map(p => 
          p.id === editingProduct.id ? { ...p, ...payload } : p
        );
        setMockProducts(updated);
        showToast('Product updated successfully! (Simulated) 🎨', 'success');
      } else {
        const newProduct = {
          id: Date.now(),
          ...payload,
          active: true
        };
        setMockProducts(prev => [newProduct, ...prev]);
        showToast('Cute new product added! (Simulated) 🍩', 'success');
      }
      setAdminModalOpen(false);
      return;
    }

    // Connect to Backend API
    try {
      const url = editingProduct 
        ? `${API_BASE}/api/products/${editingProduct.id}` 
        : `${API_BASE}/api/products`;
      const method = editingProduct ? 'PUT' : 'POST';

      const res = await fetch(url, {
        method,
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${user.token}`
        },
        body: JSON.stringify(payload)
      });

      if (!res.ok) {
        const errorData = await res.json().catch(() => ({}));
        throw new Error(errorData.error || 'Failed to save product');
      }

      showToast(editingProduct ? 'Product updated successfully! ✨' : 'Created cute product! 🎉', 'success');
      setAdminModalOpen(false);
      fetchProducts();
    } catch (err) {
      showToast(err.message || 'Failed to submit product details', 'error');
    }
  };

  const handleDeleteProduct = async (product) => {
    if (!window.confirm(`Are you sure you want to delete "${product.name}"? 😢`)) return;

    if (isOffline) {
      // Local Mock DB Delete
      setMockProducts(prev => prev.filter(p => p.id !== product.id));
      showToast('Product removed! 🧽', 'info');
      return;
    }

    try {
      const res = await fetch(`${API_BASE}/api/products/${product.id}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${user.token}`
        }
      });

      if (!res.ok) {
        const errorData = await res.json().catch(() => ({}));
        throw new Error(errorData.error || 'Failed to delete product');
      }

      showToast('Product removed! 🧽', 'info');
      fetchProducts();
    } catch (err) {
      showToast(err.message || 'Failed to delete product', 'error');
    }
  };

  // --- FILTERED PRODUCTS ---
  const filteredProducts = products.filter(p => 
    p.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
    p.description.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const cartTotal = cart.reduce((acc, item) => acc + (item.product.price * item.quantity), 0);
  const totalCartCount = cart.reduce((acc, item) => acc + item.quantity, 0);

  return (
    <div className="app-container">
      
      {/* Toast notifications */}
      <div className="toast-container">
        {toasts.map(t => (
          <div key={t.id} className={`toast ${t.type}`}>
            <span>{t.type === 'success' ? '✨' : t.type === 'error' ? '⛈️' : '🔔'}</span>
            <span>{t.text}</span>
          </div>
        ))}
      </div>

      {/* Navigation */}
      <nav className="navbar">
        <div className="nav-brand" onClick={() => setPage('catalog')}>
          <span>Sweet Shop 🛍️</span>
          {isOffline && (
            <span style={{ 
              marginLeft: '10px', 
              fontSize: '0.75rem', 
              padding: '4px 10px', 
              borderRadius: '20px', 
              background: 'rgba(255, 117, 143, 0.15)', 
              color: 'var(--primary-pink)', 
              fontWeight: '700',
              border: '1.5px solid var(--primary-pink)',
              display: 'inline-block',
              verticalAlign: 'middle',
              animation: 'float 3s ease-in-out infinite'
            }}>
              Demo Mode ✨
            </span>
          )}
        </div>
        
        <div className="nav-actions">
          <span 
            className={`nav-link ${page === 'catalog' ? 'active' : ''}`}
            onClick={() => setPage('catalog')}
          >
            Catalog 🍭
          </span>
          {user && (
            <span 
              className={`nav-link ${page === 'orders' ? 'active' : ''}`}
              onClick={() => setPage('orders')}
            >
              My Orders 🥞
            </span>
          )}

          <button 
            className="theme-toggle" 
            onClick={() => setTheme(prev => prev === 'light' ? 'dark' : 'light')}
            aria-label="Toggle theme"
          >
            {theme === 'light' ? '🌙' : '☀️'}
          </button>

          {user ? (
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.8rem' }}>
              <span style={{ fontSize: '0.9rem', opacity: 0.9 }}>
                Hi, <strong>{user.name}</strong> 
                {user.role === 'ADMIN' && <span style={{ marginLeft: '4px', fontSize: '0.75rem', background: 'var(--accent-yellow)', color: '#5d4000', padding: '2px 6px', borderRadius: '8px', fontWeight: 'bold' }}>ADMIN</span>}
              </span>
              <button className="btn-secondary" onClick={handleLogout} style={{ padding: '0.4rem 0.8rem', borderRadius: '12px' }}>
                Logout
              </button>
            </div>
          ) : (
            <button className="nav-button" onClick={() => { setAuthMode('login'); setPage('auth'); }}>
              Sign In 🔐
            </button>
          )}

          <button className="nav-cart-btn" onClick={() => setIsCartOpen(true)}>
            🛒
            {totalCartCount > 0 && <span className="cart-badge">{totalCartCount}</span>}
          </button>
        </div>
      </nav>

      {/* Main Content Area */}
      <main className="main-content">

        {/* Dynamic Pages */}
        {page === 'catalog' && (
          <div>
            <div className="catalog-header">
              <div className="catalog-title">
                <h2>Baking Deck Fresh Stock 🍩</h2>
              </div>
              <div className="catalog-actions">
                <div className="search-bar">
                  <span className="search-icon">🔍</span>
                  <input 
                    type="text" 
                    placeholder="Search sweets..." 
                    className="search-input"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                  />
                </div>
                {user?.role === 'ADMIN' && (
                  <button className="nav-button" onClick={() => openProductModal(null)}>
                    + Bake Sweet 🧁
                  </button>
                )}
              </div>
            </div>

            {filteredProducts.length === 0 ? (
              <div style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-secondary)' }}>
                <div style={{ fontSize: '4rem', marginBottom: '1rem' }}>🧁</div>
                <h3>No sweet treats match your search! Try another keyword!</h3>
              </div>
            ) : (
              <div className="product-grid">
                {filteredProducts.map(p => {
                  const out = p.stock <= 0;
                  const low = !out && p.stock <= 5;
                  return (
                    <div key={p.id} className={`product-card ${out ? 'out-of-stock' : ''}`}>
                      
                      {/* Admin controls */}
                      {user?.role === 'ADMIN' && (
                        <div className="admin-actions">
                          <button className="admin-btn edit" onClick={() => openProductModal(p)} title="Edit product">
                            ✏️
                          </button>
                          <button className="admin-btn delete" onClick={() => handleDeleteProduct(p)} title="Delete product">
                            🗑️
                          </button>
                        </div>
                      )}

                      <div className="product-image-container">
                        <div className="product-image-gradient"></div>
                        {getProductEmoji(p.name, p.description)}
                        
                        {out ? (
                          <span className="stock-tag out">Out of stock 😴</span>
                        ) : low ? (
                          <span className="stock-tag low-stock">Only {p.stock} left! ⚠️</span>
                        ) : (
                          <span className="stock-tag in-stock">In stock ({p.stock}) ✅</span>
                        )}
                      </div>

                      <div className="product-info">
                        <h3 className="product-name">{p.name}</h3>
                        <p className="product-description">{p.description}</p>
                        <div className="product-footer">
                          <span className="product-price">{p.price.toFixed(2)}</span>
                          <button 
                            className="add-to-cart-btn"
                            onClick={() => handleAddToCart(p)}
                            disabled={out}
                          >
                            <span>Add 🛒</span>
                          </button>
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        )}

        {page === 'orders' && (
          <div>
            <div className="orders-title">
              <h2>Your Baking Orders 🥞</h2>
            </div>
            
            {orders.length === 0 ? (
              <div style={{ textAlign: 'center', padding: '4rem 2rem', background: 'var(--bg-secondary)', borderRadius: '24px', border: '3px solid var(--border-color)' }}>
                <div style={{ fontSize: '4.5rem', marginBottom: '1.2rem' }}>🥞</div>
                <h3>You haven't ordered any sweets yet!</h3>
                <p style={{ color: 'var(--text-secondary)', margin: '0.8rem 0 1.5rem 0' }}>Visit our catalog to check out fresh cakes and boba pearls!</p>
                <button className="btn-primary" onClick={() => setPage('catalog')}>Browse Catalog 🍭</button>
              </div>
            ) : (
              <div className="orders-list">
                {orders.map(order => (
                  <div key={order.id} className="order-card">
                    <div className="order-main-info">
                      <div className="order-emoji">
                        {getProductEmoji(order.productName)}
                      </div>
                      <div className="order-details">
                        <h4>{order.productName}</h4>
                        <div className="order-meta">
                          <span>📅 {new Date(order.createdAt).toLocaleDateString()}</span>
                          <span>📦 Qty: {order.quantity}</span>
                        </div>
                      </div>
                    </div>
                    
                    <div style={{ display: 'flex', alignItems: 'center', gap: '1.5rem' }}>
                      <div className="order-financials">
                        <div className="order-total">{order.totalPrice.toFixed(2)}</div>
                        <div className="order-unit-info">₹{order.unitPrice.toFixed(2)} each</div>
                      </div>
                      <span className="status-badge confirmed">
                        {order.status.toLowerCase()}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {page === 'auth' && (
          <div className="auth-wrapper">
            <div className="auth-card">
              <div className="auth-header">
                <h2>{authMode === 'login' ? 'Welcome Back! 🍭' : 'Join the Sweet Club! 🧁'}</h2>
                <p>{authMode === 'login' ? 'Sign in to access your profile & order sweets!' : 'Sign up to get fresh goodies and order treats!'}</p>
              </div>

              <form onSubmit={handleAuthSubmit}>
                {authMode === 'register' && (
                  <div className="form-group">
                    <label>🧁 Display Name</label>
                    <input 
                      type="text" 
                      className="form-input" 
                      placeholder="Your cute name"
                      value={authForm.name}
                      onChange={(e) => setAuthForm(prev => ({ ...prev, name: e.target.value }))}
                      required
                    />
                  </div>
                )}
                
                <div className="form-group">
                  <label>✉️ Email Address</label>
                  <input 
                    type="email" 
                    className="form-input" 
                    placeholder="email@example.com"
                    value={authForm.email}
                    onChange={(e) => setAuthForm(prev => ({ ...prev, email: e.target.value }))}
                    required
                  />
                  {authMode === 'login' && <span style={{ fontSize: '0.75rem', opacity: 0.7 }}>Tip: enter email containing "admin" to simulate ADMIN role.</span>}
                </div>

                <div className="form-group">
                  <label>🔑 Password</label>
                  <input 
                    type="password" 
                    className="form-input" 
                    placeholder="••••••••"
                    value={authForm.password}
                    onChange={(e) => setAuthForm(prev => ({ ...prev, password: e.target.value }))}
                    required
                  />
                </div>

                <button type="submit" className="auth-submit-btn">
                  {authMode === 'login' ? 'Login 🍩' : 'Register 🍭'}
                </button>
              </form>

              <div className="auth-footer">
                {authMode === 'login' ? (
                  <span>New to the shop? <span className="auth-link" onClick={() => setAuthMode('register')}>Create account</span></span>
                ) : (
                  <span>Already a member? <span className="auth-link" onClick={() => setAuthMode('login')}>Sign in here</span></span>
                )}
              </div>
            </div>
          </div>
        )}

      </main>

      {/* Cart Slider Overlay */}
      {isCartOpen && (
        <div className="cart-overlay" onClick={() => setIsCartOpen(false)}>
          <div className="cart-drawer" onClick={(e) => e.stopPropagation()}>
            <div className="cart-header">
              <h3>🛒 My Sweet Cart ({totalCartCount})</h3>
              <button className="close-btn" onClick={() => setIsCartOpen(false)}>×</button>
            </div>
            
            <div className="cart-items">
              {cart.length === 0 ? (
                <div className="cart-empty">
                  <div className="cart-empty-emoji">🧁</div>
                  <h3>Your shopping cart is empty!</h3>
                  <p>Browse around and select some cupcakes, bubbles, and candy!</p>
                </div>
              ) : (
                cart.map(item => (
                  <div key={item.product.id} className="cart-item">
                    <div className="cart-item-emoji">
                      {getProductEmoji(item.product.name)}
                    </div>
                    <div className="cart-item-details">
                      <div className="cart-item-name">{item.product.name}</div>
                      <div className="cart-item-price">{(item.product.price * item.quantity).toFixed(2)}</div>
                    </div>
                    <div className="cart-quantity-controls">
                      <button className="qty-btn" onClick={() => updateCartQty(item.product.id, -1)}>-</button>
                      <span className="qty-val">{item.quantity}</span>
                      <button className="qty-btn" onClick={() => updateCartQty(item.product.id, 1)}>+</button>
                    </div>
                  </div>
                ))
              )}
            </div>

            <div className="cart-footer">
              <div className="cart-summary-row">
                <span>Subtotal:</span>
                <span className="cart-summary-total">{cartTotal.toFixed(2)}</span>
              </div>
              <button 
                className="checkout-btn"
                onClick={handleCheckout}
                disabled={cart.length === 0}
              >
                Place Order 🍩✨
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Admin Add/Edit Modal */}
      {adminModalOpen && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h3>{editingProduct ? '✏️ Modify Sweet' : '🧁 Bake New Sweet'}</h3>
              <button className="close-btn" onClick={() => setAdminModalOpen(false)}>×</button>
            </div>
            <form onSubmit={handleProductSubmit}>
              <div className="modal-body">
                <div className="form-group">
                  <label>🍩 Sweet Name</label>
                  <input 
                    type="text" 
                    className="form-input" 
                    placeholder="e.g. Raspberry Macaron"
                    value={productForm.name}
                    onChange={(e) => setProductForm(prev => ({ ...prev, name: e.target.value }))}
                    required
                  />
                </div>
                <div className="form-group">
                  <label>📝 Sweet Description</label>
                  <textarea 
                    className="form-input" 
                    placeholder="Describe the sweet details..."
                    style={{ minHeight: '80px', resize: 'vertical' }}
                    value={productForm.description}
                    onChange={(e) => setProductForm(prev => ({ ...prev, description: e.target.value }))}
                  />
                </div>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                  <div className="form-group">
                    <label>₹ Price</label>
                    <input 
                      type="number" 
                      step="0.01"
                      className="form-input" 
                      placeholder="120.00"
                      value={productForm.price}
                      onChange={(e) => setProductForm(prev => ({ ...prev, price: e.target.value }))}
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label>📦 Stock Count</label>
                    <input 
                      type="number" 
                      className="form-input" 
                      placeholder="25"
                      value={productForm.stock}
                      onChange={(e) => setProductForm(prev => ({ ...prev, stock: e.target.value }))}
                      required
                    />
                  </div>
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn-secondary" onClick={() => setAdminModalOpen(false)}>Cancel</button>
                <button type="submit" className="btn-primary">Save Sweet ✨</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Cute Footer */}
      <footer style={{ textAlign: 'center', padding: '2rem 1rem', borderTop: '2px solid var(--border-color)', color: 'var(--text-secondary)', fontSize: '0.9rem', marginTop: 'auto' }}>
        <p>Made with 💖, sugar, and microservices! 🧁🍬✨</p>
      </footer>

    </div>
  );
}
