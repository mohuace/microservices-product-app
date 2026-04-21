import axios from 'axios'
import type { Product, Inventory, OrderRequestItem } from '../types'

const API_BASE = import.meta.env.VITE_API_BASE ?? 'http://localhost:8086'
const api = axios.create({ baseURL: API_BASE, headers: { 'Content-Type': 'application/json' } })

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
