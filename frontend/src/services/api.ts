import axios from 'axios'
import type { Product, Inventory, OrderRequestItem } from '../types'

const API_BASE = import.meta.env.VITE_API_BASE ?? 'http://localhost:8086'
const api = axios.create({ baseURL: API_BASE, headers: { 'Content-Type': 'application/json' } })

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const response = error?.response
    if (response?.data?.message) {
      return Promise.reject(new Error(response.data.message))
    }
    if (response?.status === 429) {
      return Promise.reject(new Error('Too many requests. Please wait a moment and try again.'))
    }
    return Promise.reject(error)
  }
)

export const getProducts = async (): Promise<Product[]> => {
  const response = await api.get<Product[]>('/products')
  return response.data
}

export const getInventory = async (): Promise<Inventory[]> => {
  const response = await api.get<Inventory[]>('/inventory')
  return response.data
}

export const createOrder = async (items: OrderRequestItem[]): Promise<string> => {
  const response = await api.post<string>('/orders', items)
  return response.data
}
