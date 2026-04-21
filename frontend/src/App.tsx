/**
 * Root application component for the simplified storefront.
 *
 * Handles top-level view state, cart updates, and order result navigation.
 */
import { useState } from 'react'
import ProductsView from './components/ProductsView'
import ConfirmView from './components/ConfirmView'
import OrderResultView from './components/OrderResultView'
import AdminView from './components/AdminView'
import type { CartItem, OrderResult, Product } from './types'
import './App.css'

type View = 'products' | 'confirm' | 'order' | 'admin'

export default function App() {
  const [view, setView] = useState<View>('products')
  const [cart, setCart] = useState<CartItem[]>([])
  const [orderResult, setOrderResult] = useState<OrderResult | null>(null)

  /**
   * Add the selected product to the cart.
   * If the product already exists, increase its quantity.
   */
  const addToCart = (product: Product, quantity: number) => {
    setCart((current) => {
      const existing = current.find((item) => item.product.id === product.id)
      if (existing) {
        return current.map((item) =>
          item.product.id === product.id ? { ...item, quantity: item.quantity + quantity } : item,
        )
      }
      return [...current, { product, quantity }]
    })
  }

  /**
   * Store the order result and transition to the order result view.
   * If the order succeeded, clear the cart.
   */
  const handleOrderComplete = (result: OrderResult) => {
    setOrderResult(result)
    if (result.success) {
      setCart([])
    }
    setView('order')
  }

  const cartCount = cart.reduce((total, item) => total + item.quantity, 0)

  return (
    <div className="app-shell">
      <header className="topbar">
        <button type="button" className={view === 'products' ? 'nav-active' : ''} onClick={() => setView('products')}>
          Products
        </button>
        <button type="button" className={view === 'admin' ? 'nav-active' : ''} onClick={() => setView('admin')}>
          Admin
        </button>
        <div className="cart-summary">Cart: {cartCount}</div>
      </header>

      {view === 'products' && (
        <ProductsView cart={cart} onAddToCart={addToCart} onShowConfirm={() => setView('confirm')} />
      )}

      {view === 'confirm' && (
        <ConfirmView cart={cart} onBack={() => setView('products')} onOrderComplete={handleOrderComplete} />
      )}

      {view === 'order' && <OrderResultView orderResult={orderResult} onBack={() => setView('products')} />}

      {view === 'admin' && <AdminView onBack={() => setView('products')} />}
    </div>
  )
}
