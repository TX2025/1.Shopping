import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '../utils/request'

export const useCartStore = defineStore('cart', () => {
  const items = ref([])

  const totalCount = computed(() => items.value.reduce((s, i) => s + i.quantity, 0))
  const totalAmount = computed(() =>
    items.value.reduce((s, i) => s + i.productPrice * i.quantity, 0).toFixed(2)
  )
  const checkedItems = ref([])

  async function fetchCart() {
    try {
      const res = await request.get('/cart')
      items.value = res.data || []
    } catch { items.value = [] }
  }

  async function addToCart(productId, quantity = 1) {
    await request.post('/cart', { productId, quantity })
    await fetchCart()
  }

  async function updateQuantity(cartId, quantity) {
    await request.put(`/cart/${cartId}?quantity=${quantity}`)
    await fetchCart()
  }

  async function removeItem(cartId) {
    await request.delete(`/cart/${cartId}`)
    await fetchCart()
  }

  async function clearCart() {
    await request.delete('/cart')
    items.value = []
  }

  async function batchRemove(ids) {
    if (!ids || ids.length === 0) return
    await Promise.all(ids.map(id => request.delete(`/cart/${id}`)))
    await fetchCart()
  }

  return { items, totalCount, totalAmount, checkedItems, fetchCart, addToCart, updateQuantity, removeItem, batchRemove, clearCart }
})
