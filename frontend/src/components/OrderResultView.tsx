/**
 * Shows the final order result after order creation completes.
 *
 * The component displays either success or failure details and provides a back action.
 */
import type { OrderResult } from '../types'

interface OrderResultViewProps {
  orderResult: OrderResult | null
  onBack: () => void
}

export default function OrderResultView({ orderResult, onBack }: OrderResultViewProps) {
  if (!orderResult) {
    return (
      <main className="page-shell">
        <div className="status-message">No order data available.</div>
        <button type="button" className="primary-button" onClick={onBack}>
          Back to Products
        </button>
      </main>
    )
  }

  return (
    <main className="page-shell">
      <h1>{orderResult.success ? 'Order Created' : 'Order Failed'}</h1>
      <p>{orderResult.message}</p>
      <button type="button" className="primary-button" onClick={onBack}>
        Back to Products
      </button>
    </main>
  )
}
