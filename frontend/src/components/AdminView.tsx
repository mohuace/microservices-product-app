/**
 * Displays inventory records fetched from the backend admin endpoint.
 *
 * The inventory view is read-only and provides a back button to return to products.
 */
import { useEffect, useState } from 'react'
import { getInventory } from '../services/api'
import type { Inventory } from '../types'

interface AdminViewProps {
  onBack: () => void
}

export default function AdminView({ onBack }: AdminViewProps) {
  const [inventory, setInventory] = useState<Inventory[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    setLoading(true)
    getInventory()
      .then((data) => setInventory(data))
      .catch(() => setError('Unable to load inventory.'))
      .finally(() => setLoading(false))
  }, [])

  return (
    <main className="page-shell">
      <h1>Admin Inventory</h1>
      {loading && <div className="status-message">Loading inventory...</div>}
      {error && <div className="status-error">{error}</div>}
      {!loading && !error && (
        <table>
          <thead>
            <tr>
              <th>Product ID</th>
              <th>Quantity</th>
            </tr>
          </thead>
          <tbody>
            {inventory.map((record) => (
              <tr key={record.productId}>
                <td>{record.productId}</td>
                <td>{record.quantity}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
      <button type="button" className="secondary-button" onClick={onBack}>
        Back to Products
      </button>
    </main>
  )
}
