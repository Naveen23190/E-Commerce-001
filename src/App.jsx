import React, { useEffect, useMemo, useState } from 'react'

const API = 'http://localhost:8080'
const imageUrl = (p) => p.imageUrl ? `${API}${p.imageUrl}` : null

export default function App() {
  const [products, setProducts] = useState([])
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [auth, setAuth] = useState(() => localStorage.getItem('shopAuth') || '')
  const [role, setRole] = useState(() => localStorage.getItem('shopRole') || '')
  const [cart, setCart] = useState({items:[],total:0})
  const [orders, setOrders] = useState([])
  const [message, setMessage] = useState('')
  const [mode, setMode] = useState('login')
  const [adminForm, setAdminForm] = useState({name:'',description:'',price:'',stock:'0'})
  const [image, setImage] = useState(null)
  const [busy, setBusy] = useState(false)

  const headers = useMemo(() => auth ? {Authorization:`Basic ${auth}`} : {}, [auth])
  async function request(path, options={}) {
    const res = await fetch(`${API}${path}`, {...options, headers:{...headers,...options.headers}})
    if (!res.ok) {
      let text = await res.text()
      try { text = JSON.parse(text).detail || JSON.parse(text).message || text } catch {}
      throw new Error(text || `Request failed (${res.status})`)
    }
    if (res.status === 204) return null
    const type = res.headers.get('content-type') || ''
    return type.includes('application/json') ? res.json() : res
  }
  async function loadProducts() {
    try { setProducts(await request('/api/products', {headers:{}})) } catch(e) { setMessage(e.message) }
  }
  async function loadCart() { try { setCart(await request('/api/cart')) } catch(e) { setMessage(e.message) } }
  async function loadOrders() { try { setOrders(await request('/api/orders')) } catch(e) { setMessage(e.message) } }
  useEffect(() => { loadProducts() }, [])
  useEffect(() => { if(auth) { loadCart(); loadOrders() } else { setCart({items:[],total:0}); setOrders([]) } }, [auth])

  async function login(e) {
    e.preventDefault(); setMessage('')
    const encoded = btoa(`${username}:${password}`)
    try {
      const res = await fetch(`${API}/api/cart`, {headers:{Authorization:`Basic ${encoded}`}})
      if(!res.ok) throw new Error(res.status === 401 ? 'Invalid username or password' : 'Could not sign in')
      const userRole = username === 'admin' ? 'ADMIN' : 'CUSTOMER'
      localStorage.setItem('shopAuth', encoded); localStorage.setItem('shopRole', userRole)
      setRole(userRole); setAuth(encoded); setPassword(''); setMessage('Signed in successfully')
    } catch(e) { setMessage(e.message) }
  }
  async function register(e) {
    e.preventDefault(); setMessage('')
    try {
      await request('/api/auth/register', {method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({username,password})})
      setMode('login'); setMessage('Account created. Sign in now.')
    } catch(e) { setMessage(e.message) }
  }
  function logout() {
    localStorage.removeItem('shopAuth'); localStorage.removeItem('shopRole'); setAuth(''); setRole(''); setMessage('Signed out')
  }
  async function addToCart(p) {
    if(!auth) { setMessage('Sign in or register before adding items to your cart.'); return }
    try { setCart(await request('/api/cart/items',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({productId:p.id,quantity:1})})); setMessage(`${p.name} added to cart`) }
    catch(e) { setMessage(e.message) }
  }
  async function removeItem(id) {
    try { setCart(await request(`/api/cart/items/${id}`,{method:'DELETE'})) } catch(e) { setMessage(e.message) }
  }
  async function placeOrder() {
    try { const order=await request('/api/orders',{method:'POST'}); setMessage(`Order #${order.id} placed successfully`); await loadCart(); await loadOrders(); await loadProducts() }
    catch(e) { setMessage(e.message) }
  }
  async function createProduct(e) {
    e.preventDefault(); setBusy(true); setMessage('')
    const form = new FormData()
    Object.entries(adminForm).forEach(([k,v])=>form.append(k,v))
    if(image) form.append('image',image)
    try {
      await request('/api/admin/products',{method:'POST',body:form})
      setAdminForm({name:'',description:'',price:'',stock:'0'});setImage(null)
      e.target.reset(); await loadProducts(); setMessage('Product created; image stored in PostgreSQL.')
    } catch(e) { setMessage(e.message) } finally { setBusy(false) }
  }

  return <div className="app">
    <header className="topbar"><div><div className="brand">JAVA<span>SHOP</span></div><div className="tagline">A full-stack commerce demo</div></div>
      <div className="session">{auth ? <><span>Signed in as <b>{atob(auth).split(':')[0]}</b></span><button className="secondary" onClick={logout}>Sign out</button></> : <span>Browse products · Create an account to order</span>}</div>
    </header>
    <main>
      <section className="hero"><div><p className="eyebrow">JAVA · SPRING BOOT · REACT</p><h1>Good products.<br/><em>Simple shopping.</em></h1><p className="hero-copy">Browse the collection, add items to your cart, and place an order. Product images are persisted in PostgreSQL.</p></div><div className="hero-mark" aria-hidden="true">S<span>+</span></div></section>
      {message && <div className="notice"><span>{message}</span><button onClick={()=>setMessage('')} aria-label="Dismiss">×</button></div>}
      <div className="layout">
        <section className="catalog"><div className="section-heading"><div><p className="eyebrow">THE COLLECTION</p><h2>Featured products</h2></div><span className="count">{products.length} ITEMS</span></div>
          {products.length === 0 ? <div className="empty"><div className="empty-icon">◇</div><h3>Your storefront is ready</h3><p>Add products as an administrator to see them here.</p></div> :
            <div className="products">{products.map(p=><article className="product" key={p.id}>
              <div className="product-image">{imageUrl(p) ? <img src={imageUrl(p)} alt={p.name}/> : <div className="image-placeholder"><span>JS</span><small>JAVA SHOP</small></div>}<span className="product-id">NO. {String(p.id).padStart(3,'0')}</span></div>
              <div className="product-info"><h3>{p.name}</h3><p>{p.description || 'Thoughtfully selected for your everyday needs.'}</p><div className="product-bottom"><strong>₹{Number(p.price).toLocaleString('en-IN',{minimumFractionDigits:2})}</strong><button onClick={()=>addToCart(p)} disabled={p.stock<1}>{p.stock<1?'Sold out':'Add +'}</button></div><small className="stock">{p.stock} in stock</small></div>
            </article>)}</div>}
        </section>
        <aside className="sidebar">
          {!auth ? <section className="panel"><div className="panel-head"><span className="panel-icon">↗</span><div><p className="eyebrow">YOUR ACCOUNT</p><h2>{mode==='login'?'Welcome back':'Create account'}</h2></div></div>
            <div className="tabs"><button className={mode==='login'?'active':''} onClick={()=>setMode('login')}>Sign in</button><button className={mode==='register'?'active':''} onClick={()=>setMode('register')}>Register</button></div>
            <form onSubmit={mode==='login'?login:register} className="stack"><label>Username<input value={username} onChange={e=>setUsername(e.target.value)} required autoComplete="username" placeholder="Your username"/></label><label>Password<input type="password" value={password} onChange={e=>setPassword(e.target.value)} required minLength={mode==='register'?8:1} autoComplete={mode==='login'?'current-password':'new-password'} placeholder={mode==='register'?'At least 8 characters':'Your password'}/></label><button className="primary">{mode==='login'?'Sign in':'Create account'}</button></form>
            <p className="muted">Demo admin: <code>admin</code> / <code>Admin@12345</code></p>
          </section> : <>
            <section className="panel"><div className="panel-head"><span className="panel-icon">▤</span><div><p className="eyebrow">YOUR BAG</p><h2>Shopping cart</h2></div></div>
              {cart.items.length===0?<p className="muted">Your cart is empty. Find something you like.</p>:<><div className="cart-items">{cart.items.map(i=><div className="cart-row" key={i.productId}><div className="mini-image">{i.imageUrl?<img src={imageUrl(i)} alt=""/>:'JS'}</div><div className="cart-detail"><b>{i.name}</b><small>Qty {i.quantity} × ₹{Number(i.price).toFixed(2)}</small></div><button className="remove" onClick={()=>removeItem(i.productId)}>×</button></div>)}</div><div className="total"><span>Total</span><strong>₹{Number(cart.total).toLocaleString('en-IN',{minimumFractionDigits:2})}</strong></div><button className="primary full" onClick={placeOrder}>Place order →</button></>}
            </section>
            <section className="panel"><div className="panel-head"><span className="panel-icon">✓</span><div><p className="eyebrow">ORDER HISTORY</p><h2>Your orders</h2></div></div>
              {orders.length===0?<p className="muted">Your placed orders will appear here.</p>:orders.map(o=><div className="order-row" key={o.id}><div><b>Order #{o.id}</b><small>{new Date(o.createdAt).toLocaleDateString()}</small></div><div className="order-right"><b>₹{Number(o.total).toFixed(2)}</b><small>{o.status}</small></div></div>)}
            </section>
          </>}
          {auth && role==='ADMIN' && <section className="panel admin-panel"><div className="panel-head"><span className="panel-icon">＋</span><div><p className="eyebrow">ADMIN TOOLS</p><h2>Add product</h2></div></div>
            <form className="stack" onSubmit={createProduct}><label>Product name<input required value={adminForm.name} onChange={e=>setAdminForm({...adminForm,name:e.target.value})}/></label><label>Description<textarea value={adminForm.description} onChange={e=>setAdminForm({...adminForm,description:e.target.value})}/></label><div className="two-fields"><label>Price (₹)<input type="number" min="0" step="0.01" required value={adminForm.price} onChange={e=>setAdminForm({...adminForm,price:e.target.value})}/></label><label>Stock<input type="number" min="0" required value={adminForm.stock} onChange={e=>setAdminForm({...adminForm,stock:e.target.value})}/></label></div><label>Product image<input type="file" accept="image/*" onChange={e=>setImage(e.target.files[0]||null)}/></label><small className="muted">Images up to 8 MB are saved in PostgreSQL.</small><button className="primary" disabled={busy}>{busy?'Saving…':'Save product'}</button></form>
          </section>}
        </aside>
      </div>
    </main>
    <footer><div className="brand">JAVA<span>SHOP</span></div><p>Portfolio starter · Spring Boot + React + PostgreSQL</p><span>BUILT FOR LEARNING</span></footer>
  </div>
}
