<template>
  <div class="cart-page">
    <div class="container">
      <h2>购物车</h2>
      <p v-if="pc.promotionText" class="promotion">{{ pc.promotionText }}</p>

      <el-table v-if="cart.items.length" :data="cart.items" @selection-change="sel => cart.checkedItems = sel.map(s=>s.id)">
        <el-table-column type="selection" width="55" />
        <el-table-column label="商品" width="400">
          <template #default="{ row }">
            <div class="cart-product">
              <span class="cart-image">
                <img v-if="row.productImage" :src="row.productImage" :alt="row.productName" />
                <span v-else class="image-placeholder">{{ row.productName?.charAt(0) }}</span>
              </span>
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

      <div v-if="pc.showCouponInput !== false" class="coupon-bar">
        <el-input v-model="couponCode" placeholder="输入优惠码" size="small" style="width:200px" />
        <el-button size="small" @click="applyCoupon">使用</el-button>
      </div>

      <div v-if="cart.items.length" class="cart-footer">
        <el-button type="danger" plain @click="handleBatchRemove" :disabled="!cart.checkedItems.length">
          批量删除 {{ cart.checkedItems.length ? '(' + cart.checkedItems.length + ')' : '' }}
        </el-button>
        <span class="footer-right">
          <span>合计: <span class="total-price">¥{{ cart.totalAmount }}</span></span>
          <el-button type="danger" size="large" @click="$router.push('/checkout')">去结算</el-button>
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useCartStore } from '../../stores/cart'
import { usePageConfig } from '../../composables/usePageConfig'
import { ElMessage } from 'element-plus'

const cart = useCartStore()
const { config: pc } = usePageConfig('CART')
const couponCode = ref('')

onMounted(async () => {
  await cart.fetchCart()
})

function applyCoupon() {
  if (!couponCode.value.trim()) {
    ElMessage.warning('请输入优惠码')
    return
  }
  ElMessage.info('优惠码功能即将上线')
  couponCode.value = ''
}

async function handleBatchRemove() {
  if (!cart.checkedItems.length) {
    ElMessage.warning('请先选择要删除的商品')
    return
  }
  try {
    await cart.batchRemove(cart.checkedItems)
    ElMessage.success('已删除选中商品')
  } catch {}
}
</script>

<style scoped>
.container { max-width: 1000px; margin: 30px auto; padding: 0 20px; }
.container h2 { margin-bottom: 16px; }
.promotion { color: #e74c3c; margin-bottom: 12px; background: #fef0f0; padding: 8px 16px; border-radius: 4px; font-size: 14px; }
.cart-product { display: flex; align-items: center; gap: 12px; }
.cart-image { width: 56px; height: 56px; background: #f0f2f5; border-radius: 4px; display: inline-flex; align-items: center; justify-content: center; overflow: hidden; flex-shrink: 0; }
.cart-image img { width: 100%; height: 100%; object-fit: cover; }
.image-placeholder { font-size: 22px; color: #00676b; }
.coupon-bar { display: flex; gap: 8px; margin-top: 16px; }
.cart-footer { display: flex; justify-content: space-between; align-items: center; gap: 20px; margin-top: 20px; padding: 16px; background: #fff; border-radius: 4px; }
.footer-right { display: flex; align-items: center; gap: 20px; }
.total-price { color: #e74c3c; font-size: 22px; font-weight: bold; }
</style>
