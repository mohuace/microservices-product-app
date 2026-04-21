/**
 * Displays the product catalog and allows the user to add items to the cart.
 *
 * Fetches products from the API gateway and maintains selected quantities locally.
 */
import { useEffect, useMemo, useState } from 'react'
import { getProducts } from '../services/api'
import type { CartItem, Product } from '../types'

interface ProductsViewProps {
  cart: CartItem[]
  onAddToCart: (product: Product, quantity: number) => void
  onShowConfirm: () => void
}

type QuantityMap = Record<number, number>

export default function ProductsView({ cart, onAddToCart, onShowConfirm }: ProductsViewProps) {
  const [products, setProducts] = useState<Product[]>([])
  const [quantities, setQuantities] = useState<QuantityMap>({})
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    setLoading(true)
    getProducts()
      .then((data) => {
        setProducts(data)
        setQuantities(Object.fromEntries(data.map((product) => [product.id, 1])))
      })
      .catch(() => setError('Unable to load products.'))
      .finally(() => setLoading(false))
  }, [])

  const cartCount = useMemo(() => cart.reduce((total, item) => total + item.quantity, 0), [cart])

  /**
   * Update the local quantity for a product before adding to cart.
   */
  const handleQuantityChange = (productId: number, value: string) => {
    setQuantities((current) => ({ ...current, [productId]: Math.max(1, Number(value) || 1) }))
  }

  return (
    <main className="page-shell">
      <h1>Products</h1>
      {loading && <div className="status-message">Loading products...</div>}
      {error && <div className="status-error">{error}</div>}

      {!loading && !error && (
        <>
          <div className="grid-list">
            {products.map((product) => (
              <article key={product.id} className="product-card">
                <div>
                  <h2>{product.name}</h2>
                  <p>${product.price.toFixed(2)}</p>
                </div>
                <div className="product-actions">
                  <input
                    type="number"
                    min="1"
                    value={quantities[product.id] ?? 1}
                    onChange={(event) => handleQuantityChange(product.id, event.target.value)}
                  />
                  <button type="button" onClick={() => onAddToCart(product, quantities[product.id] ?? 1)}>
                    Add to Cart
                  </button>
                </div>
              </article>
            ))}
          </div>
          <button type="button" className="primary-button" onClick={onShowConfirm} disabled={cartCount === 0}>
            Create Order
          </button>
        </>
      )}
    </main>
  )
}
