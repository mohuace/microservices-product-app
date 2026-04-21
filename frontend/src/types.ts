export interface Product {
  id: number
  name: string
  price: number
}

export interface Inventory {
  productId: number
  quantity: number
}

export interface CartItem {
  product: Product
  quantity: number
}

export interface OrderRequestItem {
  productId: string
  quantity: number
}

export interface OrderResult {
  orderId?: string
  success: boolean
  message: string
  items?: CartItem[]
  total?: number
}
