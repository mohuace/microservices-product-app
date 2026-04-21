/**
 * Displays the cart contents and sends the order to the backend when confirmed.
 *
 * Converts local cart items into the shape expected by the order API.
 */
import { useMemo, useState } from 'react'
import { createOrder } from '../services/api'
import type { CartItem, OrderRequestItem } from '../types'

interface ConfirmViewProps {
  cart: CartItem[]
  onBack: () => void
  onOrderComplete: (result: { success: boolean; message: string; orderId?: string }) => void
}

export default function ConfirmView({ cart, onBack, onOrderComplete }: ConfirmViewProps) {
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const total = useMemo(() => cart.reduce((sum, item) => sum + item.product.price * item.quantity, 0), [cart])

  /**
   * Send the order request to the server and report success or failure.
   */
  const handleConfirm = async () => {
    setLoading(true)
    setError(null)

    const requestItems: OrderRequestItem[] = cart.map((item) => ({ productId: String(item.product.id), quantity: item.quantity }))

    try {
      const response = await createOrder(requestItems)
      const orderId = response.match(/\d+/)?.[0] ?? 'unknown'
      onOrderComplete({ success: true, message: response, orderId })
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Unable to create order.'
      setError(message)
      onOrderComplete({ success: false, message })
    } finally {
      setLoading(false)
    }
  }

  if (cart.length === 0) {
    return (
      <main className="page-shell">
        <h1>Confirm Order</h1>
        <div className="status-message">Your cart is empty.</div>
        <button type="button" className="secondary-button" onClick={onBack}>
          Back
        </button>
      </main>
    )
  }

  return (
    <main className="page-shell">
      <h1>Confirm Order</h1>
      {error && <div className="status-error">{error}</div>}
      <table>
        <thead>
          <tr>
            <th>Product</th>
            <th>Qty</th>
            <th>Price</th>
            <th>Subtotal</th>
          </tr>
        </thead>
        <tbody>
          {cart.map((item) => (
            <tr key={item.product.id}>
              <td>{item.product.name}</td>
              <td>{item.quantity}</td>
              <td>${item.product.price.toFixed(2)}</td>
              <td>${(item.product.price * item.quantity).toFixed(2)}</td>
            </tr>
          ))}
        </tbody>
      </table>
      <div className="order-total">Total: ${total.toFixed(2)}</div>
      <div className="button-row">
        <button type="button" className="primary-button" onClick={handleConfirm} disabled={loading}>
          {loading ? 'Creating order…' : 'Confirm Order'}
        </button>
        <button type="button" className="secondary-button" onClick={onBack}>
          Back
        </button>
      </div>
    </main>
  )
}
