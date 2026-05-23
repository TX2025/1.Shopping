<template>
  <div class="cart-page">
    <div class="container">
      <h2>购物车</h2>
      <p v-if="pageConfig.promotionText" class="promotion">{{ pageConfig.promotionText }}</p>
      <el-table v-if="cart.items.length" :data="cart.items" @selection-change="sel => cart.checkedItems = sel.map(s=>s.id)">
        <el-table-column type="selection" width="55" />
        <el-table-column label="商品" width="400">
          <template #default="{ row }">
            <div class="cart-product">
              <span class="cart-image">{{ row.productName?.charAt(0) }}</span>
              <span>{{ row.productName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="单价" width="120"><template #default="{row}">¥{{ row.productPrice }}</template></el-table-column>
        <el-table-column label="数量" width="150">
          <template #default="{ row }">
            <el-input-number v-model="row.quantity" :min="1" :max="row.stock" size="small"
              @change="v => cart.updateQuantity(row.id, v)" />
          </template>
        </el-table-column>
        <el-table-column label="小计" width="120">
          <template #default="{row}">¥{{ (row.productPrice * row.quantity).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{row}"><el-button type="danger" size="small" @click="cart.removeItem(row.id)">删除</el-button></template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="购物车是空的" />

      <div v-if="cart.items.length" class="cart-footer">
        <span>合计: <span class="total-price">¥{{ cart.totalAmount }}</span></span>
        <el-button type="danger" size="large" @click="$router.push('/checkout')">去结算</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useCartStore } from '../../stores/cart'
import { getPageConfig } from '../../api/admin'

const cart = useCartStore()
const pageConfig = ref({ promotionText: '满99元免运费' })

onMounted(async () => {
  await cart.fetchCart()
  try {
    const res = await getPageConfig('CART')
    if (res.data?.configJson) pageConfig.value = { ...pageConfig.value, ...JSON.parse(res.data.configJson) }
  } catch {}
})
</script>

<style scoped>
.container { max-width: 1000px; margin: 30px auto; padding: 0 20px; }
.container h2 { margin-bottom: 16px; }
.promotion { color: #f56c6c; margin-bottom: 12px; background: #fef0f0; padding: 8px 16px; border-radius: 4px; }
.cart-product { display: flex; align-items: center; gap: 12px; }
.cart-image { width: 50px; height: 50px; background: #f0f2f5; display: inline-flex; align-items: center; justify-content: center; font-size: 20px; color: #409EFF; }
.cart-footer { display: flex; justify-content: flex-end; align-items: center; gap: 20px; margin-top: 20px; padding: 16px; background: #fff; }
.total-price { color: #f56c6c; font-size: 22px; font-weight: bold; }
</style>
